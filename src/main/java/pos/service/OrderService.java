package pos.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pos.domain.*;
import pos.repository.InventoryMovementRepository;
import pos.repository.OrderRepository;
import pos.repository.ProductRepository;
import pos.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional // Isso garante que se der erro de estoque, nada é salvo no banco!
public class OrderService {

    private final OrderRepository orderRepository;
    private final ServiceSessionService serviceSessionService;
    private final UserRepository userRepository;

    // Injeção dos repositórios necessários
    private final ProductRepository productRepository;
    private final InventoryMovementRepository inventoryMovementRepository;

    public Order createCustomerOrder(Boolean delivery, String address, String phone, List<OrderItem> items, Long userId) {
        return new Order();
    }

    public Order createTableOrder(Long tableId, List<OrderItem> items, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found id=" + userId));

        ServiceSession session = serviceSessionService.findActiveSession(tableId)
                .orElseGet(() -> serviceSessionService.openSession(tableId, user.getId()));

        // Cálculo do total
        BigDecimal total = items.stream()
                .map(OrderItem::total) // Usa o método total() que já existe no OrderItem
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Prepara o objeto Pedido
        Order order = Order.builder()
                .customer(user)
                .serviceSession(session)
                .status(OrderStatus.IN_PREPARATION)
                .total(total)
                .build();

        // --- LÓGICA DE ESTOQUE ---
        for (OrderItem item : items) {
            item.setOrder(order);

            // 1. Buscar o produto pelo ID (Correção aqui: usa item.getProductId())
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado ID: " + item.getProductId()));

            int qtySold = item.getQty();

            // 2. VERIFICAÇÃO: Tem estoque suficiente?
            // Se não tiver, o sistema lança erro e cancela TUDO (rollback)
            if (product.getStock() < qtySold) {
                throw new RuntimeException("Estoque insuficiente para o produto: " + product.getName()
                        + ". Disponível: " + product.getStock() + ", Solicitado: " + qtySold);
            }

            // 3. Baixar estoque
            product.setStock(product.getStock() - qtySold);
            productRepository.save(product);

            // 4. Registrar Movimento de Saída (Correção aqui: usa MovementType.EXIT)
            InventoryMovement movement = InventoryMovement.builder()
                    .product(product)
                    .quantity(qtySold)
                    .movementType(MovementType.EXIT)
                    .note("Venda na Mesa " + tableId + " - Pedido em processamento")
                    .build();

            inventoryMovementRepository.save(movement);
        }

        order.setItems(items);

        // Salva o pedido final
        return orderRepository.save(order);
    }

    public List<Order> kitchenQueue() {
        return orderRepository.findByStatus(OrderStatus.IN_PREPARATION);
    }

    public List<Order> all() {
        return orderRepository.findAllWithDetails();
    }

    public void updateStatus(Long id, OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found id=" + id));
        order.setStatus(status);
        orderRepository.save(order);
        log.info("Order {} status updated to {}", id, status);
    }

    public List<Order> readyToPayQueue() {
        return orderRepository.findByStatus(OrderStatus.LISTO);
    }

    // Processa o pagamento e finaliza
    public void payOrder(Long id) {
        updateStatus(id, OrderStatus.PAGADO);
    }
}
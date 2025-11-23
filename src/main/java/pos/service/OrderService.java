package pos.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pos.domain.*;
import pos.repository.OrderRepository;
import pos.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final ServiceSessionService serviceSessionService;
    private final UserRepository userRepository;

    public Order createCustomerOrder(Boolean delivery, String address, String phone, List<OrderItem> items, Long userId) {
        // Mock implementation for now
        return new Order(); 
    }

    public Order createTableOrder(Long tableId, List<OrderItem> items, Long userId) {

    User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found id=" + userId));

    ServiceSession session = serviceSessionService.findActiveSession(tableId)
            .orElseGet(() -> serviceSessionService.openSession(tableId, user.getId()));

    BigDecimal total = items.stream()
            .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQty())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    Order order = Order.builder()
            .customer(user)
            .serviceSession(session)
            .status(OrderStatus.PENDING)
            .total(total)
            .build();

    // IMPORTANTÍSIMO: conectar ambos lados de la relación
    for (OrderItem item : items) {
        item.setOrder(order);
    }

    order.setItems(items);

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
}

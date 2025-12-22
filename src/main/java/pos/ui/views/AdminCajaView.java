package pos.ui.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import pos.auth.RouteGuard;
import pos.domain.Order;
import pos.service.OrderService;
import pos.ui.MainLayout;
import pos.service.CashService;

import java.util.List;

@PageTitle("Caja")
@Route(value = "admin/caja", layout = MainLayout.class)
public class AdminCajaView extends VerticalLayout implements RouteGuard {

  private final OrderService orderService;
  private final FlexLayout cardsContainer; // Onde os quadrados vão ficar

  public AdminCajaView(CashService cash, OrderService orderService) {
    this.orderService = orderService;

    addClassName("caja-view");
    setSizeFull();
    setPadding(true);
    setSpacing(true);

    // --- 1. Cabeçalho e Resumo (Mantido do seu código original) ---
    var title = new H2("Caja & Pagos");
    title.addClassName("caja-title");

    // --- 2. Área dos Quadrados das Mesas ---
    var subTitle = new H3("Mesas Listas para Cobrar");

    cardsContainer = new FlexLayout();
    cardsContainer.setFlexWrap(FlexLayout.FlexWrap.WRAP); // Permite quebrar linha
    cardsContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
    cardsContainer.getStyle().set("gap", "20px"); // Usamos CSS direto
    cardsContainer.setWidthFull();

    // Carrega os quadrados iniciais
    refreshCards();

    // Adiciona tudo na tela
    add(title, subTitle, cardsContainer);
  }

  // Método que busca os pedidos e desenha os quadrados
  private void refreshCards() {
    cardsContainer.removeAll(); // Limpa antes de desenhar

    // Busca apenas pedidos com status LISTO (Definido no passo anterior no OrderService)
    List<Order> ordersToPay = orderService.readyToPayQueue();

    if (ordersToPay.isEmpty()) {
      Span noOrders = new Span("No hay mesas pendientes de cobro.");
      noOrders.getStyle().set("color", "gray");
      cardsContainer.add(noOrders);
      return;
    }

    for (Order order : ordersToPay) {
      cardsContainer.add(createTableCard(order));
    }
  }

  // Cria o visual de UM quadrado
  private Div createTableCard(Order order) {
    Div card = new Div();

    // Estilo do quadrado (CSS inline para facilitar, mas ideal é usar classe)
    card.getStyle().set("background-color", "white");
    card.getStyle().set("border-radius", "12px");
    card.getStyle().set("box-shadow", "0 4px 8px rgba(0,0,0,0.1)");
    card.getStyle().set("padding", "20px");
    card.getStyle().set("width", "200px"); // Tamanho do quadrado
    card.getStyle().set("display", "flex");
    card.getStyle().set("flex-direction", "column");
    card.getStyle().set("align-items", "center");
    card.getStyle().set("justify-content", "space-between");

    // Identificação da Mesa
    String mesaTexto = (order.getTableId() != null) ? "MESA " + order.getTableId() : "DELIVERY";
    H3 mesaTitle = new H3(mesaTexto);
    mesaTitle.getStyle().set("margin", "0 0 10px 0");
    mesaTitle.getStyle().set("color", "#333");

    // Valor Total (Bem grande)
    Span totalLabel = new Span("Total a Pagar");
    totalLabel.getStyle().set("font-size", "0.8rem");
    totalLabel.getStyle().set("color", "#666");

    Span totalValue = new Span("$" + order.getTotal());
    totalValue.getStyle().set("font-size", "1.5rem");
    totalValue.getStyle().set("font-weight", "bold");
    totalValue.getStyle().set("color", "#28a745"); // Verde dinheiro
    totalValue.getStyle().set("margin-bottom", "15px");

    // Botão de Cobrar
    Button btnCobrar = new Button("Cobrar", new Icon(VaadinIcon.DOLLAR));
    btnCobrar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    btnCobrar.setWidthFull();

    btnCobrar.addClickListener(e -> {
      try {
        // 1. Processa pagamento
        orderService.payOrder(order.getId());

        // 2. Feedback visual
        Notification.show("Pago registrado: Mesa " + order.getTableId(),
                        3000, Notification.Position.TOP_END)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

        // 3. Remove o quadrado da tela atualizando a lista
        refreshCards();

      } catch (Exception ex) {
        Notification.show("Error: " + ex.getMessage(),
                        5000, Notification.Position.TOP_END)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
      }
    });

    card.add(mesaTitle, totalLabel, totalValue, btnCobrar);
    return card;
  }
}
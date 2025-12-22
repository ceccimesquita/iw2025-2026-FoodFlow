package pos.ui.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant; // Importante para as cores!
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import pos.auth.AuthService;
import pos.auth.RouteGuard;
import pos.domain.Product;
import pos.domain.TableSpot;
import pos.ui.MainLayout;
import pos.service.TableService;
import pos.service.MenuService;
import pos.service.OrderService;
import pos.domain.OrderItem;

import java.util.ArrayList;
import java.util.List;

@PageTitle("Crear Orden")
@Route(value = "ordenes", layout = MainLayout.class)
public class CrearOrdenView extends VerticalLayout implements RouteGuard {

  public List<OrderItem> items = new ArrayList<>();

  public CrearOrdenView(TableService tables, MenuService menu, OrderService orders, AuthService auth) {
    addClassName("orden-view");
    setSizeFull();
    setPadding(true);
    setSpacing(true);
    getStyle().set("align-items", "center");

    // --- Título ---
    var title = new H2("Crear Orden (Mesero)");
    title.addClassName("orden-title");

    // --- Selección de mesa ---
    var tableSelect = new ComboBox<TableSpot>("Mesa");
    tableSelect.setItems(tables.all());
    tableSelect.setItemLabelGenerator(TableSpot::getCode);
    tableSelect.addClassName("orden-combobox");

    // --- Tabla de productos ---
    var grid = new Grid<>(OrderItem.class, false);
    grid.addClassName("orden-grid");
    grid.addColumn(OrderItem::getProductName).setHeader("Producto");
    grid.addColumn(OrderItem::getQty).setHeader("Cant");
    grid.addColumn(OrderItem::getUnitPrice).setHeader("Precio U.");
    grid.addColumn(i -> i.getUnitPrice().multiply(java.math.BigDecimal.valueOf(i.getQty())))
            .setHeader("Subtotal");

    // Product selection and add to order
    var productSelect = new ComboBox<Product>("Producto");
    productSelect.setItems(menu.list());
    productSelect.setItemLabelGenerator(Product::getName);
    productSelect.addClassName("orden-product-select");

    var qty = new IntegerField("Cantidad");
    qty.setMin(1);
    qty.setValue(1);
    qty.setWidth("80px");
    qty.addClassName("orden-cantidad");

    var note = new TextField("Nota");
    note.setPlaceholder("Nota opcional");
    note.addClassName("orden-nota");

    var btnAdd = new Button("Agregar", e -> {
      var p = productSelect.getValue();
      var q = qty.getValue();
      if (p != null && q != null && q > 0) {
        items.add(OrderItem.builder()
                .productId(p.getId())
                .productName(p.getName())
                .qty(q)
                .unitPrice(p.getPrice())
                .comment(note.getValue())
                .build());
        grid.setItems(items); // refresh
        Notification.show("Agregado: " + p.getName());
        productSelect.clear();
        qty.setValue(1);
        note.clear();
      } else {
        Notification.show("Selecciona un producto y cantidad válida.");
      }
    });
    btnAdd.addClassName("orden-add-btn");

    var addProductLayout = new HorizontalLayout(productSelect, qty, note, btnAdd);
    addProductLayout.addClassName("orden-add-product-layout");
    addProductLayout.setAlignItems(Alignment.BASELINE);

    // --- Botones principales ---
    var btnCreate = new Button("Crear Orden", e -> {
      // 1. Validação: Mesa não selecionada
      if (tableSelect.getValue() == null) {
        Notification.show("Selecciona una mesa", 3000, Notification.Position.TOP_END)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        return;
      }

      // 2. Validação: Carrinho vazio
      if (items.isEmpty()) {
        Notification.show("Agrega productos", 3000, Notification.Position.TOP_END)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        return;
      }

      // 3. Tentativa de criar o pedido
      try {
        orders.createTableOrder(tableSelect.getValue().getId(), items, auth.currentUserId());

        // ✅ SUCESSO (VERDE)
        // LUMO_SUCCESS deixa verde. Position.TOP_END coloca no canto superior direito.
        Notification.show("Orden creada exitosamente para la mesa " + tableSelect.getValue().getId(),
                        3000, Notification.Position.TOP_END)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

        // Limpa a tela após sucesso
        items.clear();
        grid.setItems(items);
        tableSelect.clear();

      } catch (RuntimeException ex) {
        // ❌ ERRO DE ESTOQUE (VERMELHO)
        // LUMO_ERROR deixa vermelho. Mostra a mensagem exata do erro.
        Notification.show(ex.getMessage(), 5000, Notification.Position.TOP_END)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
      }
    });
    btnCreate.addClassName("orden-crear-btn");

    var btnDividir = new Button("Dividir cuenta (50/50 demo)", e -> {
      double total = items.stream().mapToDouble(i -> i.getUnitPrice().doubleValue() * i.getQty()).sum();
      Notification.show("Dos cuentas de: $" + (total / 2.0));
    });
    btnDividir.addClassName("orden-dividir-btn");

    var buttons = new HorizontalLayout(btnCreate, btnDividir);
    buttons.addClassName("orden-buttons");

    add(title, tableSelect, addProductLayout, grid, buttons);
  }
}
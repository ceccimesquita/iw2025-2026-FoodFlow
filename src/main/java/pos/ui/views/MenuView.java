package pos.ui.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import pos.domain.Product;
import pos.service.MenuService;
import pos.ui.MainLayout;

@PageTitle("Menú Digital")
@Route(value = "", layout = MainLayout.class)
public class MenuView extends VerticalLayout {

  private final Grid<Product> grid;

  // Removemos OrderService e AuthService do construtor, pois não precisamos mais deles aqui
  public MenuView(MenuService menu) {
    addClassName("menu-view");
    setSizeFull();
    setAlignItems(Alignment.CENTER);
    setJustifyContentMode(JustifyContentMode.START);
    setSpacing(true);
    setPadding(true);

    var title = new H2("Menú Digital");
    title.addClassName("menu-title");

    // --- 1. Grid apenas para Visualização ---
    grid = new Grid<>(Product.class, false);
    grid.addClassName("menu-grid");

    // Colunas simples
    grid.addColumn(Product::getName).setHeader("Producto").setAutoWidth(true);
    grid.addColumn(Product::getCategory).setHeader("Categoría");
    grid.addColumn(Product::getPrice).setHeader("Precio");

    // Removi a coluna "Acción" (botão Añadir) conforme solicitado

    grid.setItems(menu.list());

    // --- 2. Filtros (Mantive pois é útil para ver o cardápio) ---
    var category = new TextField("Categoría (opcional)");
    category.setPlaceholder("Ej: Bebidas");
    category.addClassName("menu-input");

    var btnLoad = new Button("Filtrar", e -> {
      if (category.getValue() == null || category.getValue().isBlank()) {
        grid.setItems(menu.list());
      } else {
        grid.setItems(menu.byCategory(category.getValue()));
      }
    });
    btnLoad.addClassName("menu-btn");

    var header = new HorizontalLayout(category, btnLoad);
    header.setAlignItems(Alignment.BASELINE);
    header.addClassName("menu-header");

    // --- 3. Botão de Navegação ---
    var btnGoToOrder = new Button("Realizar Pedido", e -> {
      // Redireciona para a tela de criar ordem ("ordenes")
      getUI().ifPresent(ui -> ui.navigate("ordenes"));
    });
    btnGoToOrder.addClassName("menu-btn-primary");
    btnGoToOrder.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);

    // Adiciona apenas os componentes visuais
    add(title, header, grid, btnGoToOrder);
  }
}
package pos.ui;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Nav;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

public class MainLayout extends AppLayout {

    public MainLayout() {
        setPrimarySection(Section.DRAWER);   // 🌟 sin navbar fija

        var toggle = new DrawerToggle();
        toggle.addClassName("floating-toggle");
        addToNavbar(toggle);                // 🌟 solo el botón, sin barra

        createDrawer();
    }

    private void createDrawer() {
        var nav = new Nav();
        var list = new UnorderedList();

        list.getStyle().set("list-style", "none").set("padding", "0");

        list.add(itemLink("Mesas", "/mesas"));
        list.add(itemLink("Pedidos", "/ordenes"));
        list.add(itemLink("Cocina", "/cocina"));
        list.add(itemLink("Menú Digital", "/"));
        list.add(itemLink("Caja", "/admin/caja"));
        list.add(itemLink("Inventario", "/admin/inventario"));
        list.add(itemLink("Reportes", "/reports"));
        list.add(itemLink("Login", "/login"));
        list.add(itemLink("Registro", "/register"));
        list.add(itemLink("Analytics", "admin/analytics"));

        nav.add(list);
        nav.getStyle().set("padding", "1rem");

        addToDrawer(nav);
    }

    private ListItem itemLink(String text, String href) {

        var a = new Anchor(href, text);
        a.getStyle().set("text-decoration", "none");
        a.addClassNames(LumoUtility.TextColor.BODY);

        var li = new ListItem(a);
        li.getStyle().set("margin-bottom", "0.3rem");

        return li;
    }
}

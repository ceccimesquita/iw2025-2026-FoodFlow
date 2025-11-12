package pos.ui.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import pos.auth.AuthService;
import pos.auth.RouteGuard;
import pos.domain.Role;
import pos.ui.MainLayout;

@PageTitle("Login")
@Route(value = "login", layout = MainLayout.class)
public class LoginView extends VerticalLayout implements RouteGuard {

  public LoginView(AuthService auth) {
    addClassName("login-view");
    setSizeFull();
    setAlignItems(Alignment.CENTER);
    setJustifyContentMode(JustifyContentMode.CENTER);

    // === Título ===
    var title = new H2("Iniciar Sesión");
    title.addClassName("login-title");

    // === Campos de usuario y rol ===
    var user = new TextField("Usuario");
    user.addClassName("login-input");
    user.setValue("carlos");

    var role = new ComboBox<Role>("Rol");
    role.setItems(Role.values());
    role.setValue(Role.MESERO);
    role.addClassName("login-select");

    // === Botón ===
    var btn = new Button("Entrar", e -> {
      auth.login(user.getValue(), role.getValue().name());
      getUI().ifPresent(ui -> ui.navigate(""));
    });
    btn.addClassName("login-btn");

    // === Layout principal ===
    var form = new VerticalLayout(title, user, role, btn);
    form.addClassName("login-form");
    form.setAlignItems(Alignment.CENTER);
    form.setSpacing(true);
    form.setPadding(true);

    add(form);
  }
}

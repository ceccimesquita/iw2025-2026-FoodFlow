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

@PageTitle("Registro")
@Route(value = "register", layout = MainLayout.class)
public class RegisterView extends VerticalLayout implements RouteGuard {

  public RegisterView(AuthService auth) {
    addClassName("register-view");
    setSizeFull();
    setAlignItems(Alignment.CENTER);
    setJustifyContentMode(JustifyContentMode.CENTER);

    // === Título ===
    var title = new H2("Crear cuenta");
    title.addClassName("register-title");

    // === Campos ===
    var username = new TextField("Usuario");
    username.addClassName("register-input");
    username.setValue("nuevo");

    var role = new ComboBox<Role>("Rol");
    role.setItems(Role.values());
    role.setValue(Role.CLIENTE);
    role.addClassName("register-select");

    // === Botón ===
    var registerBtn = new Button("Registrarme", e -> {
      auth.login(username.getValue(), role.getValue().name());
      getUI().ifPresent(ui -> ui.navigate("menu"));
    });
    registerBtn.addClassName("register-btn");

    // === Layout principal ===
    var form = new VerticalLayout(title, username, role, registerBtn);
    form.addClassName("register-form");
    form.setAlignItems(Alignment.CENTER);
    form.setSpacing(true);
    form.setPadding(true);

    add(form);
  }
}


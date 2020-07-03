package controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import managers.ConnectionManager;
import server.Response;
import server.database.User;

import java.awt.*;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.UnresolvedAddressException;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

class Lights {
    public final static DropShadow RED_LIGHT = new DropShadow(25, 0, 0, Color.RED);
    public final static DropShadow BLUE_LIGHT = new DropShadow(25, 0, 0, Color.DEEPSKYBLUE);
    public final static DropShadow GREEN_LIGHT = new DropShadow(25, 0, 0, Color.LIGHTGREEN);
}

class Borders {
    public final static Border RED_BORDER = new Border(new BorderStroke(Paint.valueOf("red"), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1)));
}

public class LoginController implements Initializable {

    @FXML
    private HBox toolbar;
    @FXML
    private BorderPane mainPane;
    @FXML
    private TextField addressInput;
    @FXML
    private TextField portInput;
    @FXML
    private TextField loginInput;
    @FXML
    private TextField passwordInput;
    @FXML
    private Label errorMsg;
    private String address = "";
    private int port = 0;
    private String login = "";
    private String password = "";
    private ConnectionManager conMan;
    private ResourceBundle lng;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        lng = resources;
        toolbar.setOnMousePressed(event -> toolbar.setCursor(Cursor.MOVE));
        toolbar.setOnMouseEntered(event -> {
            if (!event.isPrimaryButtonDown()) toolbar.setCursor(Cursor.HAND);
        });
        toolbar.setOnMouseExited(event -> {
            if (!event.isPrimaryButtonDown()) toolbar.setCursor(Cursor.DEFAULT);
        });
        Arrays.asList(addressInput, portInput, loginInput, passwordInput).forEach(el ->
                el.focusedProperty().addListener((obs, oldVal, newVal) ->
                        el.setEffect(newVal ? Lights.BLUE_LIGHT : null))
        );
        addressInput.textProperty().addListener((obs, oldVal, newVal) -> address = newVal);
        portInput.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                port = Integer.parseInt(newVal);
                if (port <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                showNotification(Lights.RED_LIGHT, "Wrong port. Need to be positive integer.");
            }
        });
        loginInput.textProperty().addListener((obs, oldVal, newVal) -> login = newVal);
        passwordInput.textProperty().addListener((obs, oldVal, newVal) -> password = newVal);
    }

    @FXML
    public void showCredits() throws URISyntaxException, IOException {
        Desktop.getDesktop().browse(new URI("https://vk.com/pro100nekitxd"));
    }

    @FXML
    public void minimizeWindow() {
        ClientGUI.stage.setIconified(true);
    }

    @FXML
    public void closeProgram() {
        Platform.exit();
    }

    private void doFirstConnect() {
        Arrays.asList(addressInput, portInput).forEach(f -> {
            if (f.getText().trim().isEmpty()) {
                f.setEffect(Lights.RED_LIGHT);
                showNotification(Lights.RED_LIGHT, lng.getString("error.validate.not_empty"));
            }
        });
        if (!address.isEmpty() && port > 0) {
            ConnectionManager.create(address, port);
            conMan = ConnectionManager.getInstance();
        }
    }

    @FXML
    private void showNotification(DropShadow lightning, String msg) {
        errorMsg.setText(msg);
        errorMsg.setEffect(lightning);
    }

    @FXML
    public void register() {
        doFirstConnect();
        try {
            Arrays.asList(loginInput, passwordInput).forEach(f -> {
                if (f.getText().trim().isEmpty()) {
                    f.setEffect(Lights.RED_LIGHT);
                    showNotification(Lights.RED_LIGHT, lng.getString("error.validate.not_empty"));
                }
            });
            if (!login.isEmpty() && !password.isEmpty()) {
                ConnectionManager.setUser(new User(-1, login, password));
                Response r = conMan.execute("register");
                showNotification(Lights.GREEN_LIGHT, r.getText());
                if (r.getText().toLowerCase().contains("success")) {
                    login();
                } else {
                    ConnectionManager.setUser(null);
                }
            }
        } catch (Exception e) {
            showNotification(Lights.RED_LIGHT, e.getLocalizedMessage());
        }
    }

    @FXML
    public void login() {
        doFirstConnect();
        try {
            if (ConnectionManager.getUser() == null) ConnectionManager.setUser(new User(-1, login, password));
            Response r = conMan.execute("login");
            if (!r.getText().toLowerCase().contains("success")) {
                showNotification(Lights.RED_LIGHT, lng.getString("error.login.match"));
                loginInput.setEffect(Lights.RED_LIGHT);
                passwordInput.setEffect(Lights.RED_LIGHT);
                ConnectionManager.setUser(null);
            } else {
                showNotification(Lights.GREEN_LIGHT, conMan.execute("login").getText());
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/interfaces/viewer.fxml"), ClientGUI.bundle);
                Parent root = loader.load();
                mainPane.setCenter(root);
            }
        } catch (ConnectException e) {
            showNotification(Lights.RED_LIGHT, lng.getString("connect.error"));
        } catch (UnresolvedAddressException e) {
            showNotification(Lights.RED_LIGHT, lng.getString("address.error"));
        } catch (Exception e) {
            showNotification(Lights.RED_LIGHT, lng.getString("unknown.error"));
        }
    }

    @FXML
    public void logOut() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/interfaces/login.fxml"), ClientGUI.bundle);
            Parent root = loader.load();
            ConnectionManager.setUser(null);
            mainPane.setBottom(null);
            mainPane.setCenter(root);
        } catch (Exception e) {
            showNotification(Lights.RED_LIGHT, lng.getString("unknown.error"));
        }
    }

    public void changeLocale(Locale l) {
        java.util.Locale.setDefault(l);
        ClientGUI.setBundle(ResourceBundle.getBundle("resources/locales.Locale", l));
        lng = ClientGUI.bundle;
        logOut();
    }

    @FXML
    public void russian() {
        changeLocale(new Locale("ru", "RU"));
    }

    @FXML
    public void english() {
        changeLocale(Locale.ENGLISH);
    }

    @FXML
    public void french() {
        changeLocale(Locale.FRENCH);
    }

    @FXML
    public void sk() {
        changeLocale(new Locale("sk", "SK"));
    }
}
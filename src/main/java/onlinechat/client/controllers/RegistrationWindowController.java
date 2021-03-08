package onlinechat.client.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import onlinechat.client.ChatClientApp;
import onlinechat.client.models.Network;
import onlinechat.client.models.YesNoAlert;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class RegistrationWindowController {
    private static final Logger LOGGER = LogManager.getLogger("clientLogs");
    private Network network;
    private ChatClientApp chatClientApp;

    @FXML
    private TextField loginField;

    @FXML
    private TextField nickNameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField repeatPasswordField;

    @FXML
    private Button registerButton;


    public void setNetwork(Network network) {
        this.network = network;
    }

    public void setChatClientApp(ChatClientApp chatClientApp) {
        this.chatClientApp = chatClientApp;
    }

    @FXML
    void initialize() {
        //Чтобы кнопка "зарегистрировать" становилась активной, когда все поля заполнены:
        loginField.textProperty().addListener((observableValue, oldValue, newValue) ->
                registerButton.setDisable(loginField.getText().isBlank() || nickNameField.getText().isBlank() || passwordField.getText().isBlank() || repeatPasswordField.getText().isBlank()));

        nickNameField.textProperty().addListener((observableValue, oldValue, newValue) ->
                registerButton.setDisable(loginField.getText().isBlank() || nickNameField.getText().isBlank() || passwordField.getText().isBlank() || repeatPasswordField.getText().isBlank()));

        passwordField.textProperty().addListener((observableValue, oldValue, newValue) ->
                registerButton.setDisable(loginField.getText().isBlank() || nickNameField.getText().isBlank() || passwordField.getText().isBlank() || repeatPasswordField.getText().isBlank()));

        repeatPasswordField.textProperty().addListener((observableValue, oldValue, newValue) ->
                registerButton.setDisable(loginField.getText().isBlank() || nickNameField.getText().isBlank() || passwordField.getText().isBlank() || repeatPasswordField.getText().isBlank()));
    }

    @FXML
    void registerNewUser() {
        LOGGER.info("Нажата кнопка регистрации нового пользователя");

        String login = loginField.getText().trim();
        String nickName = nickNameField.getText().trim();
        String password = passwordField.getText().trim();
        String repeatPassword = repeatPasswordField.getText().trim();
        if (checkValidFields(login, password, repeatPassword, nickName)) {
            try {
                String registerUserErrorMessage = network.sendRegisterNewUserCommand(login, nickName, DigestUtils.md5Hex(password));

                if (registerUserErrorMessage != null) {
                    if ((new YesNoAlert(Alert.AlertType.ERROR, "Ошибка при регистрации нового пользователя", registerUserErrorMessage, "Хотите повторить попытку регистрации?", false)).showAndWait().get() == YesNoAlert.noButton) {
                        chatClientApp.closeRegistrationWindow();
                    }
                } else {
                    LOGGER.info("Успещная регистрация пользователя");
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Пользователь успешно зарегистрирован");
                    alert.setHeaderText("Пользователь успешно зарегистрирован");
                    alert.setContentText("Запомните логин и пароль");
                    alert.showAndWait();
                    chatClientApp.closeRegistrationWindow();
                }

            } catch (IOException e) {
                LOGGER.error("Ошибка при регистрации нового пользователя");
                LOGGER.error(e.toString());
                e.printStackTrace();
                if ((new YesNoAlert(Alert.AlertType.ERROR, "Ошибка при регистрации нового пользователя", e.getMessage(), "Хотите повторить попытку регистрации?", false)).showAndWait().get() == YesNoAlert.noButton) {
                    chatClientApp.closeRegistrationWindow();
                }
            }
        }
    }

    private boolean checkValidFields(String login, String password, String repeatPassword, String nickName) {
        //таких условий по идее быть не должно, т.е. добвил листенеры в методе initialize(). Но на всякий случай оставлю:
        if (login.isBlank() || nickName.isBlank() || password.isBlank() || repeatPassword.isBlank()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Все поля должны быть заполнены!");
            alert.setHeaderText("Все поля должны быть заполнены!");
            alert.setContentText("Пожалуйста, заполните все поля");
            alert.showAndWait();
            return false;
        } else if (!password.equals(repeatPassword)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Введенные пароли не совпадают!");
            alert.setHeaderText("Введенные пароли не совпадают!");
            alert.setContentText("Пожалуйста, внимательно повторите ввод паролей");
            alert.showAndWait();
            passwordField.clear();
            repeatPasswordField.clear();
            passwordField.requestFocus();
            return false;
        } else {
            return true;
        }
    }

    @FXML
    void focusToNickNameField() {
        nickNameField.requestFocus();
    }

    @FXML
    void focusToPasswordField() {
        passwordField.requestFocus();

    }

    @FXML
    void focusToRepeatPasswordField() {
        repeatPasswordField.requestFocus();

    }


}

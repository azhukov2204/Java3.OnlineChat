package onlinechat.client.controllers;

import java.io.IOException;
import java.net.SocketException;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import onlinechat.client.ChatClientApp;
import onlinechat.client.models.MyAlert;
import onlinechat.client.models.Network;

public class AuthWindowController {

    private Network network;
    private ChatClientApp chatClientApp;

    public void setNetwork(Network network) {
        this.network = network;
    }

    public void setChatClientApp(ChatClientApp chatClientApp) {
        this.chatClientApp = chatClientApp;
    }


    @FXML
    private TextField loginField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button enterButton;


    @FXML
    void doAuthentication(ActionEvent event) {
        String login = loginField.getText().trim();
        String password = passwordField.getText().trim();

        if (login.length() == 0) {
            System.out.println("Логин не может быть пустым");
            return;
        }

        String authErrorMessage;
        try {
            authErrorMessage = network.sendAuthCommand(login, password);
        } catch (SocketException e) {
            e.printStackTrace();
            authErrorMessage = "SocketException";
        } catch (IOException e) {
            e.printStackTrace();
            authErrorMessage = e.getMessage();
        }

        if (authErrorMessage == null) {
            chatClientApp.startChat();
        } else if (authErrorMessage.equals("SocketException")) {
            if ((new MyAlert(Alert.AlertType.ERROR, "Отсутствует подключение", "Отсутствует подключение, возможно из-за длительного бездействия", "Повторить попытку подключения?")).showAndWait().get() == MyAlert.yesButton) {
                network.connection();
            } else {
                System.exit(-1);
            }
        }
        else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка аутентификации");
            alert.setHeaderText("Ошибка аутентификации");
            alert.setContentText(authErrorMessage);
            alert.show();
        }
    }


    @FXML
    void focusToPasswordField(ActionEvent event) {
        passwordField.requestFocus();
    }

}

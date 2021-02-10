package onlinechat.client.controllers;

import javafx.scene.control.Alert;
import onlinechat.client.ChatClientApp;
import onlinechat.client.models.Network;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.awt.*;
import java.io.IOException;

public class NickNameChangeController {
    private Network network;
    private ChatClientApp chatClientApp;

    @FXML
    private TextField newNickNameField;

    public void setNetwork(Network network) {
        this.network = network;
    }

    public void setChatClientApp(ChatClientApp chatClientApp) {
        this.chatClientApp = chatClientApp;
    }

    public void clearNewNickNameField() {
        newNickNameField.clear();
    }

    @FXML
    void changeNickName() {
        System.out.println("Смена ника");
        String newNickName = newNickNameField.getText().trim();

        if (newNickName.isBlank()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Имя пользователя не может быть пустым!");
            alert.setHeaderText("Имя пользователя не может быть пустым!");
            alert.setContentText("Введите имя пользователя");
            alert.show();
        } else {
            try {
                network.sendChangeNickNameCommand(newNickName);
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Ошибка смены имени пользователя");
                alert.setHeaderText("Ошибка смены имени пользователя");
                alert.setContentText(e.getMessage());
                alert.show();
                chatClientApp.closeChangeNickNameWindows();
            }
        }



        /*Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка аутентификации");
        alert.setHeaderText("Ошибка аутентификации");
        alert.setContentText(authErrorMessage);
        alert.show();*/

    }

}

package onlinechat.client.controllers;

import javafx.scene.control.Alert;
import onlinechat.client.models.Network;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.io.IOException;

public class NickNameChangeController {
    private Network network;

    @FXML
    private TextField newNickNameField;

    public void setNetwork(Network network) {
        this.network = network;
    }


    @FXML
    void changeNickName() {
        String newNickName = newNickNameField.getText().trim();

        if (newNickName.isBlank()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Имя пользователя не может быть пустым!");
            alert.setHeaderText("Имя пользователя не может быть пустым!");
            alert.setContentText("Введите имя пользователя");
            alert.showAndWait();
        } else {
            try {
                network.sendChangeNickNameCommand(newNickName);
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Ошибка смены имени пользователя");
                alert.setHeaderText("Ошибка смены имени пользователя");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        }

    }

}

package onlinechat.client.controllers;

import javafx.scene.control.Alert;
import onlinechat.client.models.Network;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class NickNameChangeController {
    private static final Logger LOGGER = LogManager.getLogger("clientLogs");
    private Network network;

    @FXML
    private TextField newNickNameField;

    public void setNetwork(Network network) {
        this.network = network;
    }


    @FXML
    void changeNickName() {
        LOGGER.info("Попытка смены имени пользователя");
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

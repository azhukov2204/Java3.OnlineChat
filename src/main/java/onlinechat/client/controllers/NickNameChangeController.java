package onlinechat.client.controllers;

import onlinechat.client.ChatClientApp;
import onlinechat.client.models.Network;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.awt.*;

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

    }

}

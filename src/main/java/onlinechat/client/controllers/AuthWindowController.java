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
import onlinechat.client.models.YesNoAlert;
import onlinechat.client.models.Network;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AuthWindowController {

    private static final Logger LOGGER = LogManager.getLogger("clientLogs");

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
    private TextField serverHostField;

    @FXML
    private TextField serverPortField;

    @FXML
    private Button enterButton;

    @FXML
    void initialize() {
        serverPortField.textProperty().addListener((observableValue, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                serverPortField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            if (newValue.length() >= 4) {
                serverPortField.setText(newValue.substring(0, 4));

            }
        });
    }

    @FXML
    void doConnectionAndAuthentication(ActionEvent event) {
        LOGGER.info("Попытка аутентификации");
        String login = loginField.getText().trim();
        String password = passwordField.getText().trim();

        if (login.length() == 0) {
            LOGGER.info("Введен пустой логин. Логин не может быть пустым");
            return;
        }

        tryConnection();
        if (network.isConnected()) {
            tryAuthentication(login, password);
        }
    }

    private void tryConnection() {
        if (!network.isConnected()) {
            String serverHost = serverHostField.getText().trim();
            String serverPort = serverPortField.getText().trim();

            if (!serverHost.isBlank()) {
                network.setServerHost(serverHost);
            } else {
                network.setServerHost(); //установка дефолтного значения, если ничего не введено
            }

            if (!serverPort.isBlank()) {
                network.setServerPort(Integer.parseInt(serverPort));
            } else {
                network.setServerPort(); //установка дефолтного значения, если ничего не введено
            }

            if (network.connection()) {
                serverHostField.setDisable(true);
                serverPortField.setDisable(true);
                LOGGER.info("Запись строки подключения в файл");

                chatClientApp.getLastSuccessConnectionAddressWriterAndReader().writeToFile(serverHost, serverPort);
            }
        }
    }

    private void tryAuthentication(String login, String password) {
        String authErrorMessage;
        try {
            String hashMD5Password = DigestUtils.md5Hex(password);
            authErrorMessage = network.sendAuthCommand(login, hashMD5Password);
        } catch (SocketException e) {
            LOGGER.warn("При вызове sendAuthCommand произошла SocketException");
            LOGGER.warn(e.toString());
            e.printStackTrace();
            authErrorMessage = "SocketException";
        } catch (IOException e) {
            LOGGER.warn("При вызове sendAuthCommand произошла IOException");
            LOGGER.warn(e.toString());
            e.printStackTrace();
            authErrorMessage = e.getMessage();
        }

        if (authErrorMessage == null) {
            chatClientApp.startChat();
        } else if (authErrorMessage.equals("SocketException")) {
            if ((new YesNoAlert(Alert.AlertType.ERROR, "Отсутствует подключение", "Отсутствует подключение, возможно из-за длительного бездействия", "Повторить попытку подключения?", true)).showAndWait().get() == YesNoAlert.yesButton) {
                network.closeConnection();
                serverHostField.setDisable(false);
                serverPortField.setDisable(false);
            } else {
                System.exit(-1);
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка аутентификации");
            alert.setHeaderText("Ошибка аутентификации");
            alert.setContentText(authErrorMessage);
            alert.show();
        }
    }

    public void fillConnectionAddress(String connectionString) {
        String[] str = connectionString.split("\\|");
        if (str.length == 2) { //если строка правильного формата
            String host = str[0].trim();
            String port = str[1].trim();
            if (!host.isBlank()) { //Хост
                serverHostField.setText(host);
            }
            if (!port.isBlank()) {
                serverPortField.setText(port);
            }
        }
    }

    @FXML
    void doRegisterNewUser(ActionEvent event) {
        LOGGER.info("Нажата кнопка регистрации нового пользователя");
        tryConnection();
        if (network.isConnected()) {
            try {
                chatClientApp.createAndStartRegistrationWindow();
            } catch (IOException e) {
                LOGGER.error("Не удалось запустить окно регистрации нового пользователя");
                LOGGER.error(e.toString());
                e.printStackTrace();
            }
        }
    }


    @FXML
    void focusToPortField(ActionEvent event) {
        serverPortField.requestFocus();
    }

    @FXML
    void focusToLoginField(ActionEvent event) {
        loginField.requestFocus();
    }

    @FXML
    void focusToPasswordField(ActionEvent event) {
        passwordField.requestFocus();
    }


    @FXML
    void setActiveEnterButton() {
        enterButton.setDisable(loginField.getText().trim().isBlank()); //если что-то введено, то кнопку делаем активной
    }

}

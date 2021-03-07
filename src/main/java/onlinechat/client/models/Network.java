package onlinechat.client.models;

import javafx.scene.control.Alert;
import onlinechat.client.ChatClientApp;
import onlinechat.client.controllers.MainChatWindowController;
import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Network {

    private static final String DEFAULT_SERVER_HOST = "localhost";
    private static final int DEFAULT_SERVER_PORT = 8081;

    private static final String AUTH_CMD_PREFIX = "/auth"; // + login + pass
    private static final String AUTHOK_CMD_PREFIX = "/authok"; // + username
    private static final String AUTHERR_CMD_PREFIX = "/autherr"; // + error message
    private static final String CLIENT_MSG_CMD_PREFIX = "/clientMsg"; // login + msg
    private static final String SERVER_MSG_CMD_PREFIX = "/serverMsg"; // + msg
    private static final String PRIVATE_MSG_CMD_PREFIX = "/w"; //sender + p + msg
    private static final String END_CMD_PREFIX = "/end"; //
    private static final String USERSLIST_CMD_PREFIX = "/usersList"; // + userslist
    private static final String USERSLISTRQ_CMD_PREFIX = "/usersListRq"; // + userslist

    private static final String CHANGE_NICKNAME_CMD_PREFIX = "/changeNickName"; // + newNickName
    private static final String CHANGE_NICKNAME_OK_CMD_PREFIX = "/changeNickNameOK"; // + newNickName
    private static final String CHANGE_NICKNAME_ERR_CMD_PREFIX = "/changeNickNameErr"; // + error message


    private String serverHost;
    private int serverPort;
    private Socket clientSocket;
    private DataInputStream in = null;
    private DataOutputStream out = null;

    private String nickName;
    private String userLogin = "";
    private ChatClientApp chatClientApp;
    private MainChatWindowController mainChatWindowController;

    private boolean isConnected = false;

    private static final Logger LOGGER = LogManager.getLogger("clientLogs");

    public void setChatClientApp(ChatClientApp chatClientApp) {
        this.chatClientApp = chatClientApp;
    }

    public Network() {
        setServerHost();
        setServerPort();
    }

    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

    public void setServerHost() {
        this.serverHost = DEFAULT_SERVER_HOST;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public void setServerPort() {
        this.serverPort = DEFAULT_SERVER_PORT;
    }


    public void setMainChatWindowController(MainChatWindowController mainChatWindowController) {
        this.mainChatWindowController = mainChatWindowController;
    }

    public String getNickName() {
        return nickName;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public boolean connection() {
        try {
            clientSocket = new Socket(serverHost, serverPort);
            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());
            isConnected = true;
            LOGGER.info("Соединение установлено");
        } catch (IOException e) {
            isConnected = false;
            LOGGER.warn("Отсутствует подключение");
            LOGGER.warn(e.toString());
            e.printStackTrace();

            if ((new MyAlert(Alert.AlertType.ERROR, "Невозможно установить соединение", "Невозможно установить соединение", "Повторить попытку подключения? \nПроверьте корректность указанного адреса и порта сервера")).showAndWait().get() != MyAlert.yesButton) {
                System.exit(-1);
            }
        }
        return isConnected;
    }

    public void closeConnection() {
        isConnected = false;
        try {
            clientSocket.close();
        } catch (IOException e) {
            LOGGER.error("Ошибка при закрытии clientSocket");
            LOGGER.error(e.toString());
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void startReceiver() {
        Thread receiver = new Thread(() -> {
            while (isConnected) {
                try {
                    String message = in.readUTF();
                    if (!message.isBlank()) {
                        String[] partsOfMessage = message.split(";", 2);
                        switch (partsOfMessage[0]) {
                            case CLIENT_MSG_CMD_PREFIX -> {
                                String[] partsOfClientMessage = message.split(";", 3);
                                Platform.runLater(() -> mainChatWindowController.addMessage(partsOfClientMessage[2], partsOfClientMessage[1]));
                            }
                            case SERVER_MSG_CMD_PREFIX -> {
                                String[] partsOfServerMessage = message.split(";", 2);
                                Platform.runLater(() -> mainChatWindowController.addMessage(partsOfServerMessage[1], "Сервер"));
                            }
                            case USERSLIST_CMD_PREFIX -> {
                                String[] activeUsers = message.replace(USERSLIST_CMD_PREFIX + ";", "").split(";");
                                Platform.runLater(() -> mainChatWindowController.updateUsersList(activeUsers));
                            }
                            case CHANGE_NICKNAME_OK_CMD_PREFIX -> Platform.runLater(() -> {
                                nickName = partsOfMessage[1];
                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setTitle("Имя пользователя изменено успешно");
                                alert.setHeaderText("Имя пользователя изменено успешно");
                                alert.showAndWait();
                                chatClientApp.closeChangeNickNameWindows();
                            });
                            case CHANGE_NICKNAME_ERR_CMD_PREFIX -> Platform.runLater(() -> {
                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                alert.setTitle("Ошибка смены имени пользователя");
                                alert.setHeaderText("Ошибка смены имени пользователя");
                                alert.setContentText(partsOfMessage[1]);
                                alert.showAndWait();
                            });
                            default -> Platform.runLater(() -> System.out.println("!!Неизвестная ошибка сервера" + message));
                        }
                    }
                } catch (IOException e) {
                    isConnected = false;
                    LOGGER.warn("Соединение прервано");
                    LOGGER.warn(e.toString());
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        try {
                            if ((new MyAlert(Alert.AlertType.ERROR, "Отсутствует подключение", "Отсутствует подключение", "Сеанс завершен. Повторить вход в чат? Будет запущен новый сеанс")).showAndWait().get() == MyAlert.yesButton) {
                                chatClientApp.restartChat();
                            } else {
                                System.exit(-1);
                            }
                        } catch (IOException ioException) {
                            LOGGER.error(ioException.toString());
                            ioException.printStackTrace();
                        }
                    });
                }
            }
            LOGGER.info("Receiver остановлен");
        });
        receiver.setDaemon(true);
        receiver.start();
    }


    public void sendMessage(String message, MainChatWindowController mainChatWindowController) throws IOException {
        if (clientSocket.isConnected() && isConnected) {
            out.writeUTF(message);
            mainChatWindowController.addMessage(message, "Я");
        }
    }

    public void sendPrivateMessage(String recipientNickName, String message) throws IOException {
        if (clientSocket.isConnected() && isConnected) {
            out.writeUTF(String.format("%s;%s;%s", PRIVATE_MSG_CMD_PREFIX, recipientNickName, message));
            mainChatWindowController.addMessage(message, "Я");
        }
    }

    public String sendAuthCommand(String login, String password) throws IOException {
        out.writeUTF(String.format("%s;%s;%s", AUTH_CMD_PREFIX, login, password));
        String response = in.readUTF();

        if (response.startsWith(AUTHOK_CMD_PREFIX)) {
            nickName = response.split(";", 3)[1];
            userLogin = login;
            return null;
        } else {
            return response.split(";", 2)[1];
        }
    }

    public void sendChangeNickNameCommand(String newNickName) throws IOException {
        out.writeUTF(String.format("%s;%s", CHANGE_NICKNAME_CMD_PREFIX, newNickName));
    }


}

package onlinechat.servermultiusers.myserver.handler;

import onlinechat.servermultiusers.myserver.MyServer;
import onlinechat.servermultiusers.myserver.services.AuthAndLoginService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;

public class ClientHandler {
    private final MyServer myServer;
    private final Socket clientSocket;
    private final AuthAndLoginService authAndLoginService;
    private DataInputStream in;
    private DataOutputStream out;

    private String nickName;
    private String login = "";

    private static final int SOCKET_TIMEOUT_MS = 120000; //тайм-аут 2 минуты. Если клиент не проявляет активность во время аутентификации в течении этого времени - сокет закрывается

    private static final String AUTH_CMD_PREFIX = "/auth"; // + login + pass
    private static final String AUTHOK_CMD_PREFIX = "/authok"; // + username
    private static final String AUTHERR_CMD_PREFIX = "/autherr"; // + error message
    private static final String CLIENT_MSG_CMD_PREFIX = "/clientMsg"; // + msg
    private static final String SERVER_MSG_CMD_PREFIX = "/serverMsg"; // + msg
    private static final String PRIVATE_MSG_CMD_PREFIX = "/w"; //sender + msg
    private static final String END_CMD_PREFIX = "/end"; //
    private static final String USERSLIST_CMD_PREFIX = "/usersList"; // + userslist
    private static final String USERSLISTRQ_CMD_PREFIX = "/usersListRq"; // + userslist

    private static final String CHANGE_NICKNAME_CMD_PREFIX = "/changeNickName"; // + newNickName
    private static final String CHANGE_NICKNAME_OK_CMD_PREFIX = "/changeNickNameOK"; // + newNickName
    private static final String CHANGE_NICKNAME_ERR_CMD_PREFIX = "/changeNickNameErr"; // + newNickName

    private static final String REGISTER_NEW_USER_CMD_PREFIX = "/registerNewUser"; // + login + nickname + password
    private static final String REGISTER_NEW_USER_OK_CMD_PREFIX = "/registerNewUserOK"; //
    private static final String REGISTER_NEW_USER_ERR_CMD_PREFIX = "/registerNewUserErr"; // + error message

    private static final Logger LOGGER = LogManager.getLogger("serverLogs");

    public ClientHandler(MyServer myServer, Socket clientSocket, AuthAndLoginService authAndLoginService) {
        this.myServer = myServer;
        this.clientSocket = clientSocket;
        this.authAndLoginService = authAndLoginService;
    }

    public void startHandler() throws IOException {
        in = new DataInputStream(clientSocket.getInputStream());
        out = new DataOutputStream(clientSocket.getOutputStream());

        new Thread(() -> {
            try {
                authenticationOrRegistrationAndSubscribe();
                startReceiver();
            } catch (IOException | SQLException e) {
                //e.printStackTrace();
                LOGGER.warn(e.toString()); //эта ситуация как правило ожидаемая, когда клиент отключается
            } finally {
                try {
                    myServer.unsubscribeClient(this);
                    clientSocket.close();
                } catch (IOException e) {
                    LOGGER.error("Ошибка при выполнении unsubscribeClient\n" + e.toString());
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void authenticationOrRegistrationAndSubscribe() throws IOException, SQLException {
        String message;
        LOGGER.info(String.format("Устанавливаем тайм-аут сокета %d мс", SOCKET_TIMEOUT_MS));
        clientSocket.setSoTimeout(SOCKET_TIMEOUT_MS);

        boolean isAuthenticationSuccessful = false;
        do {
            message = in.readUTF();
            if (message.startsWith(AUTH_CMD_PREFIX)) {
                isAuthenticationSuccessful = doAuthentication(message);
            } else if (message.startsWith(REGISTER_NEW_USER_CMD_PREFIX)) {
                doRegistration(message);
            } else {
                out.writeUTF(AUTHERR_CMD_PREFIX + ";Неизвестная команда");
            }
        } while (!isAuthenticationSuccessful);

        myServer.subscribeClient(this);
        LOGGER.info("Снимаем с сокета ограничение по тайм-ауту");
        clientSocket.setSoTimeout(0);   //после прохождения аутентификации снимаем ограничение по тайм-ауту
    }

    private boolean doAuthentication(String message) throws IOException, SQLException {
        String[] authMessageParts = message.split(";", 3);
        if (authMessageParts.length != 3) {
            out.writeUTF(AUTHERR_CMD_PREFIX + ";Неверная команда авторизации");
            return false;
        }
        String enteredLogin = authMessageParts[1];
        String enteredPassword = authMessageParts[2];

        nickName = authAndLoginService.getNickNameByLoginAndPassword(enteredLogin, enteredPassword);

        if (nickName != null) {
            if (myServer.isNickNameBusy(nickName)) {
                out.writeUTF(AUTHERR_CMD_PREFIX + ";Пользователь с таким логином уже авторизован");
                return false;
            } else {
                out.writeUTF(AUTHOK_CMD_PREFIX + ";" + nickName + ";успешно авторизован");
                login = enteredLogin;
                return true;
            }
        } else {
            out.writeUTF(AUTHERR_CMD_PREFIX + ";Введены неверные логин или пароль");
            return false;
        }
    }

    private void doRegistration(String message) throws IOException, SQLException {
        String[] registrationMessageParts = message.split(";", 4);
        if (registrationMessageParts.length != 4) {
            out.writeUTF(REGISTER_NEW_USER_ERR_CMD_PREFIX + ";Неверная команда регистрации нового пользователя");
            return;
        }
        String newUserLogin = registrationMessageParts[1];
        String newUserNickName = registrationMessageParts[2];
        String newUserPassword = registrationMessageParts[3];

        String errorMessage = authAndLoginService.registrationNewUser(newUserLogin, newUserNickName, newUserPassword);

        if (errorMessage != null) {
            out.writeUTF(REGISTER_NEW_USER_ERR_CMD_PREFIX + ";" + errorMessage);
        } else {
            out.writeUTF(REGISTER_NEW_USER_OK_CMD_PREFIX);
        }

    }

    private void startReceiver() throws IOException {
        while (true) {
            String message = in.readUTF();
            if (!message.isBlank()) {
                String[] partsOfMessage = message.split(";", 2);
                switch (partsOfMessage[0]) {
                    case END_CMD_PREFIX: //завершаем работу этого указателя/клиента
                        return;
                    case PRIVATE_MSG_CMD_PREFIX:
                        String[] partsOfPrivateMessages = message.split(";", 3);
                        if (partsOfPrivateMessages.length != 3) {
                            sendMessageToClient(null, " Ошибка отправки приватного сообщения");
                        } else {
                            String recipientNickName = partsOfPrivateMessages[1];
                            String privateMessage = partsOfPrivateMessages[2];
                            if (!myServer.sendPrivateUserMessage(nickName, recipientNickName, privateMessage)) { //отправляем приватное сообщение и сразу проверяем статус отпарвки
                                sendMessageToClient(null, " Ошибка отправки приватного сообщения, получатель не подключен");
                            }
                        }
                        break;
                    case CHANGE_NICKNAME_CMD_PREFIX:
                        changeNickName(partsOfMessage[1]);
                        break;
                    default:
                        myServer.sendBroadcastUserMessage(nickName, message);
                        break;
                }
            }
        }
    }

    public void sendMessageToClient(String senderNickName, String message) throws IOException {
        if (senderNickName != null) {
            out.writeUTF(String.format("%s;%s;%s", CLIENT_MSG_CMD_PREFIX, senderNickName, message));
        } else {
            out.writeUTF(String.format("%s;%s", SERVER_MSG_CMD_PREFIX, message)); //если отправитель пустой, значит это серверное сообщение
        }
    }

    public void sendUsersListToClient(String usersList) throws IOException {
        out.writeUTF(String.format("%s;%s", USERSLIST_CMD_PREFIX, usersList));
    }

    public void changeNickName(String newNickName) throws IOException {
        LOGGER.info(String.format("Попытка смены ника с %s на %s", nickName, newNickName));
        try {
            if (authAndLoginService.changeNickName(login, newNickName)) {
                out.writeUTF(String.format("%s;%s", CHANGE_NICKNAME_OK_CMD_PREFIX, newNickName));
                myServer.sendBroadcastSystemMessage(String.format("Пользователь %s изменил свой NickName на: %s", nickName, newNickName));
                nickName = newNickName;
                myServer.sendActiveUsersList();
            } else {
                out.writeUTF(String.format("%s;%s", CHANGE_NICKNAME_ERR_CMD_PREFIX, "Ошибка при изменении имени пользователя, такое имя занято"));
            }
        } catch (SQLException e) {
            out.writeUTF(String.format("%s;%s", CHANGE_NICKNAME_ERR_CMD_PREFIX, e.getMessage()));
        }
    }


    public String getNickName() {
        return nickName;
    }
}

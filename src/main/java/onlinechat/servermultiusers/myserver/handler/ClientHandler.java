package onlinechat.servermultiusers.myserver.handler;

import onlinechat.servermultiusers.myserver.MyServer;
import onlinechat.servermultiusers.myserver.authservice.BaseAuthService;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;

public class ClientHandler {
    private final MyServer myServer;
    private final Socket clientSocket;
    private final BaseAuthService baseAuthService;
    private DataInputStream in;
    private DataOutputStream out;

    private String nickName;
    private String login="";

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


    public ClientHandler(MyServer myServer, Socket clientSocket, BaseAuthService baseAuthService) {
        this.myServer = myServer;
        this.clientSocket = clientSocket;
        this.baseAuthService = baseAuthService;
    }

    public void startHandler() throws IOException {
        in = new DataInputStream(clientSocket.getInputStream());
        out = new DataOutputStream(clientSocket.getOutputStream());

        new Thread(() -> {
            try {
                authenticationAndSubscribe();
                startReceiver();
            } catch (IOException|SQLException e) {
                //e.printStackTrace();
                System.out.println(e.getMessage());
            } finally {
                try {
                    myServer.unsubscribeClient(this);
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void authenticationAndSubscribe() throws IOException, SQLException {
        String message;
        System.out.printf("Устанавливаем тайм-аут сокета %d мс%n", SOCKET_TIMEOUT_MS);
        clientSocket.setSoTimeout(SOCKET_TIMEOUT_MS);

        boolean isAuthenticationSuccessful = false;
        do {
            message = in.readUTF();
            if (message.startsWith(AUTH_CMD_PREFIX)) {
                isAuthenticationSuccessful = isAuthenticationSuccessful(message);
            } else {
                out.writeUTF(AUTHERR_CMD_PREFIX + ";Ошибка авторизации");
            }
        } while (!isAuthenticationSuccessful);

        myServer.subscribeClient(this);
        System.out.println("Снимаем с сокета ограничение по тайм-ауту");
        clientSocket.setSoTimeout(0);   //после прохождения аутентификации снимаем ограничение по тайм-ауту
    }

    private boolean isAuthenticationSuccessful(String message) throws IOException, SQLException {
        String[] authMessageParts = message.split(";", 3);
        if (authMessageParts.length != 3) {
            out.writeUTF(AUTHERR_CMD_PREFIX + ";Неверная команда авторизации");
            return false;
        }
        String enteredLogin = authMessageParts[1];
        String enteredPassword = authMessageParts[2];

        nickName = baseAuthService.getNickNameByLoginAndPassword(enteredLogin, enteredPassword);

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
        System.out.printf("Попытка смены ника с %s на %s%n", nickName, newNickName);
        try {
            if(baseAuthService.changeNickName(login, newNickName)) {
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

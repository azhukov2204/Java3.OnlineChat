package onlinechat.servermultiusers.myserver;

import onlinechat.servermultiusers.myserver.authservice.BaseAuthService;
import onlinechat.servermultiusers.myserver.handler.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MyServer {
    private final ServerSocket serverSocket;
    private final BaseAuthService baseAuthService;
    private final List<ClientHandler> activeClients = new ArrayList<>();

    public MyServer(int port) throws IOException, ClassNotFoundException, SQLException {
        serverSocket = new ServerSocket(port);
        baseAuthService = new BaseAuthService();
        baseAuthService.startAuthentication();
    }


    public void startMyServer() throws SQLException {
        System.out.println("Сервер запущен");
        try {
            while (true) {
                System.out.println("Ожидаем подключения пользователя");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Клиент подключился, создаем указатель");
                new ClientHandler(this, clientSocket, baseAuthService).startHandler();
            }
        } catch (IOException e) {
            System.out.println("Ошибка при подключении клиента");
            e.printStackTrace();
        } finally {
            if (baseAuthService != null) {
                baseAuthService.endAuthentication();
            }
            System.out.println("Сервер остановлен");
        }
    }

    //методы, которые работают с ClientHandler и могут вызываться из потоков делаем синхронными
    public synchronized boolean isNickNameBusy(String nickName) {
        for (ClientHandler activeClient : activeClients) {
            if (activeClient.getNickName().equals(nickName)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void subscribeClient(ClientHandler clientHandler) throws IOException {
        System.out.println("Подключился клиент " + clientHandler.getNickName());
        activeClients.add(clientHandler);
        sendBroadcastSystemMessage("Подключился клиент " + clientHandler.getNickName());
        printActiveClients();
        sendActiveUsersList(); //отправляем всем клиентам актуальный список подключенных логинов
    }

    public synchronized void unsubscribeClient(ClientHandler clientHandler) throws IOException {
        System.out.println("Отключился клиент " + clientHandler.getNickName());
        activeClients.remove(clientHandler);
        sendBroadcastSystemMessage("Отключился клиент " + clientHandler.getNickName());
        printActiveClients();
        sendActiveUsersList(); //отправляем всем клиентам актуальный список подключенных логинов
    }


    private synchronized void printActiveClients() {
        System.out.println("Список активных клиентов:");
        for (ClientHandler activeClient : activeClients) {
            System.out.println(activeClient.getNickName());
        }
    }

    public synchronized void sendBroadcastUserMessage(String senderNickName, String message) throws IOException {
        for (ClientHandler activeClient : activeClients) {
            if (activeClient.getNickName().equals(senderNickName)) {
                continue;   //самому себе не отправляем, т.к. такое сообщение пишется локально
            }
            activeClient.sendMessageToClient(senderNickName, message);
        }
    }

    public synchronized void sendBroadcastSystemMessage(String message) throws IOException {
        sendBroadcastUserMessage(null, message);
    }

    public synchronized boolean sendPrivateUserMessage(String senderNickName, String recipientNickName, String message) throws IOException {
        for (ClientHandler activeClient : activeClients) {
            if (activeClient.getNickName().equals(recipientNickName)) {
                activeClient.sendMessageToClient(senderNickName, message);
                return true;
            }
        }
        return false;
    }

    public synchronized void sendActiveUsersList() throws IOException {
        StringBuilder activeUsersList = new StringBuilder();

        for (ClientHandler activeClient : activeClients) {
            activeUsersList.append(activeClient.getNickName()).append(";");
        }
        for (ClientHandler activeClient : activeClients) {
            activeClient.sendUsersListToClient(activeUsersList.toString());
        }
    }

}

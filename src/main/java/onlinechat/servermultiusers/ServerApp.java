package onlinechat.servermultiusers;

import onlinechat.servermultiusers.myserver.MyServer;

import java.io.IOException;
import java.sql.SQLException;

public class ServerApp {
    private static final int DEFAULT_PORT=8081;

    public static void main(String[] args) {
        try {
            MyServer myServer = new MyServer(DEFAULT_PORT);
            myServer.startMyServer();
        } catch (IOException|ClassNotFoundException| SQLException e) {
            System.out.println("Произошла ошибка при запуске сервера");
            e.printStackTrace();
            System.exit(-1);
        }

    }
}

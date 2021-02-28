package onlinechat.servermultiusers;

import onlinechat.servermultiusers.myserver.MyServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.sql.SQLException;

public class ServerApp {
    private static final int DEFAULT_PORT=8081;

    private static final Logger LOGGER = LogManager.getLogger();

    public static void main(String[] args) {
        try {
            MyServer myServer = new MyServer(DEFAULT_PORT);
            myServer.startMyServer();
        } catch (IOException|ClassNotFoundException| SQLException e) {
            LOGGER.error("Произошла ошибка при запуске сервера");
            LOGGER.error(e.toString());
            e.printStackTrace();
            System.exit(-1);
        }

    }
}

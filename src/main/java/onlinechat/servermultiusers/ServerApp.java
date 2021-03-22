package onlinechat.servermultiusers;

import onlinechat.servermultiusers.myserver.MyServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.sql.SQLException;

public class ServerApp {
    private static final int DEFAULT_PORT=8081;

    private static final Logger LOGGER = LogManager.getLogger("serverLogs");

    public static void main(String[] args) {
        try {
            int port;
            if (args.length>0 && args[0].matches("^\\d+$")) {
                LOGGER.info("Номер порта передан в аргументах: " + args[0]);
                port = Integer.parseInt(args[0]);
            } else {
                port = DEFAULT_PORT;
            }
            LOGGER.info("Запуск сервера на порту " + port);
            MyServer myServer = new MyServer(port);
            myServer.startMyServer();
        } catch (IOException|ClassNotFoundException| SQLException e) {
            LOGGER.error("Произошла ошибка при запуске сервера");
            LOGGER.error(e.toString());
            e.printStackTrace();
            System.exit(-1);
        }
        finally {
            LOGGER.info("Сервер остановлен");
        }

    }
}

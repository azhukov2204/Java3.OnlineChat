package onlinechat.servermultiusers.myserver.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

public class DBAuthAndLoginService implements AuthAndLoginService {

    private Connection connection;

    private static final Logger LOGGER = LogManager.getLogger("serverLogs");

    @Override
    public synchronized String getNickNameByLoginAndPassword(String login, String password) throws SQLException {
        PreparedStatement getUserRecordPreparedStatement = connection.prepareStatement("SELECT * FROM USERS WHERE UPPER(LOGIN) = ?;");
        getUserRecordPreparedStatement.setString(1, login.toUpperCase());
        ResultSet getUserRecordResultSet = getUserRecordPreparedStatement.executeQuery();
        if (getUserRecordResultSet.next()) {
            //для вывода этого сообщения надо изменить уровень логирования в настройках логгера:
            LOGGER.debug(String.format("login = %s, passwd = %s, nickname = %s", getUserRecordResultSet.getString(2), getUserRecordResultSet.getString(3), getUserRecordResultSet.getString(4)));
            String userPassword = getUserRecordResultSet.getString("PASSWORD");
            String userNickName = getUserRecordResultSet.getString("NICKNAME");

            if (userPassword.equals(password)) {
                return userNickName;
            }
        }
        return null;
    }

    @Override
    public synchronized boolean changeNickName(String login, String newNickName) throws SQLException {
        PreparedStatement updateNickNameStatement = connection.prepareStatement("UPDATE USERS SET NICKNAME = ? WHERE upper(LOGIN) = upper(?);");
        updateNickNameStatement.setString(1, newNickName);
        updateNickNameStatement.setString(2, login.toUpperCase());
        if (isNickNameBusy(newNickName)) {
            return false;
        } else {
            int result = updateNickNameStatement.executeUpdate();
            //для вывода этого сообщения надо изменить уровень логирования в настройках логгера:
            LOGGER.debug(updateNickNameStatement.toString());
            return result != 0;
        }
    }

    private synchronized boolean isNickNameBusy(String newNickName) throws SQLException {
        PreparedStatement checkNickNameStatement = connection.prepareStatement("SELECT * FROM USERS WHERE NICKNAME = ?;");
        checkNickNameStatement.setString(1, newNickName);
        ResultSet resultSet = checkNickNameStatement.executeQuery();
        return resultSet.next();

    }

    @Override
    public void startAuthenticationService() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:src/main/resources/db/mainDB.db");

        LOGGER.info("Сервис аутентификации запущен");
    }

    @Override
    public void endAuthenticationService() throws SQLException {
        connection.close();
        LOGGER.info("Сервис аутентификации остановлен");
    }
}

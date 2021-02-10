package onlinechat.servermultiusers.myserver.authservice;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

public class BaseAuthService implements AuthService {

    //этот тип используется только тут, сделаем класс вложенным
    private static class User {
        private final String login;
        private final String password;
        private final String nickName;

        public User(String login, String password, String nickName) {
            this.login = login;
            this.password = password;
            this.nickName = nickName;
        }

        public String getLogin() {
            return login;
        }

        public String getPassword() {
            return password;
        }

        public String getNickName() {
            return nickName;
        }
    }

    private List<User> users;
    private Connection connection;

    //При создании объекта происходит инициализация списка учетных записей, пока хардкод
    public BaseAuthService() {
        users = List.of(
                new User("boris", "111111", "Боря"),
                new User("andrey", "111111", "Андрей"),
                new User("ivan", "111111", "Ваня")
        );
    }

    @Override
    public String getNickNameByLoginAndPassword(String login, String password) {
        for (User user : users) {
            if (user.getLogin().equals(login) && user.getPassword().equals(password)) {
                return user.getNickName();
            }
        }
        return null;
    }

    @Override
    public void startAuthentication() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:src/main/resources/db/mainDB.db");

        System.out.println("Сервис аутентификации запущен");
    }

    @Override
    public void endAuthentication() throws SQLException {
        connection.close();
        System.out.println("Сервис аутентификации остановлен");
    }
}

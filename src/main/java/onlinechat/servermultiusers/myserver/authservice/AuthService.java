package onlinechat.servermultiusers.myserver.authservice;

import java.sql.SQLException;

public interface AuthService {
    String getNickNameByLoginAndPassword(String login, String password);
    void startAuthentication() throws ClassNotFoundException, SQLException;
    void endAuthentication() throws SQLException;
}

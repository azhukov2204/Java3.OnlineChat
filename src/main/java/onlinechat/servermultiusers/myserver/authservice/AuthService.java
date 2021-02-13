package onlinechat.servermultiusers.myserver.authservice;

import java.sql.SQLException;

public interface AuthService {
    String getNickNameByLoginAndPassword(String login, String password) throws SQLException;
    void startAuthenticationService() throws ClassNotFoundException, SQLException;
    void endAuthenticationService() throws SQLException;
    boolean changeNickName(String login, String newNickName) throws SQLException;
}

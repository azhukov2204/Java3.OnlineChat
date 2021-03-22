package onlinechat.servermultiusers.myserver.services;

import java.sql.SQLException;

public interface AuthAndLoginService {
    String getNickNameByLoginAndPassword(String login, String password) throws SQLException;

    void startAuthenticationService() throws ClassNotFoundException, SQLException;

    void endAuthenticationService() throws SQLException;

    boolean changeNickName(String login, String newNickName) throws SQLException;

    String registrationNewUser(String login, String nickName, String md5password) throws SQLException;
}

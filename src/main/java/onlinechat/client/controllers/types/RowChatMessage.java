package onlinechat.client.controllers.types;

import javafx.beans.property.SimpleStringProperty;

public class RowChatMessage {
    private SimpleStringProperty user;
    private SimpleStringProperty time;
    private SimpleStringProperty message;

    public RowChatMessage(String time, String user, String message) {
        this.time = new SimpleStringProperty(time);
        this.user = new SimpleStringProperty(user);
        this.message = new SimpleStringProperty(message);
    }

    public String getUser() {
        return user.get();
    }

    public SimpleStringProperty userProperty() {
        return user;
    }

    public String getMessage() {
        return message.get();
    }

    public SimpleStringProperty messageProperty() {
        return message;
    }

    public String getTime() {
        return time.get();
    }

    public SimpleStringProperty timeProperty() {
        return time;
    }
}

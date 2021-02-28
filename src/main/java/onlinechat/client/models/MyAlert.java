package onlinechat.client.models;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class MyAlert extends Alert {

    public static final ButtonType yesButton = new ButtonType("Да");
    public static final ButtonType noButton = new ButtonType("Нет, выйти");

    public MyAlert(AlertType alertType, String title, String headerText, String contentText) {
        super(alertType);
        setTitle(title); //"Отсутствует подключение"
        setHeaderText(headerText);
        setContentText(contentText);
        getButtonTypes().clear();
        getButtonTypes().addAll(yesButton, noButton);
    }
}

package onlinechat.client.models;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class YesNoAlert extends Alert {

    public static final ButtonType yesButton = new ButtonType("Да");
    public static final ButtonType noAndCloseButton = new ButtonType("Нет, выйти");
    public static final ButtonType noButton = new ButtonType("Нет");

    public YesNoAlert(AlertType alertType, String title, String headerText, String contentText, boolean withCloseButton) {
        super(alertType);
        setTitle(title); //"Отсутствует подключение"
        setHeaderText(headerText);
        setContentText(contentText);
        getButtonTypes().clear();
        if (withCloseButton) {
            getButtonTypes().addAll(yesButton, noAndCloseButton);
        } else {
            getButtonTypes().addAll(yesButton, noButton);
        }
    }
}

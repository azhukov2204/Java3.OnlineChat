package onlinechat.client;

import javafx.stage.Modality;
import onlinechat.client.controllers.AuthWindowController;
import onlinechat.client.controllers.MainChatWindowController;
import onlinechat.client.controllers.NickNameChangeController;
import onlinechat.client.models.Network;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ChatClientApp extends Application {

    private Stage primaryStage;
    private Stage authWindowStage;
    private Stage nickNameChangeStage;
    private Network network;
    private MainChatWindowController mainChatWindowController;

    @Override
    public void start(Stage primaryStage) throws Exception {

        this.primaryStage = primaryStage;
        network = new Network(); //todo сделать возможность подключения клиента по произвольным host:port
        createAndStartAuthWindow();
        createMainChatWindow();
    }


    public static void main(String[] args) {
        launch(args);
    }

    private void createAndStartAuthWindow() throws IOException {
        FXMLLoader authWindowLoader = new FXMLLoader();
        authWindowLoader.setLocation(ChatClientApp.class.getResource("../../views/AuthWindow.fxml"));
        Parent authWindowRoot = authWindowLoader.load();
        authWindowStage = new Stage();
        authWindowStage.setTitle("Аутентификация");
        authWindowStage.setScene(new Scene(authWindowRoot));
        authWindowStage.initModality(Modality.WINDOW_MODAL);
        authWindowStage.initOwner(primaryStage);
        primaryStage.close(); // в случае сбоя запускаем повторную аутентификацию, primaryStage в этом случае закрываем
        authWindowStage.show();
        AuthWindowController authWindowController = authWindowLoader.getController();
        authWindowController.setNetwork(network);
        authWindowController.setChatClientApp(this);
        network.connection();
    }

    private void createMainChatWindow() throws IOException {
        FXMLLoader mainChatWindowLoader = new FXMLLoader();
        mainChatWindowLoader.setLocation(ChatClientApp.class.getResource("../../views/MainChatWindow.fxml"));
        Parent mainChatWindowRoot = mainChatWindowLoader.load();
        primaryStage.setTitle("Online Chat");
        primaryStage.setScene(new Scene(mainChatWindowRoot));
        mainChatWindowController = mainChatWindowLoader.getController();
        network.setChatClientApp(this);
        mainChatWindowController.setChatClientApp(this);
    }

    public void startChat() {
        authWindowStage.close();
        primaryStage.show();
        primaryStage.setTitle(network.getNickName());
        //primaryStage.setAlwaysOnTop(true);
        network.setMainChatWindowController(mainChatWindowController);
        network.startReceiver();
        mainChatWindowController.setNetwork(network);
    }

    public void createAndStartChangeNickNameWindow() throws IOException {
        FXMLLoader nickNameChangeLoader = new FXMLLoader();
        nickNameChangeLoader.setLocation(ChatClientApp.class.getResource("../../views/NickNameChange.fxml"));
        Parent nickNameChangeRoot = nickNameChangeLoader.load();
        nickNameChangeStage = new Stage();
        nickNameChangeStage.setTitle("Смена имени пользователя");
        nickNameChangeStage.setScene(new Scene(nickNameChangeRoot));
        nickNameChangeStage.initModality(Modality.WINDOW_MODAL);
        nickNameChangeStage.initOwner(primaryStage);
        NickNameChangeController nickNameChangeController = nickNameChangeLoader.getController();
        nickNameChangeController.setNetwork(network);
        nickNameChangeController.setChatClientApp(this);
        nickNameChangeStage.show();

    }

    public void closeChangeNickNameWindows() {
        nickNameChangeStage.close();
        primaryStage.setTitle(network.getNickName());
    }


    public void restartChat() throws IOException {
        createAndStartAuthWindow();
        //createMainChatWindow(); //этот метод пересоздаст главное окно чата, диалоги будут стерты. Пока закомментарил. В будущем этот метод можно вызывать, если сменился логин пользователя
    }
}

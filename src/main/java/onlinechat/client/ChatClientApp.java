package onlinechat.client;

import javafx.stage.Modality;
import onlinechat.client.controllers.AuthWindowController;
import onlinechat.client.controllers.MainChatWindowController;
import onlinechat.client.controllers.NickNameChangeController;
import onlinechat.client.models.ChatMessagesHistoryWriter;
import onlinechat.client.models.Network;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class ChatClientApp extends Application {

    private static final int COUNT_OF_MESSAGES_FROM_FILE_TO_READ = 100; //количество строк истории, которые нужно загрузить при запуске

    private Stage primaryStage;
    private Stage authWindowStage;
    private Stage nickNameChangeStage;
    private Network network;
    private ChatMessagesHistoryWriter chatMessagesHistoryWriter;
    private MainChatWindowController mainChatWindowController;

    private static final Logger LOGGER = LogManager.getLogger("clientLogs");

    @Override
    public void start(Stage primaryStage) throws Exception {
        LOGGER.info("Запуск клиента");
        this.primaryStage = primaryStage;
        network = new Network(); //todo сделать возможность подключения клиента по произвольным host:port
        createAndStartAuthWindow();
        createMainChatWindow();
    }


    public static void main(String[] args) {
        launch(args);
    }

    private void createAndStartAuthWindow() throws IOException {
        LOGGER.info("Запуск окна аутентификации");
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
        LOGGER.info("Создание основного окна чата");
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
        LOGGER.info("Запуск чата");
        chatMessagesHistoryWriter = new ChatMessagesHistoryWriter(network.getUserLogin().toLowerCase()); //создаем логгер истории сообщений
        authWindowStage.close();
        primaryStage.show();
        primaryStage.setTitle(network.getNickName());
        //primaryStage.setAlwaysOnTop(true);
        network.setMainChatWindowController(mainChatWindowController);
        network.startReceiver();
        mainChatWindowController.setNetwork(network);
        mainChatWindowController.addListMessages(chatMessagesHistoryWriter.getNMessagesFromFile(COUNT_OF_MESSAGES_FROM_FILE_TO_READ));
    }

    public void createAndStartChangeNickNameWindow() throws IOException {
        LOGGER.info("Запуск окна смены ника");
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
        nickNameChangeStage.show();

    }

    public void closeChangeNickNameWindows() {
        nickNameChangeStage.close();
        LOGGER.info("Новое имя пользователя " + network.getNickName());
        primaryStage.setTitle(network.getNickName());
    }


    public void restartChat() throws IOException {
        LOGGER.info("Рестарт чата");
        createAndStartAuthWindow();
        createMainChatWindow(); //этот метод пересоздаст главное окно чата
    }

    public ChatMessagesHistoryWriter getChatMessagesHistoryLogger() {
        return chatMessagesHistoryWriter;
    }
}

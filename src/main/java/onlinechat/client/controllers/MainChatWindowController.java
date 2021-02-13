package onlinechat.client.controllers;

import javafx.scene.input.MouseEvent;
import onlinechat.client.ChatClientApp;
import onlinechat.client.controllers.types.RowChatMessage;
import onlinechat.client.models.Network;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainChatWindowController {

    private Network network;
    private ChatClientApp chatClientApp;

    private String selectedNickName = "";

    @FXML
    private Label selectedNickNameLabel;

    public void setNetwork(Network network) {
        this.network = network;
    }

    @FXML
    private ListView<String> chatUsersList;

    @FXML
    private TableView<RowChatMessage> chatMessagesTable;

    @FXML
    private TableColumn<RowChatMessage, String> userTableField;

    @FXML
    private TableColumn<RowChatMessage, String> messageTableField;

    @FXML
    private TableColumn<RowChatMessage, String> timeTableField;


    @FXML
    private Button sendMessageButton;

    @FXML
    private TextField sendMessageText;

    public void setChatClientApp(ChatClientApp chatClientApp) {
        this.chatClientApp = chatClientApp;
    }

    @FXML
    void initialize() {
        userTableField.setCellValueFactory(new PropertyValueFactory<>("user"));
        timeTableField.setCellValueFactory(new PropertyValueFactory<>("time"));
        messageTableField.setCellValueFactory(new PropertyValueFactory<>("message"));
        //Делаем перенос текста по ширине колонки:
        messageTableField.setCellFactory(rowChatMessageStringTableColumn -> {
            TableCell<RowChatMessage, String> cell = new TableCell<>();
            Text text = new Text();
            cell.setGraphic(text);
            cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
            text.textProperty().bind(cell.itemProperty());
            text.wrappingWidthProperty().bind(cell.widthProperty());
            return cell;
        });

        chatUsersList.setCellFactory(lv -> {
            MultipleSelectionModel<String> selectionModel = chatUsersList.getSelectionModel();
            ListCell<String> cell = new ListCell<>();
            cell.textProperty().bind(cell.itemProperty());
            cell.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                chatUsersList.requestFocus();
                if (!cell.isEmpty()) {
                    int index = cell.getIndex();
                    if (selectionModel.getSelectedIndices().contains(index)) {
                        selectionModel.clearSelection(index);
                        selectedNickName = "";
                        selectedNickNameLabel.setText("Сообщение для всех пользователей чата: ");
                    } else {
                        selectionModel.select(index);
                        selectedNickName = cell.getItem();
                        selectedNickNameLabel.setText("Приватное сообщение для " + selectedNickName + ":");
                    }
                    event.consume();
                }
            });
            return cell;
        });
    }


    @FXML
    void sendMessage() {
        sendMessageText.requestFocus(); //при вызове метода фокус сразу возвращается на sendMessageText
        String message = sendMessageText.getText().trim(); //введенное сообщение
        if (!message.isBlank()) { //если что-то введено, то добавляем сообщение
            try {
                if (selectedNickName.isBlank()) {
                    network.sendMessage(message, this);
                } else {
                    network.sendPrivateMessage(selectedNickName, String.format("Приватное сообщение для %s: %s", selectedNickName, message));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            sendMessageText.clear();
            sendMessageButton.setDisable(true); //после отправки сделаем кнопку неактивной. Кнопка станет активной, если что-то введено
        }
    }

    public void addMessage(String message, String nickName) {
        Date date = new Date(); //текущая дата и время
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        String currentTime = timeFormat.format(date); //Преобразуем время в нужный формат
        chatMessagesTable.getItems().add(new RowChatMessage(currentTime, nickName, message));
        int messagesCount = chatMessagesTable.getItems().size();
        chatMessagesTable.scrollTo(messagesCount - 1); //прокрутим к последнему сообшению
    }


    @FXML
    void setActiveSendButton() { //метод вызывается при вводе текста
        //если пусто, то кнопка неактивна
        sendMessageButton.setDisable(sendMessageText.getText().trim().isBlank()); //если что-то введено, то кнопку делаем активной
    }

    @FXML
    void exit() {
        System.exit(0);
    }

    @FXML
    void about() {
        Alert about = new Alert(Alert.AlertType.INFORMATION);
        about.setTitle("О программе");
        about.setHeaderText("Online - чат");
        about.setContentText("Курс Java Core. Профессиональный уровень.");
        about.show();
    }

    public void updateUsersList(String[] usersList) {
        ObservableList<String> chatUsers = FXCollections.observableArrayList(usersList);
        chatUsersList.setItems(chatUsers.sorted());//Будем каждый раз пересоздавать список. Чтоб минимизировать ошибки в случае пропусков уведомлений
    }

    @FXML
    void openChangeNickNameWindow() {
        try {
            chatClientApp.createAndStartChangeNickNameWindow();
        } catch (IOException e) {
            System.out.println("Не удалось запустить окно для смены ника");
            e.printStackTrace();
        }
    }

}

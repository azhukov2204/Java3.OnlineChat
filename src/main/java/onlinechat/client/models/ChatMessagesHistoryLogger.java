package onlinechat.client.models;

import java.io.*;

public class ChatMessagesHistoryLogger {
    private static final String FILENAME_PREFIX = "history_";
    private static final String FILENAME_POSTFIX = ".txt";
    private static final String DIRECTORY = "src/main/resources/chat_history/";

    private String login;
    private String messagesHistoryFilename;
    private String messagesHistoryFullFilename;

    private File messagesHistoryFile;

    public ChatMessagesHistoryLogger(String login) {
        this.login = login;
        messagesHistoryFilename = FILENAME_PREFIX + login + FILENAME_POSTFIX;
        messagesHistoryFullFilename = DIRECTORY + messagesHistoryFilename;
        messagesHistoryFile = new File(messagesHistoryFullFilename);

    }

    private void createHistoryFileIfNotExist() throws IOException {
        File dir = new File(DIRECTORY);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (!messagesHistoryFile.exists()) {
            messagesHistoryFile.createNewFile();
        }
    }

    public void writeMessageToFile(String time, String nickName, String message) {
        try {
            createHistoryFileIfNotExist();
        } catch (IOException e) {
            System.out.println("Не удалось создать файл истории сообщений!");
            e.printStackTrace();
        }

        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(messagesHistoryFile, true))) {
            bufferedWriter.write(String.format("%s|%s|%s%n", time, nickName, message));

        } catch (IOException e) {
            System.out.println("Не удалось записать информацию в файл истории сообщений");
            e.printStackTrace();
        }
    }

    public void getNMessagesFromFile(int countToRead) {
        if (messagesHistoryFile.exists()) {
            int countLines = 0;
            String str;

            try (BufferedReader reader = new BufferedReader(new FileReader(messagesHistoryFile))) {
                while ((str = reader.readLine()) != null) {
                    countLines++;
                }
                System.out.println("Количество строк в файле: " + countLines);

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Файл истории сообщений еще не существует");
        }
    }


}


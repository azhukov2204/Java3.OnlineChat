package onlinechat.client.models;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.ArrayList;

public class ChatMessagesHistoryWriter {
    private static final String FILENAME_PREFIX = "history_";
    private static final String FILENAME_POSTFIX = ".txt";
    private static final String DIRECTORY = "src/main/resources/chat_history/";

    private final File messagesHistoryFile;

    private static final Logger LOGGER = LogManager.getLogger("clientLogs");

    public ChatMessagesHistoryWriter(String login) {
        String messagesHistoryFilename = FILENAME_PREFIX + login.toLowerCase() + FILENAME_POSTFIX;
        String messagesHistoryFullFilename = DIRECTORY + messagesHistoryFilename;
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
            LOGGER.error("Не удалось создать файл истории сообщений!");
            LOGGER.error(e.toString());
            e.printStackTrace();
        }

        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(messagesHistoryFile, true))) {
            bufferedWriter.write(String.format("%s|%s|%s%n", time, nickName, message));

        } catch (IOException e) {
            LOGGER.error("Не удалось записать информацию в файл истории сообщений");
            LOGGER.error(e.toString());
            e.printStackTrace();
        }
    }

    public ArrayList<String> getNMessagesFromFile(int countLinesToRead) {
        ArrayList<String> stringArrayList = new ArrayList<>();
        if (messagesHistoryFile.exists()) {
            try {
                long numberOfFileLines = getNumberOfFileLines();
                long skipLines = (countLinesToRead < numberOfFileLines) ? (numberOfFileLines - countLinesToRead) : 0;
                BufferedReader reader = new BufferedReader(new FileReader(messagesHistoryFile));
                //пропуск skipLines строк:
                for (int i = 0; i < skipLines; i++) {
                    reader.readLine();
                }
                String str;
                while ((str = reader.readLine()) != null) {
                    stringArrayList.add(str);
                }
                reader.close();
            } catch (IOException e) {
                LOGGER.error("Не удалось считать сообщения из файла");
                LOGGER.error(e.toString());
                e.printStackTrace();
            }
        } else {
            LOGGER.warn("Файл истории сообщений еще не существует");
        }
        return stringArrayList;
    }

    private long getNumberOfFileLines() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(messagesHistoryFile));
        long numberOfFileLines = reader.lines().count();
        reader.close();
        return numberOfFileLines;
    }

}


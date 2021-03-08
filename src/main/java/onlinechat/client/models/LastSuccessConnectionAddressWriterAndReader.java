package onlinechat.client.models;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;

public class LastSuccessConnectionAddressWriterAndReader {
    private static final String FILENAME = "connection_string.txt";
    private static final String DIRECTORY = "src/main/resources/settings/";

    private final File connectionStringFileFile;

    private static final Logger LOGGER = LogManager.getLogger("clientLogs");

    public LastSuccessConnectionAddressWriterAndReader() {
        connectionStringFileFile = new File(DIRECTORY+FILENAME);
    }

    private void createFileIfNotExist() throws IOException {
        File dir = new File(DIRECTORY);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (!connectionStringFileFile.exists()) {
            LOGGER.info("Файл не существует. Попытка создания");
            connectionStringFileFile.createNewFile();
        }
    }

    public void writeToFile(String host, String port) {
        try {
            LOGGER.info("Проверка, существует ли файл строки подключения");
            createFileIfNotExist();
        } catch (IOException e) {
            LOGGER.error("Не удалось создать файл!");
            LOGGER.error(e.toString());
            e.printStackTrace();
        }

        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(connectionStringFileFile, false))) {
            bufferedWriter.write(String.format("%s|%s", host, port));

        } catch (IOException e) {
            LOGGER.error("Не удалось записать информацию в файл");
            LOGGER.error(e.toString());
            e.printStackTrace();
        }
    }

    public String getConnectionStringFromFile() {
        String connectionString=null;
        if (connectionStringFileFile.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(connectionStringFileFile));
                connectionString = reader.readLine();
                reader.close();
            } catch (IOException e) {
                LOGGER.error("Не удалось считать сообщения из файла");
                LOGGER.error(e.toString());
                e.printStackTrace();
            }
        } else {
            LOGGER.warn("Файл истории сообщений еще не существует");
        }
        return connectionString;
    }
}

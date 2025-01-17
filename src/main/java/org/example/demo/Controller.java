package org.example.demo;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.IndexRange;
import javafx.scene.control.TextArea;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Controller {
    @FXML
    private Button chooseFolderButton;
    @FXML
    private Button createFileButton;
    @FXML
    private TextArea textArea;

    private final String URL = "jdbc:mysql://localhost:3306/test";
    private final String USER = "root";
    private final String PASSWORD = "";
    private final int BATCHSIZE = 1024;

    @FXML
    protected void createFileButtonClick() {
        final String[] names = {"Raja", "Ravi", "Kiran", "Rahul", "Rajesh"};
        final String[] subject1 = {"Accountancy", "Marketing", "Physics", "Programming", "English"};
        final String[] subject2 = {"Economics", "Finance", "Chemistry", "Mathematics", "History"};
        final String[] subject3 = {"Business", "Management", "Biology", "Statistics", "Geography"};
        final String[] course = {"MCA", "MBA", "B.Tech", "BBA", "BCA"};

        FileChooser fC = new FileChooser();
        fC.setInitialDirectory(new File("src/main/resources/data"));
        fC.getExtensionFilters().add(new FileChooser.ExtensionFilter("TXT", "*.txt"));
        File selectedFile = fC.showSaveDialog(null);

        if (selectedFile == null) return;
        try {
            FileWriter fw = new FileWriter(selectedFile.getAbsolutePath(), true);

            for (int i = 0; i < 10000; i++) {
                JSONObject innerObj = new JSONObject();
                innerObj.put("Name", names[i % 5]);
                innerObj.put("Subject1", subject1[i % 5]);
                innerObj.put("Subject2", subject2[i % 5]);
                innerObj.put("Subject3", subject3[i % 5]);
                innerObj.put("Course", course[i % 5]);

                fw.write(innerObj.toJSONString() + "\n");
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @FXML
    protected void chooseFolderButtonClick() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select a Folder");
        directoryChooser.setInitialDirectory(new File("src/main/resources/data"));

        File selectedDirectory = directoryChooser.showDialog(null);
        if (selectedDirectory == null) return;

        Thread mainThread = new Thread(() -> {
            while (true) {
                File[] files = selectedDirectory.listFiles();
                if (files != null)
                    for (File file : files) {
                        insertDataToDatabase(file.getAbsolutePath());
                        if (file.delete()) {
                            Platform.runLater(() -> textArea.appendText("Đã xóa file: " + file.getName() + "\n"));
                        }
                    }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        mainThread.start();
    }

    private void insertDataToDatabase(String path) {
        long start = System.nanoTime();

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = con.prepareStatement("INSERT INTO list (Name, Subject1, Subject2, Subject3, Course) VALUES (?, ?, ?, ?, ?)")) { // Assuming table and column names
            JSONParser parser = new JSONParser();
            String[] data = readFile(path, BATCHSIZE);
            AtomicInteger pos = new AtomicInteger();
            AtomicInteger len = new AtomicInteger();

            for (int i = 0; i < data.length; i++) {
                String s = data[i];
                insert(parser, s, stmt);
                updateUI(path, i, data, pos, len);
            }

            stmt.executeBatch(); // Execute the batch insert
        } catch (ParseException | SQLException e) {
            e.printStackTrace();
        }

        long end = System.nanoTime();
        Platform.runLater(() -> textArea.appendText("Thời gian thêm dữ liệu: " + (end - start) / 1e6 + " ms\n"));
    }

    private void insert(JSONParser parser, String s, PreparedStatement stmt) throws ParseException, SQLException {
        JSONObject obj = (JSONObject) parser.parse(s);

        stmt.setString(1, (String) obj.get("Name"));
        stmt.setString(2, (String) obj.get("Subject1"));
        stmt.setString(3, (String) obj.get("Subject2"));
        stmt.setString(4, (String) obj.get("Subject3"));
        stmt.setString(5, (String) obj.get("Course"));
        stmt.addBatch();
    }

    private void updateUI(String path, int i, String[] data, AtomicInteger pos, AtomicInteger len) {
        // Update UI less frequently
        if (i % 100 == 0 || i == data.length - 1) {
            final int currentIndex = i;
            Platform.runLater(() -> {
                String announce = "Đường dẫn: " + path + ", Dòng: " + (currentIndex + 1) + "\n";
                if (currentIndex == 0) {
                    textArea.appendText(announce);
                } else {
                    IndexRange range = new IndexRange(pos.get() - len.get() + 1, pos.get() + 1);
                    textArea.replaceText(range, announce);
                }
                pos.set(textArea.getText().lastIndexOf("\n"));
                len.set(announce.length());
            });
        }
    }

    // Read file using multiple buffer
    private String[] readFile(String path, int batchSize) {
        String[] data = null;
        long startTime = System.nanoTime();
        try (FileChannel fileChannel = FileChannel.open(Paths.get(path))) {
            long fileSize = fileChannel.size();
            // Define chunk size and number of threads
            int numThreads = (int) Math.ceil((double) fileSize / batchSize);
            data = new String[numThreads];
            // Create thread pool
            ExecutorService executor = Executors.newFixedThreadPool(numThreads);
            // Divide the file into chunks and submit tasks to read them
            for (int i = 0; i < numThreads; i++) {
                long start = (long) i * batchSize;
                long end = Math.min(start + batchSize, fileSize);
                createThread(fileChannel, executor, start, end, data, i);
            }
            // Shut down the thread pool
            executor.shutdown();
            // Wait for all threads to finish
            executor.awaitTermination(1000, TimeUnit.MILLISECONDS);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        long endTime = System.nanoTime();
        Platform.runLater(() -> textArea.appendText("Thời gian đọc file: " + (endTime - startTime) / 1e6 + " ms\n"));
        return String.join("", data).split("\n");
    }

    private void createThread(FileChannel fileChannel, ExecutorService executor, long start, long end, String[] data, int idx) {
        executor.execute(() -> {
            try {
                ByteBuffer buffer = ByteBuffer.allocate((int) (end - start));
                fileChannel.read(buffer, start);
                buffer.flip();
                String chunk = StandardCharsets.UTF_8.decode(buffer).toString();
                data[idx] = chunk;
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
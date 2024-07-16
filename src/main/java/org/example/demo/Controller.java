package org.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

public class Controller {
    @FXML
    private Button chooseFolderButton;
    @FXML
    private Button createFileButton;
    @FXML
    private TextArea textArea;

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


        for (int i = 0; i < 10; i++) {
            JSONObject innerObj = new JSONObject();
            innerObj.put("Name", names[(int) (Math.random() * 5)]);
            innerObj.put("subject1", subject1[(int) (Math.random() * 5)]);
            innerObj.put("subject2", subject2[(int) (Math.random() * 5)]);
            innerObj.put("subject3", subject3[(int) (Math.random() * 5)]);
            innerObj.put("Course", course[(int) (Math.random() * 5)]);
            if (selectedFile != null) {
                try {
                    FileWriter fw = new FileWriter(selectedFile, true);
                    BufferedWriter bw = new BufferedWriter(fw);
                    bw.write(innerObj.toJSONString());
                    bw.newLine();
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @FXML
    protected void chooseFolderButtonClick() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select a Folder");
        directoryChooser.setInitialDirectory(new File("src/main/resources/data"));
        File selectedDirectory = directoryChooser.showDialog(null);

        DaemonFolder daemon = new DaemonFolder() {
            @Override
            public void run() {
                while (true) {
                    try {
                        File[] files = selectedDirectory.listFiles();
                        if (files != null) {
                            for (File file : files) {
                                insertDataToDB(file.getAbsolutePath());
                                if (file.delete()) System.out.println("File deleted successfully");
                            }
                        }
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        System.out.println(e.getMessage());
                        break;
                    }
                }
            }
        };
    }

    @FXML
    private void insertDataToDB(String path) {
        String[] data = readFile(path);
        JSONParser parser = new JSONParser();
        AtomicInteger i = new AtomicInteger(1);
        for (String s : data) {
            try {
                JSONObject obj = (JSONObject) parser.parse(s);
                insert((String) obj.get("Name"),
                        (String) obj.get("subject1"),
                        (String) obj.get("subject2"),
                        (String) obj.get("subject3"),
                        (String) obj.get("Course"));
                textArea.appendText("File name: " + path + ": Line " + i.getAndIncrement() + "\n");
                Thread.sleep(500);
            } catch (ParseException | InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }

    }

    private String[] readFile(String path) {
        String[] data = null;
        try {
            RandomAccessFile aFile = new RandomAccessFile(path, "r");
            FileChannel inChannel = aFile.getChannel();
            long fileSize = inChannel.size();
            ByteBuffer buf = ByteBuffer.allocate((int) fileSize);
            inChannel.read(buf);
            buf.flip();
            data = getDataFromFile(fileSize, buf);
            inChannel.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return data;
    }

    private void insert(String name, String subject1, String subject2, String subject3, String course) {
        final String url = "jdbc:mysql://localhost:3306/test";
        final String user = "root";
        final String password = "";

        try {
            // establish the connection
            Connection con = DriverManager.getConnection(url, user, password);
            // insert data
            String sql = " insert into list (Name, Subject1, Subject2, Subject3, Course)" + " values (?, ?, ?, ?, ?)";

            PreparedStatement preparedStmt = con.prepareStatement(sql);
            preparedStmt.setString(1, name);
            preparedStmt.setString(2, subject1);
            preparedStmt.setString(3, subject2);
            preparedStmt.setString(4, subject3);
            preparedStmt.setString(5, course);
            preparedStmt.execute();

            con.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private String[] getDataFromFile(long fileSize, ByteBuffer buf) {
        char[] data = new char[(int) fileSize];
        for (int i = 0; i < 5; i++) {
            int finalI = i;
            Thread thread = new Thread(() -> {
                for (long j = fileSize * finalI / 5; j < fileSize * (finalI + 1) / 5; j++) {
                    char c = (char) buf.get((int) j);
                    data[(int) j] = c;
                }
            });
            thread.start();
        }
        synchronized (data) {
            try {
                data.wait(2000);
                System.out.println("Done");
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }

        return new String(data).split("\n");
    }
}
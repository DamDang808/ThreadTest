package org.example.demo;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Controller {
    @FXML
    private Button chooseFileButton;
    @FXML
    private Button createFileButton;
    @FXML
    private TextArea textArea;

    @FXML
    protected void createFileButtonClick() {
        textArea.clear();
        final String[] names = {"Raja", "Ravi", "Kiran", "Rahul", "Rajesh"};
        final String[] subject1 = {"Accountancy", "Marketing", "Physics", "Programming", "English"};
        final String[] subject2 = {"Economics", "Finance", "Chemistry", "Mathematics", "History"};
        final String[] subject3 = {"Business", "Management", "Biology", "Statistics", "Geography"};
        final String[] course = {"MCA", "MBA", "B.Tech", "BBA", "BCA"};

        JSONArray arr = new JSONArray();
        for (int i = 0; i < 10; i++) {
            JSONObject innerObj = new JSONObject();
            innerObj.put("Name", names[(int) (Math.random() * 5)]);
            innerObj.put("subject1", subject1[(int) (Math.random() * 5)]);
            innerObj.put("subject2", subject2[(int) (Math.random() * 5)]);
            innerObj.put("subject3", subject3[(int) (Math.random() * 5)]);
            innerObj.put("Course", course[(int) (Math.random() * 5)]);
            arr.add(innerObj);
        }

        FileChooser fC = new FileChooser();
        fC.setInitialDirectory(new File("src/main/resources/data"));
        fC.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON Files", "*.json")
        );
        File selectedFile = fC.showSaveDialog(null);


        if (selectedFile != null) {
            try (BufferedWriter bw = Files.newBufferedWriter(selectedFile.toPath(), StandardCharsets.UTF_8)) {
                bw.write(arr.toJSONString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            textArea.appendText("File creation cancelled.\n");
        }

    }

    @FXML
    protected void chooseFileButtonClick() {
        textArea.clear();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("src/main/resources/data"));
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON Files", "*.json")
        );

        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(null);
        if (selectedFiles != null) {
            for (File file : selectedFiles) {
                insertData(file.getPath());
            }
        } else {
            textArea.appendText("File selection cancelled.\n");
        }
    }

    @FXML
    private void insertData(String path) {
        Thread thread = new Thread(() -> {
            JSONArray json = readJSONFile(path);
            AtomicInteger i = new AtomicInteger(1);
            for (Object obj : json) {
                insertToDatabase((String) ((JSONObject) obj).get("Name"),
                        (String) ((JSONObject) obj).get("subject1"),
                        (String) ((JSONObject) obj).get("subject2"),
                        (String) ((JSONObject) obj).get("subject3"),
                        (String) ((JSONObject) obj).get("Course"));
                // Use Platform.runLater to safely update the UI from a different thread
                Platform.runLater(() -> textArea.appendText("File name: " + path + ": Line " + i.getAndIncrement() + "\n"));
            }
            deleteFile(path);
        });
        thread.start();
    }

    private JSONArray readJSONFile(String path) {
        //JSON parser object to parse read file
        JSONParser jsonParser = new JSONParser();
        JSONArray json = null;
        StringBuilder stringBuilder = new StringBuilder();


        try {
            // Read file into a string
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String line;
            String ls = System.lineSeparator();
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }
            // delete the last new line separator
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            reader.close();

            String content = stringBuilder.toString();
            json = (JSONArray) jsonParser.parse(content);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return json;
    }

    private void insertToDatabase(String name, String subject1, String subject2, String subject3, String course) {
        final String url = "jdbc:mysql://localhost:3306/test";
        final String user = "root";
        final String password = "";

        try {
            // establish the connection
            Connection con = DriverManager.getConnection(url, user, password);

            // insert data
            String sql = " insert into list (Name, Subject1, Subject2, Subject3, Course)"
                    + " values (?, ?, ?, ?, ?)";

            PreparedStatement preparedStmt = con.prepareStatement(sql);;
            preparedStmt.setString(1, name);
            preparedStmt.setString(2, subject1);
            preparedStmt.setString(3, subject2);
            preparedStmt.setString(4, subject3);
            preparedStmt.setString(5, course);
            preparedStmt.execute();

            con.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void deleteFile(String path) {
        File file = new File(path);
        if (file.delete()) {
            System.out.println("Deleted the file: " + file.getName());
        } else {
            System.out.println("Failed to delete the file.");
        }
    }
}
package org.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class Controller {
    @FXML
    private Button chooseFileButton;
    @FXML
    private TextArea textArea;

    @FXML
    protected void chooseFileButtonClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("src/main/resources/data"));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JSON Files", "*.json")
        );

        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(null);
        for (File file : selectedFiles) {
            readFile(file.getAbsolutePath());
        }
    }

    @FXML
    protected void readFile(String fileName) {
        Thread thread = new Thread(() -> {
            JSONArray json = readJSONFile(fileName);
            int i = 0;
            synchronized (textArea) {
                for (Object obj : json) {
                    textArea.appendText("File name: " + fileName + ": Line " + i++ + ": " + "\n");
                    textArea.appendText(obj.toString() + "\n");
//                System.out.println(obj);
                }
            }
        });
        thread.start();
    }

    public static JSONArray readJSONFile(String path) {
        //JSON parser object to parse read file
        JSONParser jsonParser = new JSONParser();
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

        } catch (IOException e) {
            e.printStackTrace();
        }

        String content = stringBuilder.toString();
        // convert to json array
        JSONArray json = null;
        try {
            json = (JSONArray) jsonParser.parse(content);
        } catch (org.json.simple.parser.ParseException e) {
            e.printStackTrace();
        }
        return json;
    }
}
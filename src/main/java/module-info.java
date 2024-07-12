module org.example.demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires json.simple;


    opens org.example.demo to javafx.fxml;
    exports org.example.demo;
}
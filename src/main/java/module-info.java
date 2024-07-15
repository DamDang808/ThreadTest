module org.example.demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires json.simple;
    requires java.sql;
    requires mysql.connector.j;


    opens org.example.demo to javafx.fxml;
    exports org.example.demo;
}
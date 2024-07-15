package org.example.demo;

import java.sql.*;

public class Test {

    public static void main(String[] args) throws Exception {

        // register Oracle thin driver with DriverManager service
        Class.forName("com.mysql.cj.jdbc.Driver");

        // declare variables
        // place your own values
        final String url = "jdbc:mysql://localhost:3306/test";
        final String user = "root";
        final String password = "";

        // establish the connection
        Connection con = DriverManager.getConnection(url, user, password);

        String sql = " insert into list (Name, Subject1, Subject2, Subject3, Course)"
                + " values (?, ?, ?, ?, ?)";
        PreparedStatement preparedStmt = con.prepareStatement(sql);
        preparedStmt.setString(1, "Raja");
        preparedStmt.setString(2, "MIS");
        preparedStmt.setString(3, "DBMS");
        preparedStmt.setString(4, "UML");
        preparedStmt.setString(5, "MCA");
        preparedStmt.execute();
        con.close();

    } //main
} //class
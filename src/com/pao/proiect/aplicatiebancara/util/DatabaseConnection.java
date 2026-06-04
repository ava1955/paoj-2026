package com.pao.proiect.aplicatiebancara.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

    private static volatile DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() throws SQLException {
        Properties props = new Properties();

        try (InputStream is = new FileInputStream("src/resources/db.properties")) {
            props.load(is);
        } catch (IOException e) {
            throw new SQLException("Eroare la citirea configuratiei DB din src/resources/db.properties: " + e.getMessage(), e);
        }

        String url      = props.getProperty("db.url");
        String user     = props.getProperty("db.user");
        String password = props.getProperty("db.password");

        this.connection = DriverManager.getConnection(url, user, password);
    }

    public static DatabaseConnection getInstance() throws SQLException {
        if (instance == null || instance.connection.isClosed()) {
            synchronized (DatabaseConnection.class) {
                if (instance == null || instance.connection.isClosed()) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}
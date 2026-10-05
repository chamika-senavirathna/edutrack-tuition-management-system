package com.edutrack.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Central JDBC connection helper (Member 05 - shared utility).
 * Reads database credentials from db.properties on the classpath -
 * credentials are never hard-coded into Java source or exposed to JSPs.
 */
public final class DB {
    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = DB.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) {
                throw new IllegalStateException("db.properties not found on classpath");
            }
            PROPS.load(in);
            Class.forName(PROPS.getProperty("db.driver", "com.mysql.cj.jdbc.Driver"));
        } catch (Exception e) {
            throw new ExceptionInInitializerError("Failed to load database configuration: " + e.getMessage());
        }
    }

    private DB() {
    }

    /** Returns a new JDBC connection. Callers must close it (try-with-resources). */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                PROPS.getProperty("db.url"),
                PROPS.getProperty("db.username"),
                PROPS.getProperty("db.password"));
    }
}

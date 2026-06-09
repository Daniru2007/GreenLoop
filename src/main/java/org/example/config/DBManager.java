package org.example.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import java.io.InputStream;
import java.util.Properties;

public class DBManager {
    private static MongoClient mongoClient;
    private static MongoDatabase database;
    private static Properties properties = new Properties();


    static {
        try (InputStream input = DBManager.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find application.properties");
            } else {
                properties.load(input);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private DBManager() {}

    public static MongoDatabase getDatabase() {
        if (mongoClient == null) {
            String uri = properties.getProperty("mongodb.uri");
            String dbName = properties.getProperty("mongodb.database");

            mongoClient = MongoClients.create(uri);
            database = mongoClient.getDatabase(dbName);
        }
        return database;
    }
}
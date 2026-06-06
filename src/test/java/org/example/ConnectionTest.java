package org.example;

import com.mongodb.client.MongoDatabase;
import org.example.config.DBManager;
import org.bson.Document;


public class ConnectionTest {
    public static void main(String[] args) {
        try {

            MongoDatabase db = DBManager.getDatabase();

            db.runCommand(new Document("ping", 1));

            System.out.println("Successfully connected to MongoDB!");


        } catch (Exception e) {
            System.err.println("Failed to connect to MongoDB.");
            e.printStackTrace();
        }
    }
}
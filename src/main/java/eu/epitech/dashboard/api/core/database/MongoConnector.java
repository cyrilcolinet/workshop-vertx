package eu.epitech.dashboard.api.core.database;

import com.mongodb.MongoClient;

/**
 * MongoConnector database
 * Connect to mongo database and execute queries
 */
public class MongoConnector {

    private MongoClient client;

    public MongoConnector() {
        this.configureDatabase();
    }

    private void configureDatabase() {

        // Start driver listening
        this.client = new MongoClient("mongodb+srv://root:root@cluster0-9ljxu.mongodb.net/test?retryWrites=true&w=majority");
    }

    public MongoClient connection() {
        return client;
    }
}

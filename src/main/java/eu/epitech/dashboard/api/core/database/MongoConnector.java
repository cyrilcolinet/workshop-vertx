package eu.epitech.dashboard.api.core.database;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

import java.util.Collections;

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
        String user = System.getenv("MONGO_USER");
        String pass = System.getenv("MONGO_PASS");
        String db = System.getenv("MONGO_DB");
        String host = System.getenv("MONGO_HOST");
        MongoCredential credentials = MongoCredential.createCredential(user, db, pass.toCharArray());

        // Start driver listening
        this.client = new MongoClient(new ServerAddress(host, 27017), Collections.singletonList(credentials));
    }

    public MongoClient connection() {
        return client;
    }
}

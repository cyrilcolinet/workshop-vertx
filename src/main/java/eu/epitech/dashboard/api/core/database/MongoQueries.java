package eu.epitech.dashboard.api.core.database;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import eu.epitech.dashboard.api.APIServer;
import eu.epitech.dashboard.api.core.oauth2.OAuth2Manager;
import eu.epitech.dashboard.api.core.oauth2.OAuthTypes;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.bson.Document;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * MongoQueries class
 * All methods in this class must be static
 */
public class MongoQueries {

    private static MongoDatabase database = APIServer.get().getMongo().connection().getDatabase("dashboard");

    /**
     * Create new user
     * @param firstName First name of user
     * @param lastName Last name of the user
     * @param hash Password hashed in MD5
     * @return Returns null if success, and error string
     */
    static public UUID createNewUser(String firstName, String lastName, String hash) {
        MongoCollection<Document> collection = MongoQueries.database.getCollection("users");
        Document newUser = new Document();
        UUID uuid = UUID.randomUUID();
        newUser.append("uuid", uuid.toString());
        newUser.append("first_name", firstName);
        newUser.append("last_name", lastName);
        newUser.append("password", hash);
        newUser.append("created_at", new Date());

        // Configure for oauth accounts
        newUser.append("accounts", new Document());

        // Check if username already exists
        if (collection != null) {
            if (collection.find(new Document("first_name", firstName).append("last_name", lastName)).first() != null)
                return null;
        } else {
            MongoQueries.database.createCollection("users");
            collection = MongoQueries.database.getCollection("users");
        }

        // Insert document
        collection.insertOne(newUser);
        return uuid;
    }

    /**
     * Create account with oauth
     * @param uniqueIdentifier UniqueIdentifier of this account
     * @return UUID unique id of account
     */
    static public UUID createUserWithOAuth(String uniqueIdentifier, OAuth2Manager.OAuth2Fields fields) {
        MongoCollection<Document> collection = MongoQueries.database.getCollection("users");
        Document newUser = new Document();
        UUID uuid = UUID.randomUUID();
        newUser.append("uuid", uuid.toString());
        newUser.append("first_name", fields.firstName);
        newUser.append("last_name", fields.lastName);
        newUser.append("oauth_type", fields.type.toString().toLowerCase());
        newUser.append("unique_id", uniqueIdentifier);
        newUser.append("created_at", new Date());

        // Adding custom values for oauth and add it to mongo user
        Document oauthAccess = new Document("access_token", fields.access_token)
                .append("token_type", fields.token_type)
                .append("token_expires", fields.token_expires)
                .append("account_id", fields.account_id);
        newUser.append("accounts", new Document(fields.type.toString().toLowerCase(), oauthAccess));

        // Check if username already exists
        if (collection != null) {
            Document search = new Document("unique_id", uniqueIdentifier).append("oauth_type", fields.type.toString().toLowerCase());
            Document result = collection.find(search).first();

            // Return existent UUID account
            if (result != null)
                return UUID.fromString(result.getString("uuid"));
        } else {
            MongoQueries.database.createCollection("users");
            collection = MongoQueries.database.getCollection("users");
        }

        // Insert document
        collection.insertOne(newUser);
        return uuid;
    }

    /**
     * Get account from username and password hash
     * @param username String
     * @param hash String (md5 encrypted password)
     * @return UUID
     */
    public static UUID getAccountByUserAndHash(String username, String hash) {
        MongoCollection<Document> collection = MongoQueries.database.getCollection("users");

        // Check if username already exists
        if (collection != null) {
            Document search = new Document("username", username).append("password", hash);
            Document result = collection.find(search).first();

            // Exists or not ?
            if (result != null) {
                try {
                    return UUID.fromString(result.getString("uuid"));
                } catch(Exception ignored) {
                    return null;
                }
            }
        }

        return null;
    }

    /**
     * Get account from username and password hash
     * @param uuid UUID
     * @return JsonObject
     */
    public static JsonObject getAccountByUUID(UUID uuid) {
        MongoCollection<Document> collection = MongoQueries.database.getCollection("users");

        // Check if username already exists
        if (collection != null) {
            Document search = new Document("uuid", uuid.toString());
            Document result = collection.find(search).first();

            // Exists or not ?
            if (result != null)
                return parseDocumentToObject(result);
        }

        return new JsonObject();
    }

    /**
     * Parse bson Document to JsonObject
     * @param bson Document
     * @return JsonObject
     */
    private static JsonObject parseDocumentToObject(Document bson) {
        JsonObject response = new JsonObject();

        // All document fields
        bson.keySet().forEach(key -> {
            if (key.equalsIgnoreCase("_id"))
                return;

            // Recursive check of document
            if (bson.get(key) instanceof Document) {
                response.put(key, parseDocumentToObject((Document) bson.get(key)));
                return;
            }

            // Add basic type to object
            if (bson.get(key) != null)
                response.put(key, bson.get(key).toString());
        });

        return response;
    }

    /**
     * Adding OAuth to account uuid
     * @param uuid UUid of user account
     * @param fields Fields for oauth
     */
    public static void addOauthToAccountWithUUID(UUID uuid, OAuth2Manager.OAuth2Fields fields) {
        MongoCollection<Document> collection = MongoQueries.database.getCollection("users");

        // Check if username already exists
        if (collection != null) {
            Document search = new Document("uuid", uuid.toString());
            Document result = collection.find(search).first();

            // Exists or not ?
            if (result != null) {
                // Adding oauth values
                Document oauthAccess = new Document("access_token", fields.access_token)
                        .append("token_type", fields.token_type)
                        .append("token_expires", fields.token_expires)
                        .append("account_id", fields.account_id);
                Document newOauth = new Document("accounts." + fields.type.toString().toLowerCase(), oauthAccess);

                collection.updateOne(search, new Document("$set", newOauth));
            }
        }
    }

    /**
     * Get account uuid with oauth account id
     * @param type Type of oauth
     * @param login Unique id for type of oauth
     * @return UUID of account
     */
    public static UUID getAccountUUIDByOAuthAccountId(OAuthTypes type, String login) {
        MongoCollection<Document> collection = MongoQueries.database.getCollection("users");

        // Check if username already exists
        if (collection != null) {
            Document search = new Document("accounts", new Document(type.toString().toLowerCase(), new Document("account_id", login)));
            Document result = collection.find(search).first();

            // Exists or not ?
            if (result != null)
                return UUID.fromString(result.getString("uuid"));
        }

        return null;
    }

    /**
     * Get access token oauth from account uuid and account type
     * @param serviceName Service name
     * @param accountUUID Account UUID
     * @return Access token in string
     */
    public static String getAccessTokenByAccountType(String serviceName, UUID accountUUID) {
        MongoCollection<Document> collection = MongoQueries.database.getCollection("users");

        // Check if username already exists
        if (collection != null) {
            Document search = new Document("uuid", accountUUID.toString());
            Document result = collection.find(search).first();

            // Exists or not ?
            if (result != null) {
                try {
                    Document accounts = ((Document) result.get("accounts"));
                    Document github = ((Document) accounts.get(serviceName));
                    return github.getString("access_token");
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                }
            }
        }

        return null;
    }

    /**
     * Add new widget for user
     * @param uuid UUID account uuid
     * @param widgetName Name of the widget
     * @param params Params for this widget
     */
    public static void addNewWidgetForUser(UUID uuid, String widgetName, Map<String, Object> params) {
        MongoCollection<Document> collection = MongoQueries.database.getCollection("widgets");

        // Check if username already exists
        if (collection != null) {
            Document newWidget = new Document();
            newWidget.append("uuid", UUID.randomUUID().toString());
            newWidget.append("account_uuid", uuid.toString());
            newWidget.append("name", widgetName);

            // Params with value
            Document paramsDoc = new Document();
            for (Map.Entry<String, Object> entry : params.entrySet())
                paramsDoc.append(entry.getKey(), entry.getValue());
            newWidget.append("params", paramsDoc);

            // Insert in collection
            collection.insertOne(newWidget);
        } else {
            MongoQueries.database.createCollection("widgets");
            MongoQueries.addNewWidgetForUser(uuid, widgetName, params);
        }
    }

    /**
     * Get all user widgets
     * @param uuid UUID of account
     * @return Array of widgets
     */
    public static JsonArray getUserWidgets(UUID uuid) {
        JsonArray array = new JsonArray();
        MongoCollection<Document> collection = MongoQueries.database.getCollection("widgets");

        // Check if username already exists
        if (collection != null) {
            Document search = new Document("account_uuid", uuid.toString());
            for (Document doc : collection.find(search)) {
                JsonObject obj = parseDocumentToObject(doc);

                // Add to array
                array.add(obj);
            }

            // Return array of widgets
            return array;
        }

        return null;
    }

    /**
     * Edit widget by their uuid
     * @param uuid UUID account of user
     * @param widgetUUID UUID of widget
     * @param params Updated params
     */
    public static void editWidgetByUUID(UUID uuid, UUID widgetUUID, Map<String, Object> params) {
        MongoCollection<Document> collection = MongoQueries.database.getCollection("widgets");

        // Check if username already exists
        if (collection != null) {
            Document search = new Document("account_uuid", uuid.toString()).append("uuid", widgetUUID.toString());
            Document result = collection.find(search).first();

            // Exists or not ?
            if (result != null) {
                Document edited = new Document();

                // Params with value
                Document paramsDoc = new Document();
                for (Map.Entry<String, Object> entry : params.entrySet())
                    paramsDoc.append(entry.getKey(), entry.getValue());
                edited.append("params", paramsDoc);

                // Update
                collection.updateOne(search, new Document("$set", edited));
            }
        }
    }

    /**
     * Delete widget with their name
     * @param uuid UUID of the current logged user account
     * @param uuid UUID of the widget
     */
    public static void deleteWidgetByName(UUID uuid, UUID widgetUUID) {
        MongoCollection<Document> collection = MongoQueries.database.getCollection("widgets");

        // Check if username already exists
        if (collection != null) {
            Document search = new Document("account_uuid", uuid.toString()).append("uuid", widgetUUID.toString());
            Document result = collection.find(search).first();

            // Exists or not ?
            if (result != null)
                collection.deleteOne(result);
        }
    }

    /**
     * Get all widget information by UUID
     * @param uuid UUID of owned widget
     * @param widgetUUID UUID of the widget
     * @return Result serialized as object
     */
    public static JsonObject getWidgetInfoByUUID(UUID uuid, UUID widgetUUID) {
        MongoCollection<Document> collection = MongoQueries.database.getCollection("widgets");

        // Check if username already exists
        if (collection != null) {
            Document search = new Document("account_uuid", uuid.toString()).append("uuid", widgetUUID.toString());
            Document result = collection.find(search).first();

            // Exists or not ?
            if (result != null)
                return parseDocumentToObject(result);
        }

        return null;
    }

}

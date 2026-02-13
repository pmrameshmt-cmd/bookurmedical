package com.bookurmedical.database;

import java.util.ArrayList;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;

@Repository
public class BloomAccountDatabase {

    @Autowired
    @Qualifier("bookurmedicalDB")
    private MongoDatabase accountDB;

    /**
     * This is a private method for getting account collection.
     * 
     * @param : none
     * @return : accountdb collection (MongoCollection<Document>).
     */
    private MongoCollection<Document> getAccountsCollection() {
        return accountDB.getCollection("account");
    }

    /**
     * This is a private method for getting user collection.
     * 
     * @param : none
     * @return : userdb collection (MongoCollection<Document>).
     */
    private MongoCollection<Document> getUsersCollection() {
        return accountDB.getCollection("users");
    }

    // -------------------------------------------------------ACCOUNT-------------------------------------------------------------------
    /**
     * This Method is to create new account on signup.
     * <p>
     * <b>Note : </b> Need to handle exception.
     * </p>
     * 
     * @param insertDocument : The document to be inserted.
     * @return : Document : Returns the inserted document which contains the _id of
     *         the document.
     */
    public Document createAccount(Document insertDocument) {
        getAccountsCollection().insertOne(insertDocument);
        return insertDocument;
    }

    public Document getSingleAccount(Document searchQuery, Document projection) {
        return getAccountsCollection().find(searchQuery).projection(projection).first();
    }

    // --------------------------------------------------------------USER--------------------------------------------------------------

    /**
     * This Method is to create new user.
     * <p>
     * <b>Note : </b> Need to handle exception.
     * </p>
     * 
     * @param insertDocument : The document to be inserted.
     * @return : Document : Returns the inserted document which contains the _id of
     *         the document.
     */
    public Document createUser(Document insertDocument) {
        getUsersCollection().insertOne(insertDocument);
        return insertDocument;
    }

    /**
     * Method to get single user.
     * 
     * @param searchQuery : search query for user collection.
     * @param projection  : required fields in return query result.
     * @return Returns single user document if query result matches else return
     *         null.
     */
    public Document getSingleUser(Document searchQuery, Document projection) {
        return getUsersCollection().find(searchQuery).projection(projection).first();
    }
    

    public ArrayList<Document> getUsers(Document searchQuery, Document projection) {
        return getUsersCollection().find(searchQuery).projection(projection).into(new ArrayList<Document>());
    }

    /**
     * Method to update single user
     * 
     * @param searchQuery : search query for user collection.
     * @param updateQuery : update query for a user.
     * @return UpdateResult : Mongo update result which contain modified.
     *         acknowledgement.
     */
    public UpdateResult updateSingleUser(Document searchQuery, Document updateQuery) {
        Document setQuery = updateQuery.containsKey("$set") ? (Document) updateQuery.get("$set") : new Document();
        setQuery.put("updatedAt", System.currentTimeMillis() / 1000L);
        updateQuery.put("$set", setQuery);
        return getUsersCollection().updateOne(searchQuery, updateQuery);
    }

}

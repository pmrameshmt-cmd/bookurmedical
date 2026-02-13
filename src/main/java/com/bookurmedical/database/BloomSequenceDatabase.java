package com.bookurmedical.database;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReturnDocument;

@Repository
public class BloomSequenceDatabase {

    @Autowired
    @Qualifier("bookurmedicalDB")
    private MongoDatabase sequenceDB;

    /**
     * This is a private method for getting seller sequence collection.
     * 
     * @param : none
     * @return : sequence collection (MongoCollection<Document>).
     */
    private MongoCollection<Document> getSequenceCollection() {
        return sequenceDB.getCollection("sequence");
    }

    public String shopNumberSequence() {
        Document searchQuery = new Document("_id", "shopNumberSequence");
        Document update = new Document("$inc", new Document("sequence", 1));
        Document result = getSequenceCollection().findOneAndUpdate(searchQuery, update,
                new com.mongodb.client.model.FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
                        .upsert(true));
        return String.valueOf(result.get("sequence"));
    }

}

package com.bookurmedical.database;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import com.mongodb.client.model.ReplaceOptions;

@Repository
public class BloomSummaryDatabase {

    @Autowired
    @Qualifier("bookurmedicalDB")
    private MongoDatabase inboundDB;

    /**
     * This is a private method for getting account collection.
     * 
     * @param : none
     * @return : accountdb collection (MongoCollection<Document>).
     */
    private MongoCollection<Document> getSummaryCollection() {
        return inboundDB.getCollection("summary");
    }

    public Document addNewSummary(Document insertDocument) {
        getSummaryCollection().insertOne(insertDocument);
        return insertDocument;
    }

    public Document getSingleInbound(Document searchQuery, Document projection) {
        return getSummaryCollection().find(searchQuery).projection(projection).first();
    }

    public void upsertSummary(Document filter, Document doc) {
        getSummaryCollection().replaceOne(filter, doc, new ReplaceOptions().upsert(true));
    }

}

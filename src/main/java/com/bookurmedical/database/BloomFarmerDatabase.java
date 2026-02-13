package com.bookurmedical.database;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;

@Repository
public class BloomFarmerDatabase {

    @Autowired
    @Qualifier("bookurmedicalDB")
    private MongoDatabase farmerDB;

    private MongoCollection<Document> getFlowerCollection() {
        return farmerDB.getCollection("farmer");
    }

    private MongoCollection<Document> getFarmerJournalCollection() {
        return farmerDB.getCollection("farmerJournal");
    }

    public Document createFarmer(Document insertDocument) {
        getFlowerCollection().insertOne(insertDocument);
        return insertDocument;
    }

    public Document getSingleFarmer(Document searchQuery, Document projection) {
        return getFlowerCollection().find(searchQuery).projection(projection).first();
    }

    public List<Document> getFarmerList(Bson searchQuery, Document projection) {
        return getFlowerCollection().find(searchQuery).projection(projection).into(new ArrayList<>());
    }

    public void updateSingleFarmer(Document filter, Document update) {
        getFlowerCollection().updateOne(filter, update);
    }

    public void addNewFarmerJournal(Document query, Document update) {
        getFarmerJournalCollection().updateOne(query, update, new UpdateOptions().upsert(true));
    }

    public List<Document> getFarmerJournalList(Bson searchQuery, Document projection) {
        return getFarmerJournalCollection().find(searchQuery).projection(projection).into(new ArrayList<>());
    }

    public void deleteFarmer(Document filter) {
        getFlowerCollection().deleteOne(filter);
    }

}

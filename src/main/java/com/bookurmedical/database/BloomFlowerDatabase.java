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

@Repository
public class BloomFlowerDatabase {

    @Autowired
    @Qualifier("bookurmedicalDB")
    private MongoDatabase priceDB;

    /**
     * This is a private method for getting account collection.
     * 
     * @param : none
     * @return : accountdb collection (MongoCollection<Document>).
     */
    private MongoCollection<Document> getFlowerCollection() {
        return priceDB.getCollection("flower");
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
    public Document createFlower(Document insertDocument) {
    	getFlowerCollection().insertOne(insertDocument);
        return insertDocument;
    }

    public Document getSingleFlower(Document searchQuery, Document projection) {
        return getFlowerCollection().find(searchQuery).projection(projection).first();
    }
    
	public List<Document> getFlowerList(Bson searchQuery, Document projection) {
		return getFlowerCollection().find(searchQuery).projection(projection).into(new ArrayList<>());
	}
	
	public void updateSingleFlower(Document filter, Document update) {
		getFlowerCollection().updateOne(filter, update);
	}
    
}

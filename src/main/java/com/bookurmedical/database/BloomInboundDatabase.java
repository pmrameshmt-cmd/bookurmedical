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
public class BloomInboundDatabase {

    @Autowired
    @Qualifier("bookurmedicalDB")
    private MongoDatabase inboundDB;

    /**
     * This is a private method for getting account collection.
     * 
     * @param : none
     * @return : accountdb collection (MongoCollection<Document>).
     */
    private MongoCollection<Document> getInboundrCollection() {
        return inboundDB.getCollection("inbound");
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
    public Document addNewInbound(Document insertDocument) {
    	getInboundrCollection().insertOne(insertDocument);
        return insertDocument;
    }

    public Document getSingleInbound(Document searchQuery, Document projection) {
        return getInboundrCollection().find(searchQuery).projection(projection).first();
    }
    
	public List<Document> getInboundList(Bson searchQuery, Document projection) {
		return getInboundrCollection().find(searchQuery).projection(projection).into(new ArrayList<>());
	}
	
	public void updateSingleInbound(Document filter, Document update) {
		getInboundrCollection().updateOne(filter, update);
	}
    
}

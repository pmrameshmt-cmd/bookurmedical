package com.bookurmedical.database;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;

@Repository
public class BloomSalesDatabase {

	@Autowired
	@Qualifier("bookurmedicalDB")
	private MongoDatabase salesDB;

	private MongoCollection<Document> getSalesCollection() {
		return salesDB.getCollection("sales");
	}

	public Document addNewSales(Document insertDocument) {
		getSalesCollection().insertOne(insertDocument);
		return insertDocument;
	}

	public Document getSingleSales(Document searchQuery, Document projection) {
		return getSalesCollection().find(searchQuery).projection(projection).first();
	}

	public List<Document> getSalesList(Bson searchQuery, Document projection) {
		return getSalesCollection().find(searchQuery).projection(projection).into(new ArrayList<>());
	}

	public void updateSingleSales(Document filter, Document update) {
		getSalesCollection().updateOne(filter, update);
	}

	public void upsertSales(Document filter, Document update) {
		getSalesCollection().updateOne(filter, update, new UpdateOptions().upsert(true));
	}

	public long updateSales(Document filter, Document update) {
		return getSalesCollection().updateOne(filter, update).getMatchedCount();
	}

	public AggregateIterable<Document> aggregate(List<Document> pipeline) {
		return getSalesCollection().aggregate(pipeline);
	}

}

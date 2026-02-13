package com.bookurmedical.database;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;

@Repository
public class BloomBuyerDatabase {

	@Autowired
	@Qualifier("bookurmedicalDB")
	private MongoDatabase buyerDB;

	private MongoCollection<Document> getBuyerCollection() {
		return buyerDB.getCollection("buyer");
	}

	private MongoCollection<Document> getBuyerJournalCollection() {
		return buyerDB.getCollection("buyerJournal");
	}

	public Document createBuyer(Document insertDocument) {
		getBuyerCollection().insertOne(insertDocument);
		return insertDocument;
	}

	public Document getSingleBuyer(Document searchQuery, Document projection) {
		return getBuyerCollection().find(searchQuery).projection(projection).first();
	}

	public List<Document> getBuyerList(Bson searchQuery, Document projection) {
		return getBuyerCollection().find(searchQuery).projection(projection).into(new ArrayList<>());
	}

	public void updateSingleBuyer(Document filter, Document update) {
		getBuyerCollection().updateOne(filter, update);
	}

	public void addNewBuyerJournal(Document query, Document update) {
		getBuyerJournalCollection().updateOne(query, update, new UpdateOptions().upsert(true));
	}

	public List<Document> getBuyerJournalList(Bson searchQuery, Document projection) {
		return getBuyerJournalCollection().find(searchQuery).projection(projection).into(new ArrayList<>());
	}

	public AggregateIterable<Document> aggregate(List<Document> pipeline) {
		return getBuyerJournalCollection().aggregate(pipeline);
	}

	public void deleteBuyer(Document filter) {
		getBuyerCollection().deleteOne(filter);
	}

	public void deleteBuyerJournal(Document filter) {
		getBuyerJournalCollection().deleteMany(filter);
	}

	public void updateBuyerJournal(Document query, Document update) {
		getBuyerJournalCollection().updateOne(query, update);
	}

}

package com.bookurmedical.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.client.model.Filters;
import com.bookurmedical.database.BloomFarmerDatabase;
import com.bookurmedical.database.BloomSalesDatabase;

@Service
public class BloomFarmerService {

	@Autowired
	BloomFarmerDatabase farmerDatabase;

	@Autowired
	BloomSalesDatabase salesDatabase;

	public Document createFarmer(String shopNumber, JSONObject jsonObject) {
		Document userDocument = new Document();
		userDocument.put("shopNumber", shopNumber);
		userDocument.put("name", jsonObject.getString("name"));
		userDocument.put("outStanding", 0);
		userDocument.put("createdAt", System.currentTimeMillis() / 1000L);
		userDocument.put("updatedAt", System.currentTimeMillis() / 1000L);
		farmerDatabase.createFarmer(userDocument);
		return userDocument;
	}

	public Document getSingleFarmer(String email, String shopNumber) {
		Document searchQuery = new Document();
		Document projection = new Document();
		searchQuery.put("email", email);
		searchQuery.put("shopNumber", shopNumber);
		projection.put("credentials", 0);
		projection.put("_id", 0);
		return farmerDatabase.getSingleFarmer(searchQuery, projection);
	}

	public JSONArray getFarmerList(String shopNumber) {
		Document projection = new Document();
		projection.put("createdAt", 0);
		projection.put("shopNumber", 0);
		Bson filter = Filters.and(Filters.eq("shopNumber", shopNumber));
		List<Document> sales = farmerDatabase.getFarmerList(filter, projection);
		JSONArray jsonArray = new JSONArray();
		for (Document doc : sales) {
			// Convert _id to string if it exists
			if (doc.containsKey("_id")) {
				Object idObj = doc.get("_id");
				if (idObj instanceof org.bson.types.ObjectId) {
					doc.put("id", ((org.bson.types.ObjectId) idObj).toHexString());
					doc.remove("_id");
				}
			}
			jsonArray.put(new JSONObject(doc));
		}
		return jsonArray;
	}

	public void updateSingleFarmer(String shopNumber, JSONObject request) {
		Document filter = new Document("_id", new ObjectId(request.getString("farmerID")));
		Document updateData = new Document();
		updateData.put("name", request.getString("farmerName"));
		updateData.put("outStanding", request.getDouble("outStanding"));
		if (request.has("commissionPercentage")) {
			updateData.put("commissionPercentage", request.getDouble("commissionPercentage"));
		}
		Document update = new Document("$set", updateData);
		farmerDatabase.updateSingleFarmer(filter, update);
	}

	public void deleteFarmer(String shopNumber, String farmerID) {
		Document filter = new Document("_id", new ObjectId(farmerID));
		filter.append("shopNumber", shopNumber);
		farmerDatabase.deleteFarmer(filter);
	}

	public ArrayList<Document> getFarmerJournal(String shopNumber, String farmerID, long startDate, long endDate) {
		ArrayList<Document> results = new ArrayList<>();
		Document matchStage = new Document("shopNumber", shopNumber).append("farmerID", farmerID).append("saleDate",
				new Document("$gte", startDate).append("$lte", endDate));
		Document projection = new Document();
		projection.put("_id", 0);
		List<Document> pipeline = farmerDatabase.getFarmerJournalList(matchStage, projection);
		for (Document doc : pipeline) {
			results.add(doc);
		}

		return results;
	}

	public JSONObject getFarmerJournalDetails(String shopNumber, String farmerID, Long saleDate) {
		JSONObject response = new JSONObject();
		JSONArray buyerItemsArray = new JSONArray();

		// Compute start and end of that day (assuming saleDate is in seconds)
		LocalDate date = Instant.ofEpochSecond(saleDate).atZone(ZoneId.systemDefault()).toLocalDate();

		long startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
		long endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toEpochSecond() - 1;

		// Build query
		Document query = new Document("shopNumber", shopNumber)
				.append("saleDate", new Document("$gte", startOfDay).append("$lte", endOfDay))
				.append("farmerID", farmerID);

		Document projection = new Document();
		projection.put("_id", 0);
		List<Document> pipeline = salesDatabase.getSalesList(query, projection);
		for (Document doc : pipeline) {
			buyerItemsArray.put(new JSONObject(doc));
		}
		response.put("status", "success");
		response.put("flowerSaleItems", buyerItemsArray);
		return response;
	}

	public JSONArray getTodayAllFarmerJournal(String shopNumber, Long startDate, Long endDate) {
		JSONArray resultArray = new JSONArray();

		// If dates not provided, use today
		if (startDate == null || endDate == null) {
			LocalDate today = LocalDate.now(ZoneId.systemDefault());
			startDate = today.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
			endDate = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toEpochSecond() - 1;
		}

		// Build query to get all farmer journals for the date range
		Document matchStage = new Document("shopNumber", shopNumber)
				.append("saleDate", new Document("$gte", startDate).append("$lte", endDate));

		Document projection = new Document();
		projection.put("_id", 0);
		projection.put("farmerID", 1);
		projection.put("saleAmount", 1);

		List<Document> farmerJournals = farmerDatabase.getFarmerJournalList(matchStage, projection);

		// Since each farmer has only one entry per day, directly convert to JSON array
		for (Document journal : farmerJournals) {
			JSONObject farmerData = new JSONObject();
			farmerData.put("farmerID", journal.getString("farmerID"));

			// Handle both Long and Double types from database
			Object saleAmountObj = journal.get("saleAmount");
			double saleAmountValue = 0.0;
			if (saleAmountObj instanceof Number) {
				saleAmountValue = ((Number) saleAmountObj).doubleValue();
			}

			farmerData.put("saleAmount", saleAmountValue);
			resultArray.put(farmerData);
		}

		return resultArray;
	}

}

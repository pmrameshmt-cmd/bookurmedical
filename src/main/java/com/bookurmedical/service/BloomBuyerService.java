package com.bookurmedical.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.client.model.Filters;
import com.bookurmedical.database.BloomBuyerDatabase;
import com.bookurmedical.database.BloomSalesDatabase;

@Service
public class BloomBuyerService {

	@Autowired
	BloomBuyerDatabase buyerDatabase;

	@Autowired
	BloomSalesDatabase salesDatabase;

	@Autowired
	BloomSalesService salesService;

	public Document createBuyer(String shopNumber, JSONObject jsonObject) {
		Document userDocument = new Document();
		userDocument.put("shopNumber", shopNumber);
		userDocument.put("name", jsonObject.getString("name"));
		userDocument.put("outStanding", 0);
		userDocument.put("createdAt", System.currentTimeMillis() / 1000L);
		userDocument.put("updatedAt", System.currentTimeMillis() / 1000L);
		buyerDatabase.createBuyer(userDocument);
		return userDocument;
	}

	public Document getSingleBuyer(String email, String shopNumber) {
		Document searchQuery = new Document();
		Document projection = new Document();
		searchQuery.put("name", email);
		searchQuery.put("shopNumber", shopNumber);
		projection.put("credentials", 0);
		projection.put("_id", 0);
		return buyerDatabase.getSingleBuyer(searchQuery, projection);
	}

	public JSONArray getBuyerList(String shopNumber) {
		Document projection = new Document();
		projection.put("createdAt", 0);
		projection.put("shopNumber", 0);
		Bson filter = Filters.and(Filters.eq("shopNumber", shopNumber));
		List<Document> sales = buyerDatabase.getBuyerList(filter, projection);
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

	public void updateSingleBuyer(String shopNumber, JSONObject request) {
		Document filter = new Document("_id", new ObjectId(request.getString("buyerID")));
		Document updateData = new Document();
		updateData.put("name", request.getString("buyerName"));
		updateData.put("outStanding", request.getDouble("outStanding"));
		Document update = new Document("$set", updateData);
		buyerDatabase.updateSingleBuyer(filter, update);
	}

	public ArrayList<Document> getBuyerJournal(String buyerID, long startDate, long endDate) {
		ArrayList<Document> results = new ArrayList<>();
		Document matchStage = new Document("buyerID", buyerID).append("saleDate",
				new Document("$gte", startDate).append("$lte", endDate));
		Document projection = new Document();
		projection.put("_id", 0);
		List<Document> pipeline = buyerDatabase.getBuyerJournalList(matchStage, projection);
		for (Document doc : pipeline) {
			results.add(doc);
		}

		return results;
	}

	public void deleteBuyer(String shopNumber, String buyerID) {
		Document filter = new Document("_id", new ObjectId(buyerID));
		buyerDatabase.deleteBuyer(filter);
		Document journalFilter = new Document("shopNumber", shopNumber).append("buyerID", buyerID);
		buyerDatabase.deleteBuyerJournal(journalFilter);
	}

	public void adjustSingleBuyer(String shopNumber, JSONObject request) {
		Document filter = new Document("_id", new ObjectId(request.getString("buyerID")));
		Document updateData = new Document();
		updateData.put("outStanding", request.getLong("closingBalance"));
		Document update = new Document("$set", updateData);
		buyerDatabase.updateSingleBuyer(filter, update);
		Instant instant = Instant.ofEpochSecond(request.getLong("paymentDate"));
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());
		String formattedDate = formatter.format(instant);
		String paymentType = Optional.ofNullable(request.optString("paymentType", null)).filter(s -> !s.isEmpty())
				.orElse("cash");
		long discount = Optional.ofNullable(request.optLong("discount", 0)).orElse(0L);
		salesService.upsertBuyerJournal(shopNumber, request.getString("buyerID"), formattedDate,
				request.getLong("openingBalance"), 0, request.getLong("creditAmount"), request.getLong("paymentDate"),
				paymentType, discount);
	}

	public JSONObject getBuyerJournalDetails(String shopNumber, String buyerID, long saleDate) {
		JSONObject response = new JSONObject();
		JSONArray buyerItemsArray = new JSONArray();

		// Compute start and end of that day (assuming saleDate is in seconds)
		LocalDate date = Instant.ofEpochSecond(saleDate).atZone(ZoneId.systemDefault()).toLocalDate();

		long startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
		long endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toEpochSecond() - 1;

		// Build query
		Document query = new Document("shopNumber", shopNumber)
				.append("saleDate", new Document("$gte", startOfDay).append("$lte", endOfDay))
				.append("flowerSaleItems.buyerID", buyerID);

		Document projection = new Document();
		projection.put("_id", 0);
		List<Document> pipeline = salesDatabase.getSalesList(query, projection);
		for (Document record : pipeline) {
			List<Document> flowerSaleItems = (List<Document>) record.get("flowerSaleItems");
			flowerSaleItems.stream().filter(item -> buyerID.equals(item.getString("buyerID")))
					.forEach(item -> buyerItemsArray.put(new JSONObject(item)));
		}
		response.put("status", "success");
		response.put("flowerSaleItems", buyerItemsArray);
		return response;
	}

	public List<Document> getShopBuyerPayments(String shopNumber, long saleDate) {
		// Convert saleDate to day start & end
		ZoneId istZone = ZoneId.of("Asia/Kolkata");

		// Convert epoch to LocalDate in IST
		LocalDate date = Instant.ofEpochSecond(saleDate).atZone(istZone).toLocalDate();

		// Get start and end of the day in IST, converted back to epoch seconds
		long startOfDay = date.atStartOfDay(istZone).toEpochSecond();
		long endOfDay = date.atTime(LocalTime.MAX).atZone(istZone).toEpochSecond();

		Document query = new Document("shopNumber", shopNumber)
				.append("saleDate", new Document("$gte", startOfDay).append("$lte", endOfDay))
				.append("credit", new Document("$gt", 0));
		Document projection = new Document();
		projection.put("_id", 0);
		projection.put("buyerID", 1);
		projection.put("credit", 1);
		projection.put("paymentType", 1);
		projection.put("gpayAmount", 1);
		projection.put("discount", 1);
		List<Document> result = buyerDatabase.getBuyerJournalList(query, projection);

		return result;
	}

	public JSONArray getTodayAllBuyerJournal(String shopNumber, Long startDate, Long endDate) {
		JSONArray resultArray = new JSONArray();

		// If dates not provided, use today
		if (startDate == null || endDate == null) {
			LocalDate today = LocalDate.now(ZoneId.systemDefault());
			startDate = today.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
			endDate = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toEpochSecond() - 1;
		}

		// Build query to get all buyer journals for the date range
		Document matchStage = new Document("shopNumber", shopNumber)
				.append("saleDate", new Document("$gte", startDate).append("$lte", endDate));

		Document projection = new Document();
		projection.put("_id", 0);
		projection.put("buyerID", 1);
		projection.put("purchase", 1);

		List<Document> buyerJournals = buyerDatabase.getBuyerJournalList(matchStage, projection);

		// Since each buyer has only one entry per day, directly convert to JSON array
		for (Document journal : buyerJournals) {
			JSONObject buyerData = new JSONObject();
			buyerData.put("buyerID", journal.getString("buyerID"));

			// Handle both Long and Double types from database
			Object purchaseObj = journal.get("purchase");
			double purchaseValue = 0.0;
			if (purchaseObj instanceof Number) {
				purchaseValue = ((Number) purchaseObj).doubleValue();
			}

			buyerData.put("purchase", purchaseValue);
			resultArray.put(buyerData);
		}

		return resultArray;
	}

}

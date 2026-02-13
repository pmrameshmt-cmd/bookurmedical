package com.bookurmedical.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.client.model.Filters;
import com.bookurmedical.database.BloomAccountDatabase;
import com.bookurmedical.database.BloomBuyerDatabase;
import com.bookurmedical.database.BloomFarmerDatabase;
import com.bookurmedical.database.BloomSalesDatabase;

@Service
public class BloomSalesService {

	@Autowired
	BloomSalesDatabase salesDatabase;

	@Autowired
	BloomBuyerDatabase buyerDatabase;

	@Autowired
	BloomFarmerDatabase farmerDatabase;

	@Autowired
	BloomAccountDatabase accountDatabase;

	public Document addNewSales(String shopNumber, JSONObject request) {
		// Keep shopNumber inside the sales record for reference
		request.put("shopNumber", shopNumber);
		// Save sales document
		Document doc;
		if (request.has("isUpsert") && request.getBoolean("isUpsert")) {
			// Logic for Upsert (Chennai Payment/Adjustment)
			long rawDate = request.getLong("saleDate");
			ZoneId istZone = ZoneId.of("Asia/Kolkata");
			LocalDate localDate = Instant.ofEpochSecond(rawDate).atZone(istZone).toLocalDate();
			long startOfDay = localDate.atStartOfDay(istZone).toEpochSecond();
			long endOfDay = localDate.atTime(LocalTime.MAX).atZone(istZone).toEpochSecond();

			String farmerID = request.getString("farmerID");

			// Try to find ANY record for this farmer on this day
			Document query = new Document("shopNumber", shopNumber)
					.append("farmerID", farmerID)
					.append("saleDate", new Document("$gte", startOfDay).append("$lte", endOfDay));

			Document incData = new Document("creditAmount", request.optLong("creditAmount", 0L))
					.append("actualSalesAmount", request.optLong("actualSalesAmount", 0L))
					.append("totalSaleAmount", request.optLong("totalSaleAmount", 0L))
					.append("commission", request.optLong("commission", 0L))
					.append("labourCost", request.optLong("labourCost", 0L))
					.append("vanRent", request.optLong("vanRent", 0L));

			Document setData = new Document("closingBalance", request.optLong("closingBalance", 0L))
					.append("updatedAt", Instant.now().getEpochSecond());

			Document update = new Document("$inc", incData).append("$set", setData);

			long matched = salesDatabase.updateSales(query, update);

			if (matched == 0) {
				// No record found, create new normalized record
				request.put("saleDate", startOfDay); // Normalize date for new record
				doc = Document.parse(request.toString());
				// Ensure timestamps are set correct
				doc.put("createdAt", Instant.now().getEpochSecond());
				salesDatabase.addNewSales(doc);
			} else {
				doc = Document.parse(request.toString()); // Dummy return
			}

		} else {
			doc = Document.parse(request.toString());
			salesDatabase.addNewSales(doc);
		}
		Instant instant = Instant.ofEpochSecond(request.getLong("saleDate"));
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());
		String formattedDate = formatter.format(instant);

		// ✅ Update Farmer Outstanding with totalSaleAmount
		if (request.has("farmerID") && request.has("closingBalance")) {
			updateFarmerOutStanding(request.getString("farmerID"), request.optLong("closingBalance", 0L));
			long overDue = 0;
			if (request.has("overDueAmount")) {
				overDue = request.optLong("overDueAmount", 0L);
			}
			upsertFarmerJournal(shopNumber, request.getString("farmerID"), formattedDate,
					request.optLong("openingBalance", 0), request.optLong("totalSaleAmount", 0),
					request.optLong("creditAmount", 0), request.getLong("saleDate"), "", 0, overDue);
		}
		Set<String> excludedIds = Set.of("cash", "gpay");
		// ✅ Update Buyer Outstanding from flowerSaleItems (use totalAmount per buyer)
		if (request.has("flowerSaleItems")) {
			JSONArray flowerItems = request.getJSONArray("flowerSaleItems");
			for (int i = 0; i < flowerItems.length(); i++) {
				JSONObject item = flowerItems.getJSONObject(i);
				String buyerId = item.optString("buyerID", "");
				if (!buyerId.isEmpty() && !excludedIds.contains(buyerId.toLowerCase()) && item.has("totalAmount")) {
					updateBuyerOutStanding(buyerId, item.optLong("totalAmount", 0L));
					upsertBuyerJournal(shopNumber, buyerId, formattedDate, item.optLong("buyerOpeningBalance", 0),
							item.optLong("totalAmount", 0), 0L, request.getLong("saleDate"), "", 0);
				}
			}
		}
		return doc;
	}

	public Document getSingleSales(String email, String shopNumber) {
		Document searchQuery = new Document();
		Document projection = new Document();
		searchQuery.put("email", email);
		searchQuery.put("shopNumber", shopNumber);
		projection.put("credentials", 0);
		projection.put("_id", 0);
		return salesDatabase.getSingleSales(searchQuery, projection);
	}

	public JSONArray getSalesList(String shopNumber, String buyerID, String farmerID, Long startDate, Long endDate,
			Integer salesPersonID) {
		// Projection (exclude unwanted fields)
		Document projection = new Document();
		projection.put("_id", 0);
		projection.put("createdAt", 0);
		projection.put("shopNumber", 0);

		// Build dynamic filter
		List<Bson> filters = new ArrayList<>();
		filters.add(Filters.eq("shopNumber", shopNumber)); // always required

		if (buyerID != null && !buyerID.isEmpty()) {
			filters.add(Filters.eq("buyerID", buyerID));
		}

		if (farmerID != null && !farmerID.isEmpty()) {
			filters.add(Filters.elemMatch("flowerSaleItems", Filters.eq("farmerID", farmerID)));
		}

		if (salesPersonID != null) {
			filters.add(Filters.eq("salePersonID", salesPersonID));
		}

		if (startDate != null && endDate != null) {
			filters.add(Filters.and(Filters.gte("saleDate", startDate), Filters.lte("saleDate", endDate)));
		} else if (startDate != null) {
			filters.add(Filters.gte("saleDate", startDate));
		} else if (endDate != null) {
			filters.add(Filters.lte("saleDate", endDate));
		}

		// Final filter
		Bson finalFilter = Filters.and(filters);
		// Fetch data from DB
		List<Document> sales = salesDatabase.getSalesList(finalFilter, projection);
		// Convert to JSONArray
		JSONArray jsonArray = new JSONArray();
		for (Document doc : sales) {
			jsonArray.put(new JSONObject(doc));
		}

		return jsonArray;
	}

	public void updateSingleSales(String shopNumber, JSONObject request) {
		Document filter = new Document("shopNumber", shopNumber).append("name", request.getString("name"));
		Document updateData = new Document();
		updateData.put("price", request.getLong("price"));
		updateData.put("updatedAt", request.getLong("updatedDate"));
		Document update = new Document("$set", updateData);
		salesDatabase.updateSingleSales(filter, update);
	}

	public void updateBuyerOutStanding(String buyerID, long newOutstanding) {
		Document query = new Document("_id", new ObjectId(buyerID));
		Document update = new Document("$inc", new Document("outStanding", newOutstanding));
		buyerDatabase.updateSingleBuyer(query, update);
		System.out.println("Buyer Outstanding incremented | BuyerID: " + buyerID + " | + " + newOutstanding);
	}

	public void upsertBuyerJournal(String shopNumber, String buyerID, String date, long ob, long purchase, long credit,
			long saleDate, String paymentType, long discount) {
		// Query to ensure uniqueness per day
		Document query = new Document("buyerID", buyerID).append("date", date);
		// Base fields to set on insert (only if record doesn't exist)
		Document setOnInsert = new Document("shopNumber", shopNumber).append("buyerID", buyerID).append("date", date)
				.append("openingBalance", ob).append("saleDate", saleDate).append("paymentType", paymentType);
		// Incremental update fields
		Document incFields = new Document("purchase", purchase).append("credit", credit);
		// Handle GPay-specific logic
		if ("Gpay".equalsIgnoreCase(paymentType)) {
			incFields.append("gpayAmount", credit);
		}
		// Prepare main update document
		Document update = new Document().append("$setOnInsert", setOnInsert).append("$inc", incFields);

		// Update discount only if non-zero
		if (discount != 0) {
			update.append("$set", new Document("discount", discount));
		}
		System.out.println("Upserting Buyer Journal | Query: " + query.toJson() + " | Update: " + update.toJson());
		// Perform the actual DB upsert
		buyerDatabase.addNewBuyerJournal(query, update);
		System.out.println("Buyer Journal upserted | BuyerID: " + buyerID + " | Date: " + date);
	}

	public void upsertFarmerJournal(String shopNumber, String farmerID, String date, long ob, long saleAmount,
			long creditAmount,
			long saleDate, String paymentType, long discount, long overDueAmount) {
		// Query to ensure uniqueness per day
		Document query = new Document("farmerID", farmerID).append("date", date);
		// Base fields to set on insert (only if record doesn't exist)
		Document setOnInsert = new Document("shopNumber", shopNumber).append("farmerID", farmerID).append("date", date)
				.append("openingBalance", ob).append("saleDate", saleDate).append("paymentType", paymentType);
		// Incremental update fields
		Document incFields = new Document("saleAmount", saleAmount).append("creditAmount", creditAmount)
				.append("overDueAmount", overDueAmount);

		// Prepare main update document
		Document update = new Document().append("$setOnInsert", setOnInsert).append("$inc", incFields);

		// Update discount only if non-zero
		if (discount != 0) {
			update.append("$set", new Document("discount", discount));
		}
		System.out.println("Upserting Buyer Journal | Query: " + query.toJson() + " | Update: " + update.toJson());
		// Perform the actual DB upsert
		farmerDatabase.addNewFarmerJournal(query, update);
		System.out.println("farmer Journal upserted | farmerID: " + farmerID + " | Date: " + date);
	}

	public void updateFarmerOutStanding(String farmerID, long closingBalance) {
		Document query = new Document("_id", new ObjectId(farmerID));
		Document update = new Document("$set", new Document("outStanding", closingBalance));
		farmerDatabase.updateSingleFarmer(query, update);
		System.out.println("Farmer Outstanding incremented | FarmerID: " + farmerID + " | + " + closingBalance);
	}

	public ArrayList<Document> getSalesManList(String shopNumber) {
		Document searchQuery = new Document("shopNumber", shopNumber);
		Document projection = new Document("credentials", 0).append("refreshToken", 0).append("shopNumber", 0);

		ArrayList<Document> salesManList = accountDatabase.getUsers(searchQuery, projection);

		return salesManList.stream().peek(doc -> {
			ObjectId id = doc.getObjectId("_id");
			if (id != null) {
				doc.put("id", id.toHexString());
				doc.remove("_id");
			}
		}).collect(Collectors.toCollection(ArrayList::new));
	}

	public Document getShopDailySummary(String shopNumber, long saleDate) {
		ZoneId istZone = ZoneId.of("Asia/Kolkata");
		LocalDate date = Instant.ofEpochSecond(saleDate).atZone(istZone).toLocalDate();

		long startOfDay = date.atStartOfDay(istZone).toEpochSecond();
		long endOfDay = date.atTime(LocalTime.MAX).atZone(istZone).toEpochSecond();

		// --- Pipeline 1: Daily Summary ---
		List<Document> pipelineSummary = Arrays.asList(
				new Document("$match",
						new Document("shopNumber", shopNumber).append("saleDate",
								new Document("$gte", startOfDay).append("$lte", endOfDay))),

				// Stage 1: Compute totals WITHOUT unwinding
				new Document("$group",
						new Document("_id", null).append("totalSale", new Document("$sum", "$actualSalesAmount"))
								// ADD overDueAmount + creditAmount
								.append("totalCredit",
										new Document("$sum", new Document("$add",
												Arrays.asList("$creditAmount",
														new Document("$ifNull", Arrays.asList("$overDueAmount", 0))))))
								.append("docs", new Document("$push", "$$ROOT"))),

				new Document("$unwind", "$docs"),
				new Document("$unwind",
						new Document("path", "$docs.flowerSaleItems").append("preserveNullAndEmptyArrays", true)),

				// Stage 3: Calculate cash sale
				new Document("$group",
						new Document("_id", null).append("totalSale", new Document("$first", "$totalSale"))
								.append("totalCredit", new Document("$first", "$totalCredit")).append("cashSale",
										new Document("$sum",
												new Document("$cond",
														Arrays.asList(
																new Document("$in",
																		Arrays.asList("$docs.flowerSaleItems.buyerID",
																				Arrays.asList("cash", "gpay"))),
																"$docs.flowerSaleItems.totalAmount", 0))))));

		// --- Pipeline 2: Farmers with credit or overdue ---
		List<Document> pipelineCredit = Arrays.asList(
				new Document("$match",
						new Document("shopNumber", shopNumber)
								.append("saleDate", new Document("$gte", startOfDay).append("$lte", endOfDay))
								// if overDueAmount > 0 OR creditAmount > 0 -> include
								.append("$or",
										Arrays.asList(new Document("creditAmount", new Document("$gt", 0)),
												new Document("overDueAmount", new Document("$gt", 0))))),
				new Document("$project", new Document("_id", 0).append("farmerName", 1)
						// Return combined credit
						.append("totalCredit", new Document("$add", Arrays.asList("$creditAmount",
								new Document("$ifNull", Arrays.asList("$overDueAmount", 0)))))));

		// Run both pipes
		List<Document> summaryList = salesDatabase.aggregate(pipelineSummary).into(new ArrayList<>());
		List<Document> creditList = salesDatabase.aggregate(pipelineCredit).into(new ArrayList<>());

		Document response = new Document();
		response.append("dailySummary", summaryList);
		response.append("creditFarmers", creditList);

		return response;
	}

	public JSONArray getFarmerLedgerSales(String shopNumber, String farmerID, Long startDate, Long endDate) {
		// Projection (exclude unwanted fields)
		Document projection = new Document();
		projection.put("_id", 0);
		projection.put("createdAt", 0);
		projection.put("shopNumber", 0);

		// Build dynamic filter
		List<Bson> filters = new ArrayList<>();
		filters.add(Filters.eq("shopNumber", shopNumber)); // always required

		if (farmerID != null && !farmerID.isEmpty()) {
			filters.add(Filters.eq("farmerID", farmerID));
		}

		if (startDate != null && endDate != null) {
			filters.add(Filters.and(Filters.gte("saleDate", startDate), Filters.lte("saleDate", endDate)));
		} else if (startDate != null) {
			filters.add(Filters.gte("saleDate", startDate));
		} else if (endDate != null) {
			filters.add(Filters.lte("saleDate", endDate));
		}

		// Final filter
		Bson finalFilter = Filters.and(filters);
		// Fetch data from DB
		List<Document> sales = salesDatabase.getSalesList(finalFilter, projection);
		// Convert to JSONArray
		JSONArray jsonArray = new JSONArray();
		for (Document doc : sales) {
			jsonArray.put(new JSONObject(doc));
		}

		return jsonArray;
	}

	public void updateBuyerSales(String shopNumber, JSONObject request) {
		try {
			long saleDate = request.getLong("saleDate");
			String buyerId = request.getString("buyerID");

			ZoneId istZone = ZoneId.of("Asia/Kolkata");
			LocalDate localDate = Instant.ofEpochSecond(saleDate).atZone(istZone).toLocalDate();
			long startOfDay = localDate.atStartOfDay(istZone).toEpochSecond();
			long endOfDay = localDate.atTime(LocalTime.MAX).atZone(istZone).toEpochSecond();

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());
			String formattedDate = formatter.format(Instant.ofEpochSecond(saleDate));

			// 1. Fetch ALL old sales records for this buyer on this date
			// We search for ANY document containing this buyer's items
			Document dateFilter = new Document("$gte", startOfDay).append("$lte", endOfDay);
			Document query = new Document("shopNumber", shopNumber)
					.append("saleDate", dateFilter)
					.append("flowerSaleItems.buyerID", buyerId);

			List<Document> oldSales = salesDatabase.getSalesList(query, new Document());
			long oldTotalAmount = 0;

			// 2. Iterate and clear old items + calculate old total
			for (Document doc : oldSales) {
				List<Document> items = (List<Document>) doc.get("flowerSaleItems");
				List<Document> newItems = new ArrayList<>();
				boolean changed = false;

				for (Document item : items) {
					// Check if item belongs to this buyer
					if (item.containsKey("buyerID") && item.get("buyerID").equals(buyerId)) {
						// Found item for this buyer - add to total and skip adding to new list (delete)
						oldTotalAmount += item.containsKey("totalAmount")
								? ((Number) item.get("totalAmount")).longValue()
								: 0L;
						changed = true;
					} else {
						// Keep items for other buyers
						newItems.add(item);
					}
				}

				if (changed) {
					// Update the document with items REMOVED
					Document update = new Document("$set", new Document("flowerSaleItems", newItems)
							.append("updatedAt", Instant.now().getEpochSecond()));
					salesDatabase.updateSales(new Document("_id", doc.getObjectId("_id")), update);
				}
			}

			System.out.println("Old total for buyer " + buyerId + ": " + oldTotalAmount);

			// 3. Create NEW document with new items
			long newTotalAmount = 0;
			JSONArray flowerItems = request.getJSONArray("flowerSaleItems");
			List<Document> newFlowerItemsList = new ArrayList<>();

			for (int i = 0; i < flowerItems.length(); i++) {
				JSONObject item = flowerItems.getJSONObject(i);
				// Ensure buyerID is present
				item.put("buyerID", buyerId);
				newFlowerItemsList.add(Document.parse(item.toString()));
				newTotalAmount += item.optLong("totalAmount", 0L);
			}

			// Add expenses if provided (from FE context)
			long expenses = request.optLong("expenses", 0L);
			long actualSalesAmount = newTotalAmount + expenses;

			Document newDoc = new Document("shopNumber", shopNumber)
					.append("saleDate", saleDate)
					.append("flowerSaleItems", newFlowerItemsList)
					.append("totalSaleAmount", newTotalAmount)
					.append("actualSalesAmount", actualSalesAmount)
					.append("createdAt", Instant.now().getEpochSecond())
					.append("updatedAt", Instant.now().getEpochSecond());

			if (expenses > 0) {
				newDoc.append("expenses", expenses);
			}

			salesDatabase.addNewSales(newDoc);
			System.out.println("New sales document created with total: " + newTotalAmount);

			// 4. Update Outstanding
			long difference = oldTotalAmount - newTotalAmount;
			if (difference != 0) {
				Document buyerQuery = new Document("_id", new ObjectId(buyerId));
				Document buyerUpdate = new Document("$inc", new Document("outStanding", -difference));
				buyerDatabase.updateSingleBuyer(buyerQuery, buyerUpdate);
				System.out.println("Outstanding adjusted by: " + (-difference));
			}

			// 5. Update Buyer Journal
			Document journalQuery = new Document("buyerID", buyerId).append("date", formattedDate);
			Document journalUpdate = new Document("$set", new Document("purchase", newTotalAmount)
					.append("updatedAt", Instant.now().getEpochSecond()));
			buyerDatabase.updateBuyerJournal(journalQuery, journalUpdate);
			System.out.println("Journal updated for date: " + formattedDate);

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Error updating buyer sales: " + e.getMessage());
		}
	}

}

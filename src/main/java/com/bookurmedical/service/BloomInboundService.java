package  com.smartstockhub.bloom.service;

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
import com.bookurmedical.database.BloomInboundDatabase;

@Service
public class BloomInboundService {

	@Autowired
	BloomInboundDatabase inboundDatabase;
	
	@Autowired
	BloomFarmerDatabase farmerDatabase;

	public Document addNewInbound(String shopNumber, JSONObject jsonObject) {
		jsonObject.put("shopNumber", shopNumber);
		Document doc = Document.parse(jsonObject.toString());
		inboundDatabase.addNewInbound(doc);
		if (jsonObject.has("farmerID") && jsonObject.has("balance")) {
			updateFarmerOutStanding(shopNumber, jsonObject.getString("farmerID"), jsonObject.optLong("balance", 0L));
		}
		return doc;
	}

	public Document getSingleInbound(String email, String shopNumber) {
		Document searchQuery = new Document();
		Document projection = new Document();
		searchQuery.put("email", email);
		searchQuery.put("shopNumber", shopNumber);
		projection.put("credentials", 0);
		projection.put("_id", 0);
		return inboundDatabase.getSingleInbound(searchQuery, projection);
	}

	public JSONArray getInboundList(String shopNumber) {
		Document projection = new Document();
		projection.put("_id", 0);
		projection.put("createdAt", 0);
		projection.put("shopNumber", 0);
		Bson filter = Filters.and(Filters.eq("shopNumber", shopNumber));
		List<Document> sales = inboundDatabase.getInboundList(filter, projection);
		JSONArray jsonArray = new JSONArray();
		for (Document doc : sales) {
			jsonArray.put(new JSONObject(doc));
		}
		return jsonArray;
	}

	public void updateSingleInbound(String shopNumber, JSONObject request) {
		Document filter = new Document("shopNumber", shopNumber).append("name", request.getString("name"));
		Document updateData = new Document();
		updateData.put("price", request.getLong("price"));
		updateData.put("updatedAt", request.getLong("updatedDate"));
		Document update = new Document("$set", updateData);
		inboundDatabase.updateSingleInbound(filter, update);
	}
	
	public void updateFarmerOutStanding(String shopNumber, String farmerID, long outstandingToAdd) {
		Document query = new Document("_id", new ObjectId(farmerID));

	    Document existingFarmer = farmerDatabase.getSingleFarmer(query, null);

		if (existingFarmer != null) {
			Number outstandingValue = existingFarmer.get("outStanding", Number.class);
			long currentOutstanding = (outstandingValue != null) ? outstandingValue.longValue() : 0L;
			long updatedOutstanding = currentOutstanding + outstandingToAdd;
			Document updateFields = new Document().append("outStanding", updatedOutstanding);
			Document update = new Document("$set", updateFields);
			farmerDatabase.updateSingleFarmer(query, update);
			System.out.println("Farmer OutStanding updated ShopNumber : " + shopNumber);
		} else {
	        System.out.println("Farmer Not Found");
	    }

	}
}

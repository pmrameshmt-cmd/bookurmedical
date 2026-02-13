package  com.smartstockhub.bloom.service;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.client.model.Filters;
import com.bookurmedical.database.BloomFlowerDatabase;

@Service
public class BloomFlowerService {

	@Autowired
	BloomFlowerDatabase flowerDatabase;

	public Document createFlower(String shopNumber, JSONObject jsonObject) {
		Document userDocument = new Document();
		userDocument.put("shopNumber", shopNumber);
		userDocument.put("name", jsonObject.getString("name"));
		userDocument.put("price", jsonObject.getLong("price"));
		userDocument.put("createdAt", System.currentTimeMillis() / 1000L);
		userDocument.put("updatedAt", System.currentTimeMillis() / 1000L);
		flowerDatabase.createFlower(userDocument);
		return userDocument;
	}

	public Document getSingleUser(String email, String shopNumber) {
		Document searchQuery = new Document();
		Document projection = new Document();
		searchQuery.put("email", email);
		searchQuery.put("shopNumber", shopNumber);
		projection.put("credentials", 0);
		projection.put("_id", 0);
		return flowerDatabase.getSingleFlower(searchQuery, projection);
	}

	public JSONArray getFlowerList(String shopNumber) {
		Document projection = new Document();
		projection.put("createdAt", 0);
		projection.put("shopNumber", 0);
		Bson filter = Filters.and(Filters.eq("shopNumber", shopNumber));
		List<Document> sales = flowerDatabase.getFlowerList(filter, projection);
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

	public void updateSingleFlower(String shopNumber, JSONObject request) {
		Document filter = new Document("shopNumber", shopNumber).append("name", request.getString("name"));
		Document updateData = new Document();
		updateData.put("price", request.getLong("price"));
		updateData.put("updatedAt", request.getLong("updatedDate"));
		Document update = new Document("$set", updateData);
		flowerDatabase.updateSingleFlower(filter, update);
	}

}

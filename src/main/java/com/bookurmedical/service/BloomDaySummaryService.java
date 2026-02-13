package com.bookurmedical.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

import org.bson.Document;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bookurmedical.database.BloomSummaryDatabase;

@Service
public class BloomDaySummaryService {

	@Autowired
	BloomSummaryDatabase BloomSummaryDatabase;

	public void addNewSummary(String shopNumber, JSONObject jsonObject) {
		System.out.println("Adding new day summary for shop: " + shopNumber + " data: " + jsonObject.toString());
		jsonObject.put("shopNumber", shopNumber);

		Long saleDate = jsonObject.getLong("saleDate");
		LocalDate date = Instant.ofEpochSecond(saleDate).atZone(ZoneId.systemDefault()).toLocalDate();
		long startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
		long endOfDay = date.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toEpochSecond();

		Document filter = new Document("shopNumber", shopNumber).append("saleDate",
				new Document("$gte", startOfDay).append("$lte", endOfDay));

		Document doc = Document.parse(jsonObject.toString());
		BloomSummaryDatabase.upsertSummary(filter, doc);
	}

	public Document getDaySummary(String shopNumber, Long saleDate) {
		LocalDate date = Instant.ofEpochSecond(saleDate).atZone(ZoneId.systemDefault()).toLocalDate();
		long startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
		long endOfDay = date.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toEpochSecond();
		Document searchQuery = new Document("shopNumber", shopNumber).append("saleDate",
				new Document("$gte", startOfDay).append("$lte", endOfDay));
		return BloomSummaryDatabase.getSingleInbound(searchQuery, null);
	}

}

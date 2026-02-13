package com.bookurmedical.restcontroller;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bookurmedical.service.BloomBuyerService;

@RestController
@RequestMapping("/buyer")
public class BloomBuyerController {

	@Autowired
	BloomBuyerService buyerService;

	@GetMapping
	public JSONObject getBuyer(@RequestAttribute(value = "shopNumber", required = true) String shopNumber) {
		JSONObject response = new JSONObject();
		response.put("status", "success");
		response.put("data", buyerService.getBuyerList(shopNumber));
		return response;
	}

	@PostMapping
	public JSONObject getbuyer(@RequestAttribute(value = "shopNumber", required = true) String shopNumber,
			@RequestBody JSONObject body) {
		System.out.println("Update buyer Request :" + body.toString());
		buyerService.createBuyer(shopNumber, body);
		JSONObject response = new JSONObject();
		response.put("status", "success");
		return response;
	}

	@PutMapping
	public JSONObject updateBuyer(@RequestAttribute(value = "shopNumber", required = true) String shopNumber,
			@RequestBody JSONObject request) {
		try {
			System.out.println("Logger shopNumber : " + shopNumber + " request : " + request.toString());
			buyerService.updateSingleBuyer(shopNumber, request);
		} catch (Exception e) {
			e.printStackTrace();
		}
		JSONObject response = new JSONObject();
		response.put("status", "success");
		return response;
	}

	@PutMapping("/adjustment")
	public JSONObject adjustBuyer(@RequestAttribute(value = "shopNumber", required = true) String shopNumber,
			@RequestBody JSONObject request) {
		try {
			System.out.println("Logger shopNumber : " + shopNumber + " request : " + request.toString());
			buyerService.adjustSingleBuyer(shopNumber, request);
		} catch (Exception e) {
			e.printStackTrace();
		}
		JSONObject response = new JSONObject();
		response.put("status", "success");
		return response;
	}

	@GetMapping("/journal")
	public JSONObject getJournal(@RequestAttribute(value = "shopNumber") String shopNumber,
			@RequestParam(value = "buyerID", required = false) String buyerID,
			@RequestParam(value = "startDate", required = false) Long startDate,
			@RequestParam(value = "endDate", required = false) Long endDate) {
		JSONObject response = new JSONObject();
		response.put("status", "success");

		if (startDate == null) {
			startDate = 0L;
		}
		if (endDate == null) {
			endDate = System.currentTimeMillis() / 1000L;
		}

		System.out.println("Journal Request : " + shopNumber + " buyerID : " + buyerID + " startDate : " + startDate
				+ " endDate : " + endDate);
		response.put("data", buyerService.getBuyerJournal(buyerID, startDate, endDate));
		return response;
	}

	@GetMapping("/journal/details")
	public JSONObject getJournalDetails(@RequestAttribute(value = "shopNumber") String shopNumber,
			@RequestParam(value = "buyerID", required = true) String buyerID,
			@RequestParam(value = "saleDate", required = true) Long saleDate) {
		JSONObject response = new JSONObject();
		response.put("status", "success");
		System.out.println("Journal Request : " + shopNumber + " buyerID : " + buyerID + " startDate : " + saleDate);
		return buyerService.getBuyerJournalDetails(shopNumber, buyerID, saleDate);
	}

	@GetMapping("/journal/today")
	public JSONObject getTodayBuyerJournal(@RequestAttribute(value = "shopNumber") String shopNumber,
			@RequestParam(value = "startDate", required = false) Long startDate,
			@RequestParam(value = "endDate", required = false) Long endDate) {
		JSONObject response = new JSONObject();
		response.put("status", "success");
		System.out.println("Today's Buyer Journal Request : " + shopNumber + " startDate : " + startDate + " endDate : "
				+ endDate);
		response.put("data", buyerService.getTodayAllBuyerJournal(shopNumber, startDate, endDate));
		return response;
	}

	@DeleteMapping
	public JSONObject deleteBuyer(@RequestAttribute(value = "shopNumber", required = true) String shopNumber,
			@RequestParam(value = "buyerID", required = true) String buyerID) {
		System.out.println("Buyer Delete Request : " + shopNumber + " buyerID : " + buyerID);
		JSONObject response = new JSONObject();
		buyerService.deleteBuyer(shopNumber, buyerID);
		response.put("status", "success");
		return response;
	}

}

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

import com.bookurmedical.service.BloomFarmerService;

@RestController
@RequestMapping("/farmer")
public class BloomFarmerController {

	@Autowired
	BloomFarmerService farmerService;

	@GetMapping
	public JSONObject getFarmers(@RequestAttribute(value = "shopNumber", required = true) String shopNumber) {
		JSONObject response = new JSONObject();
		response.put("status", "success");
		response.put("data", farmerService.getFarmerList(shopNumber));
		return response;
	}

	@PostMapping
	public JSONObject Farmers(@RequestAttribute(value = "shopNumber", required = true) String shopNumber,
			@RequestBody JSONObject body) {
		System.out.println("Bloom Create Flower Request");
		farmerService.createFarmer(shopNumber, body);
		JSONObject response = new JSONObject();
		response.put("status", "success");
		return response;
	}

	@PutMapping
	public JSONObject updateProductMaster(@RequestAttribute(value = "shopNumber", required = true) String shopNumber,
			@RequestBody JSONObject request) {
		try {
			System.out.println("Logger shopNumber : " + shopNumber + " request : " + request.toString());
			farmerService.updateSingleFarmer(shopNumber, request);
		} catch (Exception e) {
			e.printStackTrace();
		}
		JSONObject response = new JSONObject();
		response.put("status", "success");
		return response;
	}

	@DeleteMapping
	public JSONObject deleteFarmer(@RequestAttribute(value = "shopNumber", required = true) String shopNumber,
			@RequestParam(value = "farmerID", required = true) String farmerID) {
		farmerService.deleteFarmer(shopNumber, farmerID);
		JSONObject response = new JSONObject();
		response.put("status", "success");
		return response;
	}

	@GetMapping("/journal")
	public JSONObject getJournal(@RequestAttribute(value = "shopNumber") String shopNumber,
			@RequestParam(value = "farmerID", required = false) String farmerID,
			@RequestParam(value = "startDate", required = false) Long startDate,
			@RequestParam(value = "endDate", required = false) Long endDate) {
		JSONObject response = new JSONObject();
		response.put("status", "success");
		System.out.println("Journal Request : " + shopNumber + " farmerID : " + farmerID + " startDate : " + startDate
				+ " endDate : " + endDate);
		response.put("data", farmerService.getFarmerJournal(shopNumber, farmerID, startDate, endDate));
		return response;
	}

	@GetMapping("/journal/details")
	public JSONObject getJournalDetails(@RequestAttribute(value = "shopNumber") String shopNumber,
			@RequestParam(value = "farmerID", required = true) String farmerID,
			@RequestParam(value = "saleDate", required = true) Long saleDate) {
		JSONObject response = new JSONObject();
		response.put("status", "success");
		System.out.println("Journal Request : " + shopNumber + " farmerID : " + farmerID + " startDate : " + saleDate);
		return farmerService.getFarmerJournalDetails(shopNumber, farmerID, saleDate);
	}

	@GetMapping("/journal/today")
	public JSONObject getTodayFarmerJournal(@RequestAttribute(value = "shopNumber") String shopNumber,
			@RequestParam(value = "startDate", required = false) Long startDate,
			@RequestParam(value = "endDate", required = false) Long endDate) {
		JSONObject response = new JSONObject();
		response.put("status", "success");
		System.out.println("Today's Farmer Journal Request : " + shopNumber + " startDate : " + startDate
				+ " endDate : " + endDate);
		response.put("data", farmerService.getTodayAllFarmerJournal(shopNumber, startDate, endDate));
		return response;
	}

}

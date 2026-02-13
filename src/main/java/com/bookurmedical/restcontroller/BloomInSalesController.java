package com.bookurmedical.restcontroller;

import org.bson.Document;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bookurmedical.service.BloomFarmerService;
import com.bookurmedical.service.BloomSalesService;

@RestController
@RequestMapping("/sales")
public class BloomInSalesController {

	@Autowired
	BloomSalesService salesService;

	@Autowired
	BloomFarmerService BloomFarmerService;

	@GetMapping
	public JSONObject getSales(@RequestAttribute(value = "shopNumber") String shopNumber,
			@RequestParam(value = "salesPersonID", required = false) Integer salesPersonID,
			@RequestParam(value = "buyerID", required = false) String buyerID,
			@RequestParam(value = "farmerID", required = false) String farmerID,
			@RequestParam(value = "startDate", required = false) Long startDate,
			@RequestParam(value = "endDate", required = false) Long endDate) {
		JSONObject response = new JSONObject();
		response.put("status", "success");
		response.put("data",
				salesService.getSalesList(shopNumber, buyerID, farmerID, startDate, endDate, salesPersonID));
		return response;
	}

	@PostMapping
	public JSONObject addNewSales(@RequestAttribute(value = "shopNumber", required = true) String shopNumber,
			@RequestBody JSONObject body) {
		System.out.println("Bloom Create Sales Request " + body.toString());

		if (body.has("isrunningCustomer") && body.getBoolean("isrunningCustomer") == true) {
			JSONObject farmerObj = new JSONObject();
			farmerObj.put("name", body.getString("farmerName"));
			Document newFarmer = BloomFarmerService.createFarmer(shopNumber, farmerObj);
			body.put("farmerID", newFarmer.getObjectId("_id").toHexString());
		}
		salesService.addNewSales(shopNumber, body);
		JSONObject response = new JSONObject();
		response.put("status", "success");
		return response;
	}

	@PutMapping
	public JSONObject updateProductMaster(@RequestAttribute(value = "shopNumber", required = true) String shopNumber,
			@RequestBody JSONObject request) {
		try {
			System.out.println("Logger shopNumber : " + shopNumber + " request : " + request.toString());
			salesService.updateSingleSales(shopNumber, request);
		} catch (Exception e) {
			e.printStackTrace();
		}
		JSONObject response = new JSONObject();
		response.put("status", "success");
		return response;
	}

	@GetMapping("/salesman")
	public JSONObject getSalesMan(@RequestAttribute(value = "shopNumber") String shopNumber) {
		JSONObject response = new JSONObject();
		response.put("status", "success");
		response.put("data", salesService.getSalesManList(shopNumber));
		return response;
	}

	@GetMapping("/farmer-ledger")
	public JSONObject getFarmerLedgerSales(@RequestAttribute(value = "shopNumber") String shopNumber,
			@RequestParam(value = "farmerID", required = true) String farmerID,
			@RequestParam(value = "startDate", required = false) Long startDate,
			@RequestParam(value = "endDate", required = false) Long endDate) {
		JSONObject response = new JSONObject();
		response.put("status", "success");
		response.put("data",
				salesService.getFarmerLedgerSales(shopNumber, farmerID, startDate, endDate));
		return response;
	}

	@PutMapping("/buyer-sales")
	public JSONObject updateBuyerSales(@RequestAttribute(value = "shopNumber", required = true) String shopNumber,
			@RequestBody JSONObject body) {
		System.out.println("Bloom Update Buyer Sales Request " + body.toString());
		salesService.updateBuyerSales(shopNumber, body);
		JSONObject response = new JSONObject();
		response.put("status", "success");
		return response;
	}

}

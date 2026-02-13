package com.bookurmedical.restcontroller;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bookurmedical.service.BloomBuyerService;
import com.bookurmedical.service.BloomDaySummaryService;
import com.bookurmedical.service.BloomSalesService;

@RestController
@RequestMapping("/summary")
public class BloomInSummaryController {

	@Autowired
	BloomSalesService salesService;

	@Autowired
	BloomBuyerService buyerService;

	@Autowired
	BloomDaySummaryService daySummaryService;

	@GetMapping("/sales")
	public JSONObject getSalesSummary(@RequestAttribute(value = "shopNumber") String shopNumber,
			@RequestParam(value = "saleDate", required = true) Long saleDate) {
		JSONObject response = new JSONObject();
		response.put("status", "success");
		response.put("data", salesService.getShopDailySummary(shopNumber, saleDate));
		return response;
	}

	@GetMapping("/buyer")
	public JSONObject getBuyerSummary(@RequestAttribute(value = "shopNumber") String shopNumber,
			@RequestParam(value = "saleDate", required = true) Long saleDate) {
		JSONObject response = new JSONObject();
		response.put("status", "success");
		response.put("data", buyerService.getShopBuyerPayments(shopNumber, saleDate));
		return response;
	}

	@PostMapping
	public JSONObject addDaySummary(@RequestAttribute(value = "shopNumber") String shopNumber,@RequestBody JSONObject requestBody) {
		JSONObject response = new JSONObject();
		response.put("status", "success");
		daySummaryService.addNewSummary(shopNumber, requestBody);
		return response;
	}

	@GetMapping
	public JSONObject getDaySummary(@RequestAttribute(value = "shopNumber") String shopNumber,
			@RequestParam(value = "saleDate", required = true) Long saleDate) {
		JSONObject response = new JSONObject();
		response.put("status", "success");
		response.put("data", daySummaryService.getDaySummary(shopNumber, saleDate));
		return response;
	}

}

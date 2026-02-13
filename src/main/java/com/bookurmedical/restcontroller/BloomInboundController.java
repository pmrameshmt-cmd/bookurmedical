package com.bookurmedical.restcontroller;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bookurmedical.service.BloomInboundService;

@RestController
@RequestMapping("/inbound")
public class BloomInboundController {

	@Autowired
	BloomInboundService inboundService;

	@GetMapping
	public JSONObject getInbound(@RequestAttribute(value = "shopNumber", required = true) String shopNumber) {
		JSONObject response = new JSONObject();
		response.put("status", "success");
		response.put("data", inboundService.getInboundList(shopNumber));
		return response;
	}

	@PostMapping
	public JSONObject addNewInbound(@RequestAttribute(value = "shopNumber", required = true) String shopNumber,
			@RequestBody JSONObject body) {
		System.out.println("Bloom Create inbound Request");
		inboundService.addNewInbound(shopNumber, body);
		JSONObject response = new JSONObject();
		response.put("status", "success");
		return response;
	}

	@PutMapping
	public JSONObject updateProductMaster(@RequestAttribute(value = "shopNumber", required = true) String shopNumber,
			@RequestBody JSONObject request) {
		try {
			System.out.println("Logger shopNumber : " + shopNumber + " request : " + request.toString());
			inboundService.updateSingleInbound(shopNumber, request);
		} catch (Exception e) {
			e.printStackTrace();
		}
		JSONObject response = new JSONObject();
		response.put("status", "success");
		return response;
	}

}

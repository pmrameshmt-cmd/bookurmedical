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

import com.bookurmedical.service.BloomFlowerService;


@RestController
@RequestMapping("/flower")
public class BloomFlowerController {

	@Autowired
	BloomFlowerService flowerService;

	@GetMapping
	public JSONObject getFlowers(@RequestAttribute(value = "shopNumber", required = true) String shopNumber) {
		JSONObject response = new JSONObject();
		response.put("status", "success");
		response.put("data", flowerService.getFlowerList(shopNumber));
		return response;
	}

	@PostMapping
	public JSONObject addFlower(@RequestAttribute(value = "shopNumber", required = true) String shopNumber,
			@RequestBody JSONObject body) {
		System.out.println("Bloom Create Flower Request");
		flowerService.createFlower(shopNumber, body);
		JSONObject response = new JSONObject();
		response.put("status", "success");
		return response;
	}

	@PutMapping
	public JSONObject updateProductMaster(@RequestAttribute(value = "shopNumber", required = true) String shopNumber,
			@RequestBody JSONObject request) {
		try {
			System.out.println("Logger shopNumber : " + shopNumber + " request : " + request.toString());
			flowerService.updateSingleFlower(shopNumber, request);
		} catch (Exception e) {
			e.printStackTrace();
		}
		JSONObject response = new JSONObject();
		response.put("status", "success");
		return response;
	}

}

package com.example.demo.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dao.HeaderDao;
import com.example.demo.dao.ItemDao;
import com.example.demo.dao.OrderDao;
import com.example.demo.dao.OriginOrderDao;
import com.example.demo.service.TaskService;

@RestController
@RequestMapping("/RESTAdapter")
public class MainController {

	@Autowired
	TaskService taskService;

	@PostMapping(value = "/saveOrder", consumes = MediaType.APPLICATION_XML_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, Object> saveOrder(@RequestBody OriginOrderDao orderRequest) {

		Map<String, Object> response = new HashMap<>();
		List<OrderDao> finalOrderList = new ArrayList<>();

		try {
			// 1. 데이터 매핑 로직
			List<HeaderDao> headers = orderRequest.getHeaders();
			List<ItemDao> items = orderRequest.getItems();

			if (headers == null || items == null) {
				throw new Exception("XML 데이터 형식이 올바르지 않습니다. (HEADER/ITEM 누락)");
			}

			for (HeaderDao header : headers) {
				List<ItemDao> userItems = items.stream()
						.filter(item -> item.getUserId() != null && item.getUserId().equals(header.getUserId()))
						.collect(Collectors.toList());

				for (ItemDao item : userItems) {
					OrderDao order = new OrderDao();
					order.setUser_id(header.getUserId());
					order.setName(header.getName());
					order.setAddress(header.getAddress());
					order.setStatus(header.getStatus());
					order.setItem_id(item.getItemId());
					order.setItem_name(item.getItemName());
					order.setPrice(item.getPrice());
					finalOrderList.add(order);
				}
			}

			// 2. 서비스 호출 (DB 저장 및 파일 생성)
			taskService.saveOrder(finalOrderList);

			// 3. 성공 시 JSON 응답 구성
			response.put("result", "SUCCESS");
			response.put("message", "주문 저장 및 파일 생성이 완료되었습니다.");
			response.put("count", finalOrderList.size());

		} catch (Exception e) {
			// 4. 실패 시(예외 발생 시) JSON 응답 구성
			response.put("result", "FAIL");
			response.put("message", "오류 발생: " + e.getMessage());
			// 로그 출력
			e.printStackTrace();
		}

		return response; // Map을 반환하면 Jackson 라이브러리가 자동으로 JSON으로 변환합니다.
	}
}

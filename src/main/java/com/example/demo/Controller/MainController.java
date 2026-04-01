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
import com.example.demo.dao.RecrutingTestDao;
import com.example.demo.service.MonitoringLogService;
import com.example.demo.service.TaskService;

import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import com.example.demo.service.DecryptMsgUtil;

@RestController
@RequestMapping("/RESTAdapter")
public class MainController {

	@Autowired
	TaskService taskService;

	@Autowired
	MonitoringLogService logService;

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

			// 성공 로그 기록
			logService.writeLog("API_SAVE_ORDER", "SUCCESS", "주문 건수: " + finalOrderList.size());

		} catch (Exception e) {
			// 4. 실패 시(예외 발생 시) JSON 응답 구성
			response.put("result", "FAIL");
			response.put("message", "오류 발생: " + e.getMessage());

			// 실패 로그 기록
			logService.writeLog("API_SAVE_ORDER", "FAIL", "에러: " + e.getMessage());

			// 로그 출력
			e.printStackTrace();
		}

		return response; // Map을 반환하면 Jackson 라이브러리가 자동으로 JSON으로 변환합니다.
	}

	@GetMapping("/RecrutingTest")
	public RecrutingTestDao recrutingTest() {

		// 1. 내 정보 설정
		RecrutingTestDao requestDto = new RecrutingTestDao();
		requestDto.setNAME("김윤호");
		requestDto.setPHONE_NUMBER("010-2272-3969");
		requestDto.setE_MAIL("h3969@naver.com");

		// 2. 외부 API 호출 (POST)
		String url = "http://211.106.171.36:50000/RESTAdapter/RecruitingTest";
		RestTemplate restTemplate = new RestTemplate();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<RecrutingTestDao> entity = new HttpEntity<>(requestDto, headers);

		// API 호출 및 결과 수신
		RecrutingTestDao response = restTemplate.postForObject(url, entity, RecrutingTestDao.class);

		// 3. 응답 데이터 복호화 처리
		if (response != null) {
		    try {
		        javax.crypto.spec.SecretKeySpec key = DecryptMsgUtil.generateMySQLKey(requestDto.getPHONE_NUMBER());

		        // 1. ORDER_TB_CONN 복호화 (Map 내부의 모든 값 복호화)
		        if (response.getORDER_TB_CONN() != null) {
		            Map<String, String> decryptedMap = new HashMap<>();
		            for (Map.Entry<String, String> entry : response.getORDER_TB_CONN().entrySet()) {
		                decryptedMap.put(entry.getKey(), DecryptMsgUtil.decryptAES(entry.getValue(), key));
		            }
		            response.setDecryptedOrderConn(decryptedMap);
		        }

		        // 2. SHIPMENT_TB_CONN 복호화
		        if (response.getSHIPMENT_TB_CONN() != null) {
		            Map<String, String> decryptedMap = new HashMap<>();
		            for (Map.Entry<String, String> entry : response.getSHIPMENT_TB_CONN().entrySet()) {
		                decryptedMap.put(entry.getKey(), DecryptMsgUtil.decryptAES(entry.getValue(), key));
		            }
		            response.setDecryptedShipmentConn(decryptedMap);
		        }

		        // 3. FTP_CONN 복호화
		        if (response.getFTP_CONN() != null) {
		            Map<String, String> decryptedMap = new HashMap<>();
		            for (Map.Entry<String, String> entry : response.getFTP_CONN().entrySet()) {
		                decryptedMap.put(entry.getKey(), DecryptMsgUtil.decryptAES(entry.getValue(), key));
		            }
		            response.setDecryptedFtpConn(decryptedMap);
		        }

		        // 4. SAMPLE_DATA 디코딩
		        response.setDecodedXml(DecryptMsgUtil.decodeSampleData(response.getSAMPLE_DATA()));

		        logService.writeLog("RECRUITING_TEST", "SUCCESS", "데이터 수신 및 맵 기반 복호화 완료");

		    } catch (Exception e) {
		        logService.writeLog("RECRUITING_TEST", "FAIL", "복호화 에러: " + e.getMessage());
		        e.printStackTrace();
		    }
		}

		return response; // 브라우저에 복호화된 최종 결과가 JSON으로 출력됨
	}
}

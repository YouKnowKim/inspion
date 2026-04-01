package com.example.demo.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dao.OrderDao;
import com.example.demo.mapper.TaskMapper;
import com.example.demo.service.MonitoringLogService;

@Component
public class BatchController {
	
	@Autowired
    private TaskMapper taskMapper;
	
	@Autowired
    private MonitoringLogService logService;
	
	private static final String APPLICANT_KEY = "KIMYUNHO";

    /**
     * 5분 주기로 실행 (cron = "초 분 시 일 월 요일")
     * "0 0/5 * * * ?" -> 5분마다 실행
     */
    @Scheduled(cron = "0 0/5 * * * ?")
    @Transactional(rollbackFor = Exception.class) // 오류 발생 시 DB 롤백
    public void scheduleOrderMigration() {
        System.out.println("--- 배송 정보 이관 배치 시작 ---");

        // 1. 신규 주문('N') 조회
        List<OrderDao> newOrders = taskMapper.selectNewOrders();

        if (newOrders == null || newOrders.isEmpty()) {
            System.out.println("이관할 신규 주문이 없습니다.");
            return;
        }
        
        // DB에 있는 shipmentID 조회
        String lastId = taskMapper.selectMaxShipmentId(APPLICANT_KEY);

        int count = 0;
        for (OrderDao order : newOrders) {
            try {
            	// 1. shipmentId 채번
            	lastId = getNextShipmentId(lastId);
            	order.setShipment_id(lastId);
            	
                // 2. 운송 테이블로 복사
                taskMapper.insertShipment(order);

                // 3. 주문 테이블 상태 업데이트 ('N' -> 'Y')
                taskMapper.updateOrderStatus(order);
                
                count++;
            } catch (Exception e) {
                System.err.println("오류 발생 (Order ID: " + order.getOrder_id() + "): " + e.getMessage());
                
                // 실패 로그 기록
                logService.writeLog("BATCH_MIGRATION", "FAIL", "배치 중단 에러: " + e.getMessage());
                // 개별 행 오류 시 로그만 남기고 다음 행 진행 (전체 롤백 원하면 throw e)
            }
        }

        System.out.println("배치 완료: 총 " + count + "건 이관됨");
        System.out.println("--- 배송 정보 이관 배치 종료 ---");
        
        // 완료 로그 기록
        logService.writeLog("BATCH_MIGRATION", "SUCCESS", "총 " + count + "건 이관 완료");
    }
    
    // 마지막 ID를 받아 다음 순번의 ID를 반환
    // A001 -> A002, A999 -> B001
    private String getNextShipmentId(String lastId) {
        // 만약 처음 저장하는 거라 ID가 없다면 A001로 시작
        if (lastId == null || lastId.isEmpty()) {
            return "A001";
        }

        char letter = lastId.charAt(0); // 알파벳 (A)
        int number = Integer.parseInt(lastId.substring(1)); // 숫자 (001 -> 1)

        number++; // 숫자 1 증가

        // 숫자가 999를 넘어가면 알파벳을 다음 글자로 바꾸고 숫자는 001로 리셋
        if (number > 999) {
            letter++; 
            number = 1;
        }

        // 만약 Z999를 넘어가면 처리가 필요, 현재는 넘어가지 않는다고 가정
        return String.format("%c%03d", letter, number);
    }
}

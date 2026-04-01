package com.example.demo.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dao.OrderDao;
import com.example.demo.mapper.TaskMapper;

@Service
public class TaskService {

	@Autowired
	TaskMapper taskMapper;
	
	// 지원자 키
    private static final String APPLICANT_KEY = "KIMYUNHO";
    private static final String PARTICIPANT_NAME = "김윤호";

    /**
     * 주문 저장 및 파일 생성
     * @Transactional(rollbackFor = Exception.class): 예외 발생 시 DB 롤백
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer saveOrder(List<OrderDao> orderDaoList) throws Exception {
        File createdFile = null;

        try {
            // 1. DB에서 현재 가장 마지막에 생성된 ID를 가져옴
            String lastId = taskMapper.selectMaxOrderId(APPLICANT_KEY);

            for (OrderDao order : orderDaoList) {
                // 2. 마지막 ID를 기준으로 다음 ID 순차 생성 (A001, A002...)
                lastId = getNextOrderId(lastId);

                order.setOrder_id(lastId);
                order.setApplicant_key(APPLICANT_KEY);

                // DB 저장 (하나씩 수행)
                taskMapper.insertOrder(order);
            }

            // 3. 모든 데이터가 DB에 정상 세팅된 후 텍스트 파일 생성
            createdFile = createTxtFile(orderDaoList);

            // 테스트용: 강제 에러 발생시키려면 아래 주석 해제 (DB와 파일 모두 생성 안됨)
             if(true) throw new RuntimeException("트랜잭션 테스트");

        } catch (Exception e) {
            // 파일이 생성된 도중에 오류가 났다면 생성된 파일 삭제 (파일 롤백)
            if (createdFile != null && createdFile.exists()) {
                createdFile.delete();
            }
            // 예외를 다시 던져서 @Transactional이 DB를 롤백하게 함
            throw e;
        }

        return orderDaoList.size();
    }

    /**
     * 텍스트 파일 생성 로직
     */
    private File createTxtFile(List<OrderDao> orderDaoList) throws Exception {
        // 파일명: INSPIEN_참여자명_yyyyMMddHHmmss.txt
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String fileName = String.format("INSPIEN_%s_%s.txt", PARTICIPANT_NAME, timeStamp);

        // 경로: src/main/resources/ftpfile (프로젝트 상대 경로 기준)
        String path = new File("src/main/resources/ftpfile").getAbsolutePath();
        File dir = new File(path);
        if (!dir.exists()) dir.mkdirs();

        File file = new File(dir, fileName);

        // 내용 작성 (UTF-8)
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            for (OrderDao order : orderDaoList) {
                // 파일 내용 규칙 : ORDER_ID^USER_ID^ITEM_ID^APPLICANT_KEY^NAME^ADDRESS^ITEM_NAME^PRICE
                String line = String.format("%s^%s^%s^%s^%s^%s^%s^%s",
                        order.getOrder_id(),
                        order.getUser_id(),
                        order.getItem_id(),
                        order.getApplicant_key(),
                        order.getName(),
                        order.getAddress(),
                        order.getItem_name(),
                        order.getPrice());

                writer.write(line);
                writer.newLine(); // 줄바꿈
            }
        } catch (Exception e) {
            // 파일 쓰기 중 에러 시 파일 삭제 후 예외 던짐
            if (file.exists()) file.delete();
            throw e;
        }

        return file;
    }
	

    // 마지막 ID를 받아 다음 순번의 ID를 반환
    // A001 -> A002, A999 -> B001
    private String getNextOrderId(String lastId) {
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

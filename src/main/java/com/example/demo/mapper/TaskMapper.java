package com.example.demo.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import com.example.demo.dao.OrderDao;

@Mapper
@Repository
public interface TaskMapper {
	
	// orderID 채번
	String selectMaxOrderId(String applicantKey);
	
	// 주문 저장
	Integer insertOrder(OrderDao orderDao);
	
	// 이관 대상 조회 (status 가 'N'인 것만)
	List<OrderDao> selectNewOrders();
	
	// 운송 데이터 저장
    int insertShipment(OrderDao orderDao);

    // 주문 테이블 상태 변경 ('N' -> 'Y')
    int updateOrderStatus(OrderDao orderDao);
    
    // shipmentID 채번
    String selectMaxShipmentId(String applicantKey);

}

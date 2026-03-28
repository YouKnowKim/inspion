package com.example.demo.mapper;

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

}

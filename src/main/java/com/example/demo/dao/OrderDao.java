package com.example.demo.dao;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class OrderDao {
	
	String order_id;
	String applicant_key;
	String user_id;
	String item_id;
	String name;
	String address;
	String item_name;
	BigDecimal price;
	String status;
}

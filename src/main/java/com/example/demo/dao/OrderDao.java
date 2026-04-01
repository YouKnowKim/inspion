package com.example.demo.dao;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class OrderDao {
	
	private String shipment_id;
	private String order_id;
	private String applicant_key;
	private String user_id;
	private String item_id;
	private String name;
	private String address;
	private String item_name;
	private BigDecimal price;
	private String status;
}

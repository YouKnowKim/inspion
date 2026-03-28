package com.example.demo.dao;

import java.math.BigDecimal;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.Data;

@Data
public class ItemDao {
	@JacksonXmlProperty(localName = "USER_ID") private String userId;
    @JacksonXmlProperty(localName = "ITEM_ID") private String itemId;
    @JacksonXmlProperty(localName = "ITEM_NAME") private String itemName;
    @JacksonXmlProperty(localName = "PRICE") private BigDecimal price;
}

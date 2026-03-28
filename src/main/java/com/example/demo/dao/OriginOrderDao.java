package com.example.demo.dao;

import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.Data;

@Data
public class OriginOrderDao {
	@JacksonXmlProperty(localName = "HEADER")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<HeaderDao> headers;

    @JacksonXmlProperty(localName = "ITEM")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<ItemDao> items;
}

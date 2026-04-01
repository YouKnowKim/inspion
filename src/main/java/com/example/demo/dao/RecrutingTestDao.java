package com.example.demo.dao;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.Map;

@Data
public class RecrutingTestDao {
    @JsonProperty("NAME")
    private String NAME;

    @JsonProperty("PHONE_NUMBER")
    private String PHONE_NUMBER;

    @JsonProperty("E_MAIL")
    private String E_MAIL;

    @JsonProperty("APPLICANT_KEY")
    private String APPLICANT_KEY;

    // String 대신 Map으로 변경하여 객체 구조를 받아냅니다.
    @JsonProperty("ORDER_TB_CONN")
    private Map<String, String> ORDER_TB_CONN;

    @JsonProperty("SHIPMENT_TB_CONN")
    private Map<String, String> SHIPMENT_TB_CONN;

    @JsonProperty("FTP_CONN")
    private Map<String, String> FTP_CONN;

    @JsonProperty("SAMPLE_DATA")
    private String SAMPLE_DATA;

    // 복호화된 결과들을 담을 Map (추가)
    private Map<String, String> decryptedOrderConn;
    private Map<String, String> decryptedShipmentConn;
    private Map<String, String> decryptedFtpConn;
    private String decodedXml;
}
package com.example.demo.service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

public class DecryptMsgUtil {

    /**
     * 1. Key 생성 규칙 적용
     * 휴대폰 번호 -> UTF-8 바이트 -> SHA-1 해싱 -> 앞 16바이트 추출
     */
    public static SecretKeySpec generateMySQLKey(String phoneNumber) throws Exception {
        // SHA-1 해시 생성
        MessageDigest sha = MessageDigest.getInstance("SHA-1");
        byte[] key = phoneNumber.getBytes("UTF-8");
        key = sha.digest(key);
        
        // 앞 16바이트(128비트)만 사용
        byte[] aesKey = Arrays.copyOf(key, 16);
        return new SecretKeySpec(aesKey, "AES");
    }

    /**
     * 2. AES-128 (ECB Mode, PKCS5Padding) 복호화
     */
    public static String decryptAES(String encryptedBase64, SecretKeySpec secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        
        // Base64 디코딩 후 복호화
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedBase64);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        
        return new String(decryptedBytes, "UTF-8");
    }

    /**
     * 3. SAMPLE_DATA 처리 (Base64 Decode + EUC-KR 변환)
     */
    public static String decodeSampleData(String base64Xml) throws Exception {
        byte[] decodedBytes = Base64.getDecoder().decode(base64Xml);
        return new String(decodedBytes, "EUC-KR");
    }

    public static void main(String[] args) {
        try {
            String myPhone = "010-2272-3969"; // 본인 휴대폰 번호
            SecretKeySpec keySpec = generateMySQLKey(myPhone);

            // 예시: API로부터 받은 암호화된 문자열을 아래 변수에 넣으세요.
            String encryptedOrderConn = "API에서_받은_ORDER_TB_CONN_값";
            String sampleDataRaw = "API에서_받은_SAMPLE_DATA_값";

            // 복호화 실행
            // String decryptedOrder = decryptAES(encryptedOrderConn, keySpec);
            // String xmlContent = decodeSampleData(sampleDataRaw);

            System.out.println("비밀키 생성 완료 (AES-128)");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
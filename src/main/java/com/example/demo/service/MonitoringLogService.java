package com.example.demo.service;

import org.springframework.stereotype.Service;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class MonitoringLogService {

    private static final String LOG_DIR = "src/main/resources/logs";
    private static final String LOG_FILE_NAME = "monitoring.log";

    public void writeLog(String requestType, String status, String message) {
        File dir = new File(LOG_DIR);
        if (!dir.exists()) dir.mkdirs();

        File logFile = new File(dir, LOG_FILE_NAME);
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        // 로그 형식: [시간] [요청유형] [상태] 메시지
        String logEntry = String.format("[%s] [%s] [%s] %s%n", timeStamp, requestType, status, message);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
            writer.write(logEntry);
        } catch (IOException e) {
            System.err.println("로그 파일 기록 실패: " + e.getMessage());
        }
    }
}
package com.company.dotaadminbackend.application;

import com.company.dotaadminbackend.domain.payment.PaymentHistory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

@Service
@Transactional
public class PaymentHistoryService {
    
    private final ObjectMapper objectMapper;
    
    public PaymentHistoryService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    public PaymentHistory savePaymentHistory(Map<String, Object> paymentData) {
        // 기본 필수 필드들
        String paymentId = (String) paymentData.get("paymentId");
        String transactionType = (String) paymentData.get("transactionType");
        String txId = (String) paymentData.get("txId");
        
        if (paymentId == null || transactionType == null || txId == null) {
            throw new IllegalArgumentException("Required fields are missing: paymentId, transactionType, txId");
        }
        
        // 도메인 엔티티 생성
        PaymentHistory paymentHistory = new PaymentHistory(paymentId, transactionType, txId);
        
        // 추가 정보가 있으면 설정
        BigDecimal amount = extractBigDecimal(paymentData.get("amount"));
        Long challengeId = extractLong(paymentData.get("challengeId"));
        Long participantId = extractLong(paymentData.get("participantId"));
        String rawData = convertToJson(paymentData);
        
        paymentHistory.updateAdditionalInfo(amount, challengeId, participantId, rawData);
        
        // TODO: Repository로 저장 (나중에 구현)
        return paymentHistory;
    }
    
    private BigDecimal extractBigDecimal(Object value) {
        if (value == null) return null;
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    private Long extractLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.valueOf(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    private String convertToJson(Object data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            return "{}";
        }
    }
}
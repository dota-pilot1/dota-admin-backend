package com.company.dotaadminbackend.web;

import com.company.dotaadminbackend.application.PaymentHistoryService;
import com.company.dotaadminbackend.domain.payment.PaymentHistory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment-histories")
public class PaymentHistoryController {
    
    private final PaymentHistoryService paymentHistoryService;
    
    public PaymentHistoryController(PaymentHistoryService paymentHistoryService) {
        this.paymentHistoryService = paymentHistoryService;
    }
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> savePaymentHistory(@RequestBody Map<String, Object> request) {
        try {
            PaymentHistory paymentHistory = paymentHistoryService.savePaymentHistory(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Payment history saved successfully");
            response.put("paymentId", paymentHistory.getPaymentId());
            response.put("transactionType", paymentHistory.getTransactionType());
            response.put("txId", paymentHistory.getTxId());
            response.put("status", paymentHistory.getStatus().name());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Internal server error");
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    @GetMapping("/{paymentId}")
    public ResponseEntity<Map<String, Object>> getPaymentHistory(@PathVariable String paymentId) {
        // TODO: Repository에서 조회 (나중에 구현)
        Map<String, Object> response = new HashMap<>();
        response.put("paymentId", paymentId);
        response.put("status", "PAID");
        response.put("message", "Payment history found (mock data)");
        
        return ResponseEntity.ok(response);
    }
}
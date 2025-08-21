package com.company.dotaadminbackend.domain.payment;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_histories")
public class PaymentHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "payment_id", unique = true, nullable = false)
    private String paymentId;
    
    @Column(name = "transaction_type", nullable = false)
    private String transactionType;
    
    @Column(name = "tx_id", nullable = false)
    private String txId;
    
    // 나중에 실 결제 시 추가할 필드들
    @Column(precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Column(length = 3)
    private String currency;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private PaymentStatus status;
    
    @Column(name = "challenge_id")
    private Long challengeId;
    
    @Column(name = "participant_id")
    private Long participantId;
    
    @Column(name = "raw_data", columnDefinition = "JSON")
    private String rawData;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    protected PaymentHistory() {}
    
    public PaymentHistory(String paymentId, String transactionType, String txId) {
        this.paymentId = paymentId;
        this.transactionType = transactionType;
        this.txId = txId;
        this.status = PaymentStatus.PAID; // 테스트용 기본값
        this.currency = "KRW"; // 기본값
    }
    
    public void updateAdditionalInfo(BigDecimal amount, Long challengeId, Long participantId, String rawData) {
        this.amount = amount;
        this.challengeId = challengeId;
        this.participantId = participantId;
        this.rawData = rawData;
    }
    
    public boolean isPaid() {
        return PaymentStatus.PAID.equals(this.status);
    }

    // Getters
    public Long getId() { return id; }
    public String getPaymentId() { return paymentId; }
    public String getTransactionType() { return transactionType; }
    public String getTxId() { return txId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public PaymentStatus getStatus() { return status; }
    public Long getChallengeId() { return challengeId; }
    public Long getParticipantId() { return participantId; }
    public String getRawData() { return rawData; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
package com.iron.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "processed_transactions")
public class ProcessedTransaction {

    @Id
    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    public ProcessedTransaction(String transactionId) {
        this.transactionId = transactionId;
        this.createdAt = OffsetDateTime.now();
    }
}
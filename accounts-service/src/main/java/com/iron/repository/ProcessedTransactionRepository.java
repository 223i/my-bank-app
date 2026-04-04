package com.iron.repository;

import com.iron.model.ProcessedTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessedTransactionRepository extends JpaRepository<ProcessedTransaction, String> {
}
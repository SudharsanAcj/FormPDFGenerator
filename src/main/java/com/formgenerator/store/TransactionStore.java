package com.formgenerator.store;

import com.formgenerator.exception.TransactionNotFoundException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory transaction store.
 * Replace with Redis or a persistent store in production.
 */
@Component
public class TransactionStore {

    private final ConcurrentHashMap<String, TransactionRecord> store = new ConcurrentHashMap<>();

    public void save(TransactionRecord record) {
        record.setUpdatedAt(Instant.now());
        store.put(record.getTransactionId(), record);
    }

    public TransactionRecord get(String transactionId) {
        TransactionRecord record = store.get(transactionId);
        if (record == null) {
            throw new TransactionNotFoundException(transactionId);
        }
        return record;
    }

    public void updateStatus(String transactionId, TransactionStatus status) {
        TransactionRecord record = get(transactionId);
        record.setStatus(status);
        record.setUpdatedAt(Instant.now());
        store.put(transactionId, record);
    }

    public boolean exists(String transactionId) {
        return store.containsKey(transactionId);
    }
}

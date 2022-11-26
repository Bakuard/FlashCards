package com.bakuard.flashcards.service.util;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.concurrent.Callable;

public record Transaction(PlatformTransactionManager transactionManager) {

    public void commit(Runnable task) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(def);
        try {
            task.run();
            transactionManager.commit(status);
        } catch(Exception e) {
            transactionManager.rollback(status);
            throw new RuntimeException(e);
        }
    }

    public <T> T commit(Callable<T> task) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(def);
        try {
            T result = task.call();
            transactionManager.commit(status);
            return result;
        } catch(Exception e) {
            transactionManager.rollback(status);
            throw new RuntimeException(e);
        }
    }

}

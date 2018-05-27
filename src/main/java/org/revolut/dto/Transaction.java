package org.revolut.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Created by Leo on 5/27/2018.
 */
@Builder
public class Transaction {
    @Builder.Default
    @Getter
    private UUID uuid = UUID.randomUUID();
    @Getter
    private String fromAccountId;
    @Getter
    private String toAccountId;
    @Getter
    private BigDecimal amount;
    @Getter
    @Setter
    private TransactionStatus status;


    public enum TransactionStatus {
        PENDING,
        SUCCESS,
        FAILED
    }


}

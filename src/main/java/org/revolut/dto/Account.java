package org.revolut.dto;

import com.google.common.base.Preconditions;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Created by Leo on 5/27/2018.
 */
@EqualsAndHashCode(exclude = {"balance"})
@Builder(toBuilder = true)
public class Account {
    @Getter
    private String accountId;
    @Getter
    @Setter
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    public void withdrawal(BigDecimal amount) {
        BigDecimal result = balance.subtract(amount);
        validateNonNegativeResult(result);
        setBalance(result);
    }

    public void deposit(BigDecimal amount) {
        BigDecimal result = balance.add(amount);
        validateNonNegativeResult(result);
        setBalance(result);
    }

    private void validateNonNegativeResult(BigDecimal result) {
        Preconditions.checkArgument(BigDecimal.ZERO.compareTo(result) < 0, "Negative result");
    }
}

package org.revolut.validator;

import com.networknt.oas.validator.Messages;
import com.networknt.oas.validator.ValidationResults;

import java.math.BigDecimal;

/**
 * Created by Leo on 5/27/2018.
 */
public class AmountValidator implements Validator<String> {
    private static final int ALLOWED_DECIMAL_DIGITS = 2;

    public ValidationResults validate(String value) {
        return validateObject(value, new ValidationResults());
    }

    private ValidationResults validateObject(String value, ValidationResults results) {
        try {
            BigDecimal amount = new BigDecimal(value);

            int compareToResult = BigDecimal.ZERO.compareTo(amount);
            if (compareToResult == 0) {
                results.addError(Messages.m.msg("Zero amount", value), null);
            }

            if (compareToResult > 0) {
                results.addError(Messages.m.msg("Negative amount", value), null);
            }

            if (String.valueOf(amount.remainder(BigDecimal.ONE)).length() == ALLOWED_DECIMAL_DIGITS) {
                results.addError(Messages.m.msg("Invalid decimal part(cents) of amount", value), null);
            }

        } catch (NumberFormatException e) {
            results.addError(Messages.m.msg("Invalid number format", value), null);
        }
        return results;

    }

}

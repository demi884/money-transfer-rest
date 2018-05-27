package org.revolut.validator;

import com.networknt.oas.validator.ValidationResults;

/**
 * Created by Leo on 5/27/2018.
 */
public interface Validator<T> {
    ValidationResults validate(T t);
}

package io.littlehorse.examples.exceptions;

import io.littlehorse.sdk.common.exception.LHTaskException;

public class CustomerNotFoundException extends LHTaskException {
    public CustomerNotFoundException(String message) {
        super("customer-not-found",message);
    }
}

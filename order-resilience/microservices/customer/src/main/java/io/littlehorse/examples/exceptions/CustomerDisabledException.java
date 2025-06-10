package io.littlehorse.examples.exceptions;

import io.littlehorse.sdk.common.exception.LHTaskException;

public class CustomerDisabledException extends LHTaskException {
    public CustomerDisabledException(String message) {
        super("customer-disabled",message);
    }
}

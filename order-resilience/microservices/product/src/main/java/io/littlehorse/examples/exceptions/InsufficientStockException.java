package io.littlehorse.examples.exceptions;

import io.littlehorse.sdk.common.exception.LHTaskException;

public class InsufficientStockException extends LHTaskException {
    public InsufficientStockException(String message) {
        super("no-stock",message);
    }
}

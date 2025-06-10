package io.littlehorse.examples.exceptions;

import io.littlehorse.sdk.common.exception.LHTaskException;

public class ProductNotFoundException extends LHTaskException {
    public ProductNotFoundException(String message) {
        super("product-not-found",message);
    }
}

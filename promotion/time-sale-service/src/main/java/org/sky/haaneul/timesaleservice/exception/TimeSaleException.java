package org.sky.haaneul.timesaleservice.exception;

public class TimeSaleException extends RuntimeException {
    public TimeSaleException(String message) {
        super(message);
    }

    public TimeSaleException(String message, Throwable cause) {
        super(message, cause);
    }
}

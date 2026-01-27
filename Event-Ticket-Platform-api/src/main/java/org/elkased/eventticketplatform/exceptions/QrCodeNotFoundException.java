package org.elkased.eventticketplatform.exceptions;

public class QrCodeNotFoundException extends RuntimeException {
    public QrCodeNotFoundException(String message) {
        super(message);
    }

    public QrCodeNotFoundException() {
        super();
    }

    public QrCodeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public QrCodeNotFoundException(Throwable cause) {
        super(cause);
    }

    protected QrCodeNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

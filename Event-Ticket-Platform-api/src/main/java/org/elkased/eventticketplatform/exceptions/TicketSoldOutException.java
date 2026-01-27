package org.elkased.eventticketplatform.exceptions;

public class TicketSoldOutException extends RuntimeException {
    public TicketSoldOutException(String message) {
        super(message);
    }

    public TicketSoldOutException() {
        super();
    }

    public TicketSoldOutException(String message, Throwable cause) {
        super(message, cause);
    }

    public TicketSoldOutException(Throwable cause) {
        super(cause);
    }

    protected TicketSoldOutException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

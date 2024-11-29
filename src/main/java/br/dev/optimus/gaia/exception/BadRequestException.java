package br.dev.optimus.gaia.exception;

public class BadRequestException extends RuntimeException {
    private final String message;
    private final String field;

    public BadRequestException(String message, String field) {
        super(message);
        this.message = message;
        this.field = field;
    }

    public String getMessage() {
        return message;
    }

    public String getField() {
        return field;
    }

}

package com.plovdev.pornviewer.core.exceptions;

public class UnsuccessResponseException extends RequestProviderException {
    private String response;

    public UnsuccessResponseException() {
    }

    public UnsuccessResponseException(String s) {
        super(s);
    }

    public UnsuccessResponseException(String s, String response) {
        super(s);
        this.response = response;
    }

    public UnsuccessResponseException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnsuccessResponseException(Throwable cause) {
        super(cause);
    }

    public String getResponse() {
        return response;
    }
}
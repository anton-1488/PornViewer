package com.plovdev.pornviewer.core.exceptions;

public class UnsuccessResponseException extends RequestProviderException {
    private String response;
    private int code;

    public UnsuccessResponseException() {
    }

    public UnsuccessResponseException(String s) {
        super(s);
    }

    public UnsuccessResponseException(String s, String response) {
        super(s);
        this.response = response;
    }

    public UnsuccessResponseException(String s, String response, int code) {
        super(s);
        this.response = response;
        this.code = code;
    }

    public UnsuccessResponseException(String s, int code) {
        super(s);
        this.code = code;
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

    public int getCode() {
        return code;
    }
}
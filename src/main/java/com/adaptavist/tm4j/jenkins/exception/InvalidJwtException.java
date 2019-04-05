package com.adaptavist.tm4j.jenkins.exception;

public class InvalidJwtException extends RuntimeException {
    public InvalidJwtException(Exception e) {
        super(e);
    }
}

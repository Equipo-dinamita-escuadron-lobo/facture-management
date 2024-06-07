package com.facturemanagement.domain.exception;

public class FactureNotFound extends RuntimeException{
    public FactureNotFound(String message){
        super(message);
    }
}

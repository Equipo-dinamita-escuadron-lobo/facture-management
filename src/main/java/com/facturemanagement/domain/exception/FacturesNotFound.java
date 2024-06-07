package com.facturemanagement.domain.exception;

public class FacturesNotFound extends RuntimeException{
    public FacturesNotFound(String message){
        super(message);
    }
}

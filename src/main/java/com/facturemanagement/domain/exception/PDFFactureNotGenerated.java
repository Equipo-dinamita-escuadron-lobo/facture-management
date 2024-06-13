package com.facturemanagement.domain.exception;

public class PDFFactureNotGenerated extends RuntimeException{
    public PDFFactureNotGenerated(String message){
        super(message);
    }
}

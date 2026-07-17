package com.evertonhsoethe.apiattus.exception;

public class CaseNotFoundException extends RuntimeException {

    public CaseNotFoundException(Long id) {
        super("Legal case not found with id: " + id);
    }

    public CaseNotFoundException(String caseNumber) {
        super("Legal case not found with case number: " + caseNumber);
    }
}

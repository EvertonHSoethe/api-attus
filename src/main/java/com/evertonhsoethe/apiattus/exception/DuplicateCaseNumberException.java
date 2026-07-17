package com.evertonhsoethe.apiattus.exception;

public class DuplicateCaseNumberException extends RuntimeException {

    public DuplicateCaseNumberException(String caseNumber) {
        super("A legal case with case number already exists: " + caseNumber);
    }
}

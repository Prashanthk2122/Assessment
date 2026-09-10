package com.assessment.banking.model;

public record AccountPair(
        long customerNumber,
        long sourceAccountNumber,
        long destinationAccountNumber) {
}

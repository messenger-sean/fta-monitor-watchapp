package com.fsilberberg.ftamonitor.common;

/**
 * Describes what part of the event a match occurs in
 */
public enum MatchPeriod {
    PRAC("Practice"), QUAL("Qualification"), ELIM("Elimination");

    private final String printString;

    MatchPeriod(String printString) {
        this.printString = printString;
    }

    public static MatchPeriod fromOrd(int ord) {
        if (ord >= MatchPeriod.values().length) {
            throw new IllegalArgumentException("Error: ordinal " + ord + " is not a valid MatchPeriod");
        }
        return MatchPeriod.values()[ord];
    }
}

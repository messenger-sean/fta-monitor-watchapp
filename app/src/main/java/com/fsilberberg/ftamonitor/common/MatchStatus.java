package com.fsilberberg.ftamonitor.common;

/**
 * Represents the various match states that FMS supports
 */
public enum MatchStatus {
    NOT_READY("Not Ready"),
    TIMEOUT("Timeout"),
    READY_TO_PRESTART("Ready to Prestart"),
    PRESTART_INITIATED("Prestart Initiated"),
    PRESTART_COMPLETED("Prestart Completed"),
    MATCH_READY("Match Ready"),
    AUTO("Autonomous Running"),
    TELEOP("Teleop Running"),
    OVER("Match Over"),
    ABORTED("Aborted");


    private String name;

    MatchStatus(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}

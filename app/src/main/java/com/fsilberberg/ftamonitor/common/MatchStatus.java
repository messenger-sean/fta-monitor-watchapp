package com.fsilberberg.ftamonitor.common;

/**
 * Created by Fredric on 8/17/14.
 */
public enum MatchStatus {
    NOT_READY("Not Ready"),
    READY_TO_PRESTART("Ready to Prestart"),
    PRESTART_INITIATED("Prestart Initiated"),
    PRESTART_COMPLETED("Prestart Completed"),
    MATCH_READY("Match Ready"),
    AUTO("Autonomous Running"),
    TELEOP("Teleop Running"),
    OVER("Match Over");


    private String name;

    MatchStatus(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}

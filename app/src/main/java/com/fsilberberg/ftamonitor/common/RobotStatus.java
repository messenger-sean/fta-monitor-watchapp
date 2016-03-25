package com.fsilberberg.ftamonitor.common;

/**
 * Represents the various robot states that FMS supports
 */
public enum RobotStatus {
    ENABLED("Enabled"), DISABLED("Disabled"), BYPASSED("Bypassed");

    private String name;

    RobotStatus(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}

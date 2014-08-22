package com.fsilberberg.ftamonitor.common;

/**
 * Created by Fredric on 8/17/14.
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

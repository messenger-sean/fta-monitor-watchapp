package com.fsilberberg.ftamonitor.common;

/**
 * Represents the two alliances
 */
public enum Alliance {
    RED("Red"), BLUE("Blue");

    private final String m_prettyName;

    Alliance(String prettyName) {
        m_prettyName = prettyName;
    }

    public String getPrettyName() {
        return m_prettyName;
    }
}

package com.fsilberberg.ftamonitor.common;

/**
 * Represents which station a team is at, 1, 2, or 3
 */
public enum Station {
    STATION1(1), STATION2(2), STATION3(3);

    private final int m_stationNum;

    Station(int stationNum) {
        m_stationNum = stationNum;
    }

    public int getStationNumber() {
        return m_stationNum;
    }
}

package com.fsilberberg.ftamonitor.common;

import com.fsilberberg.ftamonitor.R;

/**
 * Created by Fredric on 8/25/14.
 */
public enum Alliance {
    RED(R.drawable.red_robot_with_border), BLUE(R.drawable.blue_robot_with_border);

    private final int m_backgroundId;

    Alliance(int backgroundId) {
        m_backgroundId = backgroundId;
    }

    public int getBackgroundId() {
        return m_backgroundId;
    }
}

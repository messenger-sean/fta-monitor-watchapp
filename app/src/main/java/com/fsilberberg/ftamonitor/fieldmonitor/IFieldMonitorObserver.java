package com.fsilberberg.ftamonitor.fieldmonitor;

import com.fsilberberg.ftamonitor.common.Alliance;

/**
 * Created by Fredric on 8/23/14.
 */
public interface IFieldMonitorObserver {
    void update(FieldUpdateType update);

    void update(TeamUpdateType update, int teamNum, Alliance alliance);
}

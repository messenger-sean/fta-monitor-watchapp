package com.fsilberberg.ftamonitor.services;

import android.content.Context;
import android.content.Intent;

import com.fsilberberg.ftamonitor.common.IObserver;
import com.fsilberberg.ftamonitor.fieldmonitor.TeamUpdateType;

/**
 * Created by 333fr_000 on 9/22/14.
 */
public class FieldProblemNotificationService implements IForegroundService, IObserver<TeamUpdateType> {


    @Override
    public void update(TeamUpdateType updateType) {

    }

    @Override
    public void startService(Context context, Intent intent) {

    }

    @Override
    public void stopService() {

    }
}

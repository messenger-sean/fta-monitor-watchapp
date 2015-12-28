package com.fsilberberg.ftamonitor.fieldmonitor.proxyhandlers;

import com.fsilberberg.ftamonitor.common.MatchStatus;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldMonitorFactory;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldStatus;
import microsoft.aspnet.signalr.client.hubs.SubscriptionHandler1;

import static com.fsilberberg.ftamonitor.common.MatchStatus.*;

/**
 * Handles updates the match state
 */
public class MatchStateProxyHandler extends ProxyHandlerBase implements SubscriptionHandler1<Integer> {
    private final FieldStatus m_fieldStatus = FieldMonitorFactory.getInstance().getFieldStatus();

    @Override
    public void run(Integer message) {
        MatchStatus status = NOT_READY;
        switch (message) {
            case 0: // Unknown: They aren't listed in the Json
            case 1:
                break;
            case 2: // WaitingForPrestart or WaitingForPrestartTO
            case 3:
                status = READY_TO_PRESTART;
                break;
            case 4: // Prestarting or PrestartingTO
            case 5:
                status = PRESTART_INITIATED;
                break;
            case 6: // WaitingForSetAudience or WaitingForSetAudienceTO
            case 7:
                status = PRESTART_COMPLETED;
                break;
            case 8: // WaitingForMatchReady
                status = NOT_READY;
                break;
            case 9: // WaitingForMatchReadyTO
                status = TIMEOUT;
                break;
            case 10:
                status = MATCH_READY;
                break;
            case 11:
                status = AUTO;
                break;
            case 12: // Match Transition: It stays the same as it currently is
                status = m_fieldStatus.getMatchStatus();
                break;
            case 13:
                status = TELEOP;
                break;
            case 14: // WaitingForCommit
            case 15: // WaitingForPostResults
            case 16: // TournamentLevelComplete
                status = OVER;
                break;
            case 17: // Aborted
                status = ABORTED;
                break;
        }
        m_fieldStatus.setMatchStatus(status);
    }
}

package com.fsilberberg.ftamonitor.fieldmonitor.proxyhandlers;

import com.fsilberberg.ftamonitor.common.MatchStatus;
import com.fsilberberg.ftamonitor.common.TournamentLevel;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldMonitorFactory;
import com.fsilberberg.ftamonitor.fieldmonitor.FieldStatus;
import com.fsilberberg.ftamonitor.services.FieldConnectionService;
import com.google.gson.JsonObject;

import microsoft.aspnet.signalr.client.hubs.SubscriptionHandler1;

import static com.fsilberberg.ftamonitor.common.MatchStatus.ABORTED;
import static com.fsilberberg.ftamonitor.common.MatchStatus.AUTO;
import static com.fsilberberg.ftamonitor.common.MatchStatus.MATCH_READY;
import static com.fsilberberg.ftamonitor.common.MatchStatus.NOT_READY;
import static com.fsilberberg.ftamonitor.common.MatchStatus.OVER;
import static com.fsilberberg.ftamonitor.common.MatchStatus.PRESTART_COMPLETED;
import static com.fsilberberg.ftamonitor.common.MatchStatus.PRESTART_INITIATED;
import static com.fsilberberg.ftamonitor.common.MatchStatus.READY_TO_PRESTART;
import static com.fsilberberg.ftamonitor.common.MatchStatus.TELEOP;
import static com.fsilberberg.ftamonitor.common.MatchStatus.TRANSITION;

/**
 * Handles updates the match state
 */
public class MatchStateProxyHandler extends ProxyHandlerBase implements SubscriptionHandler1<JsonObject> {
    private final FieldStatus m_fieldStatus = FieldMonitorFactory.getInstance().getFieldStatus();
    private final FieldConnectionService m_fieldConnectionService;

    public MatchStateProxyHandler(FieldConnectionService fieldConnectionService) {
        m_fieldConnectionService = fieldConnectionService;
    }

    @Override
    public void run(JsonObject message) {
        int matchState = message.getAsJsonPrimitive("P1").getAsInt();
        MatchStatus status = NOT_READY;
        switch (matchState) {
            case 0: // No currently active event or tournament level
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
            case 9: // WaitingForMatchStart
                status = MATCH_READY;
                break;
            case 10: // Match Auto
                status = AUTO;
                break;
            case 11: // Match Transition
                status = TRANSITION;
                break;
            case 12: // Match Teleop
                status = TELEOP;
                break;
            case 13: // WaitingForCommit
            case 14: // WaitingForPostResults
            case 15: // Tournament Level Complete
                status = OVER;
                break;
            case 17:
                // There is a 17 that is not in the FieldMonitor js anywhere. Not sure where it's
                // coming from, but I'm guessing that's supposed to be prestart completed.
                // TODO: Remove when Alex fixes the bug (I assume it's a bug).
                status = PRESTART_COMPLETED;
                break;
            case 16: // Match cancelled
            default:
                status = ABORTED;
                break;
        }
        m_fieldStatus.setMatchStatus(status);
        m_fieldStatus.setMatchNumber(message.getAsJsonPrimitive("P2").getAsString());
        TournamentLevel level = TournamentLevel.values()[
                message.getAsJsonPrimitive("P4").getAsInt() % TournamentLevel.values().length
                ];
        m_fieldStatus.setTournamentLevel(level);
        m_fieldConnectionService.updateMatchNumberAndPlay();
    }
}

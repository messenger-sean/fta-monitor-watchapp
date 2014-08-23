package com.fsilberberg.ftamonitor.fieldmonitor.fmsdatatypes;

/**
 * Implementation of the MatchPlayStatusInfo FMS Datatype. To deserialize, make sure to you use the Upper_Camel_Case
 * option with the Gson Builder
 */
public class MatchPlayInfo {

    private int eventId;
    private boolean inMatchPlay;
    private boolean matchPreStartComplete;
    private boolean matchStarted;
    private boolean matchEnded;
    private boolean useManualScoring;
    private MatchInfo matchInfo;

}

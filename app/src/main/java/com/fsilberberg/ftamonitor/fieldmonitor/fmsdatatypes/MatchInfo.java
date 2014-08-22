package com.fsilberberg.ftamonitor.fieldmonitor.fmsdatatypes;

/**
 * Implementation of the MatchInfo FMS Datatype. To deserialize, make sure to you use the Upper_Camel_Case
 * option with the Gson Builder
 */
public class MatchInfo {

    private String matchIdentifier;
    private String matchStatus;
    private boolean auto;
    private int autoStartTime;
    private int manualStartTime;
    private int timeLeft;
    private int red1TeamId;
    private int red2TeamId;
    private int red3TeamId;
    private int red4TeamId;
    private int blue1TeamId;
    private int blue2TeamId;
    private int blue3TeamId;
    private int blue4TeamId;
    private int red1CurrentRank;
    private int red2CurrentRank;
    private int red3CurrentRank;
    private int blue1CurrentRank;
    private int blue2CurrentRank;
    private int blue3CurrentRank;
    private int red1Card;
    private int red2Card;
    private int red3Card;
    private int blue1Card;
    private int blue2Card;
    private int blue3Card;
    private boolean red1IsBypassed;
    private boolean red2IsBypassed;
    private boolean red3IsBypassed;
    private boolean blue1IsBypassed;
    private boolean blue2IsBypassed;
    private boolean blue3IsBypassed;
    private int redFinalScore;
    private int blueFinalScore;
    private int redAutonomousScore;
    private int blueAutonomousScore;
    private int redTeleopTotal;
    private int blueTeleopTotal;
    private int redPenalty;
    private int bluePenalty;


    public String getMatchIdentifier() {
        return matchIdentifier;
    }

    public String getMatchStatus() {
        return matchStatus;
    }

    public boolean isAuto() {
        return auto;
    }

    public int getAutoStartTime() {
        return autoStartTime;
    }

    public int getManualStartTime() {
        return manualStartTime;
    }

    public int getTimeLeft() {
        return timeLeft;
    }

    public int getRed1TeamId() {
        return red1TeamId;
    }

    public int getRed2TeamId() {
        return red2TeamId;
    }

    public int getRed3TeamId() {
        return red3TeamId;
    }

    public int getRed4TeamId() {
        return red4TeamId;
    }

    public int getBlue1TeamId() {
        return blue1TeamId;
    }

    public int getBlue2TeamId() {
        return blue2TeamId;
    }

    public int getBlue3TeamId() {
        return blue3TeamId;
    }

    public int getBlue4TeamId() {
        return blue4TeamId;
    }

    public int getRed1CurrentRank() {
        return red1CurrentRank;
    }

    public int getRed2CurrentRank() {
        return red2CurrentRank;
    }

    public int getRed3CurrentRank() {
        return red3CurrentRank;
    }

    public int getBlue1CurrentRank() {
        return blue1CurrentRank;
    }

    public int getBlue2CurrentRank() {
        return blue2CurrentRank;
    }

    public int getBlue3CurrentRank() {
        return blue3CurrentRank;
    }

    public int getRed1Card() {
        return red1Card;
    }

    public int getRed2Card() {
        return red2Card;
    }

    public int getRed3Card() {
        return red3Card;
    }

    public int getBlue1Card() {
        return blue1Card;
    }

    public int getBlue2Card() {
        return blue2Card;
    }

    public int getBlue3Card() {
        return blue3Card;
    }

    public boolean isRed1IsBypassed() {
        return red1IsBypassed;
    }

    public boolean isRed2IsBypassed() {
        return red2IsBypassed;
    }

    public boolean isRed3IsBypassed() {
        return red3IsBypassed;
    }

    public boolean isBlue1IsBypassed() {
        return blue1IsBypassed;
    }

    public boolean isBlue2IsBypassed() {
        return blue2IsBypassed;
    }

    public boolean isBlue3IsBypassed() {
        return blue3IsBypassed;
    }

    public int getRedFinalScore() {
        return redFinalScore;
    }

    public int getBlueFinalScore() {
        return blueFinalScore;
    }

    public int getRedAutonomousScore() {
        return redAutonomousScore;
    }

    public int getBlueAutonomousScore() {
        return blueAutonomousScore;
    }

    public int getRedTeleopTotal() {
        return redTeleopTotal;
    }

    public int getBlueTeleopTotal() {
        return blueTeleopTotal;
    }

    public int getRedPenalty() {
        return redPenalty;
    }

    public int getBluePenalty() {
        return bluePenalty;
    }
}

package com.fsilberberg.ftamonitor.common;

/**
 * Represents the currently playing tournament level.
 */
public enum TournamentLevel {
    // The dummy level is there to increment the enum by 1, so that practice starts at enum 0.
    // It is never actually used.
    DUMMY("Dummy", "D", new StandardFormat()),
    PRACTICE("Practice", "P", new StandardFormat()),
    QUALS("Quals", "Q", new StandardFormat()),
    PLAYOFF("Playoffs", "PL", new MatchNumFormatter() {
        @Override
        public String formatNum(String matchNum, TournamentLevel level) {
            try {
                String prefix;
                int num = Integer.parseInt(matchNum);
                if (num < 13) {
                    prefix = "Quarter Final ";
                } else if (num >= 13 && num < 19) {
                    prefix = "Semi Final ";
                } else {
                    prefix = "Final ";
                }
                return prefix + matchNum;
            } catch (NumberFormatException ex) {
                return new StandardFormat().formatNum(matchNum, level);
            }

        }
    });

    private final String m_text;
    private final String m_shortText;
    private final MatchNumFormatter m_formatter;

    TournamentLevel(String text, String shortText, MatchNumFormatter formatter) {
        m_text = text;
        m_shortText = shortText;
        m_formatter = formatter;
    }

    @Override
    public String toString() {
        return m_text;
    }

    @SuppressWarnings("unused")
    public String toShortString() {
        return m_shortText;
    }

    public String toString(String matchNum) {
        return m_formatter.formatNum(matchNum, this);
    }

    private interface MatchNumFormatter {
        String formatNum(String matchNum, TournamentLevel level);
    }

    private static final class StandardFormat implements MatchNumFormatter {
        @Override
        public String formatNum(String matchNum, TournamentLevel level) {
            return level.m_text + " " + matchNum;
        }
    }
}

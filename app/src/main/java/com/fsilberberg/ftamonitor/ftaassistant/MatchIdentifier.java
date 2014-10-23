package com.fsilberberg.ftamonitor.ftaassistant;

import com.fsilberberg.ftamonitor.common.MatchPeriod;

/**
 * Represent the identification of a match. This can be practice matches, qual matches, or elim
 * matches.
 */
public interface MatchIdentifier extends Comparable<MatchIdentifier> {

    /**
     * Gets the period of the match
     *
     * @return What period this match is playing in
     */
    public MatchPeriod getPeriod();

    /**
     * Gets the string representation of the match identifier
     *
     * @return The identifier
     */
    public String getIdentifier();

    /**
     * Gets the replay number of this match
     *
     * @return The match replay
     */
    public int getReplay();
}

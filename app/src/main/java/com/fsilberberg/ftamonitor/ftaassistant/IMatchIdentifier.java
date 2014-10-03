package com.fsilberberg.ftamonitor.ftaassistant;

import com.fsilberberg.ftamonitor.common.MatchPeriod;

/**
 * Represent the identification of a match. This can be practice matches, qual matches, or elim
 * matches.
 */
public interface IMatchIdentifier extends Comparable<IMatchIdentifier> {

    /**
     * Gets the period of the match
     *
     * @return What period this match is playing in
     */
    public MatchPeriod getPeriod();

}

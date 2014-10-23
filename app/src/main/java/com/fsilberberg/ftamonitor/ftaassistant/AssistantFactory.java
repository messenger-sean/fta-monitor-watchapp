package com.fsilberberg.ftamonitor.ftaassistant;

import com.fsilberberg.ftamonitor.common.MatchPeriod;

import org.joda.time.DateTime;

/**
 * Factory for obtaining implementations of objects in the {@link com.fsilberberg.ftamonitor.ftaassistant}
 * package
 */
public class AssistantFactory {

    private static AssistantFactory instance = new AssistantFactory();

    public static AssistantFactory getInstance() {
        return instance;
    }

    /**
     * Creates a new implementation of {@link Team} with
     * the given number, name, and nickname
     *
     * @param id         The id of the team. If this is a new team and not yet saved, specify id -1
     * @param teamNumber The team number
     * @param teamName   The full name of the team
     * @param teamNick   The short name of the team
     * @return The team implementation
     */
    public Team makeTeam(long id, int teamNumber, String teamName, String teamNick) {
        return new TeamImpl(id, teamNumber, teamName, teamNick);
    }

    /**
     * Creates a new implementation of {@link Event} with the given
     * event info.
     *
     * @param id        The unique id of this event. If this is a new event and not yet saved, specify -1
     * @param eventCode The FIRST code for the event
     * @param eventName The name of the event
     * @param eventLoc  The location for the event
     * @param startTime The starting time of the event
     * @param endTime   The ending time of the event
     * @return The event implementation
     */
    public Event makeEvent(long id, String eventCode, String eventName, String eventLoc, DateTime startTime, DateTime endTime) {
        return new EventImpl(id, eventCode, eventName, eventLoc, startTime, endTime);
    }

    /**
     * Creates a new implementation of {@link com.fsilberberg.ftamonitor.ftaassistant.Match} with
     * the given id, period, identifier, and replay. The identifier will be turned into the correct
     * parsed version depending on the match period
     *
     * @param id               The unique id of this match. If this is a new match and not yet saved, specify -1
     * @param period           The period of the match
     * @param numberIdentifier The identifier of the number of the match, varies with period
     * @param replay           The replay number of this match
     * @return The match implementation
     */
    public Match makeMatch(long id, MatchPeriod period, String numberIdentifier, int replay) {
        MatchIdentifier mi = null;
        switch (period) {
            case PRAC:
                int pracNum = Integer.parseInt(numberIdentifier, 0);
                mi = new PracticeIdentifier(pracNum);
                break;
            case QUAL:
                int qualNum = Integer.parseInt(numberIdentifier, 0);
                mi = new QualIdentifier(qualNum, replay);
                break;
            case ELIM:
                mi = new ElimIdentifier(numberIdentifier, replay);
                break;
        }

        return new MatchImpl(id, mi);
    }

    /**
     * Creates a new implementation of {@link com.fsilberberg.ftamonitor.ftaassistant.Note} with
     * the given id and content.
     *
     * @param id      The id of the new note. If this is a new note and not yet saved, specify -1
     * @param content The content of the note
     * @return The note implementation
     */
    public Note makeNote(long id, String content) {
        return new NoteImpl(id, content);
    }
}

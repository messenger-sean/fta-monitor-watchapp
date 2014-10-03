package com.fsilberberg.ftamonitor.ftaassistant;

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
     * Creates a new implementation of {@link com.fsilberberg.ftamonitor.ftaassistant.ITeam} with
     * the given number, name, and nickname
     *
     * @param teamNumber The team number
     * @param teamName   The full name of the team
     * @param teamNick   The short name of the team
     * @return The team implementation
     */
    public ITeam makeTeam(int teamNumber, String teamName, String teamNick) {
        return null;
    }

    /**
     * Creates a new implementation of {@link com.fsilberberg.ftamonitor.ftaassistant.IEvent} with the given
     * event info.
     *
     * @param eventCode The FIRST code for the event
     * @param eventName The name of the event
     * @param eventLoc  The location for the event
     * @param startTime The starting time of the event
     * @param endTime   The ending time of the event
     * @return The event implementation
     */
    public IEvent makeEvent(String eventCode, String eventName, String eventLoc, DateTime startTime, DateTime endTime) {
        return null;
    }

    // TODO: Practice, Qual, and Elims factory methods

    public IMatchIdentifier makeMatchIdentifier() {
        return null;
    }

    public INote makeNote() {
        return null;
    }
}

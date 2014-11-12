package com.fsilberberg.ftamonitor.generator;

import com.sun.javafx.css.CssError;
import de.greenrobot.daogenerator.*;

import static com.fsilberberg.ftamonitor.generator.FTAMonitorContract.*;

/**
 * This generates the GreenDAO layout for the main android project
 */
public class ModelGenerator {

    public static void main(String[] args) {
        Schema mainSchema = new Schema(1, TABLE_PACKAGE);

        // Add the event table. All relations are added together below
        Entity eventEntity = mainSchema.addEntity(FTAMonitorContract.Event.TABLE_NAME);
        eventEntity.addIdProperty();
        eventEntity.addStringProperty(FTAMonitorContract.Event.EVENT_CODE).notNull();
        eventEntity.addStringProperty(FTAMonitorContract.Event.EVENT_NAME);
        eventEntity.addDateProperty(FTAMonitorContract.Event.START_DATE);
        eventEntity.addDateProperty(FTAMonitorContract.Event.END_DATE);
        eventEntity.addStringProperty(FTAMonitorContract.Event.EVENT_LOC);

        // Add the team table
        Entity teamEntity = mainSchema.addEntity(FTAMonitorContract.Team.TABLE_NAME);
        teamEntity.addIdProperty();
        teamEntity.addIntProperty(FTAMonitorContract.Team.TEAM_NUMBER).notNull();
        teamEntity.addStringProperty(FTAMonitorContract.Team.TEAM_NAME);
        teamEntity.addStringProperty(FTAMonitorContract.Team.TEAM_NICK);

        // Add the match table
        Entity matchEntity = mainSchema.addEntity(FTAMonitorContract.Match.TABLE_NAME);
        matchEntity.addIdProperty();
        Property matchIdProperty = matchEntity.addStringProperty(FTAMonitorContract.Match.MATCH_IDENTIFIER).getProperty();
        matchEntity.addIntProperty(FTAMonitorContract.Match.MATCH_PERIOD);
        matchEntity.addIntProperty(FTAMonitorContract.Match.REPLAY);
        Property matchEventProperty = matchEntity.addLongProperty(FTAMonitorContract.Match.MATCH_EVENT).getProperty();

        // Add the note table
        Entity noteEntity = mainSchema.addEntity(FTAMonitorContract.Notes.TABLE_NAME);
        Property noteIdProperty = noteEntity.addIdProperty().primaryKey().getProperty();
        noteEntity.addStringProperty(FTAMonitorContract.Notes.CONTENT);
        Property noteTeamProperty = noteEntity.addLongProperty(FTAMonitorContract.Notes.NOTE_TEAM).getProperty();
        Property noteMatchProperty = noteEntity.addLongProperty(FTAMonitorContract.Notes.NOTE_MATCH).getProperty();
        Property noteEventProperty = noteEntity.addLongProperty(FTAMonitorContract.Notes.NOTE_EVENT).getProperty();

        // Team to Event subtable
        Entity teamEventsEntity = mainSchema.addEntity(FTAMonitorContract.Teams_Events.TABLE_NAME);
        teamEventsEntity.addIdProperty();
        Property teamEventsTeamProperty = teamEventsEntity.addLongProperty(FTAMonitorContract.Teams_Events.TEAM_ID).getProperty();
        Property teamEventsEventProperty = teamEventsEntity.addLongProperty(FTAMonitorContract.Teams_Events.EVENT_ID).getProperty();
        ToMany teamEventsTeamMany = teamEntity.addToMany(teamEventsEntity, teamEventsTeamProperty);
        teamEventsTeamMany.setName(FTAMonitorContract.Team.TEAM_EVENTS);
        teamEventsEntity.addToOne(teamEntity, teamEventsTeamProperty);
        ToMany teamEventsEventMany = eventEntity.addToMany(teamEventsEntity, teamEventsEventProperty);
        teamEventsEventMany.setName(FTAMonitorContract.Event.EVENT_TEAMS);
        teamEventsEntity.addToOne(eventEntity, teamEventsEventProperty);

        // Team to Match subtable
        Entity teamMatchEntity = mainSchema.addEntity(FTAMonitorContract.Matches_Teams.TABLE_NAME);
        teamMatchEntity.addIdProperty();
        Property teamMatchTeamProperty = teamMatchEntity.addLongProperty(FTAMonitorContract.Matches_Teams.TEAM_ID).getProperty();
        Property teamMatchMatchProperty = teamMatchEntity.addLongProperty(FTAMonitorContract.Matches_Teams.MATCH_ID).getProperty();
        ToMany teamMatchTeamMany = teamEntity.addToMany(teamMatchEntity, teamMatchTeamProperty);
        teamMatchTeamMany.setName(FTAMonitorContract.Team.TEAM_MATCHES);
        teamMatchEntity.addToOne(teamEntity, teamMatchTeamProperty);
        ToMany teamMatchMatchMany = matchEntity.addToMany(teamMatchEntity, teamMatchMatchProperty);
        teamMatchMatchMany.setName(FTAMonitorContract.Match.MATCH_TEAMS);
        teamMatchEntity.addToOne(matchEntity, teamMatchMatchProperty);

        // Event to Match
        ToMany eventMatches = eventEntity.addToMany(matchEntity, matchEventProperty);
        eventMatches.setName(FTAMonitorContract.Event.EVENT_MATCHES);
        eventMatches.orderDesc(matchIdProperty);
        matchEntity.addToOne(eventEntity, matchEventProperty);

        // Team to Notes
        ToMany teamNotes = teamEntity.addToMany(noteEntity, noteTeamProperty);
        teamNotes.setName(FTAMonitorContract.Team.TEAM_NOTES);
        noteEntity.addToOne(teamEntity, noteTeamProperty);

        // Event to Notes
        ToMany eventNotes = eventEntity.addToMany(noteEntity, noteEventProperty);
        eventNotes.setName(FTAMonitorContract.Event.EVENT_NOTES);
        noteEntity.addToOne(eventEntity, noteEventProperty);

        // Match to Notes
        ToMany matchNotes = matchEntity.addToMany(noteEntity, noteMatchProperty);
        matchNotes.setName(FTAMonitorContract.Match.MATCH_NOTES);
        noteEntity.addToOne(matchEntity, noteMatchProperty);

        try {
            new DaoGenerator().generateAll(mainSchema, args[0]);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

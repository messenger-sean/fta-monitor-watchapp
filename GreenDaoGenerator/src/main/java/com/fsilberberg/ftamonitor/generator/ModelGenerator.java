package com.fsilberberg.ftamonitor.generator;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.Schema;

import static com.fsilberberg.ftamonitor.generator.FTAMonitorContract.*;

/**
 * This generates the GreenDAO layout for the main android project
 */
public class ModelGenerator {

    public static void main(String[] args) {
        Schema mainSchema = new Schema(1, TABLE_PACKAGE);

        // Add the event table. All relations are added together below
        Entity eventEntity = mainSchema.addEntity(FTAMonitorContract.Event.TABLE_NAME);
        Property eventIdProperty = eventEntity.addIdProperty().getProperty();
        eventEntity.addStringProperty(FTAMonitorContract.Event.EVENT_CODE).notNull();
        eventEntity.addStringProperty(FTAMonitorContract.Event.EVENT_NAME);
        eventEntity.addDateProperty(FTAMonitorContract.Event.START_DATE);
        eventEntity.addDateProperty(FTAMonitorContract.Event.END_DATE);
        eventEntity.addStringProperty(FTAMonitorContract.Event.EVENT_LOC);

        // Add the team table
        Entity teamEntity = mainSchema.addEntity(FTAMonitorContract.Team.TABLE_NAME);
        Property teamIdProperty = teamEntity.addIdProperty().getProperty();
        teamEntity.addStringProperty(FTAMonitorContract.Team.TEAM_NUMBER).notNull();
        teamEntity.addStringProperty(FTAMonitorContract.Team.TEAM_NAME);
        teamEntity.addStringProperty(FTAMonitorContract.Team.TEAM_NICK);

        // Add the match table
        Entity matchEntity = mainSchema.addEntity(FTAMonitorContract.Match.TABLE_NAME);
        Property matchIdProperty = matchEntity.addIdProperty().getProperty();
        matchEntity.addStringProperty(FTAMonitorContract.Match.MATCH_IDENTIFIER);
        matchEntity.addIntProperty(FTAMonitorContract.Match.MATCH_PERIOD);
        matchEntity.addIntProperty(FTAMonitorContract.Match.REPLAY);

        // Add the note table
        Entity noteEntity = mainSchema.addEntity(FTAMonitorContract.Notes.TABLE_NAME);
        Property noteIdProperty = noteEntity.addIdProperty().getProperty();
        noteEntity.addStringProperty(FTAMonitorContract.Notes.CONTENT);

        // Team to Event subtable
        addManyToManyRelationship(mainSchema,
                FTAMonitorContract.Teams_Events.TABLE_NAME,
                teamEntity, teamIdProperty, FTAMonitorContract.Team.TEAM_EVENTS,
                eventEntity, eventIdProperty, FTAMonitorContract.Event.EVENT_TEAMS);

        // Team to Match subtable
        addManyToManyRelationship(mainSchema,
                FTAMonitorContract.Matches_Teams.TABLE_NAME,
                teamEntity, teamIdProperty, FTAMonitorContract.Team.TEAM_MATCHES,
                matchEntity, matchIdProperty, FTAMonitorContract.Match.MATCH_TEAMS);

        // Event to Match
        eventEntity.addToMany(matchEntity, matchIdProperty, FTAMonitorContract.Event.EVENT_MATCHES);
        matchEntity.addToOne(eventEntity, eventIdProperty, FTAMonitorContract.Match.MATCH_EVENT);

        // Team to Notes
        teamEntity.addToMany(noteEntity, noteIdProperty, FTAMonitorContract.Team.TEAM_NOTES);
        noteEntity.addToOne(teamEntity, teamIdProperty, FTAMonitorContract.Notes.NOTE_TEAM);

        // Event to Notes
        eventEntity.addToMany(noteEntity, noteIdProperty, FTAMonitorContract.Event.EVENT_NOTES);
        noteEntity.addToOne(eventEntity, eventIdProperty, FTAMonitorContract.Notes.NOTE_EVENT);

        // Match to Notes
        matchEntity.addToMany(noteEntity, noteIdProperty, FTAMonitorContract.Match.MATCH_NOTES);
        noteEntity.addToOne(matchEntity, matchIdProperty, FTAMonitorContract.Notes.NOTE_MATCH);

        try {
            new DaoGenerator().generateAll(mainSchema, args[0]);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void addManyToManyRelationship(Schema mainSchema,
                                                 String linkName,
                                                 Entity table1Entity,
                                                 Property table1Id,
                                                 String table1ManyName,
                                                 Entity table2Entity,
                                                 Property table2Id,
                                                 String table2ManyName) {
        Entity manyLinkEntity = mainSchema.addEntity(linkName);
        Property linkId = manyLinkEntity.addIdProperty().getProperty();
        manyLinkEntity.addToOne(table1Entity, table1Id);
        manyLinkEntity.addToOne(table2Entity, table2Id);
        table1Entity.addToMany(manyLinkEntity, linkId, table1ManyName);
        table2Entity.addToMany(manyLinkEntity, linkId, table2ManyName);
    }

}

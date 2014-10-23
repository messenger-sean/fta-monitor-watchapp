package com.fsilberberg.ftamonitor.ftaassistant;

/**
 * Created by Fredric on 10/18/14.
 */
public class NoteImpl implements Note {

    private final long m_id;
    private final String m_content;

    public NoteImpl(long id, String content) {
        m_id = id;
        m_content = content;
    }

    @Override
    public long getId() {
        return m_id;
    }

    @Override
    public String getContent() {
        return m_content;
    }

    @Override
    public Team getTeam() {
        return null;
    }

    @Override
    public Event getEvent() {
        return null;
    }

    @Override
    public Match getMatch() {
        return null;
    }
}

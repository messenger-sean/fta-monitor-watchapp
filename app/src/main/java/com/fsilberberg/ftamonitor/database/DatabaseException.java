package com.fsilberberg.ftamonitor.database;

/**
 * Database exception that can be thrown when there is an error with the database
 */
public class DatabaseException extends Exception {
    public DatabaseException(String reason, Exception ex) {
        super(reason, ex);
    }
}

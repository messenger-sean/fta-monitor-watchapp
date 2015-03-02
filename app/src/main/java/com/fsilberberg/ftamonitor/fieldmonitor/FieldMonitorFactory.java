package com.fsilberberg.ftamonitor.fieldmonitor;

/**
 * Main factory for all Field Monitor related objects.
 */
public class FieldMonitorFactory {

    private static FieldMonitorFactory instance;

    public static void initialize() {
        if (instance == null) {
            instance = new FieldMonitorFactory();
        }
    }

    public static FieldMonitorFactory getInstance() {
        if (instance == null) {
            throw new RuntimeException("Error: Accessing the field monitor factory before initialization. You must call FieldMonitorFactory.initialize() before getInstance()");
        }
        return instance;
    }

    private FieldStatus fieldStatus;

    private FieldMonitorFactory() {
        fieldStatus = new FieldStatus();
    }

    public FieldStatus getFieldStatus() {
        return fieldStatus;
    }
}

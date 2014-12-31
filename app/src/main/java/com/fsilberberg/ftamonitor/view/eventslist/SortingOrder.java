package com.fsilberberg.ftamonitor.view.eventslist;

import android.util.StringBuilderPrinter;
import com.fsilberberg.ftamonitor.ftaassistant.Event;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Enum that represents the possible sorting orders. It also contains a comparator for sorting events in that order.
 */
enum SortingOrder {
    ALPHA(new Comparator<Event>() {
        @Override
        public int compare(Event e1, Event e2) {
            return e1.getEventName().compareTo(e2.getEventName());
        }
    }, "By Event Name"),
    DATE(new Comparator<Event>() {
        @Override
        public int compare(Event e1, Event e2) {
            if (e1.getEventCode().equals(e2.getEventCode())) {
                return 0;
            } else if (e1.getStartDate().isBefore(e2.getStartDate())) {
                return -1;
            } else if (e2.getStartDate().isBefore(e1.getStartDate())) {
                return 1;
            } else {
                return e1.getEventName().compareTo(e2.getEventName());
            }
        }

    }, "By Start Date");

    private final Comparator<Event> m_comparator;
    private final String m_text;

    SortingOrder(Comparator<Event> comparator, String text) {
        m_comparator = comparator;
        m_text = text;
    }


    public Comparator<Event> getComparator() {
        return m_comparator;
    }

    public static String[] getStrings() {
        String[] arr = new String[0];
        return Collections2.transform(Arrays.asList(values()), new Function<SortingOrder, String>() {
            @Override
            public String apply(SortingOrder input) {
                return input.m_text;
            }
        }).toArray(arr);
    }
}


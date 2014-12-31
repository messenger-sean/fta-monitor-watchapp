package com.fsilberberg.ftamonitor.view.eventslist;

import com.fsilberberg.ftamonitor.ftaassistant.Event;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

/**
 * Enum that represents the possible sorting orders. It also contains a comparator for sorting events in that order.
 */
enum SortingOrder {
    ALPHA(new Comparator<Event>() {
        @Override
        public int compare(Event e1, Event e2) {
            // Comparator for alphabetic order is simple, just compare the names
            return e1.getEventName().compareTo(e2.getEventName());
        }
    }, "Event Name"),
    DATE(new Comparator<Event>() {
        @Override
        public int compare(Event e1, Event e2) {
            if (e1.getEventCode().equals(e2.getEventCode())) {
                // If they have the same code, they are the same event, return 0
                return 0;
            } else if (e1.getStartDate().isBefore(e2.getStartDate())) {
                // If the first event starts before the second, it is first, so return -1
                return -1;
            } else if (e2.getStartDate().isBefore(e1.getStartDate())) {
                // If the second event starts before the first, it is first, so return 1
                return 1;
            } else {
                // They start at the same time, so resort to alphabetic sorting
                return ALPHA.getComparator().compare(e1, e2);
            }
        }

    }, "Start Date");

    private final Comparator<Event> m_comparator;
    private final String m_text;

    SortingOrder(Comparator<Event> comparator, String text) {
        m_comparator = comparator;
        m_text = text;
    }


    public Comparator<Event> getComparator() {
        return m_comparator;
    }

    /**
     * Gets an array of printable strings.
     * @return The printable string for each element
     */
    public static String[] getStrings() {
        Collection<String> strings = Collections2.transform(Arrays.asList(values()), new Function<SortingOrder, String>() {
            @Override
            public String apply(SortingOrder input) {
                return input.m_text;
            }
        });
        return strings.toArray(new String[strings.size()]);
    }
}


package com.fsilberberg.ftamonitor.services.pebble;

import android.content.Context;
import android.os.ConditionVariable;
import android.util.Log;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Sends messages to the pebble from registered observers.
 */
public class PebbleSender implements Runnable {

    private final String TAG = PebbleSender.class.getName();

    // The pebble app UID
    public static final UUID PEBBLE_UUID = UUID.fromString("5b742e45-2918-4f69-a510-7c0457d9df16");

    // Constants for the starting offset of the status, number, and battery keys.
    public static final int STATUS_START = 1;
    public static final int NUMBER_START = 9;
    public static final int BATTERY_START = 15;
    public static final int MATCH_STATE = 21;
    private static final int VIBE = 7;

    private final Object m_lock = new Object();
    private final ConditionVariable m_messageSignal = new ConditionVariable();
    private final Object m_sendSignal = new Object();
    private boolean m_sendInProgress = false;
    private final PebbleCommunicationService m_service;
    private volatile Map<Integer, PebbleMessage> m_messageMap = new HashMap<>();
    private volatile Map<Integer, PebbleMessage> m_sendMap = new HashMap<>();
    private PebbleDictionary m_lastSent = null;
    private AtomicInteger retries = new AtomicInteger(0);
    private boolean running = true;

    public PebbleSender(PebbleCommunicationService service) {
        m_service = service;
    }

    public void run() {
        // Register callbacks
        PebbleAckReceiver ackReceiver = new PebbleAckReceiver(PEBBLE_UUID);
        PebbleNackReceiver nackReceiver = new PebbleNackReceiver(PEBBLE_UUID);
        PebbleKit.registerReceivedAckHandler(m_service, ackReceiver);
        PebbleKit.registerReceivedNackHandler(m_service, nackReceiver);

        while (!Thread.interrupted() && running) {
            // If there is a send in progress, wait for it to stop
            synchronized (m_sendSignal) {
                if (m_sendInProgress) {
                    try {
                        m_sendSignal.wait();
                    } catch (InterruptedException e) {
                        Log.w(TAG, "Interrupted while waiting for send to end.", e);
                        break;
                    }
                }

                m_sendInProgress = true;
            }

            // Wait for a signal to get messages
            m_messageSignal.block();

            // If during the waits we were interrupted, exit
            if (Thread.interrupted() || !running) break;

            // Atomically swap the queues, then close the signal.
            synchronized (m_lock) {
                Map<Integer, PebbleMessage> local = m_messageMap;
                m_messageMap = m_sendMap;
                m_sendMap = local;
            }

            // Send all messages in the send queue if they exist. This takes care of spurious
            // wakeups that could potentially occur.
            if (!m_sendMap.isEmpty()) {
                sendMessages();
            } else {
                endSend();
            }
        }
        running = false;
        m_service.unregisterReceiver(ackReceiver);
        m_service.unregisterReceiver(nackReceiver);
    }

    /**
     * Queues a message to be send to the Pebble.
     *
     * @param messages The messages to send to the pebble. If the message key has not yet been sent,
     *                 and already exists in the dictionary, it is replaced.
     */
    public void addMessages(Map<Integer, PebbleMessage> messages) {
        synchronized (m_lock) {
            m_messageMap.putAll(messages);
            m_messageSignal.open();
        }
    }

    public void addMessage(int key, PebbleMessage message) {
        synchronized ((m_lock)) {
            m_messageMap.put(key, message);
            m_messageSignal.open();
        }
    }

    private void sendMessages() {
        PebbleDictionary sendDict = new PebbleDictionary();
        boolean vibrate = false;
        for (PebbleMessage message : m_sendMap.values()) {
            sendDict.addUint32(message.getKey(), message.getValue());
            vibrate = vibrate || message.isShouldVibrate();
        }

        if (vibrate) {
            sendDict.addUint32(VIBE, 1);
        }


        m_lastSent = sendDict;
        retries.set(0);
        PebbleKit.sendDataToPebble(m_service, PEBBLE_UUID, sendDict);
        m_sendMap.clear();
    }

    private void endSend() {
        synchronized (m_sendSignal) {
            m_sendInProgress = false;
            m_sendSignal.notify();
            retries.set(0);
        }
    }

    /**
     * Starts the FTA Monitor application on the Pebble
     */
    public void startApp() {
        PebbleKit.startAppOnPebble(m_service, PEBBLE_UUID);
    }

    /**
     * Simple data class for messages sent to the Pebble.
     */
    public static final class PebbleMessage {
        private final int m_key;
        private final int m_value;
        private final boolean m_shouldVibrate;

        public PebbleMessage(int key, int value, boolean shouldVibrate) {
            m_key = key;
            m_value = value;
            m_shouldVibrate = shouldVibrate;
        }

        public int getKey() {
            return m_key;
        }

        public int getValue() {
            return m_value;
        }

        public boolean isShouldVibrate() {
            return m_shouldVibrate;
        }

        @Override
        public String toString() {
            return "PebbleMessage{" +
                    "m_key=" + m_key +
                    ", m_value=" + m_value +
                    ", m_shouldVibrate=" + m_shouldVibrate +
                    '}';
        }
    }

    private final class PebbleAckReceiver extends PebbleKit.PebbleAckReceiver {
        /**
         * Instantiates a new pebble ack receiver.
         *
         * @param subscribedUuid the subscribed uuid
         */
        protected PebbleAckReceiver(UUID subscribedUuid) {
            super(subscribedUuid);
        }

        @Override
        public void receiveAck(Context context, int transactionId) {
            if (!running) {
                return;
            }

            endSend();
        }
    }

    private final class PebbleNackReceiver extends PebbleKit.PebbleNackReceiver {
        /**
         * Instantiates a new pebble nack receiver.
         *
         * @param subscribedUuid the subscribed uuid
         */
        protected PebbleNackReceiver(UUID subscribedUuid) {
            super(subscribedUuid);
        }

        @Override
        public void receiveNack(Context context, int transactionId) {
            // Attempt to resend the message, after giving a 10 ms window for cooldown
            if (!running) return;
            if (retries.getAndAdd(1) < 3) {
                try {
                    Thread.sleep(10 * retries.get());
                } catch (InterruptedException e) {
                    Log.e(PebbleNackReceiver.class.getName(), "Interrupted while attempting to retransmit!", e);
                    // If we're interrupted, we want to exit. Disable running and open the signal
                    running = false;
                    endSend();
                }

                if (!running) return;

                // Attempt to resend the transaction
                PebbleKit.sendDataToPebbleWithTransactionId(context, PEBBLE_UUID, m_lastSent, transactionId);
            } else {
                // At this point, we're blocked. Wait 40 ms, then fully update all data
                try {
                    Thread.sleep(40);
                } catch (InterruptedException e) {
                    Log.e(PebbleNackReceiver.class.getName(), "Interrupted while attempting to retransmit!", e);
                    // If we're interrupted, we want to exit. Disable running and open the signal
                    running = false;
                    endSend();
                }

                if (!running) return;

                m_service.updateAll();
                endSend();
            }
        }
    }
}

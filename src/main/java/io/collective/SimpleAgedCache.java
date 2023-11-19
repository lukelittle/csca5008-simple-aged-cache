package io.collective;

import java.time.Clock;

public class SimpleAgedCache {

    private final Clock clock;
    private ExpirableEntry head = null;
    private int length = 0;

    public SimpleAgedCache(Clock clock) {
        this.clock = clock;
    }


    public SimpleAgedCache() {
        this.clock = Clock.systemUTC();
    }

    public void put(Object key, Object value, int retentionInMillis) {
        removeExpired();
        remove(key);

        if (head == null) {
            head = new ExpirableEntry(key, value, this.clock.millis() + retentionInMillis);
        } else {
            ExpirableEntry current = head;
            while (current.getNext() != null) {
                current = current.getNext();
            }
            current.setNext(new ExpirableEntry(key, value, this.clock.millis() + retentionInMillis));
        }
        length++;
    }

    public boolean isEmpty() {
        removeExpired();
        return length == 0;
    }

    public int size() {
        removeExpired();
        return length;
    }

    public Object get(Object key) {

        removeExpired();

        ExpirableEntry current = head;

        while (current != null) {
            if (current.getKey().equals(key)) {
                return current.getValue();
            }
            current = current.getNext();
        }

        return null;
    }

    public void remove(Object key) {

        while (head != null && head.getKey().equals(key)) {
            head = head.getNext();
            length--;
        }

        if (head != null) {
            ExpirableEntry current = head.getNext();
            ExpirableEntry previous = head;
            while (current != null) {
                if (current.getKey().equals(key)) {
                    previous.setNext(current.getNext());
                    length--;
                } else {
                    previous = current;
                }
                current = current.getNext();
            }
        }
    }

    private void removeExpired() {

        ExpirableEntry current = head;

        while (current != null && isExpired(current)) {
            current = current.getNext();
            length--;
        }
    }

    private boolean isExpired(ExpirableEntry e) {
        return this.clock.millis() > e.getExpires();
    }

    private static class ExpirableEntry {

        private final Object key;

        private final Object value;

        private final long expires;

        private ExpirableEntry next;

        public ExpirableEntry(Object key, Object value, long expires) {
            this.key = key;
            this.value = value;
            this.expires = expires;
            this.next = null;
        }

        public Object getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }

        public long getExpires() {
            return expires;
        }

        public ExpirableEntry getNext() {
            return next;
        }

        public void setNext(ExpirableEntry next) {
            this.next = next;
        }
    }
}
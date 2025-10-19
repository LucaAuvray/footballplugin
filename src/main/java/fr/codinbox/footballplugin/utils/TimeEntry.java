package fr.codinbox.footballplugin.utils;

public class TimeEntry<T> {

    private final long time;
    private T value;

    public TimeEntry(long time, T value) {
        this.time = time;
        this.value = value;
    }

    public long getTime() {
        return time;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "time: " + time + " value:" + this.value.toString();
    }

}

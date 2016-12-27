package org.vyhlidka.homeautomation.util;

public final class Box<T> {

    private T value;

    public Box(final T value) {
        this.value = value;
    }

    public Box() {
    }

    public boolean contains() {
        return this.value != null;
    }

    public void set(T value) {
        this.value = value;
    }

    public T get() {
        return this.value;
    }

}

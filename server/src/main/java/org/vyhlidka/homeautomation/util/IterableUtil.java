package org.vyhlidka.homeautomation.util;

import org.apache.commons.lang3.Validate;

import java.util.Iterator;

/**
 * Created by lucky on 27.12.16.
 */
public class IterableUtil {

    /**
     * Creates an Iterable that is backed by two (cancatenated) Iterables
     * @param first
     * @param second
     * @param <T>
     * @return
     */
    public static <T> Iterable<T> concat(Iterable<T> first, Iterable<T> second) {
        Validate.notNull(first, "first can not be null;");
        Validate.notNull(second, "second can not be null;");

        return new Iterable<T>() {
            @Override
            public Iterator<T> iterator() {
                final Iterator<T> firstIterator = first.iterator();
                final Iterator<T> secondIterator = second.iterator();

                return new Iterator<T>() {
                    @Override
                    public boolean hasNext() {
                        return firstIterator.hasNext() || secondIterator.hasNext();
                    }

                    @Override
                    public T next() {
                        return firstIterator.hasNext() ? firstIterator.next() : secondIterator.next();
                    }
                };
            }
        };
    }

}

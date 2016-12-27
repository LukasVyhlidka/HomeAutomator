package org.vyhlidka.homeautomation.util;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by lucky on 27.12.16.
 */
public class IterableUtilTest {

    @Test(expected = NullPointerException.class)
    public void testWithFirstNull() throws Exception {
        IterableUtil.concat(null, new ArrayList<>());
    }

    @Test(expected = NullPointerException.class)
    public void testWithSecondNull() throws Exception {
        IterableUtil.concat(null, new ArrayList<>());
    }

    @Test
    public void testEmpty() throws Exception {
        Iterable<String> iterable = IterableUtil.concat(new ArrayList<>(), new ArrayList<>());

        assertThat(iterable.iterator()).isNotNull();
        assertThat(iterable).isEmpty();
        assertThat(iterable.iterator().hasNext()).isFalse();
    }

    @Test
    public void testSecondEmpty() throws Exception {
        Iterable<String> iterable = IterableUtil.concat(Arrays.asList("one", "two"), new ArrayList<>());
        assertThat(iterable).containsExactly("one", "two");
    }

    @Test
    public void testFirstEmpty() throws Exception {
        Iterable<String> iterable = IterableUtil.concat(new ArrayList<>(), Arrays.asList("one", "two"));
        assertThat(iterable).containsExactly("one", "two");
    }

    @Test
    public void testConcat() throws Exception {
        Iterable<String> iterable = IterableUtil.concat(
                Arrays.asList("one", "two"),
                Arrays.asList("three", "four", "five"));

        // multiple iteration
        assertThat(iterable).containsExactly("one", "two", "three", "four", "five");
        assertThat(iterable).containsExactly("one", "two", "three", "four", "five");
        assertThat(iterable).containsExactly("one", "two", "three", "four", "five");
    }
}
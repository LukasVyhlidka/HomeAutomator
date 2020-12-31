package org.vyhlidka.homeautomation.util;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by lucky on 27.12.16.
 */
public class IterableUtilTest {

    @Test
    public void testWithFirstNull() throws Exception {
        assertThatThrownBy(() -> IterableUtil.concat(null, new ArrayList<>())).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testWithSecondNull() throws Exception {
        assertThatThrownBy(() -> IterableUtil.concat(null, new ArrayList<>())).isInstanceOf(NullPointerException.class);
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
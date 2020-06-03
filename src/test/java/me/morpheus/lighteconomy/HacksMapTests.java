package me.morpheus.lighteconomy;

import me.morpheus.lighteconomy.util.hacks.Reference2ObjectOpenHacksMap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class HacksMapTests {

    @DisplayName("merge empty")
    @Test
    void merge1() {
        final Map<Object, Object> map = new Reference2ObjectOpenHacksMap<>();
        final Object key = new Object();
        final Object value = new Object();
        final Object remapped = new Object();
        final Object result = map.merge(key, value, (o, v) -> remapped);
        assertSame(value, result);
        assertSame(value, map.get(key));
    }

    @DisplayName("merge null value")
    @Test
    void merge2() {
        final Map<Object, Object> map = new Reference2ObjectOpenHacksMap<>();
        final Object key = new Object();
        map.put(key, null);
        final Object value = new Object();
        final Object remapped = new Object();
        final Object result = map.merge(key, value, (o, v) -> remapped);
        assertSame(value, result);
        assertSame(value, map.get(key));
    }

    @DisplayName("merge old value")
    @Test
    void merge3() {
        final Map<Object, Object> map = new Reference2ObjectOpenHacksMap<>();
        final Object key = new Object();
        final Object old = new Object();
        map.put(key, old);
        final Object value = new Object();
        final Object remapped = new Object();
        final Object result = map.merge(key, value, (o, v) -> remapped);
        assertSame(remapped, result);
        assertSame(remapped, map.get(key));
    }

    @DisplayName("merge null function")
    @Test
    void merge4() {
        final Map<Object, Object> map = new Reference2ObjectOpenHacksMap<>();
        final Object key = new Object();
        final Object old = new Object();
        map.put(key, old);
        final Object value = new Object();
        final Object result = map.merge(key, value, (o, v) -> null);
        assertNull(result);
        assertTrue(map.isEmpty());
    }

    @DisplayName("computeIfPresent empty")
    @Test
    void computeIfPresent1() {
        final Map<Object, Object> map = new Reference2ObjectOpenHacksMap<>();
        final Object key = new Object();
        final Object remapped = new Object();
        final Object result = map.computeIfPresent(key, (k, o) -> remapped);
        assertNull(result);
        assertTrue(map.isEmpty());
    }

    @DisplayName("computeIfPresent null value")
    @Test
    void computeIfPresent2() {
        final Map<Object, Object> map = new Reference2ObjectOpenHacksMap<>();
        final Object key = new Object();
        map.put(key, null);
        final Object remapped = new Object();
        final Object result = map.computeIfPresent(key, (k, o) -> remapped);
        assertNull(result);
        assertNull(map.get(key));
        assertEquals(1, map.size());
    }

    @DisplayName("computeIfPresent old value")
    @Test
    void computeIfPresent3() {
        final Map<Object, Object> map = new Reference2ObjectOpenHacksMap<>();
        final Object key = new Object();
        final Object value = new Object();
        map.put(key, value);
        final Object remapped = new Object();
        final Object result = map.computeIfPresent(key, (k, o) -> remapped);
        assertSame(remapped, result);
        assertSame(remapped, map.get(key));
    }

    @DisplayName("merge null function")
    @Test
    void computeIfPresent4() {
        final Map<Object, Object> map = new Reference2ObjectOpenHacksMap<>();
        final Object key = new Object();
        final Object old = new Object();
        map.put(key, old);
        final Object result = map.computeIfPresent(key, (k, o) -> null);
        assertNull(result);
        assertTrue(map.isEmpty());
    }
}

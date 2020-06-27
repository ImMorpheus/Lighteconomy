package me.morpheus.lighteconomy;

import me.morpheus.lighteconomy.util.hacks.Reference2ObjectOpenHacksMap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

final class HacksMapTests {

    @DisplayName("merge empty")
    @ParameterizedTest
    @MethodSource("mapProvider")
    void merge1(Reference2ObjectOpenHacksMap<Object, Object> custom, Map<Object, Object> map) {
        final Object key = new Object();
        final Object value = new Object();
        final Object remapped = new Object();
        assertSame(map.merge(key, value, (o, v) -> remapped), custom.merge(key, value, (o, v) -> remapped));
        assertSame(map.get(key), custom.get(key));
        assertEquals(map, custom);
    }

    @DisplayName("merge null value")
    @ParameterizedTest
    @MethodSource("mapProvider")
    void merge2(Reference2ObjectOpenHacksMap<Object, Object> custom, Map<Object, Object> map) {
        final Object key = new Object();
        map.put(key, null);
        custom.put(key, null);
        final Object value = new Object();
        final Object remapped = new Object();
        assertSame(map.merge(key, value, (o, v) -> remapped), custom.merge(key, value, (o, v) -> remapped));
        assertSame(map.get(key), custom.get(key));
        assertEquals(map, custom);
    }

    @DisplayName("merge old value")
    @ParameterizedTest
    @MethodSource("mapProvider")
    void merge3(Reference2ObjectOpenHacksMap<Object, Object> custom, Map<Object, Object> map) {
        final Object key = new Object();
        final Object old = new Object();
        map.put(key, old);
        custom.put(key, old);
        final Object value = new Object();
        final Object remapped = new Object();
        assertSame(map.merge(key, value, (o, v) -> remapped), custom.merge(key, value, (o, v) -> remapped));
        assertSame(map.get(key), custom.get(key));
        assertEquals(map, custom);
    }

    @DisplayName("merge null function")
    @ParameterizedTest
    @MethodSource("mapProvider")
    void merge4(Reference2ObjectOpenHacksMap<Object, Object> custom, Map<Object, Object> map) {
        final Object key = new Object();
        final Object old = new Object();
        map.put(key, old);
        custom.put(key, old);
        final Object value = new Object();
        assertSame(map.merge(key, value, (o, v) -> null), custom.merge(key, value, (o, v) -> null));
        assertEquals(map, custom);
    }

    @DisplayName("computeIfPresent empty")
    @ParameterizedTest
    @MethodSource("mapProvider")
    void computeIfPresent1(Reference2ObjectOpenHacksMap<Object, Object> custom, Map<Object, Object> map) {
        final Object key = new Object();
        final Object remapped = new Object();
        assertSame(map.computeIfPresent(key, (k, o) -> remapped), custom.computeIfPresent(key, (k, o) -> remapped));
        assertEquals(map, custom);
    }

    @DisplayName("computeIfPresent null value")
    @ParameterizedTest
    @MethodSource("mapProvider")
    void computeIfPresent2(Reference2ObjectOpenHacksMap<Object, Object> custom, Map<Object, Object> map) {
        final Object key = new Object();
        map.put(key, null);
        custom.put(key, null);
        final Object remapped = new Object();
        assertSame(map.computeIfPresent(key, (k, o) -> remapped), custom.computeIfPresent(key, (k, o) -> remapped));
        assertSame(map.get(key), custom.get(key));
        assertEquals(map, custom);
    }

    @DisplayName("computeIfPresent old value")
    @ParameterizedTest
    @MethodSource("mapProvider")
    void computeIfPresent3(Reference2ObjectOpenHacksMap<Object, Object> custom, Map<Object, Object> map) {
        final Object key = new Object();
        final Object value = new Object();
        map.put(key, value);
        custom.put(key, value);
        final Object remapped = new Object();
        assertSame(map.computeIfPresent(key, (k, o) -> remapped), custom.computeIfPresent(key, (k, o) -> remapped));
        assertSame(map.get(key), custom.get(key));
        assertEquals(map, custom);
    }

    @DisplayName("merge null function")
    @ParameterizedTest
    @MethodSource("mapProvider")
    void computeIfPresent4(Reference2ObjectOpenHacksMap<Object, Object> custom, Map<Object, Object> map) {
        final Object key = new Object();
        final Object old = new Object();
        map.put(key, old);
        custom.put(key, old);
        assertSame(map.computeIfPresent(key, (k, o) -> null), custom.computeIfPresent(key, (k, o) -> null));
        assertEquals(map, custom);
    }

    static Stream<Arguments> mapProvider() {
        return Stream.of(
                Arguments.arguments(new Reference2ObjectOpenHacksMap<>(), new HashMap<>())
        );
    }
}

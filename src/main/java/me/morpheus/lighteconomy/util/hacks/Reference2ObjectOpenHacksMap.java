package me.morpheus.lighteconomy.util.hacks;

public class Reference2ObjectOpenHacksMap<K, V> extends Reference2ObjectOpenHashMap<K, V> {

    public Reference2ObjectOpenHacksMap(final int expected) {
        super(expected);
    }

    public Reference2ObjectOpenHacksMap() {
        super();
    }

    @Override
    public V merge(final K k, final V v,
                        final java.util.function.BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        java.util.Objects.requireNonNull(remappingFunction);
        final int pos = find(k);
        if (pos < 0) {
            insert(-pos - 1, k, v);
            return v;
        }
        final V newValue = remappingFunction.apply(this.value[pos], v);
        if (newValue == null) {
            if (((k) == (null)))
                removeNullEntry();
            else
                removeEntry(pos);
            return this.defRetValue;
        }
        return this.value[pos] = newValue;
    }

    @Override
    public V computeIfPresent(final K k,
                                   final java.util.function.BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        java.util.Objects.requireNonNull(remappingFunction);
        final int pos = find(k);
        if (pos < 0)
            return this.defRetValue;
        final V newValue = remappingFunction.apply((k), this.value[pos]);
        if (newValue == null) {
            if (((k) == (null)))
                removeNullEntry();
            else
                removeEntry(pos);
            return this.defRetValue;
        }
        return this.value[pos] = newValue;
    }

}

package cat.ella.kissui.util;

import org.apache.logging.log4j.util.BiConsumer;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.function.IntSupplier;

public record Int2Map(int initialCapacity) {
    private static int[] keys;
    private static int[] values;
    private static int size = 0;

    public Int2Map {
        if (initialCapacity <= 0) throw new IllegalArgumentException("initial capacity must be greater than 0");
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int get(int key) {
        if (size == 0) return 0;
        int index = Arrays.binarySearch(keys, 0, size, key);
        return index >= 0 ? values[index] : 0;
    }

    public void put(int key, int value) {
        set(key, value);
    }

    public void set(int key, int value) {
        if (size == 0) {
            keys[0] = key;
            values[0] = value;
            size = 1;
            return;
        }
        int index = Arrays.binarySearch(keys, 0, size, key);
        if (index >= 0) {
            values[index] = value;
        } else {
            index = -index - 1;
            if (size == keys.length) resize(keys.length * 2);
            if (index == size) {
                keys[size] = key;
                values[size] = value;
            } else {
                System.arraycopy(keys, index, keys, index + 1, size - index);
                System.arraycopy(values, index, values, index + 1, size - index);
                keys[index] = key;
                values[index] = value;
            }
            size++;
        }
    }

    public boolean containsKey(int key) {
        return size != 0 && Arrays.binarySearch(keys, 0, size, key) >= 0;
    }

    public boolean containsValue(int value) {
        for (int i = 0; i < size; i++) if (values[i] == value) return true;
        return false;
    }

    public int remove(int key) {
        if (size == 0) return 0;
        int index = Arrays.binarySearch(keys, 0, size, key);
        if (index < 0) return 0;
        int old = values[index];
        System.arraycopy(keys, index + 1, keys, index, size - index - 1);
        System.arraycopy(values, index + 1, values, index, size - index - 1);
        size--;
        return old;
    }

    public void trim() {
        resize(size);
    }

    public void clear() {
        size = 0;
    }

    public int getOrPut(int key, IntSupplier defaultValue) {
        int index = size == 0 ? -1 : Arrays.binarySearch(keys, 0, size, key);
        if (index >= 0) return values[index];
        int value = defaultValue.getAsInt();
        set(key, value);
        return value;
    }

    public void forEach(BiConsumer<Integer, Integer> action) {
        for (int i = 0; i < size; i++) action.accept(keys[i], values[i]);
    }

    public Iter iterator() {
        return new Iter();
    }

    public class Iter {
        private int i = 0;
        public boolean hasNext() {
            return i < size;
        }

        public Entry next() {
            if (i >= size) throw new NoSuchElementException();
            long data = ((long) keys[i] << 32) | (values[i] & 0xFFFFFFFFL);
            return new Entry(data, i++);
        }
    }

    public static class Entry {
        private final long data;
        private final int index;

        public Entry(long data, int index) {
            this.data = data;
            this.index = index;
        }

        public int getKey() {
            return (int) (data >> 32);
        }

        public int getValue() {
            return (int) data;
        }

        public int component1() {
            return getKey();
        }

        public int component2() {
            return getValue();
        }
    }

    private void resize(int newSize) {
        keys = Arrays.copyOf(keys, newSize);
        values = Arrays.copyOf(values, newSize);
    }
}

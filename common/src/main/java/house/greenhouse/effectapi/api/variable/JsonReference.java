package house.greenhouse.effectapi.api.variable;

public interface JsonReference {

    /**
     * Creates a combined object, representing a JSONArray's key and a value within that array.
     * This is provided for convenience within datagen.
     *
     * @param key   The key of the array in the overhead object.
     * @param index The index of the value to get in the array.
     */
    static JsonReference createArrayInObject(String key, int index) {
        if (index < 0)
            throw new IllegalStateException("Cannot create an array value with an index less than 0.");
        return new ArrayInObject(key, index);
    }

    static JsonReference createArrayValue(int index) {
        if (index < 0)
            throw new IllegalStateException("Cannot create an array value with an index less than 0.");
        return new ArrayValue(index);
    }

    static JsonReference createObject(String key) {
        return new Object(key);
    }

    default boolean isArrayInObject() {
        return this instanceof ArrayInObject;
    }

    default boolean isArrayValue() {
        return this instanceof ArrayValue;
    }

    default boolean isObject() {
        return this instanceof Object;
    }

    default int index() {
        return -1;
    }

    default String key() {
        return "";
    }

    record ArrayInObject(String key, int index) implements JsonReference {
        @Override
        public int index() {
            return index;
        }

        @Override
        public String key() {
            return key;
        }
    }

    record ArrayValue(int index) implements JsonReference {
        @Override
        public int index() {
            return index;
        }
    }

    record Object(String key) implements JsonReference {
        @Override
        public String key() {
            return key;
        }
    }
}

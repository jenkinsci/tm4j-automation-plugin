package com.adaptavist.tm4j.jenkins.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

public class GsonUtils {
    /**
     * This allows us to overwrite Gson's behavior where it converts
     * an integer to a floating number, e.g. 50 -> 50.0
     * <p>
     * That is a problem with the custom field values, as if you want
     * to set the value of a `number` custom field and send `10` in
     * the UI, the value will be sent as `10.0` instead.
     * <p>
     * The behavior for floating numbers remains the same.
     */
    public static Gson getInstance() {
        return new GsonBuilder().
            registerTypeAdapter(Double.class, (JsonSerializer<Double>) (sourceValue, type, context) -> {
                if (sourceValue == sourceValue.longValue()) {
                    return new JsonPrimitive(sourceValue.longValue());
                }
                return new JsonPrimitive(sourceValue);
            }).create();
    }
}

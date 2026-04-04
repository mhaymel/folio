package com.util;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static com.util.Precondition.nn;

public final class ToString {

    private final Object object;

    public ToString(Object object) {
        this.object = nn(object);
    }

    public static String asString(Object o) {
        return new ToString(o).asString();
    }

    public String asString() {
        ToStringHelper stringHelper = MoreObjects.toStringHelper(object);
        for (Field field : object.getClass().getDeclaredFields()) {
            if (!isaStatic(field))
                add(stringHelper, field);
        }

        return stringHelper.toString();
    }

    private static boolean isaStatic(Field field) {
        return Modifier.isStatic(field.getModifiers());
    }

    private void add(ToStringHelper stringHelper, Field field) {
        boolean wasAccessible = field.canAccess(object);
        field.setAccessible(true);
        stringHelper.add(field.getName(), value(field));
        field.setAccessible(wasAccessible);
    }

    private Object value(Field field) {
        try {
            return field.get(object);
        } catch (IllegalAccessException e) {
            return "<illegal access exception>";
        }
    }
}

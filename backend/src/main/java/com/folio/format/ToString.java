package com.folio.format;

import com.folio.precondition.Precondition;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static com.folio.precondition.Precondition.notNull;

public final class ToString {

    private final Object object;

    public ToString(Object object) {
        this.object = Precondition.notNull(object);
    }

    public static String asString(Object o) {
        return new ToString(o).asString();
    }

    public String asString() {
        ToStringHelper stringHelper = MoreObjects.toStringHelper(object);
        for (Field field : object.getClass().getDeclaredFields()) {
            if (!isStatic(field))
                add(stringHelper, field);
        }

        return stringHelper.toString();
    }

    private static boolean isStatic(Field field) {
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
        } catch (IllegalAccessException exception) {
            return "<illegal access exception>";
        }
    }
}

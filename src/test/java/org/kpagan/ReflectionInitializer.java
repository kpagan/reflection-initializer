package org.kpagan;


import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class ReflectionInitializer {

    private Random r = new Random();

    public <T> T initialize(Class<T> type, Set<String> fieldsToSet) throws Exception {
        T instance = type.newInstance();
        ReflectionUtils.doWithFields(type, field -> {
                    try {
                        field.setAccessible(true);
                        Class<?> fieldClass = field.getType();
                        if (List.class.isAssignableFrom(fieldClass)) {
                            initializeList(instance, field, fieldsToSet);
                        } else if (isCustomObject(fieldClass)) {
                            field.set(instance, initialize(fieldClass, fieldsToSet));
                        } else {
                            field.set(instance, initializeValue(fieldClass));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                field -> fieldsToSet.contains(field.getName())
        );
        return instance;
    }

    private <T> T initializeValue(Class<T> fieldClass) throws Exception {
        if (String.class.equals(fieldClass)) {
            Constructor<T> constructor = fieldClass.getConstructor(String.class);
            return constructor.newInstance(RandomTextGenerator.getRandomText(r.nextInt(10)));
        } else {
            return fieldClass.newInstance();
        }
    }

    private <T> void initializeList(T instance, Field field, Set<String> fieldsToSet) throws Exception {
        Type genericType = field.getGenericType();
        String className = getInnerType(genericType.toString());
        Class<?> aClass = Class.forName(className);

        int len = r.nextInt(10) + 1;
        List list = new ArrayList(len);
        for (int i = 0; i < len; i++) {
            list.add(initialize(aClass, fieldsToSet));
        }
        field.set(instance, list);
    }

    private String getInnerType(String genericType) {
        return genericType.substring(genericType.indexOf('<') + 1, genericType.lastIndexOf('>'));
    }

    private boolean isCustomObject(Class<?> clazz) {
        boolean ret = false;

        if (clazz.isArray()) {
            clazz = clazz.getComponentType();
        }
        if (!clazz.isPrimitive() && !clazz.isEnum()) {
            String className = clazz.getName();
            if (className.startsWith("java.") && !className.startsWith("java.util")) {
                ret = false;
            } else {
                ret = true;
            }
        }

        return ret;
    }
}

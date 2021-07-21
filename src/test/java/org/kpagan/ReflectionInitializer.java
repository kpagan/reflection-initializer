package org.kpagan;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;

public class ReflectionInitializer {

    private static Map<String, Tuple2<Class, Object>> primitiveValues = new HashMap<>();

    static {
        primitiveValues.put("long", new Tuple2<>(Long.class, 1L));
        primitiveValues.put("int", new Tuple2<>(Integer.class, 1));
        primitiveValues.put("boolean", new Tuple2<>(Boolean.class, true));

    }

    private Random r = new Random();

    public <T> T initialize(Class<T> type, @NonNull Set<String> fieldsToSet) throws Exception {
        T instance;
        try {
            if (isCustomObject(type)) {
                instance = type.newInstance();
            } else {
                instance = initializeValue(type);
            }
        } catch (InstantiationException e) {
            instance = type.cast(instantiateUsingConstructor(type, fieldsToSet));
        }
        if (isCustomObject(type)) {
            return initializeInstance(type, fieldsToSet, instance);
        } else {
            return instance;
        }
    }

    private <T> T initializeInstance(Class<T> type, Set<String> fieldsToSet, T instance) {
        ReflectionUtils.FieldCallback fieldCallback = field -> {
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
        };

        if (fieldsToSet.isEmpty()) {
            ReflectionUtils.doWithFields(type, fieldCallback);
        } else {
            ReflectionUtils.doWithFields(type, fieldCallback,
                    field -> fieldsToSet.contains(field.getName())
            );
        }
        return instance;
    }

    private <T> Object instantiateUsingConstructor(Class<T> type, Set<String> fieldsToSet) throws Exception {
        for (Constructor constructor: type.getConstructors()) {
            List<Object> parameters = new ArrayList();
            for (Class parameter: constructor.getParameterTypes()) {
                Object initialized = initialize(parameter, fieldsToSet);
                parameters.add(initialized);
            }
            return constructor.newInstance(parameters.toArray());
        }
        return null;
    }

    private <T> T initializeValue(Class<T> fieldClass) throws Exception {
        if (String.class.equals(fieldClass)) {
            Constructor<T> constructor = fieldClass.getConstructor(String.class);
            return constructor.newInstance(RandomTextGenerator.getRandomText(1));
        } else if (Boolean.class.equals(fieldClass)) {
            Constructor<T> constructor = fieldClass.getConstructor(boolean.class);
            return constructor.newInstance(true);
        } else if (fieldClass.isPrimitive()) {
            Class<T> wrapperClass = primitiveValues.get(fieldClass.getName()).getT1();
            return initializePrimitive(wrapperClass, fieldClass);
        } else if (isCustomObject(fieldClass)){
            return fieldClass.newInstance();
        } else {
            return null;
        }
    }

    private <T> T initializePrimitive(Class<T> wrapperClass, Class<T> fieldClass) throws Exception {
        Tuple2<Class, Object> tuple = primitiveValues.get(fieldClass.getName());
        Constructor<T> constructor = wrapperClass.getConstructor(fieldClass);
        return constructor.newInstance(tuple.getT2());
    }

    private <T> void initializeList(T instance, Field field, Set<String> fieldsToSet) throws Exception {
        Type genericType = field.getGenericType();
        String className = getInnerType(genericType.toString());
        Class<?> aClass = Class.forName(className);

        int len = r.nextInt(10) + 1;
        List list = new ArrayList(len);
        for (int i = 0; i < len; i++) {
            if (isCustomObject(aClass)) {
                list.add(initialize(aClass, fieldsToSet));
            } else {
                list.add(initializeValue(aClass));
            }
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
            if (className.startsWith("java.")) {
                ret = false;
            } else {
                ret = true;
            }
        }

        return ret;
    }

    @Data
    @Builder
    private static class Tuple2<T1, T2> {
        private T1 t1;
        private T2 t2;
    }

}

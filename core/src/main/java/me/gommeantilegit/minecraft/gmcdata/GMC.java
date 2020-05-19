package me.gommeantilegit.minecraft.gmcdata;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

public class GMC {

    //TODO: UNIT TEST

    public void toGMC(@NotNull OutputStream stream, @NotNull Object source) throws IOException {
        DataOutputStream dataOut = new DataOutputStream(stream);
        Class<?> type = source.getClass();
        Map<String, Field> properties = getProperties(type);
        List<Object> values = getValues(properties, source);
        int i = 0;
        for (Map.Entry<String, Field> property : properties.entrySet()) {
            String name = property.getKey();

            Object value = values.get(i);
            int hash = name.hashCode();
            if (hash == -1)
                throw new RuntimeException("Hash of property \"" + name + "\" is -1, which is not allowed. Use a different property name.");
            dataOut.writeInt(hash);
            writeProperty(dataOut, value);
            i++;
        }
        dataOut.writeInt(-1); // terminator
    }

    private void writeProperty(@NotNull DataOutputStream dataOut, @NotNull Object value) throws IOException {
        if (value instanceof String) {
            dataOut.writeUTF((String) value);
        } else if (value instanceof Byte) {
            dataOut.writeByte((Byte) value);
        } else if (value instanceof Short) {
            dataOut.writeShort((Short) value);
        } else if (value instanceof Integer) {
            dataOut.writeInt((Integer) value);
        } else if (value instanceof Long) {
            dataOut.writeLong((Long) value);
        } else if (value instanceof Character) {
            dataOut.writeChar((Character) value);
        } else if (value instanceof Float) {
            dataOut.writeFloat((Float) value);
        } else if (value instanceof Double) {
            dataOut.writeDouble((Double) value);
        } else if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            dataOut.writeInt(length);
            for (int j = 0; j < length; j++) {
                writeProperty(dataOut, Array.get(value, j));
            }
        } else {
            toGMC(dataOut, value);
        }
    }

    @NotNull
    private List<Object> getValues(@NotNull Map<String, Field> properties, @NotNull Object source) {
        List<Object> values = new ArrayList<>(properties.size());
        for (Map.Entry<String, Field> property : properties.entrySet()) {
            Field field = property.getValue();
            Object value;
            try {
                field.setAccessible(true);
                value = field.get(source);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Invalid GMC serializable type: " + source.getClass().getSimpleName(), e);
            }
            values.add(value);
        }
        return values;
    }

    @NotNull
    public <T> T fromGMC(@NotNull InputStream stream, @NotNull Class<T> type) throws GMCCorruptedDataException, IOException {
        return fromGMC(stream, type, IDefaultValueProvider.NONE);
    }

    @NotNull
    public <T> T fromGMC(@NotNull InputStream stream, @NotNull Class<T> type, @NotNull IDefaultValueProvider provider) throws GMCCorruptedDataException, IOException {
        T t = deserializeGMC(stream, type, provider);
        if (stream.available() != 0)
            throw new GMCCorruptedDataException("Random bytes at the end of GMC stream!");
        return t;
    }

    @NotNull
    private <T> T deserializeGMC(@NotNull InputStream stream, @NotNull Class<T> type, @NotNull IDefaultValueProvider provider) throws GMCCorruptedDataException, IOException {
        Map<String, Field> properties = getProperties(type);
        Object[] valuesArray;
        try {
            valuesArray = getValues(stream, type, properties, provider);
        } catch (EOFException e) {
            throw new GMCCorruptedDataException("EOF reached!");
        }

        Class<?>[] typesArray = getTypes(properties);

        try {
            Constructor<T> constructor = type.getConstructor(typesArray);
            return constructor.newInstance(valuesArray);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Invalid GMC serializable type: " + type.getSimpleName(), e);
        }
    }

    @NotNull
    private Object[] getValues(@NotNull InputStream stream, @NotNull Class<?> classType, @NotNull Map<String, Field> properties, @NotNull IDefaultValueProvider provider) throws IOException, GMCCorruptedDataException {
        Object[] objects = new Object[properties.size()];
        DataInputStream dataIn = new DataInputStream(stream);
        int readHashCode;
        Set<String> handledProperties = new HashSet<>();
        Set<Map.Entry<String, Field>> entries = properties.entrySet();
        List<String> propertyNames = entries.stream().map(Map.Entry::getKey).collect(Collectors.toUnmodifiableList());

        while ((readHashCode = dataIn.readInt()) != -1) { // object terminator --> -1
            for (Map.Entry<String, Field> property : entries) {
                String propertyName = property.getKey();
                int propertyHashCode = propertyName.hashCode();

                if (readHashCode != propertyHashCode) {
                    continue;
                }

                Field field = property.getValue();
                Class<?> propertyType = field.getType();
                Object value = readValue(dataIn, propertyType, provider);
                handledProperties.add(propertyName);
                objects[propertyNames.indexOf(propertyName)] = value;
                break;
            }
        }
        for (
                Map.Entry<String, Field> entry : entries) {
            String propertyName = entry.getKey();
            if (handledProperties.contains(propertyName)) {
                continue;
            }
            int hash = propertyName.hashCode();
            Object value = provider.getValue(hash, classType);
            if (value == null)
                throw new GMCCorruptedDataException("GMC data provided value for property hash code " + hash + ", not expected by the default value provider.");
            objects[propertyNames.indexOf(propertyName)] = value;
        }
        return objects;
    }

    @NotNull
    private Object readValue(@NotNull DataInputStream dataIn, @NotNull Class<?> propertyType, @NotNull IDefaultValueProvider provider) throws IOException {
        Object value;
        if (propertyType == String.class) {
            value = dataIn.readUTF();
        } else if (propertyType == Byte.class || propertyType == byte.class) {
            value = dataIn.readByte();
        } else if (propertyType == Short.class || propertyType == short.class) {
            value = dataIn.readShort();
        } else if (propertyType == Integer.class || propertyType == int.class) {
            value = dataIn.readInt();
        } else if (propertyType == Long.class || propertyType == long.class) {
            value = dataIn.readLong();
        } else if (propertyType == Character.class || propertyType == char.class) {
            value = dataIn.readChar();
        } else if (propertyType == Float.class || propertyType == float.class) {
            value = dataIn.readFloat();
        } else if (propertyType == Double.class || propertyType == double.class) {
            value = dataIn.readDouble();
        } else if (propertyType.isArray()) {
            value = deserializeArray(dataIn, propertyType, provider);
        } else {
            value = deserializeGMC(dataIn, propertyType, provider);
        }
        return value;
    }

    @NotNull
    private Object deserializeArray(@NotNull DataInputStream dataIn, @NotNull Class<?> propertyType, @NotNull IDefaultValueProvider provider) throws IOException {
        int length = dataIn.readInt();
        if (length < 0)
            throw new GMCCorruptedDataException("Negative array size: " + propertyType);
        Class<?> componentType = propertyType.getComponentType();
        Object array = Array.newInstance(componentType, length);
        for (int i = 0; i < length; i++) {
            Array.set(array, i, readValue(dataIn, componentType, provider));
        }
        return array;
    }

    @NotNull
    private <T> Class<?>[] getTypes(@NotNull Map<T, Field> fields) {
        Class<?>[] types = new Class[fields.size()];
        int i = 0;
        for (Map.Entry<T, Field> field : fields.entrySet()) {
            types[i++] = (field.getValue().getType());
        }
        return types;
    }

    @NotNull
    private <T> Map<String, Field> getProperties(@NotNull Class<T> type) {
        Field[] fields = type.getDeclaredFields();
        Map<String, Field> properties = new LinkedHashMap<>(8, 0.75f, true); // keep order (field declaration order = constructor order)
        for (Field field : fields) {
            if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers())) {
                if (field.isAnnotationPresent(SerializedProperty.class)) {
                    SerializedProperty property = field.getAnnotation(SerializedProperty.class);
                    if (properties.put(property.value(), field) != null) {
                        throw new RuntimeException("Property name hash collision: \"" + property.value() + "\"");
                    }
                } else {
                    if (properties.put(field.getName(), field) != null) {
                        throw new RuntimeException("Property name hash collision: \"" + field.getName() + "\"");
                    }
                }
            }
        }
        return properties;
    }

    public interface IDefaultValueProvider {

        @NotNull
        IDefaultValueProvider NONE = (propertyHash, beanType) -> null;

        @Nullable
        Object getValue(int propertyHash, @NotNull Class<?> beanType);

    }

}
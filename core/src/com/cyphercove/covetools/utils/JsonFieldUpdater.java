/*******************************************************************************
 * Copyright 2017 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.cyphercove.covetools.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.io.StringWriter;
import java.security.AccessControlException;
import java.util.Locale;

/**
 * Reads values of Json fields and assigns them to values in existing objects or classes.
 * <p>
 * This class allows for updating parameters during runtime by updating a JSON file and reloading it in response to input.
 * <p>
 * By default errors are logged rather than throwing RuntimeExceptions when there are problems parsing the files
 * or applying values. This is because a common use is for quickly testing parameter changes without a rebuild, and it's
 * easy to create a syntax error in Json. Errors can instead be thrown using {@link #setThrowErrors(boolean) setThrowErrors(true)}.
 * <p>
 * There are some special case classes which can be specified in convenient ways:
 * <ul>
 * <li><b>Color</b> can be specified with a single field named "{@code name}" whose value matches one of the constant
 * field names in the Color class.</li>
 * <li><b>Color</b> can be specified with a single field named "{@code hex}" that is the RGBA value in hexadecimal.</li>
 * <li><b>Interpolation</b> can be specified with a single field named "{@code name}" whose value matches one of the
 * constant field names in the Interpolation class.</li>
 * <li><b>Interpolation</b> can be specified with a single field named "{@code expression}" which consists of a
 * mathematical expression involving a parameter "{@code a}", to be used for evaluating the
 * {@link Interpolation#apply(float)} method. The syntax is from the
 * <a href="http://projects.congrace.de/exp4j/index.html">exp4j library</a>.</li>
 * </ul>
 *
 * @author cypherdare
 */
public class JsonFieldUpdater {

    private static final String TAG = JsonFieldUpdater.class.getName();

    private boolean throwErrors;
    private Json json;
    private ObjectMap<Class, OrderedMap<String, Field>> staticFields = new ObjectMap<Class, OrderedMap<String, Field>>();

    public JsonFieldUpdater() {
        this(new Json());
    }

    public JsonFieldUpdater(Json json) {
        this.json = json;
        json.setIgnoreUnknownFields(true);

        json.setSerializer(Color.class, new Json.ReadOnlySerializer<Color>() {
            public Color read(Json json, JsonValue jsonData, Class type) {
                String name = json.readValue("name", String.class, (String) null, jsonData);
                if (name != null) return getStaticFieldValue(Color.class, name.toUpperCase(Locale.US));
                String hex = json.readValue("hex", String.class, (String) null, jsonData);
                if (hex != null) return Color.valueOf(hex);
                float r = json.readValue("r", float.class, 0f, jsonData);
                float g = json.readValue("g", float.class, 0f, jsonData);
                float b = json.readValue("b", float.class, 0f, jsonData);
                float a = json.readValue("a", float.class, 1f, jsonData);
                return new Color(r, g, b, a);
            }
        });

        json.setSerializer(Interpolation.class, new Json.ReadOnlySerializer<Interpolation>() {
            public Interpolation read(Json json, JsonValue jsonData, Class type) {
                String name = json.readValue("name", String.class, (String) null, jsonData);
                if (name != null) return getStaticFieldValue(Interpolation.class, name);
                final String expression = json.readValue("expression", String.class, (String) null, jsonData);
                if (expression != null) {
                    return new Interpolation() {
                        Expression e = new ExpressionBuilder(expression).variables("a").build();

                        public float apply(float a) {
                            e.setVariable("a", a);
                            return (float) e.evaluate();
                        }
                    };
                }
                error("Could not evaluate interpolation field for " + jsonData.name() + ". Applying linear interpolation.", null);
                return Interpolation.linear;
            }
        });
    }

    public Json getJson() {
        return json;
    }

    /**
     * @param throwErrors Whether to throw GdxRuntimeExceptions in response to read errors rather than logging an error
     *                    message and silently failing.
     */
    public void setThrowErrors(boolean throwErrors) {
        this.throwErrors = throwErrors;
    }

    public void readFieldsToObjects(FileHandle jsonFile, Object... objects) {
        readFieldsToObjects(jsonFile.readString(), objects);
    }

    /**
     * Reads any defined classes from the JSON and applies their field values to the given objects.
     * <p>
     * The JSON must contain a set of children, whose names correspond to classes and whose children are its field names
     * to their values.
     *
     * @param jsonText The String representing the JSON.
     * @param objects  A set of classes that can be referenced by simple name in the JSON instead of fully qualified names.
     *                 If a given object is a class, static fields will be written instead of object fields.
     */
    public void readFieldsToObjects(String jsonText, Object... objects) {
        ObjectMap<String, Class> tagsToClasses = new ObjectMap<String, Class>(objects.length);
        for (Object object : objects) {
            Class type = object instanceof Class ? (Class) object : object.getClass();
            tagsToClasses.put(type.getSimpleName(), type);
        }

        JsonValue root;
        try {
            root = new JsonReader().parse(jsonText);
        } catch (SerializationException e) {
            error("Failed to parse file.\n", e);
            return;
        }

        outer:
        for (Object object : objects) {
            if (object instanceof Class) {
                Class type = (Class) object;
                for (JsonValue child : root) {
                    if (getClass(child, tagsToClasses) == type) {
                        readStaticFields(type, child);
                        continue outer;
                    }
                }
                error(String.format("Object of type %s not found in the Json.", type), null);
            } else {
                Class type = object.getClass();
                for (JsonValue child : root) {
                    if (getClass(child, tagsToClasses) == type) {
                        json.readFields(object, child);
                        continue outer;
                    }
                }
                error(String.format("Object of type %s not found in the Json.", type), null);
            }
        }
    }

    /**
     * Returns the Class matching the name of the JsonValue, or null if there is no match or it's nameless.
     */
    private Class getClass(JsonValue value, ObjectMap<String, Class> tagsToClasses) {
        if (value.name() != null && value.name().length() > 0) {
            String className = value.name();
            Class type = tagsToClasses.get(className);
            if (type == null) {
                try {
                    type = ClassReflection.forName(className);
                } catch (ReflectionException ex) {
                    type = null;
                }
            }
            return type;
        }
        return null;
    }

    private void readStaticFields(Class target, JsonValue jsonMap) {
        OrderedMap<String, Field> fieldsByName = getStaticFields(target);
        for (JsonValue child : jsonMap) {
            if (!fieldsByName.containsKey(child.name())) {
                error(child.name() + " is not a static field in " + target.getName(), null);
                continue;
            }
            Field field = fieldsByName.get(child.name());
            try {
                field.set(null, json.readValue(field.getType(), null, child));
            } catch (ReflectionException ex) {
                error("Error setting field: " + field.getName() + " (" + target.getName() + ")", ex);
                continue;
            } catch (SerializationException ex) {
                ex.addTrace(field.getName() + " (" + target.getName() + ")");
                error("Error accessing field: " + field.getName() + " (" + target.getName() + ")", ex);
                continue;
            } catch (RuntimeException runtimeEx) {
                SerializationException ex = new SerializationException(runtimeEx);
                ex.addTrace(child.trace());
                ex.addTrace(field.getName() + " (" + target.getName() + ")");
                error("Error accessing field: " + field.getName() + " (" + target.getName() + ")", ex);
            }
        }
    }

    private OrderedMap<String, Field> getStaticFields(Class type) {
        if (staticFields.containsKey(type))
            return staticFields.get(type);
        Field[] fields = ClassReflection.getDeclaredFields(type);
        OrderedMap<String, Field> nameToField = new OrderedMap(fields.length);
        for (Field field : fields) {
            if (!field.isStatic())
                continue;
            if (!field.isAccessible()) {
                try {
                    field.setAccessible(true);
                } catch (AccessControlException ex) {
                    continue;
                }
            }

            nameToField.put(field.getName(), field);
        }
        staticFields.put(type, nameToField);
        return nameToField;
    }

    public <T> T getStaticFieldValue(Class<T> type, String fieldName) {
        OrderedMap<String, Field> fields = getStaticFields(type);
        Field field = fields.get(fieldName, null);
        Throwable throwable = null;
        if (field != null) {
            try {
                return (T) field.get(null);
            } catch (ReflectionException e) {
                throwable = e;
            }
        }
        error(String.format("Could not retrieve static field value %s from class %s.", fieldName, type.getName()), throwable);
        return null;
    }

    private void error(String message, Throwable error) {
        if (throwErrors)
            throw new GdxRuntimeException(message, error);
        else if (error == null)
            Gdx.app.error(TAG, message);
        else
            Gdx.app.error(TAG, message, error);

    }

    /** Generates a json String that is formatted for use as a json file that can be used with JsonFieldUpdater.
     * Useful for creating an initial template of a class object. Only non-transient static fields are populated.
     * @param type The class to read.
     * @returnA json representation of the class's static fields
     */
    public static String toJson (Class type){
        Field[] fields = ClassReflection.getDeclaredFields(type);
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.setOutputType(JsonWriter.OutputType.javascript);
        Json json = new Json();
        json.setWriter(writer);
        json.writeObjectStart();
        json.writeObjectStart(type.getSimpleName());
        for (Field field : fields){
            if (field.isTransient() || !field.isStatic())
                continue;
            try {
                json.writeValue(field.getName(), field.get(type));
            } catch (ReflectionException e){
                Gdx.app.error("Settings", "Failed to write field " + field.getName());
            }
        }
        json.writeObjectEnd();
        json.writeObjectEnd();
        StreamUtils.closeQuietly(writer);
        return json.prettyPrint(writer.getWriter().toString());
    }

    /** Generates a json String that is formatted for use as a json file that can be used with JsonFieldUpdater.
     * Useful for creating an initial template of an object. Only non-transient non-static fields are populated.
     * @param object The object to read.
     * @returnA json representation of the object's member fields
     */
    public static String toJson (Object object){
        Class type = object.getClass();
        Field[] fields = ClassReflection.getDeclaredFields(type);
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.setOutputType(JsonWriter.OutputType.javascript);
        Json json = new Json();
        json.setWriter(writer);
        json.writeObjectStart();
        json.writeObjectStart(type.getSimpleName());
        for (Field field : fields){
            if (field.isTransient() || field.isStatic())
                continue;
            try {
                json.writeValue(field.getName(), field.get(object));
            } catch (ReflectionException e){
                Gdx.app.error("Settings", "Failed to write field " + field.getName());
            }
        }
        json.writeObjectEnd();
        json.writeObjectEnd();
        StreamUtils.closeQuietly(writer);
        return json.prettyPrint(writer.getWriter().toString());
    }

}

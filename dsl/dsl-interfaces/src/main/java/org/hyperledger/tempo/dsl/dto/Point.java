// Copyright 2023 Huawei Cloud Computing Technology Co., Ltd.
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
//     http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and;
// limitations under the License.

// MIT License
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
// 
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
// 
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
// this code is originally from https://mvnrepository.com/artifact/org.influxdb/influxdb-java/tree/2.17/org/influxdb/dto/Point.java

package org.hyperledger.tempo.dsl.dto;

import org.hyperledger.tempo.dsl.annotation.Column;
import org.hyperledger.tempo.dsl.annotation.Measurement;
import org.hyperledger.tempo.dsl.util.StringUtil;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Representation of a InfluxDB database Point.
 */
public class Point {
    private String measurement;
    private Number time;
    private Map<String, String> tags;
    private Map<String, Object> fields;
    private TimeUnit precision = TimeUnit.NANOSECONDS;
    private static final int MAX_FRACTION_DIGITS = 340;
    private static final ThreadLocal<NumberFormat> NUMBER_FORMATTER =
            ThreadLocal.withInitial(() -> {
                NumberFormat numberFormat = NumberFormat.getInstance(Locale.ENGLISH);
                numberFormat.setMaximumFractionDigits(MAX_FRACTION_DIGITS);
                numberFormat.setGroupingUsed(false);
                numberFormat.setMinimumFractionDigits(1);
                return numberFormat;
            });

    private static final int DEFAULT_STRING_BUILDER_SIZE = 1024;
    private static final ThreadLocal<StringBuilder> CACHED_STRINGBUILDERS =
            ThreadLocal.withInitial(() -> new StringBuilder(DEFAULT_STRING_BUILDER_SIZE));

    /**
     * Create a new Point
     *
     * @param measurement the name of the measurement.
     */
    public Point(String measurement) {
        this.measurement = measurement;
    }

    /**
     * Default constructor
     */
    public Point() {
    }

    /**
     * Copy constructor
     * @param otherPoint - point object to copy from
     */
    public Point(Point otherPoint) {
        this.measurement = otherPoint.measurement;
        this.precision = otherPoint.precision;
        this.time = otherPoint.time;
        this.tags = otherPoint.tags;
        this.fields = otherPoint.fields;
    }

    /**
     * Copy constructor allowing to provide exclude set for tags and fields.
     * These tags and fields will not be presented in the new created copy.
     *
     * @param otherPoint - point object to copy from
     * @param tagExclusion - set of tags to exclude from the created copy of the Point
     * @param fieldExclusion - set of fields to exclude from the created copy of the Point
     */
    public Point(Point otherPoint, Set<String> tagExclusion, Set<String> fieldExclusion) {
        if (null == tagExclusion) {
            tagExclusion = Collections.emptySet();
        }

        if (null == fieldExclusion) {
            fieldExclusion = Collections.emptySet();
        }

        this.measurement = otherPoint.measurement;
        this.precision = otherPoint.precision;
        this.time = otherPoint.time;
        Set<String> finalTagExclusion = tagExclusion;
        this.tags = otherPoint.tags.entrySet().stream().filter(e -> !finalTagExclusion.contains(e.getKey())).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        Set<String> finalFieldExclusion = fieldExclusion;
        this.fields = otherPoint.fields.entrySet().stream().filter(e -> !finalFieldExclusion.contains(e.getKey())).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    /**
     * Returns all fields
     * @return the fields
     */
    public Map<String, Object> getFields() {
        return this.fields;
    }

    /**
     * Returns field value by given field name
     * @param name - field name
     * @return field value
     */
    public Object valueOfField(String name) {
        return this.fields.get(name);
    }

    /**
     * Returns tag value by given tag name
     * @param name - tag name
     * @return tag value
     */
    public String valueOfTag(String name) {
        return this.getTags().get(name);
    }

    /**
     * Returns time of the current point
     * @return time of the current point
     */
    public Number getTime() {
        return this.time;
    }

    /**
     * Returns time precision of the current point
     * @return time precision of the current point
     */
    public TimeUnit getPrecision() {
        return precision;
    }

    /**
     * Add a tag to this point.
     *
     * @param tagName the tag name
     * @param value   the tag value
     * @return the Builder instance
     **/
    public Point addTag(final String tagName, final String value) {
        Objects.requireNonNull(tagName, "tagName");
        Objects.requireNonNull(value, "value");
        if (!tagName.isEmpty() && !value.isEmpty()) {
            if (tags == null)
                tags = new HashMap<>();
            tags.put(tagName, value);
        }
        return this;
    }

    /**
     * Create a new Point Build build to create a new Point in a fluent manner from a POJO.
     *
     * @param clazz Class of the POJO
     * @return the Builder instance
     */
    public static Point measurementByPOJO(final Class<?> clazz) {
        Objects.requireNonNull(clazz, "clazz");
        throwExceptionIfMissingAnnotation(clazz);
        String measurementName = findMeasurementName(clazz);
        return new Point(measurementName);
    }

    /**
     * Create a new Point Build build to create a new Point in a fluent manner from a POJO.
     *
     * @param clazz Class of the POJO
     * @return the Builder instance
     */
    public static Point measurementByPOJOWithKey(final Class<?> clazz, Object key, boolean isKeyTag, String keyName) {
        Objects.requireNonNull(clazz, "clazz");
        throwExceptionIfMissingAnnotation(clazz);
        String measurementName = findMeasurementName(clazz);
        Point point = new Point(measurementName);
        if (isKeyTag) {
            point.addTag(keyName, key.toString());
        } else {
            point.addField(keyName, key);
        }
        return point;
    }

    private static void throwExceptionIfMissingAnnotation(final Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Measurement.class)) {
            throw new IllegalArgumentException("Class " + clazz.getName() + " is not annotated with @"
                    + Measurement.class.getSimpleName());
        }
    }

    /* *
     * Add a Map of tags to add to this point.
     *
     * @param tagsToAdd
     *            the Map of tags to add
     * @return the Builder instance.
     */
    private void addAllTags(final Map<String, String> tagsToAdd) {
        for (Entry<String, String> tag : tagsToAdd.entrySet()) {
            addTag(tag.getKey(), tag.getValue());
        }
    }

    /**
     * Add a field to this point.
     *
     * @param fieldName the field name
     * @param value     the value of this field
     * @return the Builder instance
     */
    public Point addField(final String fieldName, Object value) {
        if (value instanceof Number) {
            if (value instanceof Byte) {
                value = ((Byte) value).doubleValue();
            } else if (value instanceof Short) {
                value = ((Short) value).doubleValue();
            } else if (value instanceof Integer) {
                value = ((Integer) value).doubleValue();
            } else if (value instanceof Long) {
                value = ((Long) value).doubleValue();
            } else if (value instanceof BigInteger) {
                value = ((BigInteger) value).doubleValue();
            }
        }
        return putField(fieldName, value);
    }

    /**
     * Add boolean field to this point.
     *
     * @param fieldName the field name
     * @param value     the value of this field
     * @return the Builder instance
     */
    public Point addField(final String fieldName, final boolean value) {
        return putField(fieldName, value);
    }

    /**
     * Add long field to this point.
     *
     * @param fieldName the field name
     * @param value     the value of this field
     * @return the Builder instance
     */
    public Point addField(final String fieldName, final long value) {
        return putField(fieldName, value);
    }

    /**
     * Add double field to this point.
     *
     * @param fieldName the field name
     * @param value     the value of this field
     * @return the Builder instance
     */
    public Point addField(final String fieldName, final double value) {
        return putField(fieldName, value);
    }

    /**
     * Add integer field to this point.
     *
     * @param fieldName the field name
     * @param value     the value of this field
     * @return the Builder instance
     */
    public Point addField(final String fieldName, final int value) {
        return putField(fieldName, value);
    }

    /**
     * Add float field to this point.
     *
     * @param fieldName the field name
     * @param value     the value of this field
     * @return the Builder instance
     */
    public Point addField(final String fieldName, final float value) {
        return putField(fieldName, value);
    }

    /**
     * Add short field to this point.
     *
     * @param fieldName the field name
     * @param value     the value of this field
     * @return the Builder instance
     */
    public Point addField(final String fieldName, final short value) {
        return putField(fieldName, value);
    }

    /**
     * Add Number field to this point.
     *
     * @param fieldName the field name
     * @param value     the value of this field
     * @return the Builder instance
     */
    public Point addField(final String fieldName, final Number value) {
        return putField(fieldName, value);
    }

    /**
     * Add String field to this point.
     *
     * @param fieldName the field name
     * @param value     the value of this field
     * @return the Builder instance
     */
    public Point addField(final String fieldName, final String value) {
        return putField(fieldName, value);
    }


    private Point putField(final String field, final Object value) {
        Objects.requireNonNull(field, "fieldName");
        if (fields == null) {
            fields = new HashMap<>();
        }
        fields.put(field, value);
        return this;
    }

    /* *
     * Add a Map of fields to this point.
     *
     * @param fieldsToAdd the fields to add
     */
    private void addAllFields(final Map<String, Object> fieldsToAdd) {
        if (fields == null)
            fields = new HashMap<>();
        this.fields.putAll(fieldsToAdd);
    }


    /** Add a time to this point.
     *
     * @param timeToSet      the time for this point
     * @param precisionToSet the TimeUnit
     * @return the Builder instance.
     */
    public Point setTime(final Number timeToSet, final TimeUnit precisionToSet) {
        Objects.requireNonNull(timeToSet, "timeToSet");
        Objects.requireNonNull(precisionToSet, "precisionToSet");
        this.time = timeToSet;
        this.precision = precisionToSet;
        return this;
    }

    /**
     * Add a time to this point as long.
     * only kept for binary compatibility with previous releases.
     *
     * @param timeToSet      the time for this point as long
     * @param precisionToSet the TimeUnit
     * @return the Builder instance.
     */
    public Point setTime(final long timeToSet, final TimeUnit precisionToSet) {
        setTime((Number) timeToSet, precisionToSet);
        return this;
    }

    /**
     * Add a time to this point as Long.
     * only kept for binary compatibility with previous releases.
     *
     * @param timeToSet      the time for this point as Long
     * @param precisionToSet the TimeUnit
     * @return the Builder instance.
     */
    public Point setTime(final Long timeToSet, final TimeUnit precisionToSet) {
        setTime((Number) timeToSet, precisionToSet);
        return this;
    }


    /**
     * Adds field map from object by reflection using {@link Column}
     * annotation.
     *
     * @param pojo POJO Object with annotation {@link Column} on fields
     */
    public void addFieldsFromPOJO(final Object pojo) {

        Class<?> clazz = pojo.getClass();
        while (clazz != null) {

            for (Field field : clazz.getDeclaredFields()) {

                Column column = field.getAnnotation(Column.class);

                if (column == null) {
                    continue;
                }

                field.setAccessible(true);
                String fieldName = column.name();
                addFieldByAttribute(pojo, field, column, fieldName);
            }
            clazz = clazz.getSuperclass();
        }

        if (this.fields.isEmpty()) {
            throw new IllegalArgumentException("Class " + pojo.getClass().getName()
                    + " has no @" + Column.class.getSimpleName() + " annotation");
        }

    }

    private void addFieldByAttribute(final Object pojo, final Field field, final Column column,
                                     final String fieldName) {
        try {
            Object fieldValue = field.get(pojo);

            if (fieldName.equals("time")) {
                Optional.ofNullable((Number) fieldValue).ifPresent(t -> {
                    TimeUnit timeUnit = findMeasurementTimeUnit(pojo.getClass());
                    this.setTime(t, timeUnit);
                });
                return;
            }

            try {
                if (column.tag()) {
                    if (fieldValue != null) {
                        addTag(fieldName, "" + fieldValue);
                    }
                } else {
                    if (fieldValue != null) {
                        putField(fieldName, fieldValue);
                    }
                }
            } catch (RuntimeException r) {
                System.out.println("Runtime exception while handling field:" + fieldName);
                throw r;
            }

        } catch (IllegalArgumentException | IllegalAccessException e) {
            // Can not happen since we use metadata got from the object
            throw new IllegalArgumentException(
                    "Field " + fieldName + " could not found on class " + pojo.getClass().getSimpleName());
        }
    }

    /**
     * Sets measurement name
     * @param measurement the measurement to set
     */
    public void setMeasurement(final String measurement) {
        this.measurement = measurement;
    }

    /**
     * Returns all tags
     * @return the tags
     */
    public Map<String, String> getTags() {
        return this.tags;
    }

    /**
     * Sets time precision
     * @param precision the precision to set
     * @return the Builder instance
     */
    public Point setPrecision(final TimeUnit precision) {
        this.precision = precision;
        return this;
    }


    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Point point = (Point) o;
        return Objects.equals(measurement, point.measurement)
                && Objects.equals(tags, point.tags)
                && Objects.equals(time, point.time)
                && precision == point.precision
                && Objects.equals(fields, point.fields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(measurement, tags, time, precision, fields);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Point [name=");
        builder.append(this.measurement);
        if (this.time != null) {
            builder.append(", time=");
            builder.append(this.time);
        }
        builder.append(", tags=");
        builder.append(this.tags);
        if (this.precision != null) {
            builder.append(", precision=");
            builder.append(this.precision);
        }
        builder.append(", fields=");
        builder.append(this.fields);
        builder.append("]");
        return builder.toString();
    }

    /**
     * Calculate the line protocol entry for a single Point.
     * <p>
     * NaN and infinity values are silently dropped as they are unsupported:
     * https://github.com/influxdata/influxdb/issues/4089
     *
     * @return the String without newLine, empty when there are no fields to write
     * @see <a href="https://docs.influxdata.com/influxdb/v1.7/write_protocols/line_protocol_reference/">
     * InfluxDB line protocol reference</a>
     */
    public String toLineProtocol() {
        return toLineProtocol(null);
    }

    /**
     * Calculate the line protocol entry for a single point, using a specific {@link TimeUnit} for the timestamp.
     * <p>
     * NaN and infinity values are silently dropped as they are unsupported:
     * https://github.com/influxdata/influxdb/issues/4089
     *
     * @param precision the time precision unit for this point
     * @return the String without newLine, empty when there are no fields to write
     * @see <a href="https://docs.influxdata.com/influxdb/v1.7/write_protocols/line_protocol_reference/">
     * InfluxDB line protocol reference</a>
     */
    private String toLineProtocol(final TimeUnit precision) {

        // setLength(0) is used for reusing cached StringBuilder instance per thread
        // it reduces GC activity and performs better then new StringBuilder()
        StringBuilder sb = CACHED_STRINGBUILDERS.get();
        sb.setLength(0);

        escapeKey(sb, measurement);
        concatenatedTags(sb);
        int writtenFields = concatenatedFields(sb);
        if (writtenFields == 0) {
            return "";
        }
        formattedTime(sb, precision);

        return sb.toString();
    }

    /**
     * Create a single Point builder from given the line protocol entry.
     *
     * @param lineProtocol the line protocol entry
     * @return newly created Point Builder instance
     */
    public static Point fromString(String lineProtocol) {

        int offset = 0;
        Tuple2<String, Integer> tmp;

        //Get measurement
        tmp = getStringBeforeCommaOrSpace(lineProtocol, offset);
        String measurement = tmp.getFirst();

        if (measurement == null || measurement.length() == 0) {
            throw new IllegalArgumentException("Measurement missing! Line protocol must have measurement.");
        } else if (!measurementValid(measurement)) {
            throw new IllegalArgumentException("Invalid measurement: " + measurement);
        }
        Point point = new Point(measurement);

        offset = tmp.getSecond();
        Map<String, String> lineTags = new TreeMap<>();

        //Get Tags
        while (offset < lineProtocol.length() && lineProtocol.charAt(offset) == StringUtil.COMMA) {
            tmp = getStringBeforeCommaOrSpace(lineProtocol, offset + 1);
            String[] tagPart = splitOnFirstEqualChar(tmp.getFirst());
            if (tagPart.length == 2) {

                lineTags.put(tagPart[0], tagPart[1]);
            } else {
                throw new IllegalArgumentException("Invalid tag format: " + tmp.getFirst());
            }
            offset = tmp.getSecond();
        }

        if (!lineTags.isEmpty())
            point.addAllTags(lineTags);

        Map<String, Object> fields = new HashMap<>();

        boolean first = true;
        //Get fields
        while (offset < lineProtocol.length() && (first || lineProtocol.charAt(offset) == StringUtil.COMMA)) {
            first = false;
            tmp = getStringBeforeCommaOrSpace(lineProtocol, offset + 1);
            String[] fieldPart = splitOnFirstEqualChar(tmp.getFirst());
            if (fieldPart.length == 2) {

                String key = fieldPart[0];
                String value = fieldPart[1];

                if (key == null || key.length() < 1 || value == null || value.length() < 1) {
                    continue;
                }
                Object fieldValue = value;
                if (value.charAt(0) == StringUtil.DOUBLE_QUOTE && value.charAt(value.length() - 1) == StringUtil.DOUBLE_QUOTE) {
                    fieldValue = value.substring(1, value.length() - 1);
                } else if (StringUtil.isNumeric(value)) {
                    fieldValue = StringUtil.parseNumber(value);
                } else if (StringUtil.isBoolean(value)) {
                    fieldValue = StringUtil.parseBoolean(value);
                }

                fields.put(key, fieldValue);

            } else {
                throw new IllegalArgumentException("Invalid field format: " + tmp.getFirst());
            }
            offset = tmp.getSecond();
        }
        if (fields.isEmpty()) {
            throw new IllegalArgumentException("Point must have at least one field. Point without field is unsupported.");
        }
        point.addAllFields(fields);
        // Get time
        Long time = null;
        if (offset < lineProtocol.length()) {
            tmp = getStringBeforeCommaOrSpace(lineProtocol, offset + 1);
            try {
                time = Long.parseLong(tmp.getFirst());

            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Illegal timestamp " + tmp.getFirst(), e);
            }
            offset = tmp.getSecond();
            if (offset < lineProtocol.length()) {
                String leftOver = lineProtocol.substring(offset+1);
                leftOver = leftOver.trim();
                if(!leftOver.isEmpty()){
                    throw new IllegalArgumentException("Invalid line protocol format. Can't convert point with data: " + leftOver+" after timestamp! " );
                }
            }
        }
        if (time == null) {
            throw new IllegalArgumentException("Point without metric is unsupported.");
        }
        point.setTime(time, TimeUnit.NANOSECONDS);
        return point;
    }

    private static Tuple2<String, Integer> getStringBeforeCommaOrSpace(String line, int offset) {

        StringBuilder sb = new StringBuilder();
        boolean escaped = false;
        boolean quoted = false;
        char currentChar;

        while (offset < line.length()) {

            currentChar = line.charAt(offset);

            if ((currentChar == StringUtil.COMMA || currentChar == StringUtil.SPACE) && !escaped && !quoted) {
                break;
            }
            if (currentChar == StringUtil.ESCAPE && !escaped) {
                escaped = true;
            } else {
                if (currentChar == StringUtil.DOUBLE_QUOTE && !escaped) {
                    quoted = !quoted;
                }
                escaped = false;
                sb.append(currentChar);
            }
            offset++;
        }

        if (quoted) {
            throw new IllegalArgumentException("Missing quote at line: " + line);
        }
        return new Tuple2<>(sb.toString(), offset);
    }

    private static boolean measurementValid(String measurement) {
        boolean isValid = true;
        if (measurement.indexOf(StringUtil.DOUBLE_QUOTE) > 0 || measurement.indexOf(StringUtil.QUOTE) > 0
                || measurement.indexOf(StringUtil.EQUAL) > 0 || measurement.indexOf(StringUtil.SPACE) > 0 || measurement.indexOf(StringUtil.COMMA) > 0) {
            isValid = false;
        }
        return isValid;
    }

    private static String[] splitOnFirstEqualChar(String kevValue) {

        int pos = kevValue.indexOf(StringUtil.EQUAL);
        if (pos > 0) {
            String key = kevValue.substring(0, pos);
            String value = pos < kevValue.length() - 1 ? kevValue.substring(pos + 1) : "";

            return new String[]{key, value};
        }

        return new String[0];
    }

    private void concatenatedTags(final StringBuilder sb) {
        for (Entry<String, String> tag : this.tags.entrySet()) {
            sb.append(StringUtil.COMMA);
            escapeKey(sb, tag.getKey());
            sb.append(StringUtil.EQUAL);
            escapeKey(sb, tag.getValue());
        }
        sb.append(StringUtil.SPACE);
    }

    private int concatenatedFields(final StringBuilder sb) {
        int fieldCount = 0;
        for (Entry<String, Object> field : this.fields.entrySet()) {
            Object value = field.getValue();
            if (value == null || isNotFinite(value)) {
                continue;
            }
            escapeKey(sb, field.getKey());
            sb.append(StringUtil.EQUAL);
            if (value instanceof Number) {
                if (value instanceof Double || value instanceof Float || value instanceof BigDecimal) {
                    sb.append(NUMBER_FORMATTER.get().format(value));
                } else {
                    sb.append(value).append(StringUtil.I);
                }
            } else if (value instanceof String) {
                String stringValue = (String) value;
                sb.append(StringUtil.DOUBLE_QUOTE);
                escapeField(sb, stringValue);
                sb.append(StringUtil.DOUBLE_QUOTE);
            } else {
                sb.append(value);
            }
            sb.append(StringUtil.COMMA);

            fieldCount++;
        }

        // efficiently chop off the trailing comma
        int lengthMinusOne = sb.length() - 1;
        if (sb.charAt(lengthMinusOne) == StringUtil.COMMA) {
            sb.setLength(lengthMinusOne);
        }

        return fieldCount;
    }

    static void escapeKey(final StringBuilder sb, final String key) {
        for (int i = 0; i < key.length(); i++) {
            switch (key.charAt(i)) {
                case StringUtil.SPACE:
                case StringUtil.COMMA:
                case StringUtil.EQUAL:
                    sb.append(StringUtil.ESCAPE);
                default:
                    sb.append(key.charAt(i));
            }
        }
    }

    static void escapeField(final StringBuilder sb, final String field) {
        for (int i = 0; i < field.length(); i++) {
            switch (field.charAt(i)) {
                case StringUtil.ESCAPE:
                case '\"':
                    sb.append(StringUtil.ESCAPE);
                default:
                    sb.append(field.charAt(i));
            }
        }
    }

    private static boolean isNotFinite(final Object value) {
        return value instanceof Double && !Double.isFinite((Double) value)
                || value instanceof Float && !Float.isFinite((Float) value);
    }

    private void formattedTime(final StringBuilder sb, final TimeUnit precision) {
        if (this.time == null) {
            return;
        }
        TimeUnit converterPrecision = precision;

        if (converterPrecision == null) {
            converterPrecision = TimeUnit.NANOSECONDS;
        }
        if (this.time instanceof BigInteger) {
            BigInteger time = (BigInteger) this.time;
            long conversionFactor = converterPrecision.convert(1, this.precision);
            if (conversionFactor >= 1) {
                time = time.multiply(BigInteger.valueOf(conversionFactor));
            } else {
                conversionFactor = this.precision.convert(1, converterPrecision);
                time = time.divide(BigInteger.valueOf(conversionFactor));
            }
            sb.append(" ").append(time);
        } else if (this.time instanceof BigDecimal) {
            BigDecimal time = (BigDecimal) this.time;
            long conversionFactor = converterPrecision.convert(1, this.precision);
            if (conversionFactor >= 1) {
                time = time.multiply(BigDecimal.valueOf(conversionFactor));
            } else {
                conversionFactor = this.precision.convert(1, converterPrecision);
                time = time.divide(BigDecimal.valueOf(conversionFactor), RoundingMode.HALF_UP);
            }
            sb.append(StringUtil.SPACE).append(time.toBigInteger());
        } else {
            sb.append(StringUtil.SPACE).append(converterPrecision.convert(this.time.longValue(), this.precision));
        }
    }

    private static String findMeasurementName(final Class<?> clazz) {
        return clazz.getAnnotation(Measurement.class).name();
    }

    private static TimeUnit findMeasurementTimeUnit(final Class<?> clazz) {
        return clazz.getAnnotation(Measurement.class).timeUnit();
    }
}

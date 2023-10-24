package org.hyperledger.tempo.ts.dto;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class Point {
    private static final Log LOGGER = LogFactory.getLog(Point.class);

    private String measurement;
    private Map<String, String> tags;
    private Map<String, Object> fields;
    private Number time;
    private TimeUnit precision = TimeUnit.NANOSECONDS;

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

    /* *
     * Add a Map of tags to add to this point.
     *
     * @param tagsToAdd
     *            the Map of tags to add
     * @return the Builder instance.
     */
    private void addAllTags(final Map<String, String> tagsToAdd) {
        for (Map.Entry<String, String> tag : tagsToAdd.entrySet()) {
            addTag(tag.getKey(), tag.getValue());
        }
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

    public static List<Point> fromLineProtocols(String lineProtocols) {
        LOGGER.debug("fromLineProtocols :: lineProtocols: [" + lineProtocols + "]");
        String[] linesProtocolSplit = lineProtocols.split("\n");
        LOGGER.debug("linesProtocolSplit.length: " + linesProtocolSplit.length);
        List<Point> points = new ArrayList<Point>();
        for(String lp : linesProtocolSplit) {
            Point p = fromLineProtocol(lp);
            points.add(p);
        }
        return points;
    }

    public static Point fromLineProtocol(String lineProtocol) {
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
            // Don't do nothing, default behaviour of influxdb is to put the current time.
            // throw new IllegalArgumentException("Point without metric is unsupported.");
        } else {
            point.setTime(time, TimeUnit.NANOSECONDS);
        }
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

}

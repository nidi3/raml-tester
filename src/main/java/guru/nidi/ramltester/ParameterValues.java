package guru.nidi.ramltester;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class ParameterValues {
    private final Map<String, String[]> values = new HashMap<>();

    public Map<String, String[]> getValues() {
        return values;
    }

    public int size() {
        return values.size();
    }

    public void addValue(String name, String value) {
        final String[] newValues = appendValue(values.get(name), value);
        values.put(name, newValues);
    }

    public void addValues(String name, String[] values) {
        for (String value : values) {
            addValue(name, value);
        }
    }

    public void addValues(ParameterValues values) {
        for (Map.Entry<String, String[]> value : values.getValues().entrySet()) {
            addValues(value.getKey(), value.getValue());
        }
    }

    private String[] appendValue(String[] values, String value) {
        final String[] newValues;
        if (values == null) {
            newValues = new String[]{value};
        } else {
            newValues = new String[values.length + 1];
            System.arraycopy(values, 0, newValues, 0, values.length);
            newValues[values.length] = value;
        }
        return newValues;
    }
}

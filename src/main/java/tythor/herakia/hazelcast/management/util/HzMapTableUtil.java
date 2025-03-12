package tythor.herakia.hazelcast.management.util;

import com.hazelcast.map.IMap;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import tythor.herakia.utility.HazelcastUtil;

import java.lang.reflect.Field;
import java.util.*;

public class HzMapTableUtil {
    @SuppressWarnings("unchecked")
    public static String printTable(String mapName) {
        IMap<?, ?> map = HazelcastUtil.getMap(mapName);

        if (map.isEmpty()) throw new ResponseStatusException(HttpStatus.NO_CONTENT, "Map is empty.");

        Set<? extends Map.Entry<?, ?>> entrySet = map.entrySet();

        Object sampleValue = entrySet.iterator().next().getValue();

        while (sampleValue instanceof Iterable<?>) {
            sampleValue = ((Iterable<?>) sampleValue).iterator().next();
        }

        Field[] fields = sampleValue.getClass().getDeclaredFields();

        // Maintain header order and column width
        Map<String, Integer> columnWidths = new LinkedHashMap<>();
        columnWidths.put("__key", "__key".length());
        for (Field field : fields) {
            columnWidths.put(field.getName(), field.getName().length());
        }

        // Try to sort map entries
        List<? extends Map.Entry<?, ?>> entries;
        try {
            entries = entrySet.stream()
                .map(entry -> (Map.Entry<Comparable<Object>, ?>) entry)
                .sorted(Map.Entry.comparingByKey(Comparator.reverseOrder()))
                .toList();
        } catch (Exception e) {
            entries = entrySet.stream().toList();
        }

        // Collect data rows
        List<List<String>> rowDataList = new ArrayList<>();
        for (Map.Entry<?, ?> entry : entries) {
            Object entryValue = entry.getValue();

            if (entryValue instanceof Iterable<?> iterable) {
                Object next = iterable.iterator().next();
                while (next instanceof Iterable<?> nextIterable) {
                    next = nextIterable.iterator().next();

                    if (!(next instanceof Iterable<?>)) {
                        for (Object object : nextIterable) {
                            List<String> rowData = createRowData(entry.getKey(), object, fields, columnWidths);
                            rowDataList.add(rowData);
                        }
                    }
                }
            } else {
                List<String> rowData = createRowData(entry.getKey(), entryValue, fields, columnWidths);
                rowDataList.add(rowData);
            }
        }

        // Build table
        StringBuilder table = new StringBuilder();
        table.append(printSeparator(columnWidths));
        table.append(printRow(columnWidths.keySet(), columnWidths));
        table.append(printSeparator(columnWidths));

        for (List<String> rowData : rowDataList) {
            table.append(printRow(rowData, columnWidths));
        }

        table.append(printSeparator(columnWidths));
        return "<pre>\n%s</pre>".formatted(table.toString());
    }

    private static List<String> createRowData(Object key, Object valueObject, Field[] fields, Map<String, Integer> columnWidths) {
        List<String> rowData = new ArrayList<>();

        insertAndUpdateMaxWidth("__key", key, rowData, columnWidths);

        for (Field field : fields) {
            field.setAccessible(true);
            Object value;
            try {
                value = field.get(valueObject);
            } catch (IllegalAccessException e) {
                value = "N/A";
            }
            insertAndUpdateMaxWidth(field.getName(), value, rowData, columnWidths);
        }

        return rowData;
    }

    private static void insertAndUpdateMaxWidth(String key, Object value, List<String> rowData, Map<String, Integer> columnWidths) {
        String valueString = String.valueOf(value);
        rowData.add(valueString);
        columnWidths.compute(key, (k, v) -> Math.max(v, valueString.length()));
    }

    private static String printRow(Collection<String> rowData, Map<String, Integer> columnWidths) {
        StringBuilder rowString = new StringBuilder("|");
        Iterator<String> rowDataIterator = rowData.iterator();
        Iterator<Integer> columnWidthsIterator = columnWidths.values().iterator();

        while (rowDataIterator.hasNext() && columnWidthsIterator.hasNext()) {
            String data = rowDataIterator.next();
            int width = columnWidthsIterator.next();
            rowString.append((" %-" + width + "s |").formatted(data));
        }

        rowString.append("\n");
        return rowString.toString();
    }

    private static String printSeparator(Map<String, Integer> columnWidths) {
        StringBuilder separator = new StringBuilder("+");
        for (int width : columnWidths.values()) {
            separator.append("-".repeat(width + 2)).append("+");
        }
        separator.append("\n");
        return separator.toString();
    }
}

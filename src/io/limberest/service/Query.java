package io.limberest.service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

public class Query {
    public static final int MAX_ALL = -1;

    public enum Meta {
        count,
        search,
        start,
        max,
        sort,
        descending
    }
    
    public boolean isMeta(String param) {
        for (Meta meta : Meta.values())
            if (meta.toString().equals(param))
                return true;
        return false;
    }
    
    public Query() {
    }

    public Query(Map<String,String> parameters) {

        String countParam = parameters.get(Meta.count.toString());
        if (countParam != null)
            setCount(Boolean.parseBoolean(countParam));

        setSearch(parameters.get(Meta.search.toString()));

        String startParam = parameters.get(Meta.start.toString());
        if (startParam != null)
            setStart(Integer.parseInt(startParam));

        String maxParam = parameters.get(Meta.max.toString());
        if (maxParam != null)
            setMax(Integer.parseInt(maxParam));

        setSort(parameters.get(Meta.sort.toString()));

        String descendingParam = parameters.get(Meta.descending.toString());
        if (descendingParam != null)
            setDescending(Boolean.parseBoolean(descendingParam));

        for (String key : parameters.keySet()) {
            
            if (!isMeta(key))
                setFilter(key, parameters.get(key));
        }
    }

    /**
     * Count only (no retrieval)
     */
    private boolean count;
    public boolean isCount() { return count; }
    public void setCount(boolean count) { this.count = true; }

    private String search;
    public String getSearch() { return search; }
    public void setSearch(String search) { this.search = search;}

    private int start = 0;
    /**
     * Zero-based index for first item.
     */
    public int getStart() { return start; }
    public void setStart(int start) { this.start = start; }

    private int max = MAX_ALL;
    public int getMax() { return max; }
    public void setMax(int max) { this.max = max; }

    private String sort;
    public String getSort() { return sort; }
    public void setSort(String sort) { this.sort = sort; }

    private boolean descending;
    public boolean isDescending() { return descending; }
    public void setDescending(boolean descending) { this.descending = descending; }

    /**
     * All parameters are filters except count, search, start, max, sort, descending, ascending and app
     * TODO: enum for these
     */
    private Map<String,String> filters = new HashMap<String,String>();
    public Map<String,String> getFilters() { return filters; }
    public void setFilters(Map<String,String> filters) { this.filters = filters; }
    public void setFilter(String key, String value) { filters.put(key, value); }

    public String getFilter(String key) {
        return filters.get(key);
    }
    
    public boolean hasFilters() {
        return !filters.isEmpty();
    }
    
    public boolean hasFilter(String key) {
        return filters.containsKey(key);
    }

    /**
     * Empty list returns null;
     */
    public String[] getArrayFilter(String key) {
        String value = filters.get(key);
        if (value == null)
            return null;
        String[] array = new String[0];
        if (value.startsWith("[")) {
            if (value.length() > 2)
                array = value.substring(1, value.length() - 1).split(",");
        }
        else if (value.length() > 1) {
            array = value.split(",");
        }
        for (int i = 0; i < array.length; i++) {
            if (array[i].charAt(0) == '"')
                array[i] = array[i].substring(1, array[i].length() - 2);
        }
        return array;
    }
    public void setArrayFilter(String key, String[] array) {
        if (array == null || array.length == 0) {
            filters.remove(key);
        }
        else {
            String value = "[";
            for (int i = 0; i < array.length; i++) {
                value += array[i];
                if (i < array.length - 1)
                    value += ",";
            }
            value += "]";
            filters.put(key, value);
        }
    }

    public Long[] getLongArrayFilter(String key) {
        String[] strArr = getArrayFilter(key);
        if (strArr == null)
            return null;
        Long[] longArr = new Long[strArr.length];
        for (int i = 0; i < strArr.length; i++)
            longArr[i] = Long.parseLong(strArr[i]);
        return longArr;
    }

    public boolean getBooleanFilter(String key) {
        String value = filters.get(key);
        return "true".equalsIgnoreCase(value);
    }
    public void setFilter(String key, boolean value) {
        setFilter(key, String.valueOf(value));
    }

    public int getIntFilter(String key) {
        String value = filters.get(key);
        return value == null ? -1 : Integer.parseInt(value);
    }
    public void setFilter(String key, int value) {
        setFilter(key, String.valueOf(value));
    }

    public long getLongFilter(String key) {
        String value = filters.get(key);
        return value == null ? -1 : Long.parseLong(value);
    }
    public void setFilter(String key, long value) {
        setFilter(key, String.valueOf(value));
    }

    public Date getDateFilter(String key) throws ParseException {
        return getDate(filters.get(key));
    }
    public void setFilter(String key, Date date) {
        String value = getString(date);
        if (value == null)
            filters.remove(key);
        else
            filters.put(key, value);
    }

    private static DateFormat dateTimeFormat;
    protected static DateFormat getDateTimeFormat() {
        if (dateTimeFormat == null)
            dateTimeFormat = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
        return dateTimeFormat;
    }

    private static DateFormat dateFormat;
    protected static DateFormat getDateFormat() {
        if (dateFormat == null)
            dateFormat = new SimpleDateFormat("yyyy-MMM-dd"); // does not require url-encoding
        return dateFormat;
    }

    public static Date getDate(String str) throws ParseException {
        if (str == null)
            return null;
        else if (str.length() > 11)
            return getDateTimeFormat().parse(str);
        else
            return getDateFormat().parse(str);
    }

    public static String getString(Date date) {
        if (date == null)
            return null;
        else {
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            if (c.get(Calendar.HOUR_OF_DAY) == 0 && c.get(Calendar.MINUTE) == 0 && c.get(Calendar.SECOND) == 0)
                return getDateFormat().format(date);
            else
                return getDateTimeFormat().format(date);
        }
    }
    
    /**
     * Checks for matches.
     * @param matcher should check search as well as filters
     * @return true if match
     */
    public boolean match(Predicate<Entry<String,String>> matcher) {
        Map<String,String> filtersPlusSearch = new HashMap<String,String>(filters);
        if (search != null)
            filtersPlusSearch.put("search", search);
        return filtersPlusSearch.entrySet().stream().allMatch(matcher);
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (count) {
            sb.append(Meta.count).append("=").append(count);
        }
        if (search != null) {
            if (sb.length() > 0)
                sb.append("&");
            sb.append(Meta.search).append("=").append(search);
        }
        if (start > 0) {
            if (sb.length() > 0)
                sb.append("&");
            sb.append(Meta.start).append("=").append(start);
        }
        if (max != MAX_ALL) {
            if (sb.length() > 0)
                sb.append("&");
            sb.append(Meta.max).append("=").append(max);
        }
        if (sort != null) {
            if (sb.length() > 0)
                sb.append("&");
            sb.append(Meta.sort).append("=").append(sort);
        }
        if (descending) {
            if (sb.length() > 0)
                sb.append("&");
            sb.append(Meta.descending).append("=").append(descending);
        }

        if (filters != null) {
            for (String key : filters.keySet()) {
                String val = filters.get(key);
                if (val != null) {
                    if (sb.length() > 0)
                        sb.append("&");
                    sb.append(key).append("=").append(val);
                }
            }
        }

        return sb.toString();
    }
}
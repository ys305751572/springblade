package com.smallchill.core.plugins.nameconversion;

import org.apache.commons.lang3.StringUtils;
import org.beetl.sql.core.NameConversion;
import org.beetl.sql.core.annotatoin.Table;

import javax.persistence.Column;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 框架自带的javabean字段解析器并不能满足我们的需求，需要自定义字段转换器
 * Created by 叶松 on 2016/9/14.
 */
public class MyNameConversion extends NameConversion {

    static final Map<Class, Map<String, String>> PROP2COL_CACHE = new ConcurrentHashMap<Class, Map<String, String>>();
    static final Map<Class, Map<String, String>> COL2PROP_CACHE = new ConcurrentHashMap<Class, Map<String, String>>();

    @Override
    public String getTableName(Class<?> c) {
        Table table = (Table)c.getAnnotation(Table.class);
        if(table!=null){
            return table.name();
        }
        return c.getSimpleName();
    }

    @Override
    public String getColName(Class<?> c, String attrName) {
        init(c);
        String attrName2 = PROP2COL_CACHE.get(c).get(attrName.toLowerCase()) ;
        return StringUtils.isBlank(attrName2) ? attrName : attrName2;
    }

    @Override
    public String getPropertyName(Class<?> c, String colName) {
        init(c);
        String colName2 = COL2PROP_CACHE.get(c).get(colName.toLowerCase());
        return StringUtils.isBlank(colName2) ? colName : colName2;
    }

    void init(Class<?> c) {
        if(COL2PROP_CACHE.containsKey(c)) return ;
        synchronized(c){
            if(COL2PROP_CACHE.containsKey(c)) return ;
            Field[] fields = c.getDeclaredFields();
            Map<String, String> cols = PROP2COL_CACHE.get(c);
            Map<String, String> props = COL2PROP_CACHE.get(c);
            if(cols == null){
                cols = new ConcurrentHashMap<String, String>();
                for (Field field : fields) {
                    Column column = field.getAnnotation(Column.class);
                    if (column != null) {
                        cols.put(field.getName().toLowerCase(), column.name().toLowerCase());
                    }
                }
                PROP2COL_CACHE.put(c, cols);
            }
            if(props == null){
                props = new ConcurrentHashMap<String, String>();
                for (Field field : fields) {
                    Column column = field.getAnnotation(Column.class);
                    if (column != null) {
                        props.put(column.name(), field.getName());
                    }
                }
                COL2PROP_CACHE.put(c, props);
            }
        }
    }
}

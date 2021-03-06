/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.angeljsb.restservlet;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Angel
 */
public class JSONHelper {
    
    private static Method getGetter(Field field) throws NoSuchMethodException {
        Class declaring = field.getDeclaringClass();
        String fieldName = field.getName();
        
        boolean isBool = field.getType().equals(Boolean.TYPE) || field.getType().equals(Boolean.class);
        String getterName = isBool ? "is" : "get"
                .concat(fieldName.substring(0,1).toUpperCase())
                .concat(fieldName.substring(1));
        return declaring.getMethod(getterName);
    }
    
    public static JSONObject createJSON(Object bean, String... fieldNames) {
        JSONObject json = new JSONObject();
        for(String fieldName : fieldNames) {
            try {
                Field field = bean.getClass().getDeclaredField(fieldName);
                Method getter = getGetter(field);
                Object value = getter.invoke(bean);
                if(value instanceof IJsonable) {
                    value = ((IJsonable) value).toJson();
                }
                json.put(fieldName, value);
            } catch (NoSuchFieldException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
                System.err.println("Error creating JSON " + ex.getClass().getSimpleName()
                        + " in field " + fieldName);
            }
        }
        return json;
    }
    
    public static JSONArray createJSON(Object[] beans, String... fieldNames) {
        JSONArray json = new JSONArray();
        for(Object bean : beans) {
            json.put(createJSON(bean, fieldNames));
        }
        return json;
    }
    
    public static JSONArray createJSON(Collection<Object> beans, String... fieldNames) {
        JSONArray json = new JSONArray();
        for(Object bean : beans) {
            json.put(createJSON(bean, fieldNames));
        }
        return json;
    }
    
    public static JSONObject toJson(Object bean) {
        if(bean instanceof IJsonable) {
            return ((IJsonable) bean).toJson();
        }else {
            return beanToJson(bean);
        }
    }
    
    public static JSONArray toJsonArray(Object[] arr) {
        JSONArray json = new JSONArray();
        for(Object obj : arr) {
            if(obj instanceof IJsonable) {
                 json.put(((IJsonable) obj).toJson());
            } else if(obj instanceof String || obj instanceof Number
                    || obj instanceof Boolean || obj instanceof Character) {
                json.put(obj);
            } else {
                json.put(beanToJson(obj));
            }
        }
        return json;
    }
    
    public static JSONArray toJsonArray(Collection col) {
        JSONArray json = new JSONArray();
        for(Object obj : col) {
            if(obj instanceof IJsonable) {
                 json.put(((IJsonable) obj).toJson());
            } else if(obj instanceof String || obj instanceof Number
                    || obj instanceof Boolean || obj instanceof Character) {
                json.put(obj);
            } else {
                json.put(beanToJson(obj));
            }
        }
        return json;
    }
    
    public static JSONObject beanToJson(Object bean) {
        JSONObject json = new JSONObject();
        Field[] fields = bean.getClass().getDeclaredFields();
        for(Field field : fields) {
            String fieldName = field.getName();
            try {
                Method getter = getGetter(field);
                Object value = getter.invoke(bean);
                if(value instanceof IJsonable) {
                    value = ((IJsonable) value).toJson();
                }
                json.put(fieldName, value);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
                System.err.println("Error creating JSON " + ex.getClass().getSimpleName()
                        + " in field " + fieldName);
            }
        }
        return json;
    }
    
}

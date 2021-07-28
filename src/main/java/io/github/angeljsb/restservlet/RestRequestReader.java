/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.angeljsb.restservlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.ws.rs.core.MediaType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Clase que permite leer los parametros en la request a un servlet.
 * Permite leer request body de tipo JSON y obtener los parametros
 * en los distintos tipos nativos
 *
 * @since v1.0.0
 * @author Angel
 */
public class RestRequestReader {
    
    /**
     * Lee todo el texto de un input stream
     * 
     * @param is El input stream a leer
     * @return Todo el contenido del input stream como texto
     * @throws IOException Si un error de I/O ocurre
     * @since v1.0.0
     */
    public static String readInputStream(InputStream is) throws IOException{
        String res;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            res = "";
            String line;
            while((line = br.readLine()) != null){
                res += line;
            }
        }
        return res;
    }
    
    /**
     * Lee un json de un input stream
     * 
     * @param is El input stream a leer
     * @return El contenido del input stream como un JSONOject
     * @throws IOException Si un error de I/O ocurre
     * @throws JSONException Si el contenido del input stream no es un JSON
     * @since v1.0.0
     */
    public static JSONObject getParamsJson(InputStream is) throws IOException, JSONException{
        String json = readInputStream(is);
        return new JSONObject(json);
    }
    
    private HttpServletRequest httpServletRequest;
    private Map<String, String> parameterMap;
    
    public RestRequestReader(HttpServletRequest request) {
        this.httpServletRequest = request;
        
        this.parameterMap = new HashMap();
        
        Map<String, String[]> reqParams = request.getParameterMap();
        if(!reqParams.isEmpty()) {
            reqParams.forEach((key, value) -> {
                if(value.length > 1){
                    JSONArray arr = new JSONArray(value);
                    parameterMap.put(key, arr.toString());
                }else if(value.length == 1) {
                    parameterMap.put(key, value[0]);
                }
            });
        }
        
        String content = request.getContentType();
        
        if(content!=null && content.contains(MediaType.APPLICATION_JSON)){
            try {
                JSONObject json = getParamsJson(request.getInputStream());
                Set<String> keySet = json.keySet();
                for(String key : keySet) {
                    this.parameterMap.put(key, json.get(key).toString());
                }
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            }
        }
    }
    
    protected void putParameter(String key, Object value) {
        this.parameterMap.put(key, value.toString());
    }

    public HttpServletRequest getHttpServletRequest() {
        return httpServletRequest;
    }

    public Map<String, String> getParameterMap() {
        return parameterMap;
    }
    
    private void comproveKey(String key) {
        if(!this.parameterMap.containsKey(key)){
            throw new RestException(HttpServletResponse.SC_BAD_REQUEST, 
                    "Parameter " + key + " doesn't exists");
        }
    }
    
    private <T> T parse(String s, Function<String, T> mapper) {
        try {
            return mapper.apply(s);
        } catch(Exception ex) {
            throw new RestException(HttpServletResponse.SC_BAD_REQUEST, 
                    ex.getMessage());
        }
    }
    
    private <T> T getParameterOrDefault(String key, T def, Function<String, T> mapper) {
        try {
            return mapper.apply(key);
        } catch(RestException ex) {
            return def;
        }
    }
    
    public byte getByte(String key) {
        this.comproveKey(key);
        String value = this.parameterMap.get(key);
        return parse(value, Byte::parseByte);
    }
    
    public byte getByte(String key, byte def) {
        return getParameterOrDefault(key, def, this::getByte);
    }
    
    public int getInt(String key) {
        this.comproveKey(key);
        String value = this.parameterMap.get(key);
        return parse(value, Integer::parseInt);
    }
    
    public int getInt(String key, int def) {
        return getParameterOrDefault(key, def, this::getInt);
    }
    
    public long getLong(String key) {
        this.comproveKey(key);
        String value = this.parameterMap.get(key);
        return parse(value, Long::parseLong);
    }
    
    public long getLong(String key, long def) {
        return getParameterOrDefault(key, def, this::getLong);
    }
    
    public float getFloat(String key) {
        this.comproveKey(key);
        String value = this.parameterMap.get(key);
        return parse(value, Float::parseFloat);
    }
    
    public float getFloat(String key, float def) {
        return getParameterOrDefault(key, def, this::getFloat);
    }
    
    public double getDouble(String key) {
        this.comproveKey(key);
        String value = this.parameterMap.get(key);
        return parse(value, Double::parseDouble);
    }
    
    public double getDouble(String key, double def) {
        return getParameterOrDefault(key, def, this::getDouble);
    }
    
    public boolean getBoolean(String key) {
        this.comproveKey(key);
        String value = this.parameterMap.get(key);
        return parse(value, Boolean::parseBoolean);
    }
    
    public boolean getBoolean(String key, boolean def) {
        return getParameterOrDefault(key, def, this::getBoolean);
    }
    
    public String getString(String key) {
        this.comproveKey(key);
        return this.parameterMap.get(key);
    }
    
    public String getString(String key, String def) {
        return getParameterOrDefault(key, def, this::getString);
    }
    
    public JSONArray getArray(String key) {
        this.comproveKey(key);
        String value = this.parameterMap.get(key);
        return parse(value, JSONArray::new);
    }
    
    public JSONArray getArray(String key, Object[] def) {
        return getParameterOrDefault(key, new JSONArray(def), this::getArray);
    }
    
    public JSONArray getArray(String key, String def) {
        return getParameterOrDefault(key, new JSONArray(def), this::getArray);
    }
    
    public JSONObject getJSON(String key) {
        this.comproveKey(key);
        String value = this.parameterMap.get(key);
        return parse(value, JSONObject::new);
    }
    
    public JSONObject getJSON(String key, Object def) {
        return getParameterOrDefault(key, new JSONObject(def), this::getJSON);
    }
    
    public JSONObject getJSON(String key, String def) {
        return getParameterOrDefault(key, new JSONObject(def), this::getJSON);
    }
    
    public String getHeader(String name) {
        return this.httpServletRequest.getHeader(name);
    }
    
    public Enumeration<String> getHeaders(String name) {
        return this.httpServletRequest.getHeaders(name);
    }
    
    public Enumeration<String> getHeaderNames() {
        return this.httpServletRequest.getHeaderNames();
    }
    
    public int getIntHeader(String name) {
        return this.httpServletRequest.getIntHeader(name);
    }
    
    public Part getPart(String name) throws IOException, ServletException {
        try {
            return this.httpServletRequest.getPart(name);
        } catch(IOException | ServletException ex) {
            ex.printStackTrace(System.err);
            return null;
        }
    }
    
    public Collection<Part> getParts() {
        try {
            return this.httpServletRequest.getParts();
        } catch(IOException | ServletException ex) {
            ex.printStackTrace(System.err);
            return new ArrayList();
        }
    }
    
    public Cookie[] getCookies() {
        return this.httpServletRequest.getCookies();
    }
    
    public Cookie getCookie(String name) {
        Cookie[] cookies = this.getCookies();
        for(Cookie cookie : cookies) {
            if(cookie.getName().equals(name)){
                return cookie;
            }
        }
        return null;
    }
    
}

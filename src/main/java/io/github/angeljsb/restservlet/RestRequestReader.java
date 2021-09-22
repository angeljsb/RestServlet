/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.angeljsb.restservlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import javax.ws.rs.core.MediaType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Clase que permite leer los parametros en la request a un servlet.
 * Permite leer request body de tipo JSON y obtener los parametros
 * en los distintos tipos nativos, además de obtener cookies y headers
 * de la request
 *
 * @since v1.0.0
 * @author Angel
 */
public class RestRequestReader {
    
    /**
     * Lee todo el texto de un input stream
     * 
     * @param is El input stream a leer
     * @param charset El character encoding de la request a leer
     * @return Todo el contenido del input stream como texto
     * @throws IOException Si un error de I/O ocurre
     * @since v1.0.0
     */
    public static String readInputStream(InputStream is, String charset) throws IOException{
        String set = charset == null ? "UTF-8" : charset;
        try (Reader br = new InputStreamReader(is, set)) {
            StringBuilder sb = new StringBuilder();
            int cp;
            while ((cp = br.read()) != -1) {
              sb.append((char) cp);
            }
            return sb.toString();
        }
    }
    
    /**
     * Lee un json de un input stream
     * 
     * @param is El input stream a leer
     * @param charset El character encoding de la request a leer
     * @return El contenido del input stream como un JSONOject
     * @throws IOException Si un error de I/O ocurre
     * @throws JSONException Si el contenido del input stream no es un JSON
     * @since v1.0.0
     */
    public static JSONObject getParamsJson(InputStream is, String charset) throws IOException, JSONException{
        String json = readInputStream(is, charset);
        try{
            return new JSONObject(json);
        } catch(JSONException ex) {
            return new JSONObject();
        }
    }
    
    private HttpServletRequest httpServletRequest;
    private Map<String, String> parameterMap;
    private String _METHOD;
    
    /**
     * Crea un Reader que puede obtener datos desde la request especificada
     * 
     * @param request La http request a leer
     */
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
                JSONObject json = getParamsJson(request.getInputStream(), request.getCharacterEncoding());
                Set<String> keySet = json.keySet();
                for(String key : keySet) {
                    this.parameterMap.put(key, json.get(key).toString());
                }
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            }
        }
        
        String method = this.parameterMap.get("_METHOD");
        if(request.getMethod().equalsIgnoreCase("POST") && method != null){
            this._METHOD = method;
            this.parameterMap.remove("_METHOD");
        } else {
            this._METHOD = null;
        }
    }
    
    /**
     * Funciona para añadir parametros extra al lector que no han sido
     * leídos inicialmente. Esto funciona, por ejemplo, para los
     * parametros en el pathInfo, que no se pueden leer sin la ayuda
     * del Servlet, así que el servlet se puede encargar de leerlos
     * y agregarlos al objeto RestRequestReader
     * 
     * @param key El nombre del parametro
     * @param value El valor del parmetro
     */
    protected void putParameter(String key, Object value) {
        this.parameterMap.put(key, value.toString());
    }

    /**
     * Obtiene la request que se está leyendo
     * 
     * @return La request hecha al servidor
     */
    public HttpServletRequest getRequest() {
        return httpServletRequest;
    }
    
    /**
     * Obtiene la sesión http actual o crea una si no hay
     * 
     * @return La sesión http para esta request
     */
    public HttpSession getSession() {
        return httpServletRequest.getSession();
    }
    
    /**
     * Obtiene el ServletContext del cual la request fue despachada
     * 
     * @return El contexto de la aplicación
     */
    public ServletContext getServletContext() {
        return httpServletRequest.getServletContext();
    }

    /**
     * Obtiene un mapa con todos los parametros y sus valores como String.
     * Los parametros que sean tipos nativos estarán como su representación
     * en String y deberán parsearse. Los arreglos estarán en formato
     * JSONArray {@code [...values]} y pueden ser obtenidos por medio
     * del constructor {@link org.json.JSONArray#JSONArray(java.lang.String) JSONArray(String)}
     * 
     * @return Un mapa con todos los parametros en formato String
     */
    public Map<String, String> getParameterMap() {
        return parameterMap;
    }
    
    private void comproveKey(String key) {
        if(!this.parameterMap.containsKey(key)){
            throw new RestException(HttpServletResponse.SC_BAD_REQUEST, 
                    "Parameter " + key + " is obligatory");
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
    
    /**
     * Obtiene un parametro de la request convertido a {@code byte}
     * 
     * @param key El nombre del parametro
     * @return El valor del parametro
     * @throws RestException Si el parametro no existe o su valor no puede 
     * convertirse en byte. Si se quiere prevenir esto se puede usar 
     * {@link #getByte(java.lang.String, byte) }
     */
    public byte getByte(String key) {
        this.comproveKey(key);
        String value = this.parameterMap.get(key);
        return parse(value, Byte::parseByte);
    }
    
    /**
     * Obtiene un parametro de la request convertido a {@code byte}.
     * Si falla en obtener el parametro devuelve el valor por defecto
     * 
     * @param key El nombre del parametro
     * @param def El valor por defecto, que será devuelto en caso de que
     * falle en obtener el parametro
     * @return El parametro como byte o el valor por defecto
     */
    public byte getByte(String key, byte def) {
        return getParameterOrDefault(key, def, this::getByte);
    }
    
    /**
     * Obtiene un parametro de la request convertido a {@code int}
     * 
     * @param key El nombre del parametro
     * @return El valor del parametro
     * @throws RestException Si el parametro no existe o su valor no puede 
     * convertirse en int. Si se quiere prevenir esto se puede usar 
     * {@link #getInt(java.lang.String, int) }
     */
    public int getInt(String key) {
        this.comproveKey(key);
        String value = this.parameterMap.get(key);
        return parse(value, Integer::parseInt);
    }
    
    /**
     * Obtiene un parametro de la request convertido a {@code int}.
     * Si falla en obtener el parametro devuelve el valor por defecto
     * 
     * @param key El nombre del parametro
     * @param def El valor por defecto, que será devuelto en caso de que
     * falle en obtener el parametro
     * @return El parametro como int o el valor por defecto
     */
    public int getInt(String key, int def) {
        return getParameterOrDefault(key, def, this::getInt);
    }
    
    /**
     * Obtiene un parametro de la request convertido a {@code long}
     * 
     * @param key El nombre del parametro
     * @return El valor del parametro
     * @throws RestException Si el parametro no existe o su valor no puede 
     * convertirse en long. Si se quiere prevenir esto se puede usar 
     * {@link #getLong(java.lang.String, long) }
     */
    public long getLong(String key) {
        this.comproveKey(key);
        String value = this.parameterMap.get(key);
        return parse(value, Long::parseLong);
    }
    
    /**
     * Obtiene un parametro de la request convertido a {@code long}.
     * Si falla en obtener el parametro devuelve el valor por defecto
     * 
     * @param key El nombre del parametro
     * @param def El valor por defecto, que será devuelto en caso de que
     * falle en obtener el parametro
     * @return El parametro como long o el valor por defecto
     */
    public long getLong(String key, long def) {
        return getParameterOrDefault(key, def, this::getLong);
    }
    
    /**
     * Obtiene un parametro de la request convertido a {@code float}
     * 
     * @param key El nombre del parametro
     * @return El valor del parametro
     * @throws RestException Si el parametro no existe o su valor no puede 
     * convertirse en float. Si se quiere prevenir esto se puede usar 
     * {@link #getFloat(java.lang.String, float) }
     */
    public float getFloat(String key) {
        this.comproveKey(key);
        String value = this.parameterMap.get(key);
        return parse(value, Float::parseFloat);
    }
    
    /**
     * Obtiene un parametro de la request convertido a {@code float}.
     * Si falla en obtener el parametro devuelve el valor por defecto
     * 
     * @param key El nombre del parametro
     * @param def El valor por defecto, que será devuelto en caso de que
     * falle en obtener el parametro
     * @return El parametro como float o el valor por defecto
     */
    public float getFloat(String key, float def) {
        return getParameterOrDefault(key, def, this::getFloat);
    }
    
    /**
     * Obtiene un parametro de la request convertido a {@code double}
     * 
     * @param key El nombre del parametro
     * @return El valor del parametro
     * @throws RestException Si el parametro no existe o su valor no puede 
     * convertirse en double. Si se quiere prevenir esto se puede usar 
     * {@link #getDouble(java.lang.String, double) }
     */
    public double getDouble(String key) {
        this.comproveKey(key);
        String value = this.parameterMap.get(key);
        return parse(value, Double::parseDouble);
    }
    
    /**
     * Obtiene un parametro de la request convertido a {@code double}.
     * Si falla en obtener el parametro devuelve el valor por defecto
     * 
     * @param key El nombre del parametro
     * @param def El valor por defecto, que será devuelto en caso de que
     * falle en obtener el parametro
     * @return El parametro como double o el valor por defecto
     */
    public double getDouble(String key, double def) {
        return getParameterOrDefault(key, def, this::getDouble);
    }
    
    /**
     * Obtiene un parametro de la request convertido a {@code boolean}
     * 
     * @param key El nombre del parametro
     * @return El valor del parametro
     * @throws RestException Si el parametro no existe o su valor no puede 
     * convertirse en boolean. Si se quiere prevenir esto se puede usar 
     * {@link #getBoolean(java.lang.String, boolean) }
     */
    public boolean getBoolean(String key) {
        this.comproveKey(key);
        String value = this.parameterMap.get(key);
        return parse(value, Boolean::parseBoolean);
    }
    
    /**
     * Obtiene un parametro de la request convertido a {@code boolean}.
     * Si falla en obtener el parametro devuelve el valor por defecto
     * 
     * @param key El nombre del parametro
     * @param def El valor por defecto, que será devuelto en caso de que
     * falle en obtener el parametro
     * @return El parametro como boolean o el valor por defecto
     */
    public boolean getBoolean(String key, boolean def) {
        return getParameterOrDefault(key, def, this::getBoolean);
    }
    
    /**
     * Obtiene un parametro de la request como {@link java.lang.String}
     * 
     * @param key El nombre del parametro
     * @return El valor del parametro
     * @throws RestException Si el parametro no existe. Si se quiere 
     * prevenir esto se puede usar 
     * {@link #getString(java.lang.String, java.lang.String) }
     */
    public String getString(String key) {
        this.comproveKey(key);
        return this.parameterMap.get(key);
    }
    
    /**
     * Obtiene un parametro de la request convertido a {@code String}.
     * Si falla en obtener el parametro devuelve el valor por defecto
     * 
     * @param key El nombre del parametro
     * @param def El valor por defecto, que será devuelto en caso de que
     * falle en obtener el parametro
     * @return El parametro como String o el valor por defecto
     */
    public String getString(String key, String def) {
        return getParameterOrDefault(key, def, this::getString);
    }
    
    /**
     * Si se detecta que un parametro tiene más de un valor o el parametro
     * llega en un json en forma de json array, este puede ser obtenido
     * como un objeto {@link org.json.JSONArray} por medio de esta función.
     * 
     * @param key El nombre del parametro
     * @return Los valores del parametro en un JSONArray
     * @throws RestException Si el parametro no existe o no puede ser
     * convertido en arreglo. Si se quiere 
     * prevenir esto se pueden usar los metodos
     * {@link #getArray(java.lang.String, java.lang.String) getArray(String, String)} o
     * {@link #getArray(java.lang.String, java.lang.Object[]) getArray(String, Object[])}
     */
    public JSONArray getArray(String key) {
        this.comproveKey(key);
        String value = this.parameterMap.get(key);
        return parse(value, RestRequestReader::strToJSONArray);
    }
    
    /**
     * Obtiene los valores de un parametro en un objeto 
     * {@link org.json.JSONArray} o un arreglo por defecto si no es posible
     * 
     * @param key El nombre del parametro
     * @param def El arreglo por defecto
     * @return Los valores del parametro en un JSONArray o el arreglo por
     * defecto convertido a JSONArray
     */
    public JSONArray getArray(String key, Object[] def) {
        return getParameterOrDefault(key, def == null ? null : new JSONArray(def), this::getArray);
    }
    
    /**
     * Obtiene los valores de un parametro en un objeto 
     * {@link org.json.JSONArray} o un arreglo por defecto si no es posible
     * 
     * @param key El nombre del parametro
     * @param def El String que representa el JSONArray por defecto. Ej: {@code "[]"}
     * @return Los valores del parametro en un JSONArray o el string por
     * defecto convertido a JSONArray
     */
    public JSONArray getArray(String key, String def) {
        return getParameterOrDefault(key, def == null ? null : new JSONArray(def), this::getArray);
    }
    
    public List getList(String key) {
        this.comproveKey(key);
        String value = this.parameterMap.get(key);
        return parse(value, RestRequestReader::strToColl);
    }
    
    public List getList(String key, List def) {
        return getParameterOrDefault(key, def, this::getList);
    }
    
    private static JSONArray strToJSONArray(String str) {
        if (str.startsWith("[") && str.endsWith("]")) {
            return new JSONArray(str);
        }
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(str);
        return jsonArray;
    }
    
    private static List strToColl(String jsonArray) {
        JSONArray arr = strToJSONArray(jsonArray);
        ArrayList list = new ArrayList();
        for(int i=0; i<arr.length(); i++) {
            list.add(arr.get(i));
        }
        return list;
    }
    
    /**
     * Obtiene el valor de un parametro como un json. Aunque se recomienda
     * no pasar parametros de este tipo, es posible agregar en el body de
     * una request un parametro que sea un json. En ese caso, se puede obtener
     * con este metodo
     * 
     * @param key El nombre del parametro
     * @return El parametro como {@link org.json.JSONObject}
     * @throws RestException Si el parametro no existe o no puede ser
     * convertido en json. Si se quiere 
     * prevenir esto se pueden usar los metodos
     * {@link #getJSON(java.lang.String, java.lang.String) getJSON(String, String)} o
     * {@link #getJSON(java.lang.String, java.lang.Object) getJSON(String, Object[])}
     */
    public JSONObject getJSON(String key) {
        this.comproveKey(key);
        String value = this.parameterMap.get(key);
        return parse(value, JSONObject::new);
    }
    
    /**
     * Obtiene el valor de un parametro en un objeto 
     * {@link org.json.JSONObject} o un objeto por defecto si no es posible
     * 
     * @param key El nombre del parametro
     * @param def El objeto por defecto
     * @return El valor del parametro en un JSONObject o el objeto por
     * defecto convertido a JSONObject
     */
    public JSONObject getJSON(String key, Object def) {
        return getParameterOrDefault(key, def == null ? null : new JSONObject(def), this::getJSON);
    }
    
    /**
     * Obtiene el valor de un parametro en un objeto 
     * {@link org.json.JSONObject} o un json por defecto si no es posible
     * 
     * @param key El nombre del parametro
     * @param def El String que representa el JSONObject por defecto. Ej: {@code "{}"}
     * @return El valor del parametro en un JSONObject o el string por
     * defecto convertido a JSONObject
     */
    public JSONObject getJSON(String key, String def) {
        return getParameterOrDefault(key, def == null ? null : new JSONObject(def), this::getJSON);
    }
    
    /**
     * Returns the value of the specified request header as a String. 
     * If the request did not include a header of the specified name, 
     * this method returns null. If there are multiple headers with the 
     * same name, this method returns the first head in the request.
     * <p>
     * The header name is case insensitive. You can use this method with 
     * any request header.
     * </p>
     * @param name a String specifying the header name
     * @return a String containing the value of the requested header, 
     * or null if the request does not have a header of that name
     */
    public String getHeader(String name) {
        return this.httpServletRequest.getHeader(name);
    }
    
    /**
     * Returns all the values of the specified request header as an 
     * Enumeration of String objects.
     * 
     * @param name a String specifying the header name
     * @return all the values of the specified request header
     */
    public Enumeration<String> getHeaders(String name) {
        return this.httpServletRequest.getHeaders(name);
    }
    
    /**
     * Returns an enumeration of all the header names this request contains. 
     * If the request has no headers, this method returns an empty enumeration.
     * 
     * @return an enumeration of all the header names
     */
    public Enumeration<String> getHeaderNames() {
        return this.httpServletRequest.getHeaderNames();
    }
    
    /**
     * Returns the value of the specified request header as an int. 
     * If the request does not have a header of the specified name, this method 
     * returns -1. If the header cannot 
     * be converted to an integer, this method throws a NumberFormatException.
     * 
     * @param name The name of the header
     * @return 
     */
    public int getIntHeader(String name) {
        return this.httpServletRequest.getIntHeader(name);
    }
    
    public Part getPart(String name) throws IOException, ServletException {
        return this.httpServletRequest.getPart(name);
    }
    
    public Collection<Part> getParts() {
        try {
            return this.httpServletRequest.getParts();
        } catch(IOException | ServletException ex) {
            ex.printStackTrace(System.err);
            return new ArrayList();
        }
    }
    
    /**
     * Returns an array containing all of the Cookie objects the client sent with 
     * this request. This method returns null if no cookies were sent.
     * 
     * @return an array of all the Cookies included with this request, or null 
     * if the request has no cookies
     */
    public Cookie[] getCookies() {
        return this.httpServletRequest.getCookies();
    }
    
    /**
     * Obtiene una cookie de las cookies enviadas or el cliente en la request.
     * Si no se envió una cookie con el nombre especificado, devuelve null
     * 
     * @param name El nombre de la cookie a obtener
     * @return La cookie cuyo nombre coincida con el buscado o null
     * si o se encuentra
     */
    public Cookie getCookie(String name) {
        Cookie[] cookies = this.getCookies();
        if(cookies == null) return null;
        
        for(Cookie cookie : cookies) {
            if(cookie.getName().equals(name)){
                return cookie;
            }
        }
        return null;
    }
    
    /**
     * Devuelve el método de la petición
     * 
     * @return El método http de la petición
     */
    public String getMethod() {
        return this._METHOD == null ? this.getRequest().getMethod() : this._METHOD;
    }
    
}

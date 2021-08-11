/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.angeljsb.restservlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * HttpServlet especial para la creación de ApiRests. Posee cinco funciones
 * que al ser sobreescritas pueden procesar llamadas http:
 * 
 * <ul>
 * <li>
 * {@link #processGet(io.github.angeljsb.restservlet.RestRequestReader, io.github.angeljsb.restservlet.RestResponseWriter) processGet}
 * para procesar peticiones GET
 * </li>
 * <li>
 * {@link #processPost(io.github.angeljsb.restservlet.RestRequestReader, io.github.angeljsb.restservlet.RestResponseWriter) processPost}
 * para procesar peticiones POST
 * </li>
 * <li>
 * {@link #processPut(io.github.angeljsb.restservlet.RestRequestReader, io.github.angeljsb.restservlet.RestResponseWriter) processPut}
 * para procesar peticiones PUT
 * </li>
 * <li>
 * {@link #processDelete(io.github.angeljsb.restservlet.RestRequestReader, io.github.angeljsb.restservlet.RestResponseWriter) processDelete}
 * para procesar peticiones DELETE
 * </li>
 * <li>
 * {@link #processRequest(io.github.angeljsb.restservlet.RestRequestReader, io.github.angeljsb.restservlet.RestResponseWriter) processRequest}
 * para procesar todas las anteriores en caso de que la función respectiva
 * no esté sobreescrita o devuelva {@code null}
 * </li>
 * </ul>
 * 
 * Al servlet se le asigna una ruta por medio de la anotación 
 * {@link javax.servlet.annotation.WebServlet WebServlet}
 * como a cualquier servlet normal y se pueden especificar
 * parametros en la ruta por medio de la anotación {@link WithPathParameters} 
 *
 * @author Angel
 */
public abstract class RestServlet extends HttpServlet {
    
    private void getUrlParameters(RestRequestReader requestReader) {
        WithPathParameters ann = this.getClass().getAnnotation(WithPathParameters.class);
        String pathInfo = requestReader.getRequest().getPathInfo();
        if(ann == null || pathInfo == null) {
            return;
        }
        String[] patterns = ann.pathInfoPatterns();
        for(String pattern : patterns) {
            Map<String, String> map = getParams(pathInfo, pattern);
            
            if(map!=null){
                map.forEach((key, val) -> requestReader.putParameter(key, val));
                return;
            }
        }
    }
    
    private Map<String, String> getParams(String pathInfo, String pattern) {
        Matcher matcher = Pattern
                .compile("\\{([a-zA-Z0-9]+)\\}")
                .matcher(pattern);
        if(!matcher.find()){
            return null;
        }
        
        String regex = matcher.replaceAll("(?<$1>[\\\\w\\\\-~]+)");
        regex = regex.endsWith("/") ? regex + "{0,1}": regex + "/{0,1}";
        Matcher complete = Pattern.compile(regex).matcher(pathInfo);
        if(!complete.matches()) {
            return null;
        }
        
        HashMap<String, String> map = new HashMap();
        matcher.reset();
        while(matcher.find()) {
            String paramName = matcher.group(1);
            map.put(paramName, complete.group(paramName));
        }
            
        return map;
    }
    
    /**
     * Este metodo se encarga de procesar las peticiones de los metodos
     * put, post, get y delete si sus respectivos métodos {@link #processPut},
     * {@link #processPost}, {@link #processGet} y {@link #processDelete} 
     * no están declarados o devuelven null
     * 
     * @param requestReader Un objeto que permite la lectura de
     * la request
     * @param responseWriter Un objeto que permite enviar objetos
     * en forma de json en la response
     * @return Un objeto (java-bean) que será convertido en json y
     * enviado como respuesta de la petición http
     */
    protected Object processRequest(RestRequestReader requestReader, RestResponseWriter responseWriter) {
        return null;
    }
    
    /**
     * Es llamado al recibir una petición de tipo {@code GET}. Si se
     * retorna un objeto, este es enviado como json en la response,
     * sino se procede a ejecutar {@link #processRequest}.<br><br>
     * 
     * Si se quiere que las peticiones {@code GET} ejecuten tanto este
     * método como {@link #processRequest}, se debe devolver {@code null}
     * 
     * @param requestReader Un objeto que permite la lectura de
     * la request
     * @param responseWriter Un objeto que permite enviar objetos
     * en forma de json en la response
     * @return Un objeto (java-bean) que será convertido en json y
     * enviado como respuesta de la petición http o {@code null} si se
     * quiere que también se ejecute {@link #processRequest}
     */
    protected Object processGet(RestRequestReader requestReader, RestResponseWriter responseWriter) {
        return null;
    }
    
    /**
     * Es llamado al recibir una petición de tipo {@code POST}. Si se
     * retorna un objeto, este es enviado como json en la response,
     * sino se procede a ejecutar {@link #processRequest}.<br><br>
     * 
     * Si se quiere que las peticiones {@code POST} ejecuten tanto este
     * método como {@link #processRequest}, se debe devolver {@code null}
     * 
     * @param requestReader Un objeto que permite la lectura de
     * la request
     * @param responseWriter Un objeto que permite enviar objetos
     * en forma de json en la response
     * @return Un objeto (java-bean) que será convertido en json y
     * enviado como respuesta de la petición http o {@code null} si se
     * quiere que también se ejecute {@link #processRequest}
     */
    protected Object processPost(RestRequestReader requestReader, RestResponseWriter responseWriter) {
        return null;
    }
    
    /**
     * Es llamado al recibir una petición de tipo {@code PUT}. Si se
     * retorna un objeto, este es enviado como json en la response,
     * sino se procede a ejecutar {@link #processRequest}.<br><br>
     * 
     * Si se quiere que las peticiones {@code PUT} ejecuten tanto este
     * método como {@link #processRequest}, se debe devolver {@code null}
     * 
     * @param requestReader Un objeto que permite la lectura de
     * la request
     * @param responseWriter Un objeto que permite enviar objetos
     * en forma de json en la response
     * @return Un objeto (java-bean) que será convertido en json y
     * enviado como respuesta de la petición http o {@code null} si se
     * quiere que también se ejecute {@link #processRequest}
     */
    protected Object processPut(RestRequestReader requestReader, RestResponseWriter responseWriter) {
        return null;
    }
    
    /**
     * Es llamado al recibir una petición de tipo {@code DELETE}. Si se
     * retorna un objeto, este es enviado como json en la response,
     * sino se procede a ejecutar {@link #processRequest}.<br><br>
     * 
     * Si se quiere que las peticiones {@code DELETE} ejecuten tanto este
     * método como {@link #processRequest}, se debe devolver {@code null}
     * 
     * @param requestReader Un objeto que permite la lectura de
     * la request
     * @param responseWriter Un objeto que permite enviar objetos
     * en forma de json en la response
     * @return Un objeto (java-bean) que será convertido en json y
     * enviado como respuesta de la petición http o {@code null} si se
     * quiere que también se ejecute {@link #processRequest}
     */
    protected Object processDelete(RestRequestReader requestReader, RestResponseWriter responseWriter) {
        return null;
    }
    
    protected final void doRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        RestRequestReader reader = new RestRequestReader(req);
        RestResponseWriter writer = new RestResponseWriter(resp);
        
        try {
            this.getUrlParameters(reader);

            Object result = null;

            switch(reader.getMethod().toUpperCase()) {
                case "GET":
                    result = this.processGet(reader, writer);
                    break;
                case "POST":
                    result = this.processPost(reader, writer);
                    break;
                case "PUT":
                    result = this.processPut(reader, writer);
                    break;
                case "DELETE":
                    result = this.processDelete(reader, writer);
                    break;
                default:
                    break;
            }
            
            if(writer.getResponse().isCommitted()) {
                return;
            }

            if(result != null) {
                writer.send(result);
                return;
            }

            result = processRequest(reader, writer);
            
            if(writer.getResponse().isCommitted()) {
                return;
            }
            
            if(result != null) {
                writer.send(result);
                return;
            }
            
            writer.send("{\"message\":\"There is no content to show\"}");
            
        }catch(RestException ex) {
            writer.sendError(ex);
        }catch(Exception ex) {
            writer.sendError(new RestException(400, ex.getMessage()));
        }
    }

    @Override
    protected final void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doRequest(req, resp);
    }

    @Override
    protected final void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doRequest(req, resp);
    }

    @Override
    protected final void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doRequest(req, resp);
    }

    @Override
    protected final void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doRequest(req, resp);
    }
    
}

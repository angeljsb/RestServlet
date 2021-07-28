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
 *
 * @author Angel
 */
public abstract class RestServlet extends HttpServlet {
    
    private void getUrlParameters(RestRequestReader requestReader) {
        WithPathParameters ann = this.getClass().getAnnotation(WithPathParameters.class);
        String pathInfo = requestReader.getHttpServletRequest().getPathInfo();
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
     * put, post, get y delete si sus respectivos métodos processPut,
     * processPost, processGet y processDelete no están declarados
     * o devuelven null
     * 
     * @param requestReader Un objeto que permite la lectura de
     * la request
     * @param responseWriter Un objeto que permite enviar objetos
     * en forma de json en la response
     * @return Un objeto (jave-bean) que será convertido en json y
     * enviado como respuesta de la petición http
     */
    protected Object processRequest(RestRequestReader requestReader, RestResponseWriter responseWriter) {
        return null;
    }
    
    protected Object processGet(RestRequestReader requestReader, RestResponseWriter responseWriter) {
        return null;
    }
    
    protected Object processPost(RestRequestReader requestReader, RestResponseWriter responseWriter) {
        return null;
    }
    
    protected Object processPut(RestRequestReader requestReader, RestResponseWriter responseWriter) {
        return null;
    }
    
    protected Object processDelete(RestRequestReader requestReader, RestResponseWriter responseWriter) {
        return null;
    }
    
    protected void doRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        RestRequestReader reader = new RestRequestReader(req);
        RestResponseWriter writer = new RestResponseWriter(resp);
        
        try {
            this.getUrlParameters(reader);

            Object result = null;

            switch(reader.getHttpServletRequest().getMethod().toUpperCase()) {
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

            if(result != null) {
                writer.send(result);
                return;
            }

            result = processRequest(reader, writer);
            
            if(result != null) {
                writer.send(result);
                return;
            }
            
            throw new RestException(HttpServletResponse.SC_NO_CONTENT, 
                    "No content was send by the request");
            
        }catch(RestException ex) {
            writer.sendError(ex);
        }catch(Exception ex) {
            writer.sendError(new RestException(400, ex.getMessage()));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doRequest(req, resp);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doRequest(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doRequest(req, resp);
    }
    
}

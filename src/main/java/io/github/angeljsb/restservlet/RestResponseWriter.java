/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.angeljsb.restservlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Objeto para simplificar la escritura de objetos en formato json y
 * su envio a traves de la HttpResponse.
 * <p>
 * Se puede obtener el objeto 
 * {@link javax.servlet.http.HttpServletResponse HttpServletResponse} 
 * normal del servlet por medio del método
 * {@link #getHttpServletResponse()}
 * </p>
 *
 * @author Angel
 */
public class RestResponseWriter {
    
    /**
     * Obtiene un pequeño mensaje según el código de status de una respuesta
     * 
     * @param sc El código de status
     * @return Un pequeño mensaje correspondiente al código
     * @since v1.0.0
     */
    public static String getResponseMessage(int sc){
        switch(sc){
            case HttpServletResponse.SC_CONTINUE:
                return "continue";
            case HttpServletResponse.SC_SWITCHING_PROTOCOLS:
                return "server_switching_protocols";
            case HttpServletResponse.SC_OK:
                return "successful_request";
            case HttpServletResponse.SC_CREATED:
                return "successful_created";
            case HttpServletResponse.SC_ACCEPTED:
                return "accepted_request";
            case HttpServletResponse.SC_NON_AUTHORITATIVE_INFORMATION:
                return "invalid_origin";
            case HttpServletResponse.SC_NO_CONTENT:
                return "empty_response";
            case HttpServletResponse.SC_RESET_CONTENT:
                return "should_reset";
            case HttpServletResponse.SC_PARTIAL_CONTENT:
                return "partial_response";
            case HttpServletResponse.SC_MULTIPLE_CHOICES:
                return "multiples_choices";
            case HttpServletResponse.SC_MOVED_PERMANENTLY:
                return "moved_permanently";
            case HttpServletResponse.SC_FOUND:
                return "moved_temporarily";
            case HttpServletResponse.SC_SEE_OTHER:
                return "see_other";
            case HttpServletResponse.SC_NOT_MODIFIED:
                return "not_modified";
            case HttpServletResponse.SC_USE_PROXY:
                return "proxy_required";
            case HttpServletResponse.SC_TEMPORARY_REDIRECT:
                return "temporary_redirect";
            case HttpServletResponse.SC_BAD_REQUEST:
                return "bad_request";
            case HttpServletResponse.SC_UNAUTHORIZED:
                return "authentication_required";
            case HttpServletResponse.SC_PAYMENT_REQUIRED:
                return "payment_required";
            case HttpServletResponse.SC_FORBIDDEN:
                return "forbidden_request";
            case HttpServletResponse.SC_NOT_FOUND:
                return "not_found";
            case HttpServletResponse.SC_METHOD_NOT_ALLOWED:
                return "method_not_allowed";
            case HttpServletResponse.SC_NOT_ACCEPTABLE:
                return "not_acceptable_response";
            case HttpServletResponse.SC_PROXY_AUTHENTICATION_REQUIRED:
                return "proxy_authentication_required";
            case HttpServletResponse.SC_REQUEST_TIMEOUT:
                return "request_timeout";
            case HttpServletResponse.SC_CONFLICT:
                return "conflict";
            case HttpServletResponse.SC_GONE:
                return "not_longer_available";
            case HttpServletResponse.SC_LENGTH_REQUIRED:
                return "content_length_required";
            case HttpServletResponse.SC_PRECONDITION_FAILED:
                return "precondition_failed";
            case HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE:
                return "request_too_large";
            case HttpServletResponse.SC_REQUEST_URI_TOO_LONG:
                return "request_uri_too_long";
            case HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE:
                return "unsupported_media_type";
            case HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE:
                return "requested_range_not_satisfiable";
            case HttpServletResponse.SC_EXPECTATION_FAILED:
                return "expectation_failed";
            case HttpServletResponse.SC_INTERNAL_SERVER_ERROR:
                return "server_error";
            case HttpServletResponse.SC_NOT_IMPLEMENTED:
                return "not_implemented";
            case HttpServletResponse.SC_BAD_GATEWAY:
                return "bad_gateway";
            case HttpServletResponse.SC_SERVICE_UNAVAILABLE:
                return "server_overloaded";
            case HttpServletResponse.SC_GATEWAY_TIMEOUT:
                return "gateway_timeout";
            case HttpServletResponse.SC_HTTP_VERSION_NOT_SUPPORTED:
                return "http_version_not_supported";
            default:
                return "";
        }
    }
    
    /**
     * Crea un objeto de respuesta estandarizado para ser enviado como
     * json en la respuesta del servidor
     * 
     * @since v1.0.0
     * @param sc El código de status
     * @param message Un mensaje a enviar como cuerpo de la respuesta
     * @return El objeto RestResponse
     */
    public static RestResponseBody createRestResponse(int sc, String message){
        RestResponseBody res = new RestResponseBody();
        res.setStatus(sc);
        res.setMessage(getResponseMessage(sc));
        res.setBody(message);
        return res;
    }
    
    private HttpServletResponse httpServletResponse;
    
    /**
     * Crea un objeto RestResponseWriter para una response de un servlet
     * 
     * @param response La response http a la que se quiere enviar la información
     */
    public RestResponseWriter(HttpServletResponse response) {
        this.httpServletResponse = response;
    }
    
    /**
     * Obtiene la response a la que este Writer envía la información
     * 
     * @return La response asociada
     */
    public HttpServletResponse getResponse() {
        return httpServletResponse;
    }
    
    /**
     * Reinicia la response y envía el error pasado como un json, cambiando
     * el status de la petición al que declara el error
     * 
     * @param ex El error que provoca este envío, el cual debe especificar el
     * codigo de status http
     */
    public void sendError(RestException ex) {
        RestResponseBody body = createRestResponse(ex.getStatusCode(), ex.getMessage());
        this.httpServletResponse.reset();
        this.httpServletResponse.setStatus(body.getStatus());
        this.send(body);
    }
    
    /**
     * Convierte un objeto a formato json y lo envía como contenido de 
     * la respuesta. Si el objeto es un JSONObject o un JSONArray
     * lo envia tal y como está. Sino, será interpretado a formato
     * JSON tomando en cuenta lo siguiente:
     * <ul>
     * <li>Si es un array: Se pasa al constructor de {@link org.json.JSONArray#JSONArray(java.lang.Object) }</li>
     * <li>Si es un String: Se pasa al constructor {@link org.json.JSONObject#JSONObject(java.lang.Object) }</li>
     * <li>Si no es ninguno de los anteriores, se pasa a {@link org.json.JSONObject#JSONObject(java.lang.String) }</li>
     * </ul>
     * 
     * @param body El objeto que se desea enviar
     */
    public void send(Object body) {
        Object jsonBody;
        if(body instanceof IJsonable) {
            jsonBody = ((IJsonable) body).toJson();
        } else if(body instanceof JSONObject || body instanceof JSONArray) {
            jsonBody = body;
        } else if(body.getClass().isArray()) {
            jsonBody = JSONHelper.toJsonArray((Object[])body);
        } else if(body instanceof String) {
            String jsonStr = (String)body;
            if(jsonStr.startsWith("[")){
                jsonBody = new JSONArray(jsonStr);
            } else if(jsonStr.startsWith("{")){
                jsonBody = new JSONObject();
            }else {
                throw new IllegalArgumentException("Se intentó enviar "
                        + "un recurso que no puede ser interpretado como"
                        + "json: " + jsonStr);
            }
        } else if(body instanceof Collection) {
            Collection coll = (Collection) body;
            jsonBody = JSONHelper.toJsonArray(coll);
        } else {
            jsonBody = new JSONObject(body);
        }
        
        this.getResponse().setContentType(MediaType.APPLICATION_JSON);
        this.getResponse().setCharacterEncoding("UTF-8");
        try(PrintWriter out = this.getResponse().getWriter()){
            out.print(jsonBody);
        }catch (IOException e){
            throw new RestException(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }
    
    public void sendRedirect(String path) {
        try {
            this.httpServletResponse.sendRedirect(path);
        } catch (IOException ex) {
            throw new RestException(500, ex.getMessage());
        }
    }
    
    public void addHeader(String name, String value) {
        this.httpServletResponse.addHeader(name, value);
    }
    
    public void setHeader(String name, String value) {
        this.httpServletResponse.setHeader(name, value);
    }
    
    public String getHeader(String name) {
        return this.httpServletResponse.getHeader(name);
    }
    
    public Collection<String> getHeaders(String name) {
        return this.httpServletResponse.getHeaders(name);
    }
    
    public Collection<String> getHeaderNames() {
        return this.httpServletResponse.getHeaderNames();
    }
    
    public void addCookie(Cookie cookie) {
        this.httpServletResponse.addCookie(cookie);
    }
    
}

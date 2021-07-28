/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.angeljsb.restservlet;

/**
 * Bean que define el formato para la respuesta de una api rest.
 * No es un formato obligatorio para las respuestas, pero permite tener
 * un manejo adecuado de los códigos de status y mensajes de error
 *
 * @author Angel
 */
public class RestResponseBody {
   
    private int status;
    private String message;
    private String body;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
    
}

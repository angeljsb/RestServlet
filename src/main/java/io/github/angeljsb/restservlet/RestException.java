/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.angeljsb.restservlet;

/**
 * Error que maneja un codigo de error para repostar errores de http
 * y enviarlos en las responses
 *
 * @author Angel
 */
public class RestException extends RuntimeException {
    
    private int statusCode;
    
    public RestException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
    
}

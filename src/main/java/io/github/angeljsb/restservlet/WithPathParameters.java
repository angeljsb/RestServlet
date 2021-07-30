/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.angeljsb.restservlet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación para señalar el formato de parametros en el pathInfo
 * de una petición y poder leerlos automaticamente.<br>
 * La anotación {@link javax.servlet.annotation.WebServlet WebServlet} y el señalamiento en esta de urlPatterns es
 * necesario para que esta clase haga su función.<br><br>
 * 
 * En esta anotación se puede pasar un arreglo de strings 
 * {@link #pathInfoPatterns} que
 * especifique el nombre y la posición de los parametros, colocando
 * el nombre del parametro dentro entre corchetes:<br>
 * Si la anotación de {@link javax.servlet.annotation.WebServlet WebServlet} es: <code>@WebServlet(urlPatterns = {"/api/*"})</code>
 * Podemos usar una anotación : <code>@WithPathParameters(pathInfoPatterns = {"/{id}"})</code><br>
 * Y se hace el llamado a <code>https://example.com/api/12</code><br>
 * El requestReader para esa petición tendrá un parametro
 * llamado {@code id} con el valor "12".<br><br>
 * 
 * Los nombres de los parametros en la ruta solo pueden tener números y
 * letras, no se permiten guiones (-) ni guiones bajos (_)
 *
 * @author Angel
 */
@Target(value = {ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface WithPathParameters {
    
    public String[] pathInfoPatterns() default {};
    
}

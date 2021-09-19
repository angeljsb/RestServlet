/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.angeljsb.restservlet;

import org.json.JSONObject;

/**
 *
 * @author Angel
 */
@FunctionalInterface
public interface IJsonable {
    
    public JSONObject toJson();
    
}

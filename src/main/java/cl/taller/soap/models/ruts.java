/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cl.taller.soap.models;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Isra Ramirez
 */
public class ruts {
    private String rut;
    public List<Double> puntajes = new ArrayList<Double>();
    public List<Integer> codCarreras = new ArrayList<Integer>();
    public void setRut(String rut){
        this.rut = rut;
    }
    public String getRut(){
        return this.rut;
    }
    
}

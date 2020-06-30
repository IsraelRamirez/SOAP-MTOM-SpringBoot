package cl.taller.soap.models;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.util.Pair;

public class ruts {
    private String rut;
    public List<Pair<Integer,Double>> carreraPondera = new ArrayList<Pair<Integer,Double>>();
    public void setRut(String rut){
        this.rut = rut;
    }
    public String getRut(){
        return this.rut;
    }
    
}

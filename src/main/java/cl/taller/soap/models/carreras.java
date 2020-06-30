package cl.taller.soap.models;

import java.util.ArrayList;
import java.util.List;

public class carreras {
    private int codCarrera;
    private double first;
    private int vacant;
    public List<Double> ponderacion = new ArrayList<Double>();
    public List<ruts> personas = new ArrayList<ruts>();
    
    public int getCod(){
        return this.codCarrera;
    }
    public void setCod(int codCarrera){
        this.codCarrera=codCarrera;
    }
    public int getVacant(){
        return this.vacant;
    }
    public void setVacant(int vacant){
        this.vacant = vacant;
    }
    public double getFirst(){
        return first;
    }
    public void setFirst(double first){
        this.first = first;
    }
    
}

package cl.taller.soap.models;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.util.Pair;

public class carreras {
    private int codCarrera;
    private int vacant;
    public List<Pair<String,Double>> personas = new ArrayList<Pair<String,Double>>();
    
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
    
}

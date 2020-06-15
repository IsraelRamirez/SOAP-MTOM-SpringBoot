package cl.taller.soap.models;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.util.Pair;

public class carreras {
    private int codCarrera;
    public List<Pair<String,Double>> personas = new ArrayList<Pair<String,Double>>();
    
    public int getCod(){
        return this.codCarrera;
    }
    public void setCod(int codCarrera){
        this.codCarrera=codCarrera;
    }
    
}

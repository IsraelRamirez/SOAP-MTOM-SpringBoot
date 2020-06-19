#include <cstdlib>
#include <iostream>
#include <sstream>
#include <fstream>
#include <utility>
#include <vector>
#include <string>
#include <list>

#include "ruts.h"
#include "carreras.h"
#include "connect.h"

using namespace std;
//Es necesario cambiar por los datos del nuevo servidor
char* ipserver = (char *) "192.168.0.16";
char* port = (char *) "5432";
char* dbname =(char *)  "psudb";
char* user =(char *)  "psu";
char* password =(char *)  "psu";

vector<string> split(string data, char c);
vector<carreras> initCarreras();
void operador(vector<string> data, vector<carreras> carrera, vector<ruts> ruts);
double ponderador(vector<string> data,int codCarrera);
int indexador(vector<pair<string,double>> carrera, double ponderado,int vacant);
vector<pair<string,double>> remove(vector<pair<string,double>> v,int index);
vector<pair<string,double>> insert(vector<pair<string,double>> v,pair<string,double> e, int index);

int main(int argc, char** argv) {
    
    vector<carreras> carreras=initCarreras();
    vector<ruts> ruts;
    string fullData ="14916641;556;553;491;660;555;656\n14916642;706;610;696;629;550;564\n16170579;618;545;583;650;621;661\n18678455;679;603;689;563;643;516\n14916643;631;533;619;625;634;487\n17424517;490;732;495;647;560;583\n14916644;479;609;688;603;490;750\n18678456;485;543;483;502;659;550\n16170580;661;518;622;493;680;539\n17424518;639;654;500;583;714;724\n14916645;547;542;622;495;563;710\n18678457;692;659;658;625;625;617\n16170581;662;529;540;588;525;593\n17424519;540;738;732;529;588;601\n14916646;520;529;555;510;664;501\n18678458;519;560;522;736;626;538\n16170582;557;562;626;661;749;690\n";//= argv[1];
    
    vector<string> datas = split(fullData,'\n');
    operador(datas,carreras,ruts);
    for(int i =0;i<carreras.size();i++){
        cout<<carreras[i].getCod()<<endl;
        for(int j=0;j<carreras[i].personas.size();j++){
            cout<<"["<<carreras[i].personas[j].first<<";"<<carreras[i].personas[j].second<<"],";
        }
        cout<<endl<<endl;
    }
    
    
    /*
    while(!isEmpty(ruts)){

        int rutLength = ruts.size();
        for(int i = 0; i<rutLength;i++){
            int rutCodLength = ruts.get(i).codCarreras.size();

            if(rutCodLength>0){

                if(ruts.get(i).codCarreras.get(rutCodLength-1)!=-1){ //Si el último código ingresado es -1, significa que está en un carrera
                    Pair<Integer, Double> pair = ponderadorMultiple(ruts.get(i));
                    if(pair.getValue()!=0.0){ //Si la ponderación es 0, significa que no existe una carrera en la que este rut pueda ingresar, por lo tanto se le asigna -1 como código de carrera
                        for(int j=0;j<carreras.size();j++){
                            if(carreras.get(j).getCod()==pair.getKey()){
                                int index = indexador(carreras.get(j).personas,pair.getValue(),carreras.get(j).getVacant());
                                if(index>=0){
                                    carreras.get(j).personas.add(index,new Pair<>(ruts.get(i).getRut(),pair.getValue()));
                                    ruts.get(i).codCarreras.add(-1);
                                }
                                else if(index == -2){
                                    ruts.get(i).codCarreras.add(carreras.get(j).getCod());
                                }
                                else if(index == -1){
                                    carreras.get(j).personas.add(new Pair<>(ruts.get(i).getRut(),pair.getValue()));
                                    ruts.get(i).codCarreras.add(-1);
                                }
                                int carPersonasLength = carreras.get(j).personas.size();
                                if(carPersonasLength>carreras.get(j).getVacant()){
                                    String tmpRut = carreras.get(j).personas.get(carPersonasLength-1).getKey();
                                    carreras.get(j).personas.remove(carPersonasLength-1);
                                    for(int k=0;k<rutLength;k++){
                                        if(ruts.get(k).getRut().equals(tmpRut)){
                                            ruts.get(k).codCarreras.add(carreras.get(j).getCod());
                                            break;
                                        }
                                    }
                                }
                                break;
                            }
                        }
                    }
                    else{
                        ruts.get(i).codCarreras.add(-1);
                    }
                }

            }
        }
    }*/
    
    return 0;
}

vector<carreras> initCarreras(){
    vector<carreras> carrera;
    connect db;
    db.dbconnect(ipserver,port,dbname,user,password);
    
    char* dbquery =(char *) "SELECT codCarrera, vacant FROM ponderados";
    db.dbquery(dbquery);
    for(int i=0;i<db.dbnumrows();i++){
        carreras Tmpcarrera;
        Tmpcarrera.setCod(stoi(db.dbOutCome(i,0)));
        Tmpcarrera.setVacant(stoi(db.dbOutCome(i,1)));
        carrera.push_back(Tmpcarrera);
    }
        
    return carrera;
}

vector<string> split(string data, char c){
    vector<string> splited;
    string linea="";
    for(int i=0;i<data.length();i++){
        if(data[i]!=c){
            linea+=data[i];
        }
        else{
            splited.push_back(linea);
            linea="";
        }
    }
    return splited;
}

void operador(vector<string> data, vector<carreras> carrera, vector<ruts> rutsV){
    for(int i=0;i<data.size();i++){
        vector<string> datos = split(data[i],';');
        if((stod(datos[3])+stod(datos[4]))/2>=450){

            double mayor = 0.0;
            int codCarrera = carrera[0].getCod();
            for(int j=0;j<carrera.size();j++){
                if(datos.size()>1){
                    double ponderado = ponderador(datos,carrera[j].getCod());
                    if(ponderado > mayor){
                        codCarrera = carrera[j].getCod();
                        mayor = ponderado;
                    }

                }
            }
            for(int j=0;j<carrera.size();j++){
                if(carrera[j].getCod()==codCarrera){

                    ruts persona;
                    persona.setRut(datos[0]);
                    persona.puntajes.push_back(stod(datos[1]));
                    persona.puntajes.push_back(stod(datos[2]));
                    persona.puntajes.push_back(stod(datos[3]));
                    persona.puntajes.push_back(stod(datos[4]));
                    persona.puntajes.push_back(stod(datos[5]));
                    persona.puntajes.push_back(stod(datos[6]));
                    rutsV.push_back(persona);

                    int const index = indexador(carrera[j].personas,mayor,carrera[j].getVacant());
                    pair<string,double> pa(datos[0],mayor);
                    if(index>=0){
                        carrera[j].personas = insert(carrera[j].personas,pa,index);
                    }
                    else if(index==-2){

                        int length = rutsV.size();
                        rutsV[length-1].codCarreras.push_back(codCarrera);
                    }
                    else if(index==-1){
                        
                        carrera[j].personas.push_back(pa);
                    }
                    int carPersonasLength = carrera[j].personas.size();
                    if(carPersonasLength>carrera[j].getVacant()){
                        string tmpRut = carrera[j].personas[carPersonasLength-1].first;
                        carrera[j].personas=remove(carrera[j].personas,carPersonasLength--);
                        for(int k=0;k<(int)rutsV.size();k++){
                            if(rutsV[k].getRut()==tmpRut){
                                rutsV[k].codCarreras.push_back(carrera[j].getCod());
                                break;
                            }
                        }
                    }
                    break;
                }
            }
        }
    }
}
double ponderador(vector<string> data,int codCarrera){
    
    connect db;
    db.dbconnect(ipserver,port,dbname,user,password);
    string tmp = "SELECT nem, ranking,lenguaje,matematica,histociencia FROM ponderado WHERE codCarrera=" + to_string(codCarrera);
    char* uri = new char[250];
    snprintf(uri, 250, "SELECT nem, ranking,lenguaje,matematica,histociencia FROM ponderado WHERE codCarrera=%s",to_string(codCarrera));
    db.dbquery(uri);
    double ponderado = 0.0;
    ponderado = (stod(data[1]) * stod(db.dbOutCome(0,0)))+
            (stod(data[2]) * stod(db.dbOutCome(0,1)))+
            (stod(data[3]) * stod(db.dbOutCome(0,2)))+
            (stod(data[4]) * stod(db.dbOutCome(0,3)));
    
        if(stod(data[5])>stod(data[6])){
            ponderado += (stod(data[5]) * stod(db.dbOutCome(0,4)));
        }
        else{
            ponderado += (stod(data[6]) * stod(db.dbOutCome(0,4)));
        }
        return ponderado;

    
    return 0.0;
}

int indexador(vector<pair<string,double>> carrera,double ponderado,int vacant){
    int index=-1;
    int length = carrera.size();
    if(length < vacant){
        for(int i =0; i<length;i++){
            if(ponderado > carrera[i].second){
                return i;
            }
        }
    }
    else{
        if(ponderado>carrera[length-1].second){
            for(int i =0; i<length;i++){
                if(ponderado > carrera[i].second){
                    return i;
                }
            }
        }
        else{
            return -2;
        }
    }

    return index;
}

vector<pair<string,double>> remove(vector<pair<string,double>> v,int index){
    vector<pair<string,double>> nuevoVector;
    for(int i = 0; i<v.size();i++){
        if(i != index){
            nuevoVector.push_back(v[i]);
        }
    }
    return nuevoVector;
}
vector<pair<string,double>> insert(vector<pair<string,double>> v,pair<string,double> e, int index){
    vector<pair<string,double>> nuevoVector;
    for(int i = 0; i<v.size();i++){
        if(i == index){
            nuevoVector.push_back(e);
        }
        nuevoVector.push_back(v[i]);
    }
    return nuevoVector;
}
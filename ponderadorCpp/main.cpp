#include <cstdlib>
#include <iostream>
#include <sstream>
#include <fstream>
#include <utility>
#include <vector>
#include <string>
#include <list>
#include <omp.h>

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
void operador(vector<string> data, vector<carreras>* carrera, vector<ruts>* ruts);
double ponderador(vector<string> data,int codCarrera);
pair<int, double> ponderadorMultiple(ruts rut);
int indexador(vector<pair<string,double>> carrera, double ponderado,int vacant);
vector<pair<string,double>> remove(vector<pair<string,double>> v,int index);
vector<pair<string,double>> insert(vector<pair<string,double>> v,pair<string,double> e, int index);
char* tc(string linea);
bool isEmpty(vector<ruts> rut);

int main(int argc, char** argv) {
    
    vector<carreras> carrera=initCarreras();
    vector<ruts> rut;
    ifstream entrada("/media/compartida/puntajes2.csv");
    //string fullData ="14916641;556;553;491;660;555;656\n14916642;706;610;696;629;550;564\n16170579;618;545;583;650;621;661\n18678455;679;603;689;563;643;516\n14916643;631;533;619;625;634;487\n17424517;490;732;495;647;560;583\n14916644;479;609;688;603;490;750\n18678456;485;543;483;502;659;550\n16170580;661;518;622;493;680;539\n17424518;639;654;500;583;714;724\n14916645;547;542;622;495;563;710\n18678457;692;659;658;625;625;617\n16170581;662;529;540;588;525;593\n17424519;540;738;732;529;588;601\n14916646;520;529;555;510;664;501\n18678458;519;560;522;736;626;538\n16170582;557;562;626;661;749;690\n";//= argv[1];
    vector<string> datas; //= split(fullData,'\n');
    for(string linea;getline(entrada,linea);){
        datas.push_back(linea);
    }
    entrada.close();
     
    operador(datas,&carrera,&rut);
    
    while(!isEmpty(rut)){
        int rutLength = rut.size();
#pragma omp parallel
{
#pragma omp for
{
        for(int i = 0; i<rutLength;i++){
            int rutCodLength = rut.at(i).codCarreras.size();
            
            if(rutCodLength>0){
                if(rut.at(i).codCarreras.at(rutCodLength-1)!=-1){ //Si el último código ingresado es -1, significa que está en un carrera
                    
                    pair<int, double> pa = ponderadorMultiple(rut.at(i));
                    
                    if(pa.second!=0.0){ //Si la ponderación es 0, significa que no existe una carrera en la que este rut pueda ingresar, por lo tanto se le asigna -1 como código de carrera
                        
                        for(int j=0;j<carrera.size();j++){
                            
                            if(carrera.at(j).getCod()==pa.first){
#pragma omp critical
{
                                int index = indexador(carrera.at(j).personas,pa.second,carrera.at(j).getVacant());
                                pair<string,double> paTmp(rut.at(i).getRut(),pa.second);
                                
                                if(index>=0){
                                    
                                    //cambiar por la funcion de insertar
                                    carrera.at(j).personas = insert(carrera.at(j).personas,paTmp,index);
                                    rut.at(i).codCarreras.push_back(-1);
                                    
                                }
                                else if(index == -2){
                                    
                                    rut.at(i).codCarreras.push_back(carrera.at(j).getCod());
                                    
                                }
                                else if(index == -1){
                                    
                                    carrera.at(j).personas.push_back(paTmp);
                                    rut.at(i).codCarreras.push_back(-1);
                                    
                                }
                                
                                int carPersonasLength = carrera.at(j).personas.size();
                                if(carPersonasLength>carrera.at(j).getVacant()){
                                    
                                    string tmpRut = carrera.at(j).personas.at(carPersonasLength-1).first;
                                    //usar la funcion remove 
                                    carrera.at(j).personas=remove(carrera.at(j).personas,carPersonasLength--);
                                    for(int k=0;k<rutLength;k++){
                                        if(rut.at(k).getRut()==tmpRut){
                                            
                                            rut.at(k).codCarreras.push_back(carrera.at(j).getCod());
                                            break;
                                        }
                                    }
                                
                                }
                            
                                break;
}
                            }
                        

                        }
                    
                        
                    }
                    else{
                        rut.at(i).codCarreras.push_back(-1);
                    }
                }

            }
        }
}
}
    }
    for(int i =0;i<carrera.size();i++){
        cout<<carrera[i].getCod()<<endl;
        for(int j=0;j<carrera[i].personas.size();j++){
            cout<<"["<<carrera[i].personas[j].first<<";"<<carrera[i].personas[j].second<<"],";
        }
        cout<<endl<<endl;
    }
    
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
    db.dbfree();
    db.dbclose();
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
    splited.push_back(linea);
    return splited;
}

void operador(vector<string> data, vector<carreras>* carrera, vector<ruts>* rutsV){
#pragma omp parallel
{
#pragma omp for
{
    for(int i=0;i<data.size();i++){
        vector<string> datos = split(data.at(i),';');
        if(datos.size()>1){
            if((stod(datos.at(3))+stod(datos.at(4)))/2>=450){
                double mayor = 0.0;
                int codCarrera = (*carrera).at(0).getCod();
                for(int j=0;j<(*carrera).size();j++){
                    double ponderado = ponderador(datos,(*carrera).at(j).getCod());
                    if(ponderado > mayor){
                        codCarrera = (*carrera).at(j).getCod();
                        mayor = ponderado;
                    }
                }
                for(int j=0;j<(*carrera).size();j++){
                    if((*carrera).at(j).getCod()==codCarrera){

                        ruts persona;
                        persona.setRut(datos[0]);
                        persona.puntajes.push_back(stod(datos.at(1)));
                        persona.puntajes.push_back(stod(datos.at(2)));
                        persona.puntajes.push_back(stod(datos.at(3)));
                        persona.puntajes.push_back(stod(datos.at(4)));
                        persona.puntajes.push_back(stod(datos.at(5)));
                        persona.puntajes.push_back(stod(datos.at(6)));
#pragma omp critical
{
                        (*rutsV).push_back(persona);

                        int index = indexador((*carrera).at(j).personas,mayor,(*carrera).at(j).getVacant());
                        
                        if(index>=0){
                            pair<string,double> pa(datos.at(0),mayor);
                            (*carrera).at(j).personas = insert((*carrera).at(j).personas,pa,index);
                        }
                        else if(index==-2){

                            for(int w=(*rutsV).size()-1;w>=0;w--){
                                if((*rutsV).at(w).getRut()==datos[0]){
                                    (*rutsV).at(w).codCarreras.push_back(codCarrera);
                                    break;
                                }
                            }
                            
                        }
                        else if(index==-1){
                            pair<string,double> pa(datos.at(0),mayor);
                            (*carrera).at(j).personas.push_back(pa);
                        }
                        int carPersonasLength = (*carrera).at(j).personas.size();
                        if(carPersonasLength>(*carrera).at(j).getVacant()){
                            string tmpRut = (*carrera).at(j).personas.at(carPersonasLength-1).first;
                            (*carrera).at(j).personas=remove((*carrera).at(j).personas,carPersonasLength-1);
                            for(int k=0;k<(*rutsV).size();k++){
                                if((*rutsV).at(k).getRut()==tmpRut){
                                    (*rutsV).at(k).codCarreras.push_back((*carrera).at(j).getCod());
                                    break;
                                }
                            }
                        }
}
                        break;
                    }
                }
            }
        }
        
    }
}
}
}
double ponderador(vector<string> data,int codCarrera){
    connect db;
    db.dbconnect(ipserver,port,dbname,user,password);
    string tmp = "SELECT nem, ranking,lenguaje,matematica,histociencia FROM ponderados WHERE codCarrera=" + to_string(codCarrera);
    
    db.dbquery(tc(tmp));
    double ponderado;
    ponderado = (stod(data.at(1)) * stod(db.dbOutCome(0,0)))+(stod(data.at(2)) * stod(db.dbOutCome(0,1)))+(stod(data.at(3)) * stod(db.dbOutCome(0,2)))+(stod(data.at(4)) * stod(db.dbOutCome(0,3)));
    
    if(stod(data.at(5))>stod(data.at(6))){
        ponderado += (stod(data.at(5)) * stod(db.dbOutCome(0,4)));
    }
    else{
        ponderado += (stod(data.at(6)) * stod(db.dbOutCome(0,4)));
    }
    
    db.dbfree();
    db.dbclose();
    return ponderado;
}

pair<int,double> ponderadorMultiple(ruts rut){
    connect db;
    db.dbconnect(ipserver,port,dbname,user,password);
    
    if(rut.codCarreras.at(0)!=-1){
        string fullQuery = "SELECT nem, ranking,lenguaje,matematica,histociencia,codCarrera FROM ponderados WHERE codCarrera != "+to_string(rut.codCarreras.at(0));
        for(int i=1;i<rut.codCarreras.size();i++){
            if(rut.codCarreras.at(i)!=-1){
                fullQuery += " AND codCarrera != "+to_string(rut.codCarreras.at(i));
            }
        }
        db.dbquery(tc(fullQuery));
        int cod = 0;
        double mayor = 0.0;
        for(int i=0; i<db.dbnumrows();i++){
            double ponderado = 0.0;
            ponderado = (rut.puntajes.at(0) * stod(db.dbOutCome(i,0)) )+(rut.puntajes.at(1)  * stod(db.dbOutCome(i,1)) )+(rut.puntajes.at(2)+stod(db.dbOutCome(i,2)))+(rut.puntajes.at(3)  * stod(db.dbOutCome(i,0)));
            if(rut.puntajes.at(4)>rut.puntajes.at(5)){
                ponderado += (rut.puntajes.at(4)  * stod(db.dbOutCome(i,4)));
            }
            else{
                ponderado += (rut.puntajes.at(5)  * stod(db.dbOutCome(i,4)));
            }
            if((rut.puntajes.at(2) +rut.puntajes.at(3) )/2 >=450){
                if(ponderado > mayor){
                    mayor=ponderado;
                    cod = stoi(db.dbOutCome(i,5));
                }
            }
        }
        db.dbfree();
        db.dbclose();
        pair<int,double> pa(cod,mayor);
        return pa;
    }
    db.dbfree();
    db.dbclose();
    pair<int,double> pa(0,0.0);
    return pa;
}

int indexador(vector<pair<string,double>> carrera,double ponderado,int vacant){
    int index=-1;
    int length = carrera.size();
    if(length < vacant){
        for(int i =0; i<length;i++){
            if(ponderado > carrera.at(i).second){
                return i;
            }
        }
    }
    else{
        if(ponderado>carrera[length-1].second){
            for(int i =0; i<length;i++){
                if(ponderado > carrera.at(i).second){
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
            nuevoVector.push_back(v.at(i));
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
        nuevoVector.push_back(v.at(i));
    }
    return nuevoVector;
}
char* tc(string linea){
    char* tmp=new char[linea.length()+1];
    for(int i=0;i<linea.length();i++){
        tmp[i]=linea[i];
    }
    tmp[linea.length()]='\0';
    return tmp;
}
bool isEmpty(vector<ruts> rut){
    bool isEmpty = true;
    for(int i=0;i<rut.size();i++){
        if(rut.at(i).codCarreras.size()>0){
            if(rut.at(i).codCarreras.at(rut.at(i).codCarreras.size()-1)!=-1){
                return false;
            }
        }
    }
    return isEmpty;
}


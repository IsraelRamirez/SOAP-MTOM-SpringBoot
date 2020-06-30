package cl.taller.soap.service;

import static cl.taller.soap.dbconnetion.connetiondb.connectDB;
import cl.taller.soap.models.File;
import cl.taller.soap.models.GetDataRequest;
import cl.taller.soap.models.GetDataResponse;
import cl.taller.soap.models.carreras;
import cl.taller.soap.models.ruts;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import org.apache.commons.math3.util.Pair;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
public class WServiceImple{
    
    private static final String token = "fk2x6rpw6fDCkXqDlqeeR22u8jpN6qGa";
    private static final String error = "Token ingresado no válido o Tipo mime ingresado no válido.";
    private static final String queryInit = "SELECT codCarrera, vacant, nem, ranking,matematica,lenguaje,histociencia, first FROM ponderado";
    private static final String mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final String fileName = "puntajes.xlsx";
    private static final String mimeTypeRequest = "text/csv";


    /**
     * Función que valida la llave token ingresada por el cliente
     * @param request Objecto con los puntajes y datos de los alumnos
     * @return Los puntajes ponderados en caso de que el token esté bien ingresado
     * @throws java.io.UnsupportedEncodingException Excepción con respecto a la encodificación de datos
     * @throws java.io.IOException Excepción respectiva a un error de input/output
     * @throws java.io.FileNotFoundException Excepción respectiva a que no se encuentra un archivo
     * @throws java.sql.SQLException Excepción respectiva a un error en la conexión a la base de datos o en la sentencia SQL
     */
    public GetDataResponse auth(GetDataRequest request) throws IOException, UnsupportedEncodingException, FileNotFoundException, SQLException{
        
        if(request.getToken().equals(token) && request.getMimetype().equals(mimeTypeRequest)){
            return puntajes(request);
        }
        File invalidToken = new File();
        
        GetDataResponse data = new GetDataResponse();
        invalidToken.setContent(error.getBytes());
        invalidToken.setFilename("error.txt");
        invalidToken.setMimetype("text/plain");
        data.setFile(invalidToken);
        return data;
             
    }
    /**
     * Función principal del WS soap, calcula las ponderaciones de los de acuerdo a sus puntajes PSU y la carrera que eligen
     * @param request Requerimiento del cliente en forma de objeto con los datos asociados
     * @return devuelve las ponderaciones de los puntajes de cada alumno dependiendo de la carrera
     * @throws java.io.UnsupportedEncodingException Excepción con respecto a la encodificación de datos
     * @throws java.io.IOException Excepción respectiva a un error de input/output
     * @throws java.io.FileNotFoundException Excepción respectiva a que no se encuentra un archivo
     * @throws java.sql.SQLException Excepción respectiva a un error en la conexión a la base de datos o en la sentencia SQL
     */
    private GetDataResponse puntajes(GetDataRequest request) throws UnsupportedEncodingException, IOException, FileNotFoundException, SQLException{
        GetDataResponse getData = new GetDataResponse();
                
        List<carreras> carreras = initCarreras();
        List<ruts> ruts = new ArrayList<ruts>();
        
        String fullData = new String(Base64.getDecoder().decode(request.getContent()),"UTF-8");
        String[] datas = fullData.split("\n");
        
        ordenarDesdeExtraccion(carreras, ruts, datas);

        acomodador(carreras, ruts);

        getData.setFile(setData(getExcel(carreras),fileName,mimeType));
        carreras.clear();
        ruts.clear();
        return getData;
    }
    /**
     * Función que genera, según los ruts ingresados en la carrera, un excel con los datos obtenidos
     * @param carreras Lista con las carreras, sus ruts ingresados y sus ponderaciones
     * @return Devuelve un Array de byte con el formato de excel "xlsx"
     * @throws IOException Excepción relacionada con algún fallo generado por el ingreso de los datos en el formato excel     
     */
    private ByteArrayOutputStream getExcel(List<carreras> carreras) throws IOException{
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Workbook wb = new SXSSFWorkbook();
        
        for(int i=0;i< carreras.size();i++){
            Sheet sheet = wb.createSheet(String.valueOf(carreras.get(i).getCod()));
            for(int j=0;j<carreras.get(i).personas.size();j++){
                Row row = sheet.createRow(j);
                
                Cell cell = row.createCell(0);
                cell.setCellValue(carreras.get(i).personas.get(j).getRut());
                cell = row.createCell(1);
                cell.setCellValue(carreras.get(i).personas.get(j).carreraPondera.get(0).getValue());
            }
        }
        
        wb.write(bos);
        wb.close();
        return bos;
    }
    /**
     * Función que genera una lista de carreras, con el codigo de carrera correspondiente.
     * @return Devuelve una lista de las carreras inicialiazadas con el código de carrera.
     * @throws SQLException Excepción relacionada con algún problema con el motor de base de datos usados.
     */
    private List<carreras> initCarreras() throws SQLException{
        List<carreras> carreras=new ArrayList<carreras>();
        Connection connectionDB = connectDB();
        
        if(connectionDB!=null){
            Statement st = connectionDB.createStatement();
            ResultSet rs = st.executeQuery(queryInit);
            while(rs.next()){
                if(rs.getInt(1)!=0){
                    carreras carrera = new carreras();
                    carrera.setCod(rs.getInt(1));
                    carrera.setVacant(rs.getInt(2));
                    for(int i=3;i<8;i++){
                        carrera.ponderacion.add(rs.getDouble(i));
                    }
                    carrera.setFirst(rs.getDouble(8));
                    carreras.add(carrera);
                }
                
            }
        }
        return carreras;
    }
    
    /**
     * Función para setear los datos del objeto File
     * @param bos Array de bytes con el binario del archvio
     * @param fileName Nombre del archivo
     * @param mimeType Tipo mime del archivo
     * @param carrera código de la carrera seleccionada
     * @return devuelve el objeto con File con los datos aplicados
     */
    private File setData(ByteArrayOutputStream bos, String fileName, String mimeType){
        File file = new File();
        file.setFilename(fileName);
        file.setContent(bos.toByteArray());
        file.setMimetype(mimeType);
        return file;
    }
    /**
     * Función que recibe ruts y sus respectivos puntajes, para ponderarlos según cada carrera y ordenarlos de mayor a menor con algunas restricciones
     * una ves ordenados los guarda en una lista de tipo "ruts"
     * @param carreras Lista con los datos de todas la carreras en la base de datos
     * @param ruts Lista de ruts no poblada, esta se poblará mediante el transcurso de la función
     * @param fullData Datos separados por ";" y por línea, cada línea es un rut con sus puntajes
     */
    private void ordenarDesdeExtraccion(List<carreras> carreras,List<ruts> ruts,String[] fullData){
        
        for(int i=0;i<fullData.length;i++){
            String[] tmpData = fullData[i].split(";");
            if(tmpData.length>1){
                
                List<Double> tmpPuntajes = fromStringToDouble(tmpData);
                //Solo avanzará si el promedio simple entre los puntajes de matematicas y lenguaje es mayor o igual que 450
                if((tmpPuntajes.get(2)+tmpPuntajes.get(3))/2>=450.0){

                    ruts tmpRut = new ruts();
                    tmpRut.setRut(tmpData[0]);
                    //Para cada carrera obtiene la ponderación del rut
                    for(int j=0;j<carreras.size();j++){
                        Double tmpPonderado = (tmpPuntajes.get(0)* carreras.get(j).ponderacion.get(0))+
                                            (tmpPuntajes.get(1)* carreras.get(j).ponderacion.get(1))+
                                            (tmpPuntajes.get(2)* carreras.get(j).ponderacion.get(2))+
                                            (tmpPuntajes.get(3)* carreras.get(j).ponderacion.get(3));
                        if(tmpPuntajes.get(4)>tmpPuntajes.get(5)){
                            tmpPonderado += tmpPuntajes.get(4)* carreras.get(j).ponderacion.get(4);
                        }
                        else{
                            tmpPonderado += tmpPuntajes.get(5)* carreras.get(j).ponderacion.get(4);
                        }
                        int tmpRutSize = tmpRut.carreraPondera.size();
                        //Los guarda ordenadamente según algunas condiciones 
                        //Primera condición, si existe alguna ponderación guardada antes entra a una segunda condición de orden
                        if(tmpRutSize>0){
                            //Verifica rapidamente si la ponderación actual es mayor a la menor ponderación ingresada
                            //Si se cumple la condición, se coloca donde indique el indexador
                            if(tmpRut.carreraPondera.get(tmpRutSize-1).getValue()<tmpPonderado){
                                
                                tmpRut.carreraPondera.add(indexador(tmpRut, tmpPonderado, carreras.get(j), carreras),new Pair<>(carreras.get(j).getCod(),tmpPonderado));
                            }
                            //Sino, verifica si esta es mayor o igual
                            else{
                                //Si es igual, se ejecuta el indexador
                                if(tmpRut.carreraPondera.get(tmpRutSize-1).getValue()==tmpPonderado){
                                    int index = indexador(tmpRut, tmpPonderado, carreras.get(j), carreras);
                                    //Si el indexador entrega -1 significa que no cumple los requerimientos para estar en posiciones más altas y se agrega al final de la lista
                                    if(index == -1){
                                        tmpRut.carreraPondera.add(new Pair<>(carreras.get(j).getCod(),tmpPonderado));
                                    }
                                    //En caso contrario se agrega donde el index lo indique
                                    else{
                                        tmpRut.carreraPondera.add(index,new Pair<>(carreras.get(j).getCod(),tmpPonderado));
                                    }
                                }
                                //Sino, significa es que es menor y simplemente se agrega al final de la lista
                                else{
                                    tmpRut.carreraPondera.add(new Pair<>(carreras.get(j).getCod(),tmpPonderado));
                                }
                                
                            }
                        }
                        //Si no existe ninguna ponderación guarda, la guarda simplemente
                        else{
                            tmpRut.carreraPondera.add(new Pair<>(carreras.get(j).getCod(),tmpPonderado));
                        }
                    }
                    //Finalmente se agrega el rut a la lista de todos los ruts
                    ruts.add(tmpRut);
                }
            }
        }
    }
    /**
     * Esta función, Principalmente acomoda cada rut en la carrera que mejor pondere
     * @param carreras Lista con todas las carreras en la base de datos y algunos datos
     * @param ruts Lista con todos los ruts ingresados y por analizar
     */
    
    private void acomodador(List<carreras> carreras ,List<ruts> ruts){
        int carrerasSize = carreras.size();
        //Condición simple, sólo si no quedan más ruts por analizar se termina la función
        while(!ruts.isEmpty()){
            int rutsSize = ruts.size();
            //Empieza a recorrer cada rut
            for(int i=rutsSize-1;i>=0;i--){
                for(int j =0; j< carrerasSize;j++){
                    //Si la ponderación más alta es de la carrera indexada proceda.
                    if(carreras.get(j).getCod()==ruts.get(i).carreraPondera.get(0).getFirst()){
                        int personasEnCarreraSize = carreras.get(j).personas.size();
                        //Explicación de las condiciones: Se colocaron tantas restricciones para optimizar los tiempos en las fases tempranas de ejecución
                        //Las condiciones que se espera que se cumplan en fase media-fin de la función son las marcadas por "(*)"

                        //(*) Verifica que existan ruts ingresados en la lista, proceda.
                        if(personasEnCarreraSize > 0){
                            //(*) Si la cantidad de ruts es igual a la cantidad de cupos máximos permitidos en la carrera, proceda.
                            if(carreras.get(j).getVacant()==personasEnCarreraSize){
                                //(*) Si la ponderación actual es mayor a la ponderación del último rut ingresado entonces se ingrea el rut donde
                                //el indexador simple indique, se remueve el rut actual de la lista de ruts, se quita la primera ponderación
                                //de la lista de ponderaciones del último rut, si ya no le quedan ponderaciones no se agrega a la lista de ruts,
                                //en caso contrario, se agrega a la lista de ruts, y finalmente se remueve el último rut de la carrera
                                if(ruts.get(i).carreraPondera.get(0).getValue() > carreras.get(j).personas.get(personasEnCarreraSize-1).carreraPondera.get(0).getSecond()){
                                    carreras.get(j).personas.add(indexadorSimple(ruts.get(i),carreras.get(j)), ruts.get(i));
                                    ruts.remove(i);
                                    carreras.get(j).personas.get(personasEnCarreraSize).carreraPondera.remove(0);
                                    //Verifica si aún contiene ponderaciones de otras carreras.
                                    if(!carreras.get(j).personas.get(personasEnCarreraSize).carreraPondera.isEmpty()){
                                        ruts.add(carreras.get(j).personas.get(personasEnCarreraSize));
                                    }
                                    carreras.get(j).personas.remove(personasEnCarreraSize);
                                }
                                //(*) Si la ponderación actual es menor a la ponderación del último rut ingresado, significa que no merece estar en esa carrera
                                //por lo tanto simplemente se remueve la ponderación más alta de la lista de ponderaciones de ese rut, si ya no le quedan
                                //ponderaciones, simplemente se remueve el rut de la lista de ruts.
                                else{
                                    ruts.get(i).carreraPondera.remove(0);
                                    if(ruts.get(i).carreraPondera.isEmpty()){
                                        ruts.remove(i);
                                    }
                                }
                            }
                            // Si la cantidad de ruts es menor o mayor(está programado para que no sea mayor nunca) a la cantidad de cupos máximos permitidos en la carrera, proceda.
                            else{
                                // Si la ponderación del rut actual es menor o igual a la del último ingresado a la carrera, se inyecta en la última posición de la lista y se remueve de la lista de ruts.
                                if(ruts.get(i).carreraPondera.get(0).getValue() <= carreras.get(j).personas.get(personasEnCarreraSize-1).carreraPondera.get(0).getValue()){
                                    carreras.get(j).personas.add(ruts.get(i));
                                    ruts.remove(i);
                                }
                                // Si la ponderación del rut actual es mayor a la del último ingresado a la carrera, se inyecta en la posición de la lista donde indique el indexador simple y se remueve de la lista de ruts.
                                else{
                                    carreras.get(j).personas.add(indexadorSimple(ruts.get(i), carreras.get(j)),ruts.get(i));
                                    ruts.remove(i);
                                }
                            }
                        }
                        //Si no existe nadie registrado en la carrera, simplemente se agrega y se remueve el rut de la lista de ruts
                        //Para que este no sea consultado nuevamente
                        else{
                            carreras.get(j).personas.add(ruts.get(i));
                            ruts.remove(i);
                        }
                        //(*) No tiene sentido seguir consultando por otra carrera, se pasa al siguiente rut.
                        break;
                    }
                }
            }
        }
    }
    /**
     * Esta función obtiene el indice donde se debe ingresar la ponderación en la lista de ponderaciones de un rut
     * @param rut Objeto "ruts", donde se encuentra principalmente la lista de ponderaciones del rut
     * @param ponderacion Es la ponderación a comparar dentro de la lista de ponderaciones
     * @param carrera Objeto "carreras", donde se encuentra la información de la carrera a la que pondera "@param ponderación"
     * @param carreras Lista de carreras, donde se encuentra la información de todas las carreras
     * @return Devuelve el índice donde se debe ingresar la ponderación dentro de la lista de ponderaciones de "@param rut"
     */
    private int indexador(ruts rut,Double ponderacion, carreras carrera, List<carreras> carreras){
        for(int i=0;i<rut.carreraPondera.size();i++){
            //Verifica si la ponderación es mayor a una cierta ponderación en la posición "i"
            if(ponderacion>rut.carreraPondera.get(i).getValue()){
                return i;
            }
            //En caso de se iguales
            else if(ponderacion==rut.carreraPondera.get(i).getValue()){
                for(int j=0;j<carreras.size();j++){
                    
                    if(carreras.get(j).getCod()==rut.carreraPondera.get(i).getKey()){
                        //Verifica que el puntaje del primero sea igual al de la otra carrera
                        if(carrera.getFirst() == carreras.get(j).getFirst()){
                            //Verifica si la cantidad de de cupos máximos de la acarrera actual es menor o igual que la otra carrera
                            if(carrera.getVacant()<=carreras.get(j).getVacant()){
                                return i;
                            }
                            //En caso de no serlo, se analiza el siguiente elemento
                            else{
                                break;
                            }
                        }
                        //Verifica si el puntaje del primero es mayor al de la otra carrera
                        else if(carrera.getFirst() > carreras.get(j).getFirst()){
                            return i;
                        }
                        //Si es menor, analiza el siguiente elemento
                        else{
                            break;
                        }
                    }
                    
                }
            }
        }
        //Se asume que nunca llegará a este return, debido a las condiciones del código que ejecuta esta función
        return -1;
    }
    /**
     * Función que entrega el índice donde debe ir el rut dentro de la carrera.
     * Nota: Se considera que tiene mayor prioridad alquien que entró antes a la carrera, es por eso que solo verifica
     * si es estrictamente mayor que...
     * @param rut Objeto "ruts" con los datos de los ruts
     * @param carrera Objeto "carreras" con los datos de una carrera
     * @return Devuelve el índice donde debe ingresarse el rut dentro de la carrera
     */
    private int indexadorSimple(ruts rut,carreras carrera){
        for(int i=0;i<carrera.personas.size();i++){
            if(rut.carreraPondera.get(0).getValue() > carrera.personas.get(i).carreraPondera.get(0).getValue()){
                return i;
            }
        }
        //Se asume que nunca llegará a este return, debido a las condiciones del código que ejecuta esta función
        return -1;
    }
    /**
     * Simplemente recibe un conjunto de datos en forma de Array de strings y los transforma a una lista de Doubles
     * @param data Son los datos ingresados en un array de string
     * @return Devuelve una lista de los puntajes en formato "double"
     */
    private List<Double> fromStringToDouble(String[] data){
        List<Double> tmpData = new ArrayList<Double>();
        for(int i=1;i<7;i++){
            tmpData.add(Double.parseDouble(data[i]));

        }
        return tmpData;
    }

    
}
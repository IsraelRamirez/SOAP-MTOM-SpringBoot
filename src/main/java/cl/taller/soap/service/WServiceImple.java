package cl.taller.soap.service;

import static cl.taller.soap.dbconnetion.connetiondb.connectDB;
import cl.taller.soap.localhost.soap.File;
import cl.taller.soap.localhost.soap.GetDataRequest;
import cl.taller.soap.localhost.soap.GetDataResponse;
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
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
public class WServiceImple{
    
    private static final String token = "fk2x6rpw6fDCkXqDlqeeR22u8jpN6qGa";
    private static final String error = "Token ingresado no valido.";
    private static final String query = "SELECT nem, ranking,lenguaje,matematica,histociencia FROM ponderado WHERE codCarrera =";
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
        
        if(request.getToken().equals(token)){
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
        String mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        String fileName= "puntajes.xlsx";
        
        int codCarrera = Integer.parseInt(request.getCodCarrera());
        Workbook wb = new SXSSFWorkbook();
        Sheet sheet = wb.createSheet();
        
        String fullData = new String(Base64.getDecoder().decode(request.getContent()),"UTF-8");
        String[] datas = fullData.split("\n");
        
        for(int i = 0; i< datas.length;i++){
            List<String> full = operador(datas[i],codCarrera);
            Row row = sheet.createRow(i);
            for(int j=0; j <2;j++){
                Cell cell = row.createCell(j);
                cell.setCellValue(full.get(j));
            }
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        wb.write(bos);

        getData.setFile(setData(bos,fileName,mimeType,codCarrera));
        return getData;
    }
    /**
     * Función para setear los datos del objeto File
     * @param bos Array de bytes con el binario del archvio
     * @param fileName Nombre del archivo
     * @param mimeType Tipo mime del archivo
     * @param carrera código de la carrera seleccionada
     * @return devuelve el objeto con File con los datos aplicados
     */
    private File setData(ByteArrayOutputStream bos, String fileName, String mimeType, int carrera){
        File file = new File();
        file.setFilename(String.valueOf(carrera)+"_"+fileName);
        file.setContent(bos.toByteArray());
        file.setMimetype(mimeType);
        return file;
    }
    /**
     * Función para operar sobre los datos de un alumno 
     * @param data datos de un alumno
     * @param carrera código de la carrera perteneciente 
     * @return La información de un alumno, según la carrera elegida
     * @throws java.io.FileNotFoundException Excepción respectiva a que no se encuentra un archivo
     * @throws java.io.IOException Excepción respectiva a un error de input/output
     * @throws java.sql.SQLException Excepción respectiva a un error en la conexión a la base de datos o en la sentencia SQL
     */
    private List<String> operador(String data, int carrera) throws IOException, FileNotFoundException, SQLException{
        String[] datos = data.split(";");
        List<String> info = new ArrayList<>();
        if(datos.length>1){
            info.add(datos[0]);
            info.add(String.valueOf(ponderador(datos, carrera)));
        }
        return info;
    }
    /**
     * Función que pondera los puntajes psu de un alumno
     * @param data Array de strings con los puntajes de un alumno
     * @param carrera Codigo de la carrera perteneciente
     * @return La ponderación
     * @throws java.io.FileNotFoundException Excepción respectiva a que no se encuentra un archivo
     * @throws java.io.IOException Excepción respectiva a un error de input/output
     * @throws java.sql.SQLException Excepción respectiva a un error en la conexión a la base de datos o en la sentencia SQL
     */
    private double ponderador(String[] data, int carrera) throws FileNotFoundException, IOException, SQLException{
        Connection connectionDB = connectDB();
        double ponderado = 0.0;
        if(connectionDB!=null){
            Statement st = connectionDB.createStatement();
            ResultSet rs = st.executeQuery(query+carrera);
            rs.next();
            if(rs.isLast()){
                ponderado = (Double.parseDouble(data[1]) * rs.getDouble(1) )+
                            (Double.parseDouble(data[2]) * rs.getDouble(2) )+
                            (Double.parseDouble(data[3]) * rs.getDouble(3) )+
                            (Double.parseDouble(data[4]) * rs.getDouble(4) );
                if(Double.parseDouble(data[5])>Double.parseDouble(data[6])){
                    ponderado += (Double.parseDouble(data[5]) * rs.getDouble(5));
                }
                else{
                    ponderado += (Double.parseDouble(data[6]) * rs.getDouble(5));
                }
            }
        }
        return ponderado;
    }
}
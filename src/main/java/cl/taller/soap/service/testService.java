package cl.taller.soap.service;


import cl.taller.soap.localhost.soap.GetDataRequest;
import cl.taller.soap.localhost.soap.GetDataResponse;
import cl.taller.soap.localhost.soap.Info;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import static java.lang.ProcessBuilder.Redirect.Type.APPEND;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardOpenOption.CREATE;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class testService {
    
    public GetDataResponse checkInfo(GetDataRequest request){
        GetDataResponse getData = new GetDataResponse();
        Info info = new Info();
        //Path path = Paths.get("./test.xlsx");
        
        if(request.getToken().equals("permitir")){
            
            
            String fullData = request.getData();
            String[] datas = fullData.split("\n");
            
            fullData="";
            for(int i = 0; i< datas.length;i++){
                fullData+=formato(datas[i]);
            }
            //try{
                byte data[]= fullData.getBytes();
                //OutputStream out;
                //out = new BufferedOutputStream(Files.newOutputStream(path, CREATE));
                //out.write(data,0,data.length);
                info.setContent(data);
            //}catch(IOException e){
            //    System.err.println(e);
            //}
            
            info.setData(fullData);
            getData.setInfo(info);
            return getData;
        }
        info.setData("El token ingresado es incorrecto...");
        getData.setInfo(info);
        return getData;
    }
    public String formato(String data){
        String[] datos = data.split(";");
        String info="";
        if(datos.length>1){
            info=datos[0]+";"+Double.toString(promediador(datos))+"\n";
        }
        return info;
    }
    public double promediador(String[] data){
        double promedio=0;
        for(int i = 1;i<data.length;i++){
            promedio += Double.parseDouble(data[i]);
        }
        return (promedio/(data.length-1));
    }
}

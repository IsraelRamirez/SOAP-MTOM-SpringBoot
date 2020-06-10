package cl.taller.soap.endpoint;

import cl.taller.soap.models.GetDataRequest;
import cl.taller.soap.models.GetDataResponse;
import cl.taller.soap.service.WServiceImple;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class soapEndPoint {
    private static final String NAMESPACE="http://cl/taller/soap/models";
    @Autowired
    private WServiceImple service;
    @PayloadRoot(namespace=NAMESPACE,localPart="getDataRequest")
    @ResponsePayload
    public GetDataResponse endPoint(@RequestPayload GetDataRequest request) throws UnsupportedEncodingException, IOException, FileNotFoundException, SQLException{
        return service.auth(request);
    }
}

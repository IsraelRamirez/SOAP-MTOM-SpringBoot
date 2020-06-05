package cl.taller.soap.endpoint;

import cl.taller.soap.localhost.soap.GetDataRequest;
import cl.taller.soap.localhost.soap.GetDataResponse;
import cl.taller.soap.localhost.soap.Info;
import cl.taller.soap.service.testService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class testEndpoint {
    
    
    private static final String NAMESPACE="http://localhost/soap";
    @Autowired
    private testService service;
    @PayloadRoot(namespace=NAMESPACE,localPart="getDataRequest")
    @ResponsePayload
    public GetDataResponse test(@RequestPayload GetDataRequest request){
        return service.checkInfo(request);
    }
}

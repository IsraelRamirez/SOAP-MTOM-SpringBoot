package cl.taller.soap.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurationSupport;
import org.springframework.ws.server.endpoint.adapter.DefaultMethodEndpointAdapter;
import org.springframework.ws.server.endpoint.adapter.method.MarshallingPayloadMethodProcessor;
import org.springframework.ws.server.endpoint.adapter.method.MethodArgumentResolver;
import org.springframework.ws.server.endpoint.adapter.method.MethodReturnValueHandler;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

@Configuration
@EnableWs
public class SoapWSConfig extends WsConfigurationSupport{
    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(ApplicationContext context){
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(context);
        servlet.setTransformWsdlLocations(true);
        return new ServletRegistrationBean<MessageDispatcherServlet>(servlet,"/ws/*");
    }
    /*
    @Bean
    public Jaxb2Marshaller marshaller(){
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("cl.taller.soap.localhost.soap"); //Cambiar por ruta del archivo.
        marshaller.setMtomEnabled(true);
        return marshaller;
    }
    @Bean
    @Override
    public DefaultMethodEndpointAdapter defaultMethodEndpointAdapter(){
        List<MethodArgumentResolver> argumentResolvers = new ArrayList<>();
        argumentResolvers.add(methodProcessor());
        List<MethodReturnValueHandler> returnValueHandlers = new ArrayList<>();
        returnValueHandlers.add(methodProcessor());
        DefaultMethodEndpointAdapter adapter = new DefaultMethodEndpointAdapter();
        adapter.setMethodArgumentResolvers(argumentResolvers);
        adapter.setCustomMethodReturnValueHandlers(returnValueHandlers);
        return adapter;
    } 
    @Bean
    public MarshallingPayloadMethodProcessor methodProcessor(){
        return new MarshallingPayloadMethodProcessor(marshaller());
    }*/
    @Bean(name="soap")
    public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema schema){
        DefaultWsdl11Definition defaultWsdl11Definition=new DefaultWsdl11Definition();
        defaultWsdl11Definition.setPortTypeName("test");
        defaultWsdl11Definition.setLocationUri("/ws");
        defaultWsdl11Definition.setTargetNamespace("http://localhost/soap");
        defaultWsdl11Definition.setSchema(schema);
        return defaultWsdl11Definition;
    }
    @Bean
    public XsdSchema schema(){
        return new SimpleXsdSchema(new ClassPathResource("testsoap.xsd"));
    }
}

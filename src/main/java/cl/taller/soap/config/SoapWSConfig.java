package cl.taller.soap.config;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurationSupport;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

@Configuration
@EnableWs
public class SoapWSConfig extends WsConfigurationSupport{
    /**
     * Función despachadora de mensajes de servlet
     * @param context Recibe el contexto de la aplicación
     * @return Devuelve un mensaje necesario dado por el contexto
     */
    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(ApplicationContext context){
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(context);
        servlet.setTransformWsdlLocations(true);
        return new ServletRegistrationBean<MessageDispatcherServlet>(servlet,"/ws/*");
    }
    /**
     * Función que genera la definición del Wsdl según el esquema ingresado
     * @param schema Es el esquema declarado de como se recibe y se envia la información
     * @return Devuelve la definición wsdl
     */
    @Bean(name="soap")
    public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema schema){
        DefaultWsdl11Definition defaultWsdl11Definition=new DefaultWsdl11Definition();
        defaultWsdl11Definition.setPortTypeName("endpoint");
        defaultWsdl11Definition.setLocationUri("/ws");
        defaultWsdl11Definition.setTargetNamespace("http://localhost/soap");
        defaultWsdl11Definition.setSchema(schema);
        return defaultWsdl11Definition;
    }
    /**
     * Función que devuelve el esquema xml creado
     * @return Devuelve el esquema creado "soapSchema.xsd"
     */
    @Bean
    public XsdSchema schema(){
        return new SimpleXsdSchema(new ClassPathResource("soapSchema.xsd"));
    }
}

# TestSOAP-MTOM-Attachment-in-SpringBoot
In this repository an attempt will be made to create a SOAP Web Service with MTOM in the request and in the response using "spring boot". This repository is based on a problem raised by a workshop in the "Parallel and Distributed Computing" class.

WSaccess = "http://localhost:8080/ws/soap.wsdl"

XML Request struct

<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:soap="http://localhost/soap">
   <soapenv:Header/>
   <soapenv:Body>
      <soap:getDataRequest>
         <soap:token>"Token(default=permitir)"</soap:token>
         <soap:data>"String data"</soap:data>
         <soap:content>"String Base64 encoded"</soap:content>
      </soap:getDataRequest>
   </soapenv:Body>
</soapenv:Envelope>

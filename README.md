# SOAP-MTOM-SpringBoot

Servicio web SOAP

Recibe el contenido de un archivo ".csv", encodeado en base64 con ruts y puntajes psus (NEM, RANKING, PSULENGUAJE, PSUMATEMATICA, PSUHISTORIA/CIENCIAS), devolviendo un archivo .xlsx encodeado en base64 con los puntajes ponderados segun un codigo de carrera ingresado en la base de datos en access.

WSaccess = "http://localhost:8080/ws/soap.wsdl".

DefaultTokenAccess = "fk2x6rpw6fDCkXqDlqeeR22u8jpN6qGa".

XML Request struct

<<soap:getDataRequest>>

   <<soap:token>>?</soap:token>

   <<soap:codCarrera>>?</soap:codCarrera>

   <<soap:filename>>?</soap:filename>

   <<soap:mimetype>>?</soap:mimetype>

   <<soap:content>>?Base64string</soap:content>

</soap:getDataRequest>

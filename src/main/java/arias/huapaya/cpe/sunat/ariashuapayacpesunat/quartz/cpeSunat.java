package arias.huapaya.cpe.sunat.ariashuapayacpesunat.quartz;

import java.io.IOException;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Component
public class cpeSunat {

    private static final Logger logger = LoggerFactory.getLogger(cpeSunat.class);

    // !Cada 8 segundos
    @Scheduled(cron = "0/8 0/1 * * * ?")
    private void taskCpeSunat() {
        // !Variables para generar el token
        String endPointToken = "https://api-seguridad.sunat.gob.pe/v1/clientesextranet/client_id/oauth2/token/";
        String grant_type = "client_credentials";
        String scope = "https://api.sunat.gob.pe/v1/contribuyente/contribuyentes";
        String client_id = "";
        String client_secret = "";
        String token = ""; // !Token de acceso

        // !Variables para consultar documento electrónico
        String endPointSunat = "https://api.sunat.gob.pe/v1/contribuyente/contribuyentes/ruc/validarcomprobante";
        String ruc = "12345678901"; // !Ruc de la empresa
        String codigoComprobante = "03"; // !Codigo del documento (Boleta / Factura)
        String serie = "B001"; // !Serie del documento
        String numero = "1"; // !Correlativo del documento
        String emision = "11/07/2023"; // !Fecha de emisión del documento
        String monto = "100.00"; // !Monto del documento
        String resultadoCPE = ""; // !Resultado de la consulta

        try {
            while (token.equals("")) {
                token = this.generarToken(grant_type, scope, client_id, client_secret, endPointToken);
            }
            if (!token.equals("")) {
                resultadoCPE = this.consultarCPE(endPointSunat, ruc, codigoComprobante, serie, numero, emision,
                        monto, token);
                if (!resultadoCPE.equals("")) {
                    JSONObject jsonResultadoCPE = new JSONObject(resultadoCPE);
                    if (jsonResultadoCPE.getJSONObject("data").length() == 0) {
                        logger.info("CPE: Problemas con SUNAT");
                    } else {
                        String estadoComprobante = "";
                        switch (jsonResultadoCPE.getJSONObject("data").getString("estadoCp").toString()) {
                            case "0":
                                estadoComprobante = "COMPROBANTE NO INFORMADO";
                                break;
                            case "1":
                                estadoComprobante = "COMPROBANTE ACEPTADO";
                                break;
                            case "2":
                                estadoComprobante = "COMUNICADO EN UNA BAJA";
                                break;
                            case "3":
                                estadoComprobante = "CON AUTORIZACION DE IMPRENTA";
                                break;
                            case "4":
                                estadoComprobante = "NO AUTORIZADO POR IMPRENTA";
                                break;
                        }
                        System.out.println("Estado del documento " + serie + "-" + numero + " : " + estadoComprobante);
                    }
                } else {
                    logger.error("Problemas: Consultar documento SUNAT");
                }
            } else {
                logger.error("Error: Generar Token");
            }
        } catch (Exception ex) {
            logger.error("Error: " + ex.getMessage());
        }
    }

    private String generarToken(String grant_type, String scope, String client_id, String client_secret,
            String endPointToken) {
        String token = "";
        try {
            OkHttpClient okHttpClient = new OkHttpClient();
            RequestBody requestBody = new FormBody.Builder()
                    .add("grant_type", grant_type)
                    .add("scope", scope)
                    .add("client_id", client_id)
                    .add("client_secret", client_secret)
                    .build();
            Request request = new Request.Builder()
                    .url(endPointToken.replace("client_id", client_id))
                    .post(requestBody)
                    .addHeader("accept", "application/json")
                    .addHeader("content-type", "application/json")
                    .build();
            Response response = okHttpClient.newCall(request).execute();
            if (response.code() == 200) {
                Object obj = response.body().string();
                String json = obj.toString();
                JSONObject jsonObject = new JSONObject(json);
                token = jsonObject.getString("access_token");
            }
        } catch (IOException e) {
            logger.error("Error generar Token: " + e.getMessage());
        }
        return token;
    }

    private String consultarCPE(String endPointSunat, String ruc, String codigoComprobante, String serie, String numero,
            String emision, String monto, String token) {
        OkHttpClient okHttpClient = new OkHttpClient();
        String json = "";
        try {
            MediaType mediaType = MediaType.parse("application/json");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("numRuc", ruc);
            jsonObject.put("codComp", codigoComprobante);
            jsonObject.put("numeroSerie", serie);
            jsonObject.put("numero", numero);
            jsonObject.put("fechaEmision", emision);
            jsonObject.put("monto", monto);
            RequestBody requestBody = RequestBody.create(jsonObject.toString(), mediaType);
            Request request = new Request.Builder()
                    .url(endPointSunat.replace("ruc", ruc))
                    .post(requestBody)
                    .addHeader("Authorization", ("Bearer " + token))
                    .addHeader("content-type", "application/json")
                    .build();
            Response response = okHttpClient.newCall(request).execute();
            if (response.code() == 200) {
                Object obj = response.body().string();
                json = obj.toString();
            }
        } catch (IOException e) {
            logger.error("Error consultar comprobante: " + e.getMessage());
        }
        return json.toString();
    }

}

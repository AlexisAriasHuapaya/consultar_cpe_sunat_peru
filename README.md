# Consulta CPE SUNAT Perú
- Java 
- Spring Boot 
- Quartz

## Requerimientos
- JDK 17

## Uso del aplicativo
Es necesario obtener accesos de `client_id` y `client_secret` desde la documentación de SUNAT.
[Guía Oficial](https://orientacion.sunat.gob.pe/images/imagenes/contenido/comprobantes/Manual-de-Consulta-Integrada-de-Comprobante-de-Pago-por-ServicioWEB.pdf)

## EndPoint
1. Generar Token `https://api-seguridad.sunat.gob.pe/v1/clientesextranet/client_id/oauth2/token/`
    - client_id - Generar: [Guía Oficial](https://orientacion.sunat.gob.pe/images/imagenes/contenido/comprobantes/Manual-de-Consulta-Integrada-de-Comprobante-de-Pago-por-ServicioWEB.pdf)
2. Consultar Documento `https://api.sunat.gob.pe/v1/contribuyente/contribuyentes/ruc/validarcomprobante`
    - ruc (RUC Empresa)

## Código de comprobantes

|Código | Descripción                |
|-------|----------------------------|
|01     | Factura                    |
|03     | Boleta de venta            |

## Estado de comprobantes

Código | Descripción                           |
-------|---------------------------------------|
0 | NO EXISTE (Comprobante no informado) |
1 | ACEPTADO (Comprobante aceptado) |
2 | ANULADO (Comunicado en una baja) |
3 | AUTORIZADO (con autorización de imprenta) |
4 | NO AUTORIZADO (no autorizado por imprenta) |



# Estructura en Cisco IOS

```mermaid

flowchart TD
    %% Título General
    IOS["CISCO IOS<br/>Configuración de Redes"]

    %% 1. Métodos de Acceso
    IOS --> ACC["1. MÉTODOS DE ACCESO<br/>Conexión inicial y remota"]
    ACC --> ACC_EX["Acceso por:<br/>• Consola (Config. Inicial)<br/>• Auxiliar (AUX)<br/>• Telnet / SSH (VTY)"]

    %% 2. Estructura del Sistema
    IOS --> SO["2. COMPONENTES DEL SO<br/>Capas del sistema operativo"]
    SO --> SO_CAPAS["Estructura:<br/>• Hardware ➔ Kernel ➔ Shell<br/>• Interfaz: CLI (Teclado) o GUI"]

    %% 3. Jerarquía de Modos
    IOS --> MODOS["3. NAVEGACIÓN EN CLI<br/>Jerarquía de Modos de Comandos"]

    MODOS --> EXEC_U["Mode EXEC Usuario<br/>Switch>"]
    EXEC_U -->|"comando: enable"| EXEC_P["Mode EXEC Privilegiado<br/>Switch#"]
    EXEC_P -->|"comando: configure terminal"| CONF_G["Configuración Global<br/>Switch(config)#"]

    CONF_G -->|"comando: line con 0 / line vty"| CONF_L["Submodo Línea<br/>Switch(config-line)#"]
    CONF_G -->|"comando: interface vlan 1"| CONF_IF["Submodo Interfaz<br/>Switch(config-if)#"]

    %% Retornos
    CONF_L -->|"comando: exit"| CONF_G
    CONF_IF -->|"comando: exit"| CONF_G
    CONF_G -->|"comando: exit"| EXEC_P
    EXEC_P -->|"comando: disable / exit"| EXEC_U
    CONF_IF -->|"comando: end"| EXEC_P

    %% 4. Archivos de Configuración y Memoria
    IOS --> MEM["4. GESTIÓN DE MEMORIA<br/>Archivos de Configuración"]
    MEM --> RAM["RAM (Volátil)<br/>running-config<br/>Configuración activa"]
    MEM --> NVRAM["NVRAM (No Volátil)<br/>startup-config<br/>Configuración de inicio"]

    RAM -->|"copy running-config startup-config"| NVRAM

    %% Estilos visuales optimizados para Obsidian (Coherentes con el diseño solicitado)
    style IOS fill:#1e293b,stroke:#94a3b8,stroke-width:2px,color:#fff
    style ACC fill:#1e3a8a,stroke:#60a5fa,color:#fff
    style ACC_EX fill:#0f172a,stroke:#475569,color:#cbd5e1
    style SO fill:#1d4ed8,stroke:#93c5fd,color:#fff
    style SO_CAPAS fill:#0f172a,stroke:#475569,color:#cbd5e1
    style MODOS fill:#334155,stroke:#94a3b8,color:#fff
    style EXEC_U fill:#2563eb,stroke:#bfdbfe,color:#fff
    style EXEC_P fill:#1e40af,stroke:#93c5fd,color:#fff
    style CONF_G fill:#166534,stroke:#86efac,color:#fff
    style CONF_L fill:#15803d,stroke:#bbf7d0,color:#fff
    style CONF_IF fill:#15803d,stroke:#bbf7d0,color:#fff
    style MEM fill:#4338ca,stroke:#a5b4fc,color:#fff
    style RAM fill:#991b1b,stroke:#fca5a5,color:#fff
    style NVRAM fill:#3730a3,stroke:#a5b4fc,color:#fff
    
```


#### Comandos de Configuración Inicial y Seguridad

|**Área**|**Comando IOS**|**Función PDF**|
|---|---|---|
|**Nombre de Host**|`hostname [nombre]`|Asigna un identificador al dispositivo (debe iniciar con letra, sin espacios, <64 caracteres).|
|**Cifrado de Claves**|`service password-encryption`|Cifra todas las contraseñas guardadas en texto plano en el archivo de configuración.|
|**Aviso Legal**|`banner motd #[mensaje]#`|Muestra un mensaje de advertencia legal antes del inicio de sesión (evitar palabras de bienvenida).|
#### Gestión de Archivos de Configuración y Memoria

#### Gestión de Archivos de Configuración y Memoria

|**Archivo / Memoria**|**Tipo de Memoria**|**Volátil**|**Descripción PDF**|**Comando de Comprobación / Copia PDF**|
|---|---|---|---|---|
|**Running-config**|RAM|**Sí**|Configuración activa. Se modifica inmediatamente en operación.|`show running-config`|
|**Startup-config**|NVRAM|**No**|Configuración de inicio. Se carga al reiniciar o encender el dispositivo.|`copy running-config startup-config`|

#### Esquema de Direccionamiento IP y Verificación de Red

| **Elemento / Protocolo** | **Parámetros / Comandos**                                     | **Descripción / Uso PDF**                                                          |
| ------------------------ | ------------------------------------------------------------- | ---------------------------------------------------------------------------------- |
| **Estructura IPv4**      | Formato decimal punteado (ej. `192.168.1.10 / 255.255.255.0`) | Dirección única de 32 bits requerida para comunicación en red.                     |
| **DHCP**                 | Automático                                                    | Asigna IP, máscara y puerta de enlace dinámicamente a los hosts.                   |
| **Interfaz SVI**         | `interface vlan 1` `ip address [IP] [Máscara]`                | Interfaz virtual de switch empleada para la administración remota del equipo.      |
| **Verificación IP**      | `show ip interface brief`                                     | Muestra el estado operativo (`up/up`) y direcciones IP asignadas a las interfaces. |
| **Prueba Conectividad**  | `ping [IP / Dominio]`                                         | Envía paquetes ICMP para verificar la conectividad de extremo a extremo.           |



# Protocolo Ethernet
```mermaid
flowchart TD
    %% Ethernet Subcapas y Trama
    ETH["ETHERNET (IEEE 802.3)<br/>Capa 1 y Capa 2"] --> SUB["1. SUBCAPAS Y TRAMA"]
    SUB --> LLC["Subcapa LLC: Comunicación con Capa 3"]
    SUB --> MAC["Subcapa MAC: Encapsulamiento y Control de Acceso"]
    MAC --> TRAMA["Trama (64 a 1518 Bytes)<br/>Preámbulo | MAC Destino | MAC Origen | Tipo | Datos | FCS"]

    %% Direccionamiento MAC
    ETH --> ADDR["2. DIRECCIONES MAC (48 bits / 12 Hex)"]
    ADDR --> UNI["Unidifusión (Unicast): Un destino único"]
    ADDR --> BROAD["Difusión (Broadcast): FF-FF-FF-FF-FF-FF"]
    ADDR --> MULTI["Multidifusión (Multicast): 01-00-5E-xx-xx-xx"]

    %% Switch y Tabla CAM
    ETH --> SW["3. SWITCHES DE RED LAN (Capa 2)"]
    SW --> CAM["Tabla CAM / Direcciones MAC<br/>Aprende MACs de origen"]
    SW --> METODOS["Métodos de Reenvío"]
    METODOS --> SF["Almacenamiento y Reenvío (Store-and-Forward)"]
    METODOS --> CT["Método de Corte (Cut-Through)"]
    CT --> FastForward["Reenvío Rápido (Fast-Forward)"]
    CT --> FragmentFree["Libre de Fragmentos (Fragment-Free)"]

    %% Protocolo ARP
    ETH --> ARP["4. PROTOCOLO ARP"]
    ARP --> ARPFUNC["Mapeo: IP (Capa 3) ➔ MAC (Capa 2)"]
    ARP --> ARPTAB["Tabla ARP<br/>• Windows: arp -a<br/>• Cisco IOS: show ip arp"]
    ARP --> RISK["Riesgos: Saturación por Broadcast / Suplantación ARP"]

    style ETH fill:#1e293b,stroke:#94a3b8,stroke-width:2px,color:#fff
    style SUB fill:#1e3a8a,stroke:#60a5fa,color:#fff
    style ADDR fill:#1d4ed8,stroke:#93c5fd,color:#fff
    style SW fill:#166534,stroke:#86efac,color:#fff
    style ARP fill:#3730a3,stroke:#a5b4fc,color:#fff


```


> [!important] Requisitos de tamaño de trama Ethernet
> - **Límite mínimo:** 64 bytes.
> - **Límite máximo:** 1518 bytes.
> - **Tramas menores a 64 bytes:** se consideran *runt frames* (tramas enanas), generalmente asociadas con colisiones o transmisiones incompletas, y son descartadas.
> - **Tramas mayores a 1518 bytes:** se consideran tramas excesivamente grandes; dependiendo del estándar y configuración de la red, pueden ser descartadas.


#### 1. Tipos de Direccionamiento en la Capa de Enlace (MAC)

| **Tipo de Dirección**         | **Formato / Valor de Destino**                         | **Descripción y Uso Principal**                                                                                |
| ----------------------------- | ------------------------------------------------------ | -------------------------------------------------------------------------------------------------------------- |
| **Unidifusión (Unicast)**     | Dirección MAC física específica de un host.            | Envío de datos desde un único emisor hacia un único receptor. La dirección de origen siempre debe ser unicast. |
| **Difusión (Broadcast)**      | `FF-FF-FF-FF-FF-FF` (48 unos en binario).              | Envío masivo a todos los dispositivos dentro del mismo segmento de red local.                                  |
| **Multidifusión (Multicast)** | Inicia obligatoriamente con `01-00-5E` en hexadecimal. | Envío a un grupo específico de dispositivos suscriptos dentro del segmento.                                    |

#### 2. Métodos de Reenvío de Tramas en Switches Cisco

| **Método de Reenvío**                              | **Funcionamiento Técnico**                                                                    | **Ventajas y Características**                                                   |
| -------------------------------------------------- | --------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------- |
| **Store-and-Forward** _(Almacenamiento y Reenvío)_ | Recibe la trama completa, verifica el campo FCS (verificación de errores) y luego la reenvía. | Garantiza que no se reenvíen tramas corruptas con errores.                       |
| **Cut-Through** _(Fast-Forward)_                   | Lee únicamente la dirección MAC de destino e inicia el reenvío de inmediato.                  | Ofrece el nivel de latencia más bajo en la red.                                  |
| **Cut-Through** _(Fragment-Free)_                  | Lee y almacena los primeros 64 bytes de la trama antes de reenviar.                           | Filtra la mayoría de los errores y colisiones sin sacrificar excesiva velocidad. |
###  Funcionamiento de la Tabla MAC / CAM en Switches

- **Naturaleza del Dispositivo:** El switch es un dispositivo de conmutación que opera en la Capa 2 (Enlace de Datos) del modelo OSI.
    
- **Proceso de Aprendizaje:** Construye dinámicamente la tabla de memoria de contenido direccionable (CAM) inspeccionando la dirección MAC de **origen** de cada trama que ingresa por sus puertos.
    
- **Reenvío y Filtrado de Tramas:**
    
    - **Destino Conocido:** Si la dirección MAC de **destino** está registrada en la tabla CAM, el switch envía la trama únicamente por el puerto asignado (filtrado).
        
    - **Inundación (Flooding):** Si la MAC de destino no se encuentra en la tabla, el switch transmite una unidifusión desconocida a todos los puertos activos con excepción del puerto de entrada.
        

###  Protocolo ARP (Address Resolution Protocol)

- **Función Principal:** Mapea y resuelve direcciones lógicas IPv4 (Capa 3) a direcciones físicas MAC (Capa 2) para permitir la entrega de paquetes dentro de la red local.
    
- **Tipos de Mensajes:**
    
    - **Solicitud ARP:** Se envía mediante difusión (_Broadcast_) a la dirección `FF-FF-FF-FF-FF-FF` cuando un dispositivo conoce la IP de destino pero desconoce su MAC.
        
    - **Respuesta ARP:** El dispositivo receptor que posee la IP consultada responde mediante un mensaje de unidifusión (_Unicast_).
        
- **Comandos de Consulta de Tabla ARP:**
    
    - **En Sistemas Windows:** Se consulta mediante el comando `arp -a`.
        
    - **En Cisco IOS:** Se visualiza mediante el comando `show ip arp`.
        

# La capa de red en las comunicaciones

![](../../img/Pasted%20image%2020260926131733.png)


```mermaid
flowchart TD
    %% Capa de Red y Protocolos
    L3["CAPA DE RED (Capa 3 OSI)<br/>Transporte de extremo a extremo"] --> PROT["1. PROTOCOLOS Y CARACTERÍSTICAS"]
    PROT --> IPV4["IPv4: Sin conexión, Mejor esfuerzo, Independiente de medios"]
    PROT --> IPV6["IPv6: Mayor espacio de direcciones, Encabezado simplificado"]

    %% Decisiones de Reenvío y Routing
    L3 --> ROUTING["2. DECISIONES DE REENVÍO Y ROUTING"]
    ROUTING --> HOST["Reenvío del Host: A sí mismo, Local, Remoto (vía Gateway)"]
    ROUTING --> TABLES["Tablas de Routing"]
    TABLES --> HTAB["Tabla Host: netstat -r"]
    TABLES --> RTAB["Tabla Router: show ip route"]

    %% Anatomía y Componentes del Router
    L3 --> ROUTER["3. ANATOMÍA Y PROCESO DEL ROUTER"]
    ROUTER --> HARDWARE["Hardware: CPU, Interfaces LAN/WAN, Memorias"]
    HARDWARE --> MEM["RAM, ROM, NVRAM (startup-config), Flash (IOS)"]
    ROUTER --> BOOT["Proceso de Arranque: POST ➔ Cargar IOS ➔ Cargar Configuración"]

    %% Configuración de Interfaces
    L3 --> CONF["4. CONFIGURACIÓN Y VERIFICACIÓN"]
    CONF --> CLI["Pasos Iniciales: Nombre, Contraseñas, Banner, SVI"]
    CONF --> INT["Configuración Interfaz: IP, Descripción, no shutdown"]
    CONF --> VERIF["Verificación: show ip interface brief, ping"]

    style L3 fill:#1e293b,stroke:#94a3b8,stroke-width:2px,color:#fff
    style PROT fill:#1e3a8a,stroke:#60a5fa,color:#fff
    style ROUTING fill:#1d4ed8,stroke:#93c5fd,color:#fff
    style ROUTER fill:#166534,stroke:#86efac,color:#fff
    style CONF fill:#3730a3,stroke:#a5b4fc,color:#fff

```
### Tablas Informativas de Consulta Rápida

#### 1. Comparativa de Encabezados y Protocolos de la Capa de Red

| Característica / Campo              | Protocolo IPv4                                 | Protocolo IPv6                                    |
| ----------------------------------- | ---------------------------------------------- | ------------------------------------------------- |
| **Tamaño de Dirección**             | 32 bits.                                       | 128 bits.<br>                                     |
| **Tamaño Base del Encabezado**      | 20 bytes.<br><br>                              | 40 bytes (fijo y simplificado).<br><br>           |
| **Límite de Vida del Paquete**      | Campo _Tiempo de duración_ (TTL).<br><br>      | Campo _Límite de saltos_ (_Hop Limit_).<br><br>   |
| **Prioridad / Calidad de Servicio** | _Servicios diferenciados_ (DS / DSCP).<br><br> | _Clase de tráfico_ (_Traffic Class_).<br><br>     |
| **Identificación de Flujo**         | No disponible en encabezado base.<br>          | Campo _Etiqueta de flujo_ (_Flow Label_).<br><br> |

#### 2. Tipos de Memorias en un Router Cisco y su Función

| Tipo de Memoria | Volatilidad        | Contenido Principal Almacenado                                                            |
| --------------- | ------------------ | ----------------------------------------------------------------------------------------- |
| **RAM (SDRAM)** | Volátil<br><br>    | Configuración en ejecución (`running-config`), tablas de routing y búfer de paquetes.<br> |
| **Flash**       | No volátil<br><br> | Imagen del sistema operativo Cisco IOS y otros archivos del sistema.<br><br>              |
| **NVRAM**       | No volátil<br><br> | Archivo de configuración de inicio (`startup-config`).<br><br>                            |
| **ROM**         | No volátil<br><br> | Instrucciones de arranque, diagnóstico POST y software IOS simplificado.<br><br>          |

### Conceptos 

> [!info] Funciones de la Capa de Red y Características de IP
> 
> - **Propósito:** Direccionamiento de terminales, encapsulamiento, routing y desencapsulamiento entre dispositivos de origen y destino.
>     
>     
>     
> - **Sin Conexión:** No establece un canal previo con el receptor antes de enviar los paquetes de datos.
>     
>     
>     
> - **Mejor Esfuerzo (Best Effort):** El protocolo IP no garantiza la entrega de los paquetes enviando información sin confirmación previa.
>     
>     
>     
> - **Independiente de los Medios:** Los paquetes pueden transitar por diferentes tipos de medios físicos (cobre, fibra óptica, inalámbrico).
>     
>     
>     

> [!example]  Decisiones de Reenvío y Tablas de Routing
> 
> - **Tipos de Destino:** Un host puede enviar paquetes a sí mismo (`127.0.0.1`), a un host local en la misma red o a un host remoto.
>     
>     
>     
> - **Gateway Predeterminado:** Dirección IP local del router que reenvía el tráfico hacia redes externas.
>     
>     
>     
> - **Comandos de Verificación:**
>     
>     - **Windows:** Comando `netstat -r` para desplegar la tabla de rutas del host.
>         
>         
>         
>     - **Cisco IOS:** Comando `show ip route` para visualizar la tabla de enrutamiento del router.
>         
>         
>         
> - **Códigos en Tabla del Router:** `C` indica red directamente conectada y `L` identifica la dirección IP local de la interfaz.
>     
>     
>     


# Asignación de Direcciones IP

![](../../img/Pasted%20image%2020260926141852.png)

```mermaid
flowchart TD
    %% Capa de Direccionamiento IP y Conectividad
    L3["ASIGNACIÓN DE DIRECCIONES IP<br/>Capítulo 7 CCNA Routing & Switching"] --> IPv4_SEC["1. DIRECCIONES DE RED IPv4"]
    L3 --> IPv6_SEC["2. DIRECCIONES DE RED IPv6"]
    L3 --> VERIF_SEC["3. VERIFICACIÓN DE CONECTIVIDAD"]

    %% Estructura e IPv4
    IPv4_SEC --> IPV4_STRUCT["Estructura: 32 bits en 4 octetos<br/>Conversión Binario ➔ Decimal"]
    IPv4_SEC --> IPV4_MASK["Máscara de Subred y Prefijo<br/>Operación AND Lógico"]
    IPv4_SEC --> IPV4_TYPES["Unidifusión, Difusión y Multidifusión"]
    IPv4_SEC --> IPV4_ADDR["Públicas, Privadas /8, /12, /16<br/>Especiales: Loopback, APIPA, TEST-NET"]

    %% Estructura e IPv6
    IPv6_SEC --> IPV6_NEED["Agotamiento IPv4 y Coexistencia<br/>Doble pila, Tunelización, NAT64"]
    IPv6_SEC --> IPV6_STRUCT["128 bits Hexadecimales<br/>Reglas de omisión de ceros"]
    IPv6_SEC --> IPV6_TYPES["Unidifusión Global, Link-Local,<br/>Local Única, Multidifusión"]
    IPv6_SEC --> IPV6_CONF["Configuración Estática y Dinámica<br/>SLAAC, DHCPv6"]

    %% ICMP y Pruebas
    VERIF_SEC --> ICMP["ICMPv4 e ICMPv6<br/>Mensajes RS, RA, NS, NA, DAD"]
    VERIF_SEC --> TOOLS["Pruebas con Ping y Traceroute<br/>Loopback, LAN, Red Remota, TTL"]

    style L3 fill:#1e293b,stroke:#94a3b8,stroke-width:2px,color:#fff
    style IPv4_SEC fill:#1e3a8a,stroke:#60a5fa,color:#fff
    style IPv6_SEC fill:#166534,stroke:#86efac,color:#fff
    style VERIF_SEC fill:#3730a3,stroke:#a5b4fc,color:#fff
```
### Tablas Informativas de Consulta Rápida

#### Comparativa de Direcciones IPv4 e IPv6

| Característica / Propiedad      | Protocolo IPv4                                       | Protocolo IPv6                                                    |
| ------------------------------- | ---------------------------------------------------- | ----------------------------------------------------------------- |
| **Longitud y Formato**          | 32 bits divididos en 4 octetos decimales.<br>        | 128 bits organizados en 8 hextetos hexadecimales.                 |
| **Tipos de Tráfico**            | Unidifusión, Difusión (_Broadcast_) y Multidifusión. | Unidifusión, Multidifusión y Difusión por proximidad (_Anycast_). |
| **Coexistencia / Transición**   | N/A                                                  | Doble pila, Tunelización y Traducción (NAT64).                    |
| **Asignación Automática Local** | APIPA (`169.254.0.0/16`).                            | Direcciones Link-Local creadas autónomamente por el host.         |
| **Mecanismos Dinámicos**        | Servidor DHCP.                                       | SLAAC y DHCPv6.                                                   |

#### Rangos de Direcciones IPv4 Privadas y Especiales

|Tipo / Propósito|Rango CIDR / Prefijo|Rango de Direcciones IP|
|---|---|---|
|**Privada Clase A**|`10.0.0.0/8`|`10.0.0.0` a `10.255.255.255`|
|**Privada Clase B**|`172.16.0.0/12`|`172.16.0.0` a `172.31.255.255`|
|**Privada Clase C**|`192.168.0.0/16`|`192.168.0.0` a `192.168.255.255`|
|**Loopback**|`127.0.0.0/8`|`127.0.0.1` a `127.255.255.254`|
|**APIPA (Link-Local)**|`169.254.0.0/16`|`169.254.0.1` a `169.254.255.254`|
|**TEST-NET**|`192.0.2.0/24`|`192.0.2.0` a `192.0.2.255`|
![](../../img/Pasted%20image%2020260926141934.png)
### Módulos Informativos de Conceptos Clave

> [!info] 1. Estructura y Tipos de Comunicación en IPv4
> 
> - **Estructura:** Formada por 32 bits divididos en porción de red y porción de host según la máscara de subred o prefijo.
>     
>     
>     
> - **Operación AND:** Proceso matemático lógico entre la dirección IP y la máscara para determinar la dirección de red.
>     
> - **Unidifusión (Unicast):** Envío de paquetes individualmente desde un host origen hacia un único host destino.
>     
> - **Difusión (Broadcast):** Envío de información desde un host hacia todos los dispositivos presentes en la red.
>     
> - **Multidifusión (Multicast):** Transmisión dirigida a un grupo específico de hosts suscritos.
>     

> [!example] 2. Direccionamiento IPv6: Estructura y Simplificación
> 
> - **Estructura:** Consta de 128 bits con un prefijo de red (típicamente `/64`) y un ID de interfaz.
>     
> - **Reglas de Compresión:**
>     
>     - **Ceros iniciales:** Se pueden omitir los ceros a la izquierda en cualquier segmento hexadecimal.
>         
>     - **Doble dos puntos (`::`):** Reemplaza una secuencia continua de uno o más segmentos compuestos únicamente de ceros (solo una vez por dirección).
>         
> - **Unidifusión Global (GUA):** Direcciones únicas y enrutables en Internet compuestas por un prefijo de routing global, ID de subred e ID de interfaz.
>     
> - **Link-Local:** Utilizadas para comunicarse con dispositivos en el mismo enlace local sin necesidad de enrutamiento.
>     

> [!abstract] 3. Mensajería ICMP y Protocolos de Descubrimiento
> 
> - **ICMPv4 e ICMPv6:** Informan la entrega de paquetes, confirmación de host, destino inalcanzable y tiempo superado.
>     
> - **Mensajes Router ICMPv6:**
>     
>     - **RS (Router Solicitation):** El dispositivo solicita parámetros de red a los routers presentes.
>         
>     - **RA (Router Advertisement):** El router anuncia prefijos y configuraciones a los hosts.
>         
> - **Mensajes Vecinos ICMPv6:**
>     
>     - **NS / NA (Neighbor Solicitation / Advertisement):** Intercambio entre dispositivos para resolución de direcciones y detección de direcciones duplicadas (DAD).
>         

# División de redes 

```mermaid
flowchart TD
    %% Capa de División de Redes IP en Subredes
    L3["DIVISIÓN DE REDES IP EN SUBREDES<br/>Capítulo 8 CCNA Introducción a Redes v6.0"] --> IPv4_SUB["8.1 DIVISIÓN DE RED IPv4 EN SUBREDES"]
    L3 --> ADDR_PLAN["8.2 ESQUEMAS DE ASIGNACIÓN DE DIRECCIONES"]
    L3 --> IPv6_DESIGN["8.3 CONSIDERACIONES DE DISEÑO PARA IPv6"]

    %% 8.1 División en Subredes IPv4
    IPv4_SUB --> SEG["Segmentación & Dominios de Difusión<br/>Reducción de tráfico y mejor rendimiento"]
    IPv4_SUB --> BOUND["Límites de Octeto (/8, /16, /24)<br/>Con clase vs. Sin clase"]
    IPv4_SUB --> FORM["Fórmulas de Subredes<br/>Subredes: 2ⁿ | Hosts: 2ʰ - 2"]
    IPv4_SUB --> VLSM["Máscara de Subred de Longitud Variable<br/>Optimización de direcciones y enlaces punto a punto (/30)"]

    %% 8.2 Esquemas de Asignación
    ADDR_PLAN --> PLAN["Diseño Estructurado de Red<br/>Evitar duplicados, seguridad y control"]
    ADDR_PLAN --> DEV_TYPES["Asignación por Tipo de Dispositivo<br/>Hosts, Servidores, Impresoras, Routers, Gateways"]

    %% 8.3 Diseño IPv6
    IPv6_DESIGN --> GUA_STRUCT["Dirección GUA IPv6<br/>Prefijo Global /48 + Subred 16 bits + Interfaz 64 bits"]
    IPv6_DESIGN --> IPv6_SUB["Subredes IPv6<br/>Hasta 65 536 subredes /64 sin desperdiciar direcciones"]

    style L3 fill:#1e293b,stroke:#94a3b8,stroke-width:2px,color:#fff
    style IPv4_SUB fill:#1e3a8a,stroke:#60a5fa,color:#fff
    style ADDR_PLAN fill:#166534,stroke:#86efac,color:#fff
    style IPv6_DESIGN fill:#3730a3,stroke:#a5b4fc,color:#fff
```

#### Fórmulas y Cálculos de Subredes IPv4

|**Parámetro / Concepto**|**Fórmula / Regla**|**Descripción**|
|---|---|---|
|**Cantidad de Subredes**|$2^n$|$n$ = Bits tomados prestados de la porción de host.|
|**Hosts Utiles por Subred**|$2^h - 2$|$h$ = Bits restantes para la porción de host ($2$ se descuentan por red y _broadcast_).|
|**Subred con VLSM para Enlaces**|Prefijo `/30` (`255.255.255.252`)|Proporciona exactamente 2 direcciones de host útiles, ideal para enlaces serie.|
#### Ejemplos de División en Subredes según Prefijo

|**Prefijo Base**|**Nuevo Prefijo**|**Subredes Creadas**|**Hosts Útiles por Subred**|**Aplicación Típica**|
|---|---|---|---|---|
|`/24`|`/25`|2|126|Segmentación básica de una LAN pequeña.|
|`/24`|`/26`|4|62|División en 4 departamentos o áreas.|
|`/24`|`/27`|8|30|Cumplir requisitos de hasta 7 departamentos (máx. 29 hosts).|
|`/16`|`/18`|4|16 382|Subredes de gran tamaño corporativo.|
|`/16`|`/23`|128|510|Redes medianas con alta densidad de subredes.|
###  Conceptos 

> [!info] 1. Segmentación de Red y Dominios de Difusión
> 
> - **Dominios de Difusión:** Cada interfaz de un router delimita un dominio de difusión distinto.
>     
> - **Problemas de Redes Grandes:** Exceso de tráfico de difusión que degrada el rendimiento de la red y ralentiza los dispositivos procesando cada paquete.
>     
> - **Solución:** La división en subredes permite crear dominios de difusión más pequeños reduciendo el impacto del tráfico redundante.
>     

> [!example] 2. Máscara de Subred de Longitud Variable (VLSM)
> 
> - **Desperdicio en Subredes Tradicionales:** Aplicar una máscara fija a todas las subredes desperdicia direcciones IP en enlaces punto a punto o redes pequeñas.
>     
> - **Beneficio de VLSM:** Permite aplicar máscaras personalizadas según el tamaño real de cada segmento de red.
>     
> - **Uso en Enlaces Serie:** Asignar un prefijo `/30` deja solo 2 direcciones para hosts (RouterA y RouterB), optimizando la asignación.
>     

> [!tip] 4. Consideraciones de Diseño para IPv6
> 
> - **Estructura GUA:** Compuesta por un prefijo de enrutamiento global (típicamente `/48`), un ID de subred de 16 bits y una ID de interfaz de 64 bits.
>     
> - **Capacidad de Subredes:** La porción de ID de subred de 16 bits permite crear hasta 65 536 subredes `/64` independientes.
>     
> - **Diseño Simplificado:** A diferencia de IPv4, el agotamiento no es un problema en IPv6, por lo que el diseño se enfoca en una asignación lógica jerárquica en lugar del ahorro de direcciones.

# Capa de Transmisión  

```mermaid

flowchart TD
    %% Capa de Transporte
    L4["CAPA DE TRANSPORTE<br/>Capítulo 9 CCNA Introducción a Redes v6.0"] --> L4_PROTO["9.1 PROTOCOLOS DE LA CAPA DE TRANSPORTE"]
    L4 --> L4_COMM["9.2 TCP Y UDP"]

    %% 9.1 Protocolos de la Capa de Transporte
    L4_PROTO --> L4_ROLE["Funciones Clave<br/>• Seguimiento de conversaciones<br/>• Segmentación y reensamblado<br/>• Identificación de aplicaciones"]
    L4_PROTO --> L4_PORTS["Direccionamiento por Puertos<br/>• Pares de Sockets IP:Puerto<br/>• Conocidos 0-1023<br/>• Registrados 1024-49151<br/>• Dinámicos 49152-65535"]
    L4_PROTO --> L4_NETSTAT["Verificación de Conexiones<br/>Comando netstat"]

    %% 9.2 TCP y UDP
    L4_COMM --> TCP_SEC["Protocolo TCP Orientado a Conexión<br/>• Encabezado de 20 bytes<br/>• Enlace de 3 vías SYN, SYN-ACK, ACK<br/>• Cierre de sesión con FIN/ACK<br/>• Control de flujo Ventana y Congestión<br/>• Entrega ordenada Secuencia y ISN"]
    L4_COMM --> UDP_SEC["Protocolo UDP Sin Conexión<br/>• Encabezado de 8 bytes<br/>• Sin estado ni confirmación<br/>• Baja sobrecarga e ideado para streaming, VoIP, DNS"]

    style L4 fill:#1e293b,stroke:#94a3b8,stroke-width:2px,color:#fff
    style L4_PROTO fill:#1e3a8a,stroke:#60a5fa,color:#fff
    style L4_COMM fill:#166534,stroke:#86efac,color:#fff
    style TCP_SEC fill:#3730a3,stroke:#a5b4fc,color:#fff
    style UDP_SEC fill:#065f46,stroke:#6ee7b7,color:#fff
```

#### Comparativa entre TCP y UDP

|**Característica / Propiedad**|**Protocolo TCP (Transmission Control Protocol)**|**Protocolo UDP (User Datagram Protocol)**|
|---|---|---|
|**Tipo de Conexión**|Orientado a conexión (establece sesión previa).|Sin conexión (envía datagramas directamente).|
|**Sobrecarga de Encabezado**|20 bytes de sobrecarga.|8 bytes de sobrecarga.|
|**Manejo de Estado**|Con información de estado (_Stateful_).|Sin información de estado (_Stateless_).|
|**Fiabilidad y Confirmación**|Alta: Números de secuencia, confirmaciones (ACK) y retransmisión.|Sin confirmación ni retransmisión integrada.|
|**Control de Flujo y Congestión**|Sí (mediante el tamaño de ventana y ajuste de bytes enviables).|No existe a nivel de transporte.|
|**Casos de Uso Ideales**|Navegación Web (HTTP/HTTPS), Correo (SMTP/POP3), Bases de Datos.|VoIP, Transmisión de vídeo en vivo, DNS, DHCP, TFTP.|

#### 2. Clasificación de Grupos de Puertos (IANA)

|**Grupo de Puertos**|**Rango de Valores**|**Propósito / Ejemplo de Uso**|
|---|---|---|
|**Puertos Conocidos** (_Well-Known_)|`0` a `1023`|Reservados para servicios de red estándar (Ej. HTTP: 80, POP3: 110).|
|**Puertos Registrados**|`1024` a `49151`|Asignados por la IANA a aplicaciones de usuario específicas.|
|**Puertos Privados o Dinámicos**|`49152` a `65535`|Asignados dinámicamente por el S.O. cliente al iniciar una comunicación.|

### Conceptos 

> [!info] 1. Funciones de la Capa de Transporte y Sockets
> 
> - **Funciones Principales:** Seguimiento de conversaciones individuales, segmentación de datos, reensamblado en el destino e identificación de la aplicación adecuada.
>     
> - **Multiplexión:** Permite que múltiples aplicaciones transmitan datos simultáneamente etiquetando cada fragmento mediante números de puerto.
>     
> - **Sockets:** Combinación de una dirección IP y un número de puerto (ejemplo: `192.168.1.5:1099`). Un par de sockets (origen-destino) identifica de manera única una conexión de red activa.
>     
> - **Verificación (`netstat`):** Comando ejecutable en cliente/servidor para inspeccionar las conexiones TCP/UDP activas y sus puertos.
>     

> [!example] 2. Funcionamiento del Protocolo TCP
> 
> - **Enlace de Tres Vías (_Three-Way Handshake_):**
>     
>     1. El cliente envía un segmento **SYN** para iniciar la comunicación.
>         
>     2. El servidor responde con **SYN-ACK** reconociendo la solicitud y pidiendo sesión inversa.
>         
>     3. El cliente responde con un **ACK** confirmando el establecimiento del enlace.
>         
> - **Terminación de Sesión:** Utiliza la bandera **FIN** acompañada de confirmaciones **ACK** en ambas direcciones para cerrar la conexión de forma ordenada.
>     
> - **Entrega Ordenada:** Cada segmento incluye un número de secuencia inicial (ISN) que se incrementa por byte enviado, lo que permite reordenar segmentos fuera de tiempo.
>     
> - **Control de Flujo:** El campo _Tamaño de Ventana_ define cuántos bytes puede procesar el receptor antes de exigir un acuse de recibo.
>     

> [!abstract] 3. Funcionamiento del Protocolo UDP
> 
> - **Baja Sobrecarga:** Al no mantener estado ni requerir confirmación, minimiza los retrasos de red.
>     
> - **Reorganización en Aplicación:** Si los datagramas llegan desordenados o se pierden, la responsabilidad del control recae sobre la capa de aplicación.
>     
> - **Inversión de Puertos:** Al responder, el servidor invierte el puerto de origen dinámico del cliente y lo usa como destino del datagrama.


# Capa  de Aplicación


```mermaid
flowchart TD
    %% Capa de Aplicación
    L7["CAPA DE APLICACIÓN<br/>Capítulo 10 CCNA Introducción a Redes v6.0"] --> L7_PROTO["10.1 PROTOCOLOS DE CAPA DE APLICACIÓN"]
    L7 --> L7_SERVICES["10.2 PROTOCOLOS Y SERVICIOS RECONOCIDOS"]

    %% 10.1 Protocolos de Capa de Aplicación
    L7_PROTO --> L7_OSI["Relación OSI vs TCP/IP<br/>• Equivale a Capas 5, 6 y 7 de OSI<br/>• Presentación: Cifrado, compresión, formato<br/>• Sesión: Crea y mantiene diálogos"]
    L7_PROTO --> L7_ARCH["Arquitecturas de Red<br/>• Cliente-Servidor: Solicitud / Respuesta<br/>• P2P / Punto a Punto: Clientes actúan como servidores<br/>• BitTorrent: Rastreadores y archivos torrent"]

    %% 10.2 Protocolos y Servicios
    L7_SERVICES --> WEB_EMAIL["Web y Correo Electrónico<br/>• Web: HTTP, HTTPS URL y solicitudes GET<br/>• Envío Email: SMTP TCP 25<br/>• Recepción Email: POP TCP 110 y IMAP"]
    L7_SERVICES --> IP_SERVICES["Servicios de Red IP<br/>• DNS: Nombres a IP, Jerarquía Raíz, TLD, Registros A/AAAA/MX<br/>• DHCP: Proceso DORA Discover, Offer, Request, ACK"]
    L7_SERVICES --> FILE_SERVICES["Servicios de Archivos<br/>• FTP: Puerto 21 Control, Puerto 20 Datos<br/>• SMB: Uso compartido de archivos e impresoras"]

    style L7 fill:#1e293b,stroke:#94a3b8,stroke-width:2px,color:#fff
    style L7_PROTO fill:#065f46,stroke:#34d399,color:#fff
    style L7_SERVICES fill:#1e3a8a,stroke:#60a5fa,color:#fff
    style WEB_EMAIL fill:#3730a3,stroke:#a5b4fc,color:#fff
    style IP_SERVICES fill:#854d0e,stroke:#fde047,color:#fff
    style FILE_SERVICES fill:#831843,stroke:#f472b6,color:#fff
```


#### Principales Protocolos de la Capa de Aplicación

|**Protocolo**|**Función Principal**|**Puerto(s) de Operación**|**Tipo de Servicio**|
|---|---|---|---|
|**HTTP / HTTPS**|Transferencia de páginas web y contenido multimedia.|TCP 80 / TCP 443.|Web / Cifrado web.|
|**SMTP**|Envío y retransmisión de mensajes de correo electrónico entre servidores.|TCP 25.|Correo Electrónico.|
|**POP3**|Descarga de correo desde el servidor al cliente (suele eliminar del servidor).|TCP 110.|Correo Electrónico.|
|**IMAP**|Visualización y sincronización de correo manteniendo copia en el servidor.|TCP 143 / TCP 993|Correo Electrónico.|
|**DNS**|Traducción dinámica de nombres de dominio (URL) a direcciones IP.|UDP/TCP 53.|Asignación de Direcciones.|
|**DHCP / DHCPv6**|Asignación automatizada de IP, máscara, gateway y DNS a hosts.|UDP 67 (Servidor) / UDP 68 (Cliente).|Configuración de Host.|
|**FTP**|Transferencia confiable de archivos entre cliente y servidor.|TCP 21 (Control) / TCP 20 (Datos).|Transferencia de Archivos.|
|**SMB**|Intercambio de recursos (archivos e impresoras) en redes locales.|TCP 445.|Compartir Recursos.|

#### Comparativa entre Modelos de Arquitectura (Cliente-Servidor vs. P2P)

|**Característica**|**Modelo Cliente-Servidor**|**Redes Punto a Punto (P2P)**|
|---|---|---|
|**Estructura**|Servidor centralizado atendiendo a múltiples clientes.|Descentralizado; cada punto (_peer_) actúa como cliente y servidor.|
|**Ubicación de Datos**|Almacenados en servidores dedicados.|Distribuidos entre los hosts conectados.|
|**Escalabilidad**|Limitada por la capacidad técnica del servidor central.|Alta; cada nuevo nodo aporta ancho de banda y recursos.|
|**Ejemplos**|Servidores Web (HTTP), Servidores de Correo (SMTP/POP).|BitTorrent, eDonkey, G2.|

> [!example]  Servicios de Red Esenciales (DNS y DHCP)
> 
> - **Estructura Jerárquica DNS:** Compuesta por servidores Raíz, dominios de nivel superior (TLD como `.com`, `.org`, `.co`) y dominios de nivel secundario.
>     
> - **Registros DNS Comunes:**
>     
>     - **A:** Mapea un nombre de host a una dirección IPv4.
>         
>     - **AAAA:** Mapea un nombre de host a una dirección IPv6.
>         
>     - **MX:** Identifica los servidores de correo electrónico para el dominio.
>         
>     - **NS:** Especifica los servidores de nombres autoritativos.
>         
> - **Proceso DORA de DHCP:**
>     
>     1. **D**iscover: El cliente transmite un mensaje en búsqueda de un servidor DHCP.
>         
>     2. **O**ffer: El servidor responde ofreciendo una dirección IP y parámetros de red.
>         
>     3. **R**equest: El cliente solicita formalmente el arrendamiento de los datos ofrecidos.
>         
>     4. **A**CK: El servidor confirma la concesión mediante un acuse de recibo.
>         

> [!abstract] Transferencia de Archivos y Correo Electrónico
> 
> - **Mecanismo Dual de FTP:** Utiliza dos conexiones independientes; la **conexión de control** en el puerto TCP 21 para enviar comandos, y la **conexión de datos** en el puerto TCP 20 para la transferencia de archivos.
>     
> - **Flujo de Correo Electrónico:** Un emisor envía un correo mediante **SMTP** a su servidor local, este lo retransmite vía **SMTP** al servidor de destino, y el receptor lo consulta o descarga empleando **POP3** o **IMAP**.



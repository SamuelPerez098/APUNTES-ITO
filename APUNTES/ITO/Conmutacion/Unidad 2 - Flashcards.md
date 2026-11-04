---
tags:
  - flashcards/ccna/unidad-2
---
#  Unidad 2 — Flashcards

> [!info] Requiere el plugin **Spaced Repetition** (Plugins de la comunidad)
> Formato: `Pregunta::Respuesta` (una línea). Las tarjetas se repasan con el icono de tarjetas en la barra lateral.
> Apuntes completos: [[Unidad 2 - Apuntes]]

## Switch y arranque

¿Qué memoria guarda el POST y el cargador de arranque?::ROM
¿Qué memoria guarda el IOS?::Flash
¿Qué memoria guarda la startup-config?::NVRAM
Orden de arranque del switch::POST → Boot loader → Init Flash → Carga IOS (Flash a RAM) → startup-config (NVRAM a RAM)
Comando para ver el IOS de arranque configurado::`show boot`
Comando que define el IOS a cargar de Flash::`boot system`
Cómo entrar al modo recuperación `switch:`::Mantener el botón Mode al encender hasta que el LED pase de ámbar a verde sólido
¿Qué falla si falta `ip default-gateway` en un switch?::La administración remota desde otras redes (la misma subred sí funciona)
¿Qué requiere `mdix auto`?::`speed auto` y `duplex auto` activados
Full-duplex vs half-duplex::Full: enviar y recibir a la vez, sin colisiones. Half: uno u otro, puede haber colisiones

## Port Security y errores

Port Security: estado por defecto::Deshabilitado
Port Security: máximo de MACs por defecto::1
Port Security: modo de violación por defecto::Shutdown
MAC segura estática::Se configura manualmente
<!--SR:!2026-11-07,4,270-->
MAC segura dinámica::Se aprende sola y se borra al reiniciar
MAC segura sticky::Se aprende sola y se agrega a la running-config
Violación Protect::Descarta tráfico en silencio (sin syslog, sin contador, sin apagar)
Violación Restrict::Descarta + syslog + incrementa contador, sin apagar el puerto
Violación Shutdown::Descarta + contador + puerto en err-disabled
Recuperar puerto err-disabled::`shutdown` y luego `no shutdown`
Runts::Tramas menores a 64 bytes
Giants::Tramas mayores a 1518 bytes
Errores CRC::Checksum no coincide; ruido eléctrico / EMI
Late collisions::Colisión tras 512 bits; causa típica: duplex mismatch

## VLANs, DTP y VTP

VLAN 1::Default: administración y nativa por defecto; no se puede eliminar ni renombrar
VLAN nativa::Tráfico sin etiquetar en un trunk; debe coincidir en ambos extremos
VLAN de voz: retardo máximo::Menos de 150 ms
VLAN rango normal::1 a 1005, guardadas en vlan.dat (Flash)
VLAN rango extendido::1006 a 4094, guardadas en running-config
VLAN IDs 1002-1005::Reservadas para Token Ring/FDDI; no se pueden borrar
Tamaño de la etiqueta 802.1Q::4 bytes
EtherType de 802.1Q::0x8100
Tamaño del campo VID::12 bits
Dynamic Auto + Dynamic Auto::Access
Dynamic Auto + Dynamic Desirable::Trunk
Dynamic Desirable + Dynamic Desirable::Trunk
DTP es un protocolo...::Propietario de Cisco
VTP modo Servidor::Crea, modifica y elimina VLANs; envía anuncios
VTP modo Cliente::No crea VLANs; sincroniza con los anuncios
VTP modo Transparente::No sincroniza, reenvía anuncios, permite VLANs extendidas
Número de revisión VTP::Entero de 32 bits; el switch con el más alto sobrescribe a los demás

## Diseño, STP y EtherChannel

Capa de acceso::Conecta dispositivos finales; Port Security y VLANs
Capa de distribución::Agrega tráfico, routing inter-VLAN, límites de broadcast
Capa de núcleo::Backbone de alta velocidad, conmutación rápida
¿Quién es el Root Bridge?::El switch con el BID más bajo
BID =::Prioridad (default 32768) + ID de VLAN + MAC
Costo STP 10 Gbps::2
Costo STP 1 Gbps::4
Costo STP 100 Mbps::19
Costo STP 10 Mbps::100
Puerto raíz::En switch no-raíz, el de menor costo hacia el Root Bridge
Puerto designado::El que reenvía en un segmento; todos los del Root Bridge lo son
Puerto alternativo::Puerto bloqueado para evitar bucles
STP (802.1D)::Lento, una instancia para todas las VLANs
PVST+::Cisco, lento, una instancia por VLAN
RSTP (802.1w)::Rápido, una instancia
<!--SR:!2026-11-03,0,230-->
Rapid PVST+::Cisco, rápido, una instancia RSTP por VLAN
PortFast::Pasa a Forwarding directo; solo en puertos de dispositivos finales
BPDU Guard::Pone el puerto PortFast en err-disabled si recibe una BPDU
PAgP::Propietario Cisco; modos Desirable y Auto
LACP::Estándar IEEE 802.3ad; modos Active y Passive
PAgP Auto + Auto::No forma canal
LACP Passive + Passive::No forma canal
EtherChannel modo On::Fuerza el canal sin protocolo; ambos extremos en On
Requisitos de puertos en EtherChannel::Misma velocidad, dúplex, VLAN nativa, VLANs permitidas y modo access/trunk

## Inter-VLAN y FHRP

Inter-VLAN Legacy::Una interfaz física del router por VLAN
Router-on-a-Stick::Una interfaz física con subinterfaces y encapsulación 802.1Q
SVI::Interfaz lógica de una VLAN en un switch de Capa 3
Puerto enrutado::Puerto con `no switchport`, actúa como interfaz de router
Comando de subinterfaz ROAS para VLAN 10::`encapsulation dot1Q 10`
HSRP::Propietario Cisco; routers Activo y Standby
VRRP::Estándar abierto (RFC 5798); routers Master y Backup
GLBP::Propietario Cisco; permite balanceo de carga
Prioridad HSRP por defecto::100
`standby preempt`::Permite que el router de mayor prioridad retome el rol Activo
Multicast HSRP v1::224.0.0.2
Multicast HSRP v2::224.0.0.102

## ACLs

Orden de evaluación de una ACL::De arriba hacia abajo; se detiene en la primera coincidencia
Deny implícito::`deny any` invisible al final; toda ACL necesita al menos un permit
Rango ACL estándar::1-99 y 1300-1999
Rango ACL extendida::100-199 y 2000-2699
<!--SR:!2026-11-03,0,230-->
ACL estándar filtra por::Solo IP de origen
ACL estándar se ubica::Cerca del DESTINO
ACL extendida se ubica::Cerca del ORIGEN
Aplicar ACL a una interfaz::`ip access-group`
Aplicar ACL a líneas VTY::`access-class`

## DHCP y NAT

Mensajes DHCPv4 en orden::DISCOVER, OFFER, REQUEST, ACK (DORA)
DISCOVER y REQUEST se envían por::Broadcast
OFFER y ACK se envían por::Unicast
Comando para DHCP Relay::`ip helper-address <IP del servidor>`
SLAAC::El host crea su IPv6 con el prefijo del RA + su ID de interfaz; sin servidor
Flags M=0, O=0::Solo SLAAC
Flags M=0, O=1::DHCPv6 sin estado (SLAAC + DHCPv6 para DNS/dominio)
Flag M=1::DHCPv6 con estado; el servidor asigna todo
Inside Local::IP privada del host interno
Inside Global::IP pública que representa al host interno
<!--SR:!2026-11-03,0,230-->
Outside Local::IP del destino vista desde la red interna
Outside Global::IP pública real del host externo
<!--SR:!2026-11-03,0,230-->
NAT estática::Mapeo 1 a 1
NAT dinámica::Toma IP de un pool por orden de llegada
PAT / overload::Muchas IP privadas a una pública, diferenciadas por puerto

## EIGRP

Algoritmo de EIGRP::DUAL
Transporte de EIGRP::RTP
Multicast EIGRP IPv4::224.0.0.10
AD EIGRP interna::90
AD EIGRP externa::170
<!--SR:!2026-11-06,3,250-->
AD EIGRP resumen (Null0)::5
Métrica por defecto de EIGRP::Ancho de banda más lento + suma de retardos (K1=1, K3=1)
Sucesor::Ruta primaria que va a la tabla de enrutamiento
Distancia Factible (FD)::La métrica más baja calculada hacia el destino
Distancia Notificada (RD)::Métrica anunciada por el vecino
Condición de Factibilidad::RD menor que FD
Sucesor Factible::Ruta de respaldo libre de bucles que cumple la FC
Balanceo de carga de distinto costo en EIGRP::`variance`
EIGRP IPv6: cómo se habilita::En la interfaz con `ipv6 eigrp <AS>`
EIGRP IPv6: IP de origen de los mensajes::Dirección link-local
EIGRP IPv6: Router ID::Requerido explícitamente (32 bits) y requiere `no shutdown`

## OSPF

Tipo de protocolo OSPF::Estado de enlace
<!--SR:!2026-11-06,3,250-->
Algoritmo de OSPF::SPF (Dijkstra)
AD de OSPF::110
Métrica de OSPF::Costo = ancho de banda de referencia / ancho de banda de la interfaz
Multicast OSPF todos los routers (IPv4)::224.0.0.5
Multicast OSPF DR/BDR (IPv4)::224.0.0.6
<!--SR:!2026-11-03,0,230-->
Paquete OSPF tipo 1::Hello
Paquete OSPF tipo 2::DBD
Paquete OSPF tipo 3::LSR
Paquete OSPF tipo 4::LSU (contiene LSA)
Paquete OSPF tipo 5::LSAck
Estados OSPF en orden::Down, Init, Two-Way, ExStart, Exchange, Loading, Full
¿En qué estado OSPF se elige DR/BDR?::Two-Way
Estado operacional normal OSPF::Full
Prioridad OSPF por defecto y rango::1; de 0 a 255
Prioridad OSPF 0::Nunca será DR ni BDR
Desempate en elección de DR::Router ID más alto
Orden para elegir Router ID::Manual, luego Loopback más alta, luego IP física más alta
ABR::Router que conecta el Área 0 con otras áreas
ASBR::Router que conecta OSPF con redes externas
LSA tipo 1::Router LSA; todos los routers; enlaces de su área
LSA tipo 2::Network LSA; lo genera el DR
LSA tipo 3::Summary LSA; lo genera el ABR; redes inter-área
LSA tipo 4::ASBR Summary; lo genera el ABR; ubicación del ASBR
LSA tipo 5::AS External; lo genera el ASBR; rutas externas

## Troubleshooting

Requisitos para formar adyacencia::Hello/Dead iguales, red/máscara y área iguales, mismo AS (EIGRP), interfaz no pasiva, misma autenticación
Comando para ver vecinos OSPF::`show ip ospf neighbor`
Comando para ver vecinos EIGRP::`show ip eigrp neighbors`
Comando para ver parámetros del protocolo::`show ip protocols`
Propagar ruta por defecto en OSPF::`default-information originate`
Propagar ruta por defecto en EIGRP::`redistribute static`

---
tags:
  - tech/stack
  - webdev/backend
  - database/sql
  - architecture/monolith
date_created: 2026-08-29
aliases:
  - LAMP
  - LAMP Stack
---

#  LAMP Stack

##  Descripción General
El **LAMP Stack** es una de las arquitecturas de software web más tradicionales, estables y probadas en la historia de Internet. Compuesto íntegramente por tecnologías de código abierto (*Open Source*), ha servido como la columna vertebral de la Web 2.0 y sigue siendo el motor de gran parte de los sitios web activos a nivel mundial.

- **L - Linux:** Sistema operativo del servidor.
    
- **A - Apache:** Servidor web HTTP para procesar y entregar las peticiones.
    
- **M - MySQL / MariaDB:** Sistema de gestión de bases de datos relacional (SQL).
    
- **P - PHP:** Lenguaje de programación en el lado del servidor (en ocasiones se sustituye por Python o Perl).

---

##  Componentes del Stack

```
 ┌────────────────────────────────────────────────────────┐
 │                   Operating System                     │
 │  Linux (Server OS Layer)                               │
 └──────────────────────────┬─────────────────────────────┘
                            │
 ┌──────────────────────────▼─────────────────────────────┐
 │                     Web Server                         │
 │   Apache HTTP Server                                   │
 └──────────────────────────┬─────────────────────────────┘
                            │
 ┌──────────────────────────▼─────────────────────────────┐
 │                 Database Management                    │
 │   MySQL / MariaDB (Relational SQL DB)                  │
 └──────────────────────────┬─────────────────────────────┘
                            │
 ┌──────────────────────────▼─────────────────────────────┐
 │                 Programming Language                   │
 │  PHP (Server-Side Scripting)                           │
 └────────────────────────────────────────────────────────┘
```

### 1.  Linux (Operating System)
* **Rol en el stack:** Es el sistema operativo base que proporciona la infraestructura de red, gestión de procesos, permisos y seguridad del servidor.

### 2.  Apache (Web Server)
* **Rol en el stack:** Servidor HTTP encargado de procesar las solicitudes de los clientes (navegadores), gestionar módulos (como `mod_php`) y despachar archivos estáticos o dinámicos.

### 3.  MySQL / MariaDB (Database)
* **Tipo:** Sistema de Gestión de Bases de Datos Relacionales (RDBMS).
* **Rol en el stack:** Almacenar la información estructurada mediante tablas, relaciones, claves foráneas y consultas SQL estándar.

### 4.  PHP (Programming Language)
* **Tipo:** Lenguaje de programación interpretado ejecutable en el lado del servidor.
* **Rol en el stack:** Procesar la lógica de negocio, interactuar con la base de datos MySQL y generar contenido HTML dinámico enviado al cliente.

---

##  Caso de Uso Ideal
* Sitios web de contenidos, blogs y portales de noticias.
* Sistemas CMS como **WordPress**, **Drupal** o **Joomla**.
* Aplicaciones empresariales tradicionales con arquitectura monolítica (ej. usando frameworks como Laravel o Symfony).

---

##  Ventajas y Desventajas

###  Ventajas
* **Extrema estabilidad y madurez:** Décadas de documentación, soporte comunitario y librerías probadas en producción.
* **Despliegue económico y universal:** Compatible con prácticamente cualquier hosting compartido (*shared hosting*) o servidor VPS.
* **Excelente para SEO:** Renderizado de contenido del lado del servidor (SSR) de forma nativa sin configuraciones complejas.

###  Desventajas
* **Renderizado monolítico:** Tradicionalmente acopla el backend con el frontend, dificultando la creación de interfaces dinámicas estilo SPA.
* **Escalabilidad de concurrencia:** Apache y el modelo de procesamiento de PHP consumen más memoria por hilo en comparación con la arquitectura de eventos de Node.js.

---

## 🔗 Notas Relacionadas
* [[MEAN_Stack]]
* [[MERN Stack]]
* [[PHP]]
* [[MySQL]]

---
tags:
  - tech/stack
  - webdev/backend
  - webdev/frontend
  - database/nosql
  - architecture/fullstack
date_created: 2026-08-29
aliases:
  - MEAN
  - MEAN Stack
---
# MEAN Stack

## Descripción General
El **MEAN Stack** es un conjunto de tecnologías de desarrollo web de código abierto compuestas íntegramente por **JavaScript / TypeScript** de extremo a extremo (desde la base de datos hasta la interfaz de usuario). Es ampliamente utilizado en el ámbito empresarial para construir aplicaciones web dinámicas, escalables y mantenibles de una sola página (**SPA** - *Single Page Application*).


- **M - MongoDB:** Base de datos NoSQL orientada a documentos (JSON/BSON).
    
- **E - Express.js:** Framework minimalista para el servidor (backend) sobre Node.js.
    
- **A - Angular:**  (framework de Google)..
    
- **N - Node.js:** Entorno de ejecución de JavaScript en el lado del servidor.

---

##  Componentes del Stack

```
 ┌────────────────────────────────────────────────────────┐
 │                    Client (Browser)                    │
 │   Angular (TypeScript UI Framework)                    │
 └──────────────────────────┬─────────────────────────────┘
                            │ HTTP / REST / GraphQL / WebSockets
 ┌──────────────────────────▼─────────────────────────────┐
 │                    Server (Backend)                    │
 │   Node.js (Runtime)  +  ⚡ Express.js (Framework)       │
 └──────────────────────────┬─────────────────────────────┘
                            │ Driver (Mongoose / Native)
 ┌──────────────────────────▼─────────────────────────────┐
 │                    Database (Storage)                  │
 │   MongoDB (NoSQL Document Store)                       │
 └────────────────────────────────────────────────────────┘
```

### 1.  MongoDB (Data Layer)
* **Tipo:** Base de datos NoSQL orientada a documentos.
* **Formato de datos:** Almacena la información en documentos BSON (Binary JSON).
* **Rol en el stack:** Proveer una capa de datos flexible, escalable y sin esquemas rígidos que interactúa de forma nativa con objetos de JavaScript.

### 2.  Express.js (Backend Framework)
* **Tipo:** Framework web minimalista y unwritten para Node.js.
* **Rol en el stack:** Simplifica la creación de servidores HTTP, manejo de rutas (Routing), controladores y middle-wares para la construcción de APIs RESTful.

### 3. Angular (Frontend Framework)
* **Tipo:** Framework de desarrollo frontend mantenido por Google.
* **Lenguaje:** Escrito nativamente en **TypeScript**.
* **Rol en el stack:** Proporcionar una arquitectura MVC/MVVM robusta en el cliente mediante Inyección de Dependencias, Data Binding bidireccional, módulos y componentes altamente estructurados.

### 4.  Node.js (Execution Environment)
* **Tipo:** Entorno de ejecución de JavaScript asíncrono e impulsado por eventos (basado en el motor V8 de Chrome).
* **Rol en el stack:** Ejecutar código JavaScript en el servidor de manera no bloqueante (I/O no bloqueante), garantizando alta concurrencia.

---

##  Caso de Uso Ideal
* Aplicaciones empresariales de gran escala que requieren arquitecturas estrictas.
* Sistemas donde se prefiere **TypeScript** en todo el flujo de trabajo.
* Dashboards corporativos, plataformas de gestión académica y portales transaccionales.

---

## ⚖️ Ventajas y Desventajas

###  Ventajas
* **Un solo lenguaje:** Permite reutilizar lógica de código y utilizar JavaScript/TypeScript en todas las capas.
* **Arquitectura sólida:** Angular impone una estructura clara, ideal para equipos medianos y grandes.
* **Alto rendimiento:** Manejo eficiente de peticiones concurrentes gracias al modelo de E/S no bloqueante de Node.js.

###  Desventajas
* **Curva de aprendizaje elevada:** Angular es un framework completo y complejo (conceptos de RxJS, TypeScript, inyección de dependencias).
* **Sobrecarga para proyectos pequeños:** Puede resultar excesivamente estructurado para MVPs o landings sencillas.

---

## 🔗 Notas Relacionadas
* [[MERN_Stack]]
* [[LAMP_Stack]]
* [[JavaScript]]
* [[TypeScript]]
* [[MongoDB]]
* [[Node.js]]




Claro. Te las dejo **directas y fáciles de estudiar**, pero con el nivel suficiente para responderlas en un examen.

### 1. ¿Qué tecnologías tiene el Stack MERN, LAMP y los clásicos?

**MERN**:

- **M** → MongoDB: base de datos NoSQL.
- **E** → Express.js: framework para backend de Node.js.
- **R** → React: biblioteca para frontend.
- **N** → Node.js: entorno de ejecución para JavaScript en backend.

**LAMP**:

- **L** → Linux: sistema operativo.
- **A** → Apache: servidor web.
- **M** → MySQL/MariaDB: sistema gestor de bases de datos.
- **P** → PHP: lenguaje de programación para backend.

**Stack clásico** puede referirse a tecnologías tradicionales como:

- HTML
- CSS
- JavaScript
- PHP
- MySQL
- Apache

**Next.js**:

- Framework basado en **React**.
- Se utiliza principalmente para desarrollar aplicaciones web modernas.
- Puede manejar tanto **frontend como funcionalidades de backend**.

---

### 2. ¿Para qué sirve HTML?

**HTML (HyperText Markup Language)** sirve para **estructurar el contenido de una página web**.

Define elementos como:

- Títulos
- Párrafos
- Imágenes
- Enlaces
- Formularios
- Tablas
- Botones

**Importante:** HTML **no es un lenguaje de programación**; es un **lenguaje de marcado**.

---

### 3. ¿Qué significan HTTP y HTTPS?

**HTTP** = **HyperText Transfer Protocol**  
(Protocolo de Transferencia de Hipertexto)

Es el protocolo utilizado para la comunicación entre un cliente, como un navegador, y un servidor web.

**HTTPS** = **HyperText Transfer Protocol Secure**

Es la versión **segura de HTTP**, ya que utiliza **cifrado mediante TLS** para proteger la comunicación.

**Diferencia principal:**

> HTTP → comunicación sin cifrado.  
> HTTPS → comunicación cifrada y más segura.

---

### 4. Comandos de SQL

Algunos comandos importantes de SQL son:

|Comando|Función|
|---|---|
|`SELECT`|Consultar datos|
|`INSERT`|Insertar datos|
|`UPDATE`|Modificar datos|
|`DELETE`|Eliminar datos|
|`CREATE`|Crear objetos, como tablas|
|`ALTER`|Modificar estructuras|
|`DROP`|Eliminar objetos|

Los cuatro primeros son especialmente importantes porque corresponden a las operaciones **CRUD**:

> **Create → Read → Update → Delete**

Aunque en SQL normalmente `CREATE` no es el comando usado para el "Create" de CRUD; para insertar registros se utiliza **`INSERT`**.

---

### 5. ¿Qué es y qué no es un SGBD relacional?

**SGBD** significa **Sistema Gestor de Bases de Datos**.

Un **SGBD relacional** es un sistema que permite almacenar y administrar información utilizando **tablas relacionadas entre sí** mediante elementos como claves primarias y foráneas.

Ejemplos:

- MySQL
- PostgreSQL
- MariaDB
- Oracle Database
- Microsoft SQL Server

**Sí es:**

- Basado principalmente en tablas.
- Utiliza relaciones entre tablas.
- Generalmente utiliza SQL.
- Permite establecer restricciones e integridad de datos.

**No es:**

- Una simple hoja de Excel.
- Una base de datos NoSQL.
- Solamente un conjunto de archivos de texto.
- Una aplicación frontend.

---

### 6. ¿En qué aplicaciones se utilizan las bases de datos NoSQL?

Las bases de datos **NoSQL** se utilizan especialmente cuando se necesitan manejar **grandes cantidades de información**, estructuras de datos flexibles o **altos volúmenes de operaciones**.

Ejemplos:

- Redes sociales.
- Aplicaciones web de gran escala.
- Sistemas de tiempo real.
- Plataformas de contenido.
- Aplicaciones que manejan grandes cantidades de usuarios.
- Sistemas donde los datos pueden tener estructuras variables.

Por ejemplo, una red social puede necesitar almacenar rápidamente publicaciones, comentarios, reacciones, perfiles y relaciones entre usuarios.

Algunos ejemplos de NoSQL son:

- **MongoDB**
- Cassandra
- Redis
- DynamoDB

**Ojo para el examen:** no significa que _"NoSQL = más rápido siempre"_. La ventaja depende del tipo de aplicación, modelo de datos y operación que se necesite realizar.

---

### 7. Diferencias entre Frontend y Backend

|Frontend|Backend|
|---|---|
|Es la parte que ve el usuario.|Es la parte que funciona detrás de la aplicación.|
|Maneja la interfaz gráfica.|Maneja la lógica del sistema.|
|Se ejecuta principalmente en el navegador.|Se ejecuta principalmente en el servidor.|
|HTML, CSS, JavaScript, React.|Node.js, PHP, Java, Python, etc.|
|Interactúa con el usuario.|Interactúa con bases de datos y servicios.|

Una forma fácil de recordarlo:

> **Frontend = lo que el usuario ve y utiliza.**  
> **Backend = lo que ocurre detrás para que la aplicación funcione.**
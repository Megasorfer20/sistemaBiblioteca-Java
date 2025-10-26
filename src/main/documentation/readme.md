# Sistema de Gestion de Biblioteca

Este proyecto implementa un sistema de gestion para una biblioteca universitaria, permitiendo tanto a administradores como a usuarios interactuar con la coleccion de libros y la informacion de los miembros del sistema.

## 1. Caracteristicas Principales

El sistema aborda tres areas principales de gestion:

### 1.1. Administracion de la Coleccion de Libros

- **Agregar Libros:** Los administradores pueden anadir nuevos libros al sistema. Para ello, deben especificar el nombre, autor, las unidades totales disponibles y la biblioteca a la que pertenece el libro. El sistema genera codigos de libro unicos automaticamente si no se provee uno, siguiendo el formato `ID_BIB-SEDE_INICIAL-NUMERO_SECUENCIAL` (Ej: `1-M-123` para la Biblioteca 1, sede Medellin, libro secuencial 123).
- **Editar Libros:** Los administradores tienen la capacidad de modificar la informacion de los libros existentes, incluyendo el nombre, autor y el numero total de unidades. Se valida que las nuevas unidades totales no sean menores que las unidades actualmente prestadas.
- **Eliminar Libros:** Los administradores pueden eliminar unidades especificas de un libro. Si el libro tiene unidades prestadas, solo las unidades disponibles (libres) pueden ser eliminadas. Si despues de la eliminacion el libro no tiene unidades libres y tampoco unidades prestadas, el registro completo del libro es removido del sistema. Las copias digitalmente "prestadas" a usuarios se mantienen en su registro de prestamos, pero no son contadas en el inventario fisico.
- **Buscar Libros:** Los usuarios y administradores pueden buscar libros utilizando diversos criterios como el nombre del libro, el autor, el codigo unico del libro, la sede de la biblioteca o el ID de la biblioteca.

### 1.2. Administracion de Usuarios

- **Roles de Usuario:** El sistema diferencia entre varios tipos de miembros, definidos por su rol:
  - **Admin (Rol 0):** Es el administrador del sistema, con acceso completo a todas las funcionalidades, incluyendo la gestion de libros, usuarios y bibliotecas.
  - **Usuario (Hereda de Miembro):** Base para los usuarios regulares, con roles especificos y limites de prestamo:
    - **Estudiante (Rol 1):** Puede pedir prestados hasta 5 libros simultaneamente.
    - **Profesor (Rol 2):** Puede pedir prestados hasta 3 libros simultaneamente.
    - **Administrativo Universidad (Rol 3):** Puede pedir prestado un unico libro a la vez.
- **Agregar Usuarios:** Los administradores pueden crear nuevos usuarios en el sistema. Para cada nuevo usuario, deben proporcionar su tipo y numero de documento, rol, nombre, apellido, un nombre de usuario unico, una contrasena, la sede de la universidad a la que pertenece y su carrera.
- **Editar Usuarios:** Los administradores pueden modificar la informacion de usuarios existentes, incluyendo su rol (con precauciones para no inadvertidamente deshabilitar el acceso de administradores si se cambia su rol a uno de usuario regular).
- **Eliminar Usuarios:** Los administradores solo pueden eliminar un registro de usuario si este no tiene libros prestados actualmente y no posee ninguna deuda pendiente con la biblioteca.
- **Autogestion de Usuario:** Cada usuario (no administrador) tiene la capacidad de cambiar su informacion de contacto (como tipo de documento, nombre, apellido, sede de la universidad, y carrera) y puede actualizar su contrasena para acceder al sistema.

### 1.3. Prestamo, Devolucion y Multas

- **Prestar Libros:** Los usuarios pueden solicitar el prestamo de libros disponibles a traves de su cuenta personal. Esta accion esta sujeta a los limites de prestamo establecidos para su rol de usuario y a la condicion de no tener deudas pendientes con la biblioteca.
- **Devolver Libros:** Los usuarios pueden devolver los libros que tienen prestados. El sistema registra la fecha real de devolucion.
- **Deuda y Multas:** Existe un periodo maximo de 30 dias para la devolucion de un libro. Si un libro se devuelve despues de la fecha estimada, se aplica una multa de 4.750 pesos por cada dia de retraso por libro. Si un usuario tiene multiples libros atrasados, la multa se acumula. Un usuario con deuda pendiente no podra tomar nuevos libros prestados hasta que abone la totalidad de su deuda.
- **Historial de Prestamos:** Cada usuario puede consultar una lista de todos los libros que tiene o ha tenido prestados, incluyendo la fecha de prestamo, la fecha de devolucion estimada, la fecha real de devolucion (si aplica) y el estado actual del prestamo.

## 2. Visibilidad y Gestion (para Administradores)

La cuenta de administrador ofrece una vista global y total control sobre el sistema:

- **Ver Todos los Libros:** Los administradores pueden ver una lista completa de todos los libros registrados, incluyendo sus unidades libres y unidades prestadas.
- **Ver Libros Prestados:** Un administrador puede obtener una lista de todos los libros que estan actualmente en estado de prestamo, detallando a que usuario estan prestados, la fecha de prestamo y la biblioteca de origen.
- **Ver Prestamos por Usuario:** Es posible consultar los libros que ha tomado prestados y/o devuelto un usuario especifico, identificandolo por su numero de documento.
- **Ver Todos los Usuarios:** El administrador tiene acceso a la lista completa de todos los miembros registrados en el sistema, diferenciando entre administradores y los distintos tipos de usuarios.
- **Gestionar Bibliotecas:** El administrador puede anadir nuevas bibliotecas al sistema (especificando ID, sede y nombre), editar los datos de bibliotecas existentes y eliminar bibliotecas si ya no son necesarias.

## 3. Estructura del Proyecto

El proyecto sigue una organizacion de codigo dentro del paquete `com.mycompany.sistemabiblioteca`. Las clases principales y su rol son:

- `Admin`: Hereda de `Miembro`. Contiene todos los metodos para las funcionalidades de administracion del sistema (gestion de libros, usuarios y bibliotecas).
- `Biblioteca`: Gestiona la informacion particular de cada biblioteca (ID, sede, nombre) y encapsula reglas de negocio para el prestamo, devolucion y aplicacion de multas.
- `Fecha`: Una clase de utilidad que provee metodos estaticos para el manejo, formato y calculo de diferencias entre fechas.
- `IPersistible`: Una interfaz que define un contrato para los objetos que pueden ser guardados y eliminados del almacenamiento persistente, estandarizando los metodos `guardar()`, `eliminar()` y `construirLinea()`.
- `Libro`: Representa un libro individual en el sistema, con atributos como nombre, autor, codigo identificador, unidades libres y prestadas, y la identificacion de la biblioteca a la que pertenece. Implementa `IPersistible`.
- `Miembro`: Clase base para `Admin` y `Usuario`. Contiene la informacion comun a todos los miembros (documento, rol, nombre, etc.) y la logica para la autenticacion de usuarios mediante hashing seguro de contrasenas. Implementa `IPersistible`.
- `PathManager`: Una clase de utilidad crucial que centraliza la logica para resolver las rutas de los archivos de persistencia. Esto asegura que la aplicacion pueda encontrar y manipular sus archivos de datos de forma fiable, tanto en un entorno de desarrollo como si la aplicacion esta empaquetada en un archivo JAR.
- `Prestamo`: Un objeto que registra los detalles de cada operacion de prestamo de un libro por parte de un miembro, incluyendo fechas y estado del prestamo. Implementa `IPersistible`.
- `SistemaBiblioteca`: La clase principal que orquesta la ejecucion de la aplicacion. Se encarga de la configuracion inicial de datos, la ejecucion de pruebas automaticas, la gestion del proceso de inicio de sesion y la presentacion de los menus interactivos para administradores y usuarios.
- `Usuario`: Hereda de `Miembro`. Representa a los usuarios regulares del sistema (estudiantes, profesores, administrativos), anadiendo atributos especificos como `deuda`, `sedeUniversidad` y `carrera`, asi como metodos para interactuar con la biblioteca desde su perspectiva.

## 4. Persistencia de Datos

Para garantizar que la informacion del sistema se mantenga incluso despues de cerrar la aplicacion, los datos se almacenan en archivos de texto plano (`.txt`). Estos archivos se crean y se gestionan en un subdirectorio llamado `datos` dentro del directorio de trabajo de la aplicacion (donde se ejecuta el JAR o la clase principal).

Los archivos de persistencia son los siguientes:

- `datos/Bibliotecas.txt`: Contiene la informacion estructurada de cada biblioteca registrada en el sistema.
- `datos/Miembros.txt`: Almacena los datos de todos los administradores y usuarios, incluyendo sus roles y la version hasheada de sus contrasenas para seguridad.
- `datos/Libros.txt`: Guarda los detalles de todos los libros disponibles en las distintas bibliotecas del sistema.
- `datos/LibroPrestado.txt`: Registra todos los prestamos realizados, tanto los que estan activos como los que ya han sido devueltos.

Los datos dentro de estos archivos estan formateados con campos delimitados por el caracter `\` (barra invertida) para facilitar su parseo y reconstruccion en objetos Java.

## 5. Datos Quemados (Inicializacion del Sistema)

Al iniciar la aplicacion (`SistemaBiblioteca.main`), se ejecuta automaticamente el metodo `setupInitialData()`, que se encarga de preparar el entorno del sistema:

1.  **Creacion de `Bibliotecas.txt`:** Si el archivo `datos/Bibliotecas.txt` no existe o esta vacio, se crearan dos bibliotecas de ejemplo por defecto:
    - "Biblioteca Central" ubicada en "Medellin" con ID 1.
    - "Biblioteca Norte" ubicada en "Bogota" con ID 2.
2.  **Creacion de `Admin`:** Si no se encuentra ningun administrador registrado en `datos/Miembros.txt`, se crea automaticamente un administrador. Se generaran credenciales (usuario y contrasena) aleatorias para este admin, las cuales seran mostradas en la consola para que el usuario pueda iniciar sesion.
3.  **Creacion de `Usuarios`:** Si el sistema tiene menos de 3 usuarios registrados (excluyendo el administrador), se generaran usuarios aleatorios para completar ese numero. Estos usuarios seran de tipo estudiante, profesor o administrativo de la universidad, con sus respectivas credenciales aleatorias (tambien mostradas en consola).

**Para iniciar la aplicacion con un entorno de datos "limpio" y reseteado, simplemente borre manualmente todos los archivos `.txt` que se encuentran dentro de la carpeta `datos` antes de ejecutar el programa.**

## 6. Nomenclatura del Codigo

Para cumplir con los requisitos especificos, se han aplicado las siguientes reglas de nomenclatura en todo el codigo fuente del proyecto:

- **Identificadores en Espanol:** Todas las variables, metodos y nombres de clases estan definidos en espanol.
- **Reemplazo de 'ñ':** La letra `ñ` ha sido sistematicamente reemplazada por la letra `n` (Ej: `contrasena` en lugar de `contraseña`, `planificacion` en lugar de `planificación`).
- **Ausencia de Tildes:** Las vocales con tilde (`á`, `é`, `í`, `ó`, `ú`) han sido sustituidas por sus equivalentes sin tilde (`a`, `e`, `i`, `o`, `u`) (Ej: `numero` en lugar de `número`, `biblioteca` en lugar de `biblioteca` - sin tilde en el codigo).

## 7. Como Ejecutar el Proyecto

Siga estos pasos para compilar y ejecutar la aplicacion:

1.  **Guardar Archivos:** Asegurese de que cada bloque de codigo Java proporcionado se guarde en su respectivo archivo `.java` dentro de la estructura de carpetas `com/mycompany/sistemabiblioteca/`. Adicionalmente, cree una carpeta `datos` en el directorio raiz del proyecto.
2.  **Compilar (desde la raiz del proyecto):** Abra una terminal o linea de comandos. Navegue hasta el directorio raiz de su proyecto (donde se encuentra la carpeta `com`). Compile todos los archivos Java del paquete:
    ```bash
    javac com/mycompany/sistemabiblioteca/*.java
    ```
    Si no hay errores, se generaran los archivos `.class` correspondientes.
3.  **Ejecutar (desde la raiz del proyecto):** Despues de la compilacion, ejecute la clase principal del sistema:
    ```bash
    java com/mycompany/sistemabiblioteca/SistemaBiblioteca
    ```

**Flujo de Ejecucion:**

- El programa iniciara mostrando un mensaje de bienvenida.
- Inmediatamente despues, ejecutara una serie de **tests automaticos** que demostraran la correcta implementacion y funcionamiento de varias caracteristicas (gestion de libros, usuarios, prestamos, etc.). Estos tests incluye pausas interactivas para que pueda seguir el flujo de salida en la consola.
- Una vez que los tests automaticos finalicen y haya presionado "Enter" en las pausas solicitadas, se le pedira que **seleccione una biblioteca** para interactuar.
- Posteriormente, se le solicitara **iniciar sesion** con las credenciales de un administrador o un usuario regular (puede usar las credenciales mostradas durante la `setupInitialData`).
- Finalmente, entrara en el **menu interactivo** correspondiente a su rol (Administrador o Usuario), desde donde podra explorar todas las funcionalidades del sistema.

## 8. Tecnologias Utilizadas

- **Lenguaje de Programacion:** Java (compatible con version 8 en adelante).
- **Manejo de Archivos:** Las APIs `java.nio.file` (para operaciones robustas con rutas y archivos) y `java.io` (para lectura y escritura de texto).
- **Criptografia:** Implementacion de PBKDF2 con HmacSHA256 para el hashing seguro de contrasenas, protegiendo la informacion de acceso de los miembros.
- **Manejo de Fechas:** Las clases `java.util.Date`, `java.util.Calendar` y `java.text.SimpleDateFormat` para la gestion, formato y calculo de diferencias de fechas en el sistema (prestamos, devoluciones, multas).

---

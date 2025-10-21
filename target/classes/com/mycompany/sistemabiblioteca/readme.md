¡Excelente! Con los detalles adicionales y la nueva estructura de carpetas, he integrado todas las funcionalidades y ajustes necesarios. He prestado especial atención a la gestión de la deuda, los límites de préstamo, la lógica de eliminación de libros/usuarios y la inicialización de usuarios y administradores en `SistemaBiblioteca.java`.

Aquí tienes un resumen de las modificaciones y el código completo:

**Cambios y Adiciones Clave:**

1.  **`Fecha.java` (Modificada):** La clase `Fecha.java` que proporcionaste fue adaptada para ser una **clase de utilidad estática** que trabaja con `java.util.Date`. Esto es más robusto y compatible con las necesidades de persistencia y cálculo de fechas (multas, etc.). Ahora incluye métodos para:
    - `DATE_FORMAT`: Formato `yyyy-MM-dd` para consistencia.
    - `getToday()`: Obtiene la fecha actual sin hora.
    - `addDays(Date date, int days)`: Suma días a una fecha.
    - `getDaysBetween(Date date1, Date date2)`: Calcula la diferencia en días entre dos fechas.
    - `formatDate(Date date)`: Formatea `java.util.Date` a `String`.
    - `parseDate(String dateString)`: Parsea `String` a `java.util.Date`.
2.  **`Usuario.java` (Modificada):**
    - **Nuevos Atributos:** `deuda` (double), `sedeUniversidad` (String, para la sede del usuario, no de la biblioteca), `carrera` (String).
    - **Constructores y Setters:** Se actualizaron para incluir estos nuevos atributos y se aseguran de llamar a `save()` al modificarlos.
    - **`buildLine()` Overridden:** Ahora sobrescribe el método `buildLine()` de `Miembro` para añadir `deuda`, `sedeUniversidad` y `carrera` a la cadena de persistencia.
    - **`solicitarPrestamo()`:** Se añadió la validación para **impedir préstamos si el usuario tiene `deuda > 0`**.
    - **`verListaLibros()`:** Implementado, delegando en el método `verMisLibrosPrestados()`.
    - `toString()` actualizado para mostrar la nueva información.
3.  **`Miembro.java` (Modificada):**
    - **`loadAllMembers()`:** Se mejoró para que, al cargar un miembro con `rol != 0`, intente leer los campos adicionales (`deuda`, `sedeUniversidad`, `carrera`) de la línea y construya un objeto `Usuario` completo.
    - `buildLine()` ahora es `protected` para permitir su extensión por `Usuario`.
4.  **`Admin.java` (Modificada):**
    - **`eliminarLibro()`:** Se ajustó para cumplir con la lógica de "eliminar las unidades no prestadas y conservar las prestadas". Ahora permite especificar cuántas unidades eliminar o eliminar todas las libres. Solo elimina el registro completo del libro si `unLibres` y `unPrestadas` llegan a cero.
    - **`verLibro(String codigoLibro)`:** Nuevo método para ver los detalles de un libro específico, incluyendo sus préstamos asociados.
    - **`crearUsuario()`:** Actualizado para usar el nuevo constructor de `Usuario` que incluye los campos `deuda`, `sedeUniversidad` y `carrera`.
    - **`editarUsuario()`:** Actualizado para permitir la edición de los nuevos atributos de `Usuario` (deuda, sede, carrera).
    - **`eliminarUsuario()`:** Ahora verifica que el usuario **no tenga libros prestados y no tenga `deuda > 0`** antes de permitir la eliminación.
5.  **`Biblioteca.java` (Modificada):**
    - **Constantes:** `MULTA_POR_DIA` (4.750) y `DIAS_MAX_PRESTAMO` (30 días).
    - **`prestarLibro()`:** Configura la `fechaDevolucionEstimada` a `DIAS_MAX_PRESTAMO` días después. También valida si el usuario tiene deuda.
    - **`multar()`:** Calcula la multa basándose en `MULTA_POR_DIA` por cada día de retraso (usando `Fecha.getDaysBetween()`) y **suma este monto a la `deuda` del `Usuario`**.
6.  **`Prestamo.java` (Modificada):**
    - Todas las operaciones de fecha (`formatDate`, `parseDate`) ahora usan los métodos estáticos de la nueva clase `Fecha`.
    - Se utiliza el archivo `LibroPrestado.txt` para persistencia.
7.  **`SistemaBiblioteca.java` (Modificada significativamente):**
    - **`main()`:** Se simplificó para llamar a `setupInitialData()` y luego al flujo de `login()`.
    - **`setupInitialData()` (Nueva):**
      - Asegura que `Bibliotecas.txt` exista con datos de ejemplo si no lo hace.
      - Verifica si ya existe un `Admin`; si no, **crea un admin aleatorio y imprime su usuario y contraseña en consola** para que puedas iniciar sesión.
      - Crea 3 `Usuario` aleatorios (con roles variados: estudiante, profesor, administrativo) si no hay suficientes usuarios, y también imprime sus credenciales (para pruebas).
      - Este método es clave para que puedas empezar a probar el sistema sin tener que crear los archivos manualmente cada vez.
    - **`login()`:** Se actualizó para manejar correctamente la instanciación de `Admin` o `Usuario`, leyendo todos los campos (`deuda`, `sedeUniversidad`, `carrera`) cuando corresponda.
    - **Menús (`mostrarMenuAdmin`, `mostrarMenuUsuario`, `menuAdminLibros`, `menuAdminUsuarios`):** Adaptados para reflejar los nuevos métodos, pedir los campos adicionales, y mostrar la deuda del usuario en su menú principal.
    - Los métodos para cambiar información personal y contraseña fueron ajustados para recibir los parámetros correctos y para mostrar mensajes informativos.

---

### **Código Completo Actualizado:**

Por favor, asegúrate de que todos estos archivos `.java` estén en el paquete `com.mycompany.sistemabiblioteca` dentro de tu estructura de proyecto `src/main/java/`. Los archivos `.txt` deben estar en `src/main/resources/`.

**1. `Fecha.java`**

```java
package com.mycompany.sistemabiblioteca;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Clase de utilidad para el manejo de fechas, adaptada para usar java.util.Date.
 * Proporciona métodos para formatear, parsear y realizar operaciones básicas con fechas.
 *
 * @author edrui (originalmente de Estudiante, adaptado)
 */
public class Fecha {
    // Definimos el SimpleDateFormat aquí para su uso consistente en todo el sistema.
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    // Los atributos dd, mm, aa del usuario original se eliminan ya que esta clase
    // ahora actúa como un utilitario para java.util.Date.

    // El constructor por defecto o con parámetros para dd,mm,aa no son necesarios
    // si esta clase es solo un utilitario con métodos estáticos.

    /**
     * Obtiene la fecha actual (hoy) sin información de hora, minutos, segundos o milisegundos.
     * @return Una instancia de Date representando el inicio del día actual.
     */
    public static Date getToday() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * Añade o resta un número de días a una fecha dada.
     * @param date La fecha base.
     * @param days El número de días a añadir (positivo) o restar (negativo).
     * @return La nueva fecha resultante.
     */
    public static Date addDays(Date date, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_MONTH, days);
        return cal.getTime();
    }

    /**
     * Calcula la diferencia en días entre dos fechas.
     * @param date1 La primera fecha (normalmente la más reciente).
     * @param date2 La segunda fecha (normalmente la más antigua).
     * @return El número de días entre date1 y date2. Positivo si date1 es posterior a date2.
     *         Retorna 0 si las fechas son iguales o date1 es anterior a date2 (si se quiere solo días de retraso).
     */
    public static long getDaysBetween(Date date1, Date date2) {
        // Asegurarse de comparar solo el día, ignorando la hora
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        cal1.set(Calendar.HOUR_OF_DAY, 0);
        cal1.set(Calendar.MINUTE, 0);
        cal1.set(Calendar.SECOND, 0);
        cal1.set(Calendar.MILLISECOND, 0);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        cal2.set(Calendar.HOUR_OF_DAY, 0);
        cal2.set(Calendar.MINUTE, 0);
        cal2.set(Calendar.SECOND, 0);
        cal2.set(Calendar.MILLISECOND, 0);

        long diffMillis = cal1.getTimeInMillis() - cal2.getTimeInMillis();
        long diffDays = diffMillis / (1000 * 60 * 60 * 24);
        return diffDays;
    }

    /**
     * Formatea una instancia de {@code Date} a una cadena de texto en el formato "yyyy-MM-dd".
     * Si la fecha es {@code null}, retorna "null" (para persistencia).
     * @param date La fecha a formatear.
     * @return La fecha formateada como {@code String} o "null".
     */
    public static String formatDate(Date date) {
        if (date == null) {
            return "null"; // Consistent with how we save null dates in Prestamo
        }
        return DATE_FORMAT.format(date);
    }

    /**
     * Parsea una cadena de texto en formato "yyyy-MM-dd" a una instancia de {@code Date}.
     * Si la cadena es "null" o vacía, retorna {@code null}.
     * @param dateString La cadena de texto de la fecha.
     * @return La instancia de {@code Date} o {@code null} si la cadena es "null" o vacía.
     * @throws ParseException Si la cadena no puede ser parseada al formato esperado.
     */
    public static Date parseDate(String dateString) throws ParseException {
        if (dateString == null || dateString.trim().isEmpty() || dateString.equalsIgnoreCase("null")) {
            return null;
        }
        return DATE_FORMAT.parse(dateString);
    }
}
```

**2. `Usuario.java`**

```java
package com.mycompany.sistemabiblioteca;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class Usuario extends Miembro {

    private double deuda;
    private String sedeUniversidad; // Renamed to clarify: sede de la universidad del usuario
    private String carrera;

    public Usuario() {
        super();
        this.deuda = 0.0;
        this.sedeUniversidad = "";
        this.carrera = "";
    }

    public Usuario(byte tipoDoc, long noDoc, byte rol, String nombre, String apellido, String usuario, String contrasena,
                   double deuda, String sedeUniversidad, String carrera) {
        super(tipoDoc, noDoc, rol, nombre, apellido, usuario, contrasena);
        // Asegúrate de que el rol no sea 0 (Admin)
        if (rol == 0) {
            System.err.println("Advertencia: Se intentó crear un Usuario con rol de Administrador. Estableciendo rol a Estudiante (1).");
            this.rol = 1; // Default to student if trying to create admin as user
        }
        this.deuda = deuda;
        this.sedeUniversidad = sedeUniversidad;
        this.carrera = carrera;
    }

    // Constructor para cuando se carga desde Miembros.txt y no tiene los atributos extra.
    // Se usará al cargar un Miembro y luego castearlo si es Usuario
    public Usuario(Miembro miembroBase, double deuda, String sedeUniversidad, String carrera) {
        super(miembroBase.getTipoDoc(), miembroBase.getNoDoc(), miembroBase.getRol(),
              miembroBase.getNombre(), miembroBase.getApellido(), miembroBase.getUsuario(), miembroBase.getContrasena());
        this.deuda = deuda;
        this.sedeUniversidad = sedeUniversidad;
        this.carrera = carrera;
    }


    // Getters para nuevos atributos
    public double getDeuda() {
        return deuda;
    }

    public String getSedeUniversidad() {
        return sedeUniversidad;
    }

    public String getCarrera() {
        return carrera;
    }

    // Setters para nuevos atributos (que invocan save() para persistir cambios)
    public void setDeuda(double deuda) {
        this.deuda = deuda;
        save();
    }

    public void setSedeUniversidad(String sedeUniversidad) {
        this.sedeUniversidad = sedeUniversidad;
        save();
    }

    public void setCarrera(String carrera) {
        this.carrera = carrera;
        save();
    }

    // Override cambiarInfoPersonal para incluir los nuevos atributos
    // Ojo: El rol no se cambia aquí, solo en el admin. La deuda no se cambia aquí.
    public void cambiarInfoPersonal(byte newTipoDoc, String newNombre, String newApellido, String newSedeUniversidad, String newCarrera) {
        super.cambiarInfoPersonal(newTipoDoc, newNombre, newApellido); // Llama al método del padre
        this.setSedeUniversidad(newSedeUniversidad);
        this.setCarrera(newCarrera);
        // Los setters individuales ya llaman a save().
    }

    /**
     * Permite al usuario ver la lista de libros que tiene prestados actualmente.
     * Corregido para usar `Fecha.formatDate`.
     *
     * @return Una lista de objetos Prestamo que el usuario tiene activos.
     */
    public List<Prestamo> verMisLibrosPrestados() {
        System.out.println("\n--- MIS LIBROS PRESTADOS (" + this.getNombre() + " " + this.getApellido() + ") ---");
        List<Prestamo> misPrestamos = Prestamo.findPrestamosByMember(this.getNoDoc());

        if (misPrestamos.isEmpty()) {
            System.out.println("No tienes libros prestados o devueltos.");
            return misPrestamos;
        } else {
            for (Prestamo prestamo : misPrestamos) {
                Libro libro = Libro.findBookByCode(prestamo.getCodigoLibro());
                String infoLibro = (libro != null) ? libro.getNombre() + " (Código: " + libro.getCodigo() + ")" : "Libro Desconocido";
                System.out.println("  - " + infoLibro + " | Fecha Préstamo: " + Fecha.formatDate(prestamo.getFechaPrestamo()) + " | Fecha Devolución Estimada: " + Fecha.formatDate(prestamo.getFechaDevolucionEstimada()) + " | Estado: " + prestamo.getEstado() + " | Biblioteca ID: " + prestamo.getIdBiblioteca());
            }
        }
        System.out.println("-----------------------------------------------------------------------------------\n");
        return misPrestamos;
    }

    // Este es el método "verListaLibros" solicitado
    public List<Prestamo> verListaLibros() {
        return verMisLibrosPrestados(); // Delega en el método ya existente
    }


    /**
     * Permite al usuario solicitar el préstamo de un libro.
     * La lógica de validación (límites de libros, disponibilidad, DEUDA) está en la clase Biblioteca.
     *
     * @param codigoLibro El código del libro que se desea prestar.
     * @param biblioteca  La instancia de la biblioteca desde la que se presta el libro.
     * @return Un mensaje de éxito o error del préstamo.
     */
    public String solicitarPrestamo(String codigoLibro, Biblioteca biblioteca) {
        if (this.deuda > 0) {
            return "Error: No puedes pedir prestado un libro nuevo. Tienes una deuda pendiente de " + String.format("%.2f", this.deuda) + " pesos.";
        }
        if (biblioteca == null) {
            return "Error: No se ha seleccionado una biblioteca para realizar el préstamo.";
        }
        System.out.println("Intentando prestar libro '" + codigoLibro + "' para " + this.getUsuario() + " desde biblioteca " + biblioteca.getNombreBiblioteca());
        return biblioteca.prestarLibro(this, codigoLibro);
    }

    /**
     * Permite al usuario devolver un libro que tiene prestado.
     * La lógica de actualización de estados y multas está en la clase Biblioteca.
     *
     * @param codigoLibro El código del libro que se desea devolver.
     * @param biblioteca  La instancia de la biblioteca a la que se devuelve el libro.
     * @return Un mensaje de éxito o error de la devolución.
     */
    public String realizarDevolucion(String codigoLibro, Biblioteca biblioteca) {
        if (biblioteca == null) {
            return "Error: No se ha seleccionado una biblioteca para realizar la devolución.";
        }
        System.out.println("Intentando devolver libro '" + codigoLibro + "' para " + this.getUsuario() + " en biblioteca " + biblioteca.getNombreBiblioteca());
        return biblioteca.devolverLibro(this, codigoLibro);
    }

    // Override de buildLine para incluir los nuevos atributos de Usuario
    @Override
    protected String buildLine() {
        // Llama al buildLine del padre y le concatena los nuevos atributos
        return super.buildLine() + "\\" +
               String.format("%.2f", this.deuda) + "\\" + // Formato de deuda
               (this.sedeUniversidad == null ? "" : this.sedeUniversidad) + "\\" +
               (this.carrera == null ? "" : this.carrera);
    }

    // El toString sobrescrito en la versión anterior es bueno y se mantiene.
    @Override
    public String toString() {
        String rolText;
        switch (this.rol) {
            case 1: rolText = "Estudiante"; break;
            case 2: rolText = "Profesor"; break;
            case 3: rolText = "Administrativo Universidad"; break;
            default: rolText = "Usuario (Rol desconocido: " + this.rol + ")"; break;
        }
        return "Usuario {"
                + "\n  Tipo de Documento: " + tipoDoc
                + "\n  Número de Documento: " + noDoc
                + "\n  Rol: " + rolText
                + "\n  Nombre: " + nombre
                + "\n  Apellido: " + apellido
                + "\n  Usuario: " + usuario
                + "\n  Deuda: " + String.format("%.2f", deuda) + " pesos"
                + "\n  Sede Universidad: " + sedeUniversidad
                + "\n  Carrera: " + carrera
                + "\n}";
    }
}
```

**3. `Miembro.java`**

```java
package com.mycompany.sistemabiblioteca;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author edrui
 */
public class Miembro {

    protected long noDoc;
    protected byte tipoDoc;
    protected byte rol; // 0: Admin, 1: Estudiante, 2: Profesor, 3: Administrativo
    protected String nombre;
    protected String apellido;
    protected String usuario;
    protected String contrasena;

    public Miembro() {
    }

    public Miembro(byte tipoDoc, long noDoc, byte rol, String nombre, String apellido, String usuario, String contrasena) {
        this.tipoDoc = tipoDoc;
        this.noDoc = noDoc;
        this.rol = rol;
        this.nombre = nombre;
        this.apellido = apellido;
        this.usuario = usuario;
        // Hashea la contraseña antes de almacenarla
        String hashed = hashPassword(contrasena);
        if (hashed != null && !hashed.isEmpty()) {
            this.contrasena = hashed;
        } else {
            // Si el hash falla, conserva la contraseña sin modificar (fallback)
            this.contrasena = contrasena;
        }
    }

    public long getNoDoc() {
        return noDoc;
    }

    public void setNoDoc(long noDoc) {
        long oldNoDoc = this.noDoc;
        this.noDoc = noDoc;
        saveInternal(null, oldNoDoc); // saveInternal is designed to handle changes to noDoc
    }

    public byte getTipoDoc() {
        return tipoDoc;
    }

    public void setTipoDoc(byte tipoDoc) {
        this.tipoDoc = tipoDoc;
        save();
    }

    public byte getRol() {
        return rol;
    }

    public void setRol(byte rol) {
        this.rol = rol;
        save();
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
        save();
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
        save();
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        String oldUsuario = this.usuario;
        this.usuario = usuario;
        // If new usuario is null/empty => delete the record (if exists).
        // Otherwise update or append.
        saveInternal(oldUsuario, this.noDoc);
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        // Hashea antes de almacenar
        String hashed = hashPassword(contrasena);
        if (hashed != null && !hashed.isEmpty()) {
            this.contrasena = hashed;
            save();
        } else {
            // fallback: no se guarda si falla el hash
            this.contrasena = contrasena; // Keep original if hash fails, maybe log error
            save();
        }
    }

    public void cambiarInfoPersonal(byte newTipoDoc, String newNombre, String newApellido) {
        this.setTipoDoc(newTipoDoc);
        this.setNombre(newNombre);
        this.setApellido(newApellido);
        // The individual setters already call save()
    }

    public void cambiarContrasena(String nuevaContrasena) {
        String hashed = hashPassword(nuevaContrasena);
        if (hashed != null && !hashed.isEmpty()) {
            this.contrasena = hashed;
            save();
        }
    }

    // Guarda o actualiza el registro de este Miembro en el archivo Miembros.txt.
    // Si usuario es null/empty, elimina la entrada correspondiente.
    public synchronized void save() {
        saveInternal(null, this.noDoc);
    }

    // Guarda/actualiza utilizando información extra sobre usuario antiguo o nro documento antiguo.
    // oldUsuario puede ser null; oldNoDoc se usa como respaldo para encontrar la línea si oldUsuario es null/empty.
    protected synchronized void saveInternal(String oldUsuario, long oldNoDoc) {
        try {
            Path path = resolveMiembrosPath();
            List<String> lines = new ArrayList<>();
            if (Files.exists(path)) {
                lines = new ArrayList<>(Files.readAllLines(path, java.nio.charset.StandardCharsets.UTF_8));
            }

            // Normaliza las líneas (quita líneas vacías)
            List<String> normalized = new ArrayList<>();
            for (String l : lines) {
                if (l != null && !l.trim().isEmpty()) normalized.add(l);
            }
            lines = normalized;

            boolean found = false;
            // Buscamos coincidencias por usuario (preferible) o por número de documento
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                String[] parts = line.split("\\\\");
                if (parts.length >= 7) { // Mínimo 7 partes para un Miembro base
                    String usuarioLinea = parts[5].trim();
                    long noDocLinea = -1;
                    try {
                        noDocLinea = Long.parseLong(parts[0].trim());
                    } catch (NumberFormatException ex) {
                        // ignorar parseo si falla
                    }
                    boolean match = false;
                    if (oldUsuario != null && !oldUsuario.trim().isEmpty()) {
                        match = usuarioLinea.equals(oldUsuario.trim());
                    } else if (this.usuario != null && !this.usuario.trim().isEmpty()) {
                        // si no tenemos oldUsuario, intentar emparejar por el usuario actual (actualización directa)
                        match = usuarioLinea.equals(this.usuario.trim());
                    } else {
                        // si usuario actual es vacío/nulo, intentar emparejar por documento antiguo
                        match = (noDocLinea == oldNoDoc);
                    }

                    if (match) {
                        // Si el miembro actual tiene usuario vacío -> eliminar la línea
                        if (this.usuario == null || this.usuario.trim().isEmpty()) {
                            lines.remove(i);
                            i--; // ajustar índice tras eliminación
                        } else {
                            // Reemplazar línea por la representación actual.
                            // Aquí se usa this.buildLine(), que será el de Usuario si this es un Usuario.
                            String nuevaLinea = this.buildLine();
                            lines.set(i, nuevaLinea);
                        }
                        found = true;
                        break;
                    }
                }
            }

            if (!found) {
                // Si no se encontró y usuario no está vacío -> agregar al final
                if (this.usuario != null && !this.usuario.trim().isEmpty()) {
                    lines.add(this.buildLine()); // Usa this.buildLine()
                }
                // Si no se encontró y usuario está vacío -> nada que eliminar
            }

            // Escribir archivo sin líneas vacías
            List<String> toWrite = new ArrayList<>();
            for (String l : lines) {
                if (l != null && !l.trim().isEmpty()) toWrite.add(l);
            }
            // Asegura que el directorio exista
            Path parent = path.getParent();
            if (parent != null && !Files.exists(parent)) {
                try {
                    Files.createDirectories(parent);
                } catch (IOException ex) {
                    System.err.println("Error creating directory for Miembros.txt: " + ex.getMessage());
                }
            }
            Files.write(path, toWrite, java.nio.charset.StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // Construye la representación en el formato esperado por login: noDoc\tipoDoc\rol\nombre\apellido\ usuario\contrasena
    // Este método es protegido para que las subclases puedan extenderlo.
    protected String buildLine() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.noDoc).append("\\")
                .append(this.tipoDoc).append("\\")
                .append(this.rol).append("\\")
                .append(this.nombre == null ? "" : this.nombre).append("\\")
                .append(this.apellido == null ? "" : this.apellido).append("\\")
                .append(this.usuario == null ? "" : this.usuario).append("\\")
                .append(this.contrasena == null ? "" : this.contrasena);
        return sb.toString();
    }

    // Intenta resolver la ruta del recurso Miembros.txt en el sistema de archivos.
    // Si el recurso está empaquetado (p. ej. dentro de un JAR) o no se encuentra,
    // utiliza el archivo "Miembros.txt" en el directorio de trabajo.
    protected static Path resolveMiembrosPath() {
        try {
            java.net.URL res = Miembro.class.getClassLoader().getResource("Miembros.txt");
            if (res != null && "file".equalsIgnoreCase(res.getProtocol())) {
                try {
                    return Paths.get(res.toURI());
                } catch (Exception ex) {
                    // fallback a user.dir
                }
            }
        } catch (Exception ex) {
            // ignore and fallback
        }
        return Paths.get(System.getProperty("user.dir"), "Miembros.txt");
    }

    public String login(String usuarioIngresado, String contrasenaIngresada) {
        Path path = resolveMiembrosPath();
        if (!Files.exists(path)) {
            System.err.println("Advertencia: Archivo Miembros.txt no encontrado en " + path.toAbsolutePath());
            return ""; // File doesn't exist
        }

        try (BufferedReader reader = Files.newBufferedReader(path, java.nio.charset.StandardCharsets.UTF_8)) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                // Separa por '\' (en Java hay que escapar la barra invertida)
                String[] parts = linea.split("\\\\");
                // Según el orden: noDoc(0), tipoDoc(1), rol(2), nombre(3), apellido(4), usuario(5), contrasena(6)
                if (parts.length >= 7) {
                    String usuario = parts[5].trim();
                    String contrasenaAlmacenada = parts[6].trim();
                    if (usuario.equals(usuarioIngresado.trim()) && verifyPassword(contrasenaIngresada, contrasenaAlmacenada)) {
                        return linea; // devuelve la línea completa que coincide
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    // --- Static methods for loading members ---
    public static List<Miembro> loadAllMembers() {
        List<Miembro> miembros = new ArrayList<>();
        Path path = resolveMiembrosPath();
        if (!Files.exists(path)) {
            return miembros;
        }

        try (BufferedReader reader = Files.newBufferedReader(path, java.nio.charset.StandardCharsets.UTF_8)) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] parts = linea.split("\\\\");
                // Mínimo 7 partes para un Miembro base. Un Usuario tendrá más (mínimo 10).
                if (parts.length >= 7) {
                    try {
                        long noDoc = Long.parseLong(parts[0].trim());
                        byte tipoDoc = Byte.parseByte(parts[1].trim());
                        byte rol = Byte.parseByte(parts[2].trim());
                        String nombre = parts[3].trim();
                        String apellido = parts[4].trim();
                        String usuario = parts[5].trim();
                        String contrasena = parts[6].trim(); // This is the hashed password

                        if (rol == 0) {
                            miembros.add(new Admin(tipoDoc, noDoc, rol, nombre, apellido, usuario, contrasena));
                        } else {
                            // Si es un Usuario, intentamos leer los campos adicionales
                            double deuda = 0.0;
                            String sedeUniversidad = "";
                            String carrera = "";
                            // Un Usuario debe tener al menos 10 partes si se guardan todos los campos.
                            if (parts.length >= 10) {
                                try {
                                    deuda = Double.parseDouble(parts[7].trim());
                                    sedeUniversidad = parts[8].trim();
                                    carrera = parts[9].trim();
                                } catch (NumberFormatException e) {
                                    System.err.println("Error parsing Usuario specific fields in line: " + linea + " - " + e.getMessage());
                                    // Fallback to default values if parsing fails
                                }
                            }
                            miembros.add(new Usuario(tipoDoc, noDoc, rol, nombre, apellido, usuario, contrasena, deuda, sedeUniversidad, carrera));
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing member line: " + linea + " - " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return miembros;
    }

    public static Miembro findMemberByUsername(String username) {
        List<Miembro> allMembers = loadAllMembers();
        for (Miembro miembro : allMembers) {
            if (miembro.getUsuario().equalsIgnoreCase(username.trim())) {
                return miembro;
            }
        }
        return null;
    }

    public static Miembro findMemberByNoDoc(long noDoc) {
        List<Miembro> allMembers = loadAllMembers();
        for (Miembro miembro : allMembers) {
            if (miembro.getNoDoc() == noDoc) {
                return miembro;
            }
        }
        return null;
    }


    // --- Helpers para hashing/verificación PBKDF2 ---
    // Genera un hash seguro para la contraseña usando PBKDF2WithHmacSHA256.
    // Devuelve una cadena con el formato: iteraciones:saltBase64:hashBase64
    private String hashPassword(String password) {
        try {
            // Número de iteraciones del algoritmo (mayor => más costoso de calcular)
            int iterations = 65536;
            // Longitud de la clave en bits
            int keyLength = 256;
            // Genera un salt aleatorio de 16 bytes
            byte[] salt = new byte[16];
            new java.security.SecureRandom().nextBytes(salt);

            // Calcula el hash PBKDF2 con los parámetros anteriores
            byte[] hash = pbkdf2(password.toCharArray(), salt, iterations, keyLength);
            if (hash == null) return null;

            // Codifica salt y hash en Base64 para almacenarlos como texto
            String saltB64 = Base64.getEncoder().encodeToString(salt);
            String hashB64 = Base64.getEncoder().encodeToString(hash);
            // Devuelve la representación que incluye iteraciones, salt y hash
            return iterations + ":" + saltB64 + ":" + hashB64;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    // Verifica si la contraseña indicada coincide con la representación almacenada.
    // Espera que 'stored' tenga el formato iteraciones:saltBase64:hashBase64
    private boolean verifyPassword(String password, String stored) {
        try {
            // Separa la cadena almacenada en sus partes
            String[] parts = stored.split(":");
            if (parts.length != 3) return false;
            // Recupera las iteraciones y decodifica el salt y el hash almacenado
            int iterations = Integer.parseInt(parts[0]);
            byte[] salt = Base64.getDecoder().decode(parts[1]);
            byte[] hashStored = Base64.getDecoder().decode(parts[2]);

            // Calcula el hash de intento con los mismos parámetros (longitud en bits)
            byte[] hashAttempt = pbkdf2(password.toCharArray(), salt, iterations, hashStored.length * 8);
            if (hashAttempt == null) return false;

            // Comparación en tiempo-constante para evitar ataques por cronometraje
            int diff = hashStored.length ^ hashAttempt.length;
            for (int i = 0; i < Math.min(hashStored.length, hashAttempt.length); i++) {
                diff |= hashStored[i] ^ hashAttempt[i];
            }
            return diff == 0;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // Implementación de PBKDF2 que devuelve los bytes derivados.
    // Parámetros:
    //  - password: contraseña como array de caracteres
    //  - salt: salt aleatorio
    //  - iterations: número de iteraciones del algoritmo
    //  - keyLength: longitud de la clave en bits
    private byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLength) {
        try {
            // PBEKeySpec construye la especificación con los parámetros dados
            javax.crypto.spec.PBEKeySpec spec = new javax.crypto.spec.PBEKeySpec(password, salt, iterations, keyLength);
            // Obtiene la fábrica de claves para PBKDF2 con SHA-256
            javax.crypto.SecretKeyFactory skf = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            // Genera la clave derivada y devuelve sus bytes
            return skf.generateSecret(spec).getEncoded();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        return "Miembro {"
                + "\n  Tipo de Documento: " + tipoDoc
                + "\n  Número de Documento: " + noDoc
                + "\n  Rol: " + rol
                + "\n  Nombre: " + nombre
                + "\n  Apellido: " + apellido
                + "\n  Usuario: " + usuario
                + "\n  Contraseña: " + (contrasena != null && !contrasena.isEmpty() ? "[HASHED]" : "[EMPTY]") // Avoid printing raw hash
                + "\n}";
    }
}
```

**4. `Admin.java`**

```java
package com.mycompany.sistemabiblioteca;

import java.util.List;
import java.util.Scanner;
import java.util.Date;
import java.util.stream.Collectors;

public class Admin extends Miembro {

    public Admin() {
        super();
        this.rol = 0; // Asegura que el rol sea Admin
    }

    public Admin(byte tipoDoc, long noDoc, byte rol, String nombre, String apellido, String usuario, String contrasena) {
        super(tipoDoc, noDoc, (byte) 0, nombre, apellido, usuario, contrasena); // Rol siempre 0 para Admin
    }

    // --- Métodos de Administración de Libros ---

    // Este es el método "agregarLibro" solicitado
    public void agregarLibro(String nombre, String autor, String codigo, int totalUnidades, int idBiblioteca, String sedeBiblioteca) {
        if (Libro.findBookByCode(codigo) != null) {
            System.out.println("Error: Ya existe un libro con el código " + codigo + ".");
            return;
        }
        Libro nuevoLibro = new Libro(nombre, autor, codigo, totalUnidades, idBiblioteca, sedeBiblioteca);
        nuevoLibro.save();
        System.out.println("Libro '" + nombre + "' agregado con éxito.");
    }

    // Este es el método "editarLibro" solicitado
    public void editarLibro(String codigoLibro, String nuevoNombre, String nuevoAutor, int nuevasUnidadesTotales, int nuevaIdBiblioteca, String nuevaSedeBiblioteca) {
        Libro libro = Libro.findBookByCode(codigoLibro);
        if (libro == null) {
            System.out.println("Error: Libro con código " + codigoLibro + " no encontrado.");
            return;
        }

        int unidadesPrestadasActuales = libro.getUnPrestadas();
        if (nuevasUnidadesTotales < unidadesPrestadasActuales) {
            System.out.println("Error: Las nuevas unidades totales (" + nuevasUnidadesTotales + ") no pueden ser menores que las unidades actualmente prestadas (" + unidadesPrestadasActuales + ").");
            return;
        }

        libro.setNombre(nuevoNombre);
        libro.setAutor(nuevoAutor);
        libro.setUnLibres(nuevasUnidadesTotales - unidadesPrestadasActuales); // Recalcular unidades libres
        libro.setIdBiblioteca(nuevaIdBiblioteca);
        libro.setSedeBiblioteca(nuevaSedeBiblioteca);

        // Las llamadas a los setters ya invocan save(), pero una llamada final asegura la consistencia.
        libro.save();
        System.out.println("Libro '" + codigoLibro + "' actualizado con éxito.");
    }

    // Este es el método "eliminarLibro" solicitado, con la lógica de unidades prestadas.
    // "Puede eliminar los libros que no están prestados, si están prestados se eliminan los no prestados,
    // y las copias que tiene el usuario se conservan aún pero los que no han prestado se eliminan"
    public void eliminarLibro(String codigoLibro, int unidadesAeliminar) {
        Libro libro = Libro.findBookByCode(codigoLibro);
        if (libro == null) {
            System.out.println("Error: Libro con código " + codigoLibro + " no encontrado.");
            return;
        }

        if (unidadesAeliminar <= 0) {
            System.out.println("Error: El número de unidades a eliminar debe ser mayor que 0.");
            return;
        }

        // Si se intenta eliminar más unidades de las libres, ajustamos a las libres
        if (unidadesAeliminar > libro.getUnLibres()) {
            System.out.println("Advertencia: Se intenta eliminar " + unidadesAeliminar + " unidades, pero solo hay " + libro.getUnLibres() + " unidades libres. Se eliminarán solo las unidades libres.");
            unidadesAeliminar = libro.getUnLibres();
        }

        if (unidadesAeliminar == 0 && libro.getUnPrestadas() > 0) {
            System.out.println("No hay unidades libres para eliminar del libro '" + libro.getNombre() + "'. Las unidades prestadas (" + libro.getUnPrestadas() + ") no se pueden eliminar hasta que sean devueltas.");
            return;
        }

        if (unidadesAeliminar > 0) {
            libro.setUnLibres(libro.getUnLibres() - unidadesAeliminar);
            // Si todas las unidades (libres y prestadas) se eliminaran y quedan 0, entonces eliminar el registro completo del libro.
            // La lógica "las copias que tiene el usuario se conservan aún" implica que no debemos eliminar el registro del libro
            // si unPrestadas es > 0, solo reducir las unLibres. Si unPrestadas es 0 y unLibres llega a 0, entonces sí.
            if (libro.getUnLibres() == 0 && libro.getUnPrestadas() == 0) {
                libro.delete(); // Esto elimina el registro del archivo
                System.out.println("Libro '" + libro.getNombre() + "' completamente eliminado (todas las unidades).");
            } else {
                libro.save(); // Solo actualiza las unidades
                System.out.println(unidadesAeliminar + " unidades del libro '" + libro.getNombre() + "' eliminadas con éxito. Quedan " + libro.getUnLibres() + " libres y " + libro.getUnPrestadas() + " prestadas.");
            }
        } else {
             System.out.println("No se realizaron cambios en las unidades del libro '" + libro.getNombre() + "'.");
        }
    }

    // Sobrecarga para eliminar todas las unidades libres si no se especifica cantidad
    public void eliminarLibro(String codigoLibro) {
        Libro libro = Libro.findBookByCode(codigoLibro);
        if (libro == null) {
            System.out.println("Error: Libro con código " + codigoLibro + " no encontrado.");
            return;
        }
        // Llamar al método con la cantidad de unidades libres
        eliminarLibro(codigoLibro, libro.getUnLibres());
    }

    // --- Métodos de Administración de Usuarios ---

    // Este es el método "crearUsuario" solicitado
    public void crearUsuario(byte tipoDoc, long noDoc, byte rol, String nombre, String apellido, String usuario, String contrasena,
                             double deuda, String sedeUniversidad, String carrera) {
        if (Miembro.findMemberByUsername(usuario) != null) {
            System.out.println("Error: Ya existe un usuario con el nombre de usuario '" + usuario + "'.");
            return;
        }
        if (Miembro.findMemberByNoDoc(noDoc) != null) {
             System.out.println("Error: Ya existe un usuario con el número de documento '" + noDoc + "'.");
             return;
        }
        // Creamos una instancia de Usuario para que los atributos específicos se guarden correctamente
        Usuario nuevoUsuario = new Usuario(tipoDoc, noDoc, rol, nombre, apellido, usuario, contrasena, deuda, sedeUniversidad, carrera);
        nuevoUsuario.save();
        System.out.println("Usuario '" + usuario + "' agregado con éxito (Rol: " + rol + ").");
    }

    // Este es el método "editarUsuario" solicitado
    public void editarUsuario(String usuarioExistente, byte nuevoTipoDoc, long nuevoNoDoc, byte nuevoRol, String nuevoNombre, String nuevoApellido, String nuevoUsuario, String nuevaContrasena,
                              double nuevaDeuda, String nuevaSedeUniversidad, String nuevaCarrera) {
        Miembro miembroBase = Miembro.findMemberByUsername(usuarioExistente);
        if (miembroBase == null) {
            System.out.println("Error: Usuario '" + usuarioExistente + "' no encontrado.");
            return;
        }

        // Si el usuario es un Admin, no debe tener campos de deuda, sede, carrera.
        if (miembroBase.getRol() == 0 && nuevoRol != 0) {
            System.out.println("Advertencia: Se está intentando cambiar el rol de un Admin a un rol de usuario normal. Los campos específicos de usuario se aplicarán.");
        } else if (miembroBase.getRol() != 0 && nuevoRol == 0) {
            System.out.println("Advertencia: Se está intentando cambiar el rol de un usuario normal a Admin. Los campos específicos de usuario serán ignorados en esta instancia.");
        }


        // Verificar si el nuevo usuario ya existe y no es el mismo
        if (!usuarioExistente.equalsIgnoreCase(nuevoUsuario) && Miembro.findMemberByUsername(nuevoUsuario) != null) {
            System.out.println("Error: El nuevo nombre de usuario '" + nuevoUsuario + "' ya está en uso.");
            return;
        }

        // Verificar si el nuevo número de documento ya existe y no es el mismo
        if (miembroBase.getNoDoc() != nuevoNoDoc && Miembro.findMemberByNoDoc(nuevoNoDoc) != null) {
             System.out.println("Error: El nuevo número de documento '" + nuevoNoDoc + "' ya está en uso.");
             return;
        }

        miembroBase.setTipoDoc(nuevoTipoDoc);
        miembroBase.setNoDoc(nuevoNoDoc); // Esto llama a saveInternal() en Miembro
        miembroBase.setRol(nuevoRol); // El rol sí se puede cambiar por el admin
        miembroBase.setNombre(nuevoNombre);
        miembroBase.setApellido(nuevoApellido);
        miembroBase.setUsuario(nuevoUsuario); // Esto llama a saveInternal() en Miembro
        if (nuevaContrasena != null && !nuevaContrasena.isEmpty()) {
            miembroBase.setContrasena(nuevaContrasena);
        }

        // Campos específicos de Usuario (solo aplicar si el miembro es o se convierte en un Usuario)
        if (miembroBase.getRol() != 0) { // Si el rol final no es Admin, se trata como Usuario
            Usuario usuarioEdit;
            if (miembroBase instanceof Usuario) {
                usuarioEdit = (Usuario) miembroBase; // Si ya es Usuario, lo casteamos
            } else {
                // Si era Admin y su rol fue cambiado, creamos una nueva instancia de Usuario
                usuarioEdit = new Usuario(miembroBase.getTipoDoc(), miembroBase.getNoDoc(), miembroBase.getRol(), miembroBase.getNombre(), miembroBase.getApellido(), miembroBase.getUsuario(), miembroBase.getContrasena(), 0.0, "", "");
                System.out.println("El miembro fue convertido de Admin a Usuario. Se inicializan campos de Usuario.");
            }
            usuarioEdit.setDeuda(nuevaDeuda);
            usuarioEdit.setSedeUniversidad(nuevaSedeUniversidad);
            usuarioEdit.setCarrera(nuevaCarrera);
            usuarioEdit.save(); // Guarda el objeto Usuario completo
        } else {
             miembroBase.save(); // Si es Admin o se mantiene como Admin, solo guardar los campos base
        }

        System.out.println("Miembro '" + usuarioExistente + "' actualizado con éxito.");
    }

    // Este es el método "eliminarUsuario" solicitado
    public void eliminarUsuario(String usuarioAEliminar) {
        Miembro miembro = Miembro.findMemberByUsername(usuarioAEliminar);
        if (miembro == null) {
            System.out.println("Error: Usuario '" + usuarioAEliminar + "' no encontrado.");
            return;
        }

        // No se puede eliminar un Admin (rol 0) a menos que sea una función muy específica.
        // Asumimos que no se puede eliminar el propio admin o un admin sin otra lógica.
        if (miembro.getRol() == 0) {
            System.out.println("Error: No se puede eliminar a un usuario con rol de Administrador directamente.");
            return;
        }

        // Verificar si el usuario tiene libros prestados
        List<Prestamo> prestamosActivos = Prestamo.findPrestamosByMember(miembro.getNoDoc()).stream()
                                                    .filter(p -> "PRESTADO".equals(p.getEstado()))
                                                    .collect(Collectors.toList());
        if (!prestamosActivos.isEmpty()) {
            System.out.println("Error: No se puede eliminar al usuario '" + usuarioAEliminar + "' porque tiene " + prestamosActivos.size() + " libros prestados.");
            return;
        }

        // Verificar si el usuario tiene deuda
        if (miembro instanceof Usuario) { // Asegurarse de que es un objeto Usuario para acceder a la deuda
            Usuario user = (Usuario) miembro;
            if (user.getDeuda() > 0) {
                System.out.println("Error: No se puede eliminar al usuario '" + usuarioAEliminar + "' porque tiene una deuda pendiente de " + String.format("%.2f", user.getDeuda()) + " pesos.");
                return;
            }
        }

        miembro.setUsuario(null); // Marcar para eliminación (internamente en saveInternal, si usuario es null, se elimina)
        miembro.save(); // saveInternal() eliminará la línea
        System.out.println("Usuario '" + usuarioAEliminar + "' eliminado con éxito.");
    }

    // --- Métodos de Visualización ---

    // Este es el método "verUsuarios" solicitado (o "verTodosUsuarios")
    public void verTodosUsuarios() {
        System.out.println("\n--- LISTADO DE TODOS LOS MIEMBROS DEL SISTEMA (incluye Admins y Usuarios) ---");
        List<Miembro> miembros = Miembro.loadAllMembers();
        if (miembros.isEmpty()) {
            System.out.println("No hay miembros registrados en el sistema.");
        } else {
            miembros.forEach(System.out::println); // toString() de cada clase se encargará del formato
        }
        System.out.println("-------------------------------------------------------------------\n");
    }

    // Este es el método "verLibros" solicitado (o "verTodosLibros")
    public void verTodosLibros() {
        System.out.println("\n--- LISTADO DE TODOS LOS LIBROS ---");
        List<Libro> libros = Libro.loadAllBooks();
        if (libros.isEmpty()) {
            System.out.println("No hay libros registrados en el sistema.");
        } else {
            libros.forEach(System.out::println);
        }
        System.out.println("-----------------------------------\n");
    }

    // Este es el método "verLibro" solicitado (para un libro específico)
    public void verLibro(String codigoLibro) {
        Libro libro = Libro.findBookByCode(codigoLibro);
        if (libro == null) {
            System.out.println("Error: Libro con código " + codigoLibro + " no encontrado.");
            return;
        }
        System.out.println("\n--- INFORMACIÓN DEL LIBRO ---");
        System.out.println(libro);

        // También mostrar los préstamos asociados a este libro si es relevante para el admin
        List<Prestamo> prestamosLibro = Prestamo.loadAllPrestamos().stream()
                                            .filter(p -> p.getCodigoLibro().equals(codigoLibro))
                                            .collect(Collectors.toList());
        if (!prestamosLibro.isEmpty()) {
            System.out.println("\n--- PRÉSTAMOS ASOCIADOS AL LIBRO " + libro.getNombre() + " ---");
            for (Prestamo p : prestamosLibro) {
                Miembro m = Miembro.findMemberByNoDoc(p.getNoDocMiembro());
                System.out.println("  - Miembro: " + (m != null ? m.getNombre() + " " + m.getApellido() : "Desconocido") +
                                   ", Fecha Prestamo: " + Fecha.formatDate(p.getFechaPrestamo()) +
                                   ", Fecha Devolución Estimada: " + Fecha.formatDate(p.getFechaDevolucionEstimada()) +
                                   ", Estado: " + p.getEstado());
            }
        } else {
            System.out.println("No hay préstamos registrados para este libro.");
        }
        System.out.println("-----------------------------------\n");
    }


    public void verLibrosPrestados() {
        System.out.println("\n--- LIBROS ACTUALMENTE PRESTADOS ---");
        List<Prestamo> prestamos = Prestamo.loadAllPrestamos().stream()
                                    .filter(p -> "PRESTADO".equals(p.getEstado()))
                                    .collect(Collectors.toList());

        if (prestamos.isEmpty()) {
            System.out.println("No hay libros prestados actualmente.");
        } else {
            for (Prestamo prestamo : prestamos) {
                Miembro miembro = Miembro.findMemberByNoDoc(prestamo.getNoDocMiembro());
                Libro libro = Libro.findBookByCode(prestamo.getCodigoLibro());
                String infoMiembro = (miembro != null) ? miembro.getNombre() + " " + miembro.getApellido() + " (Usuario: " + miembro.getUsuario() + ")" : "Miembro Desconocido";
                String infoLibro = (libro != null) ? libro.getNombre() + " (Código: " + libro.getCodigo() + ")" : "Libro Desconocido";
                System.out.println("Prestamo: " + infoLibro + " a " + infoMiembro + " | Fecha Préstamo: " + Fecha.formatDate(prestamo.getFechaPrestamo()) + " | Fecha Devolución Estimada: " + Fecha.formatDate(prestamo.getFechaDevolucionEstimada()) + " | Biblioteca ID: " + prestamo.getIdBiblioteca());
            }
        }
        System.out.println("------------------------------------\n");
    }

    // Método para que el admin pueda ver los libros prestados por un usuario específico
    public void verLibrosPrestadosPorUsuario(long noDocMiembro) {
        Miembro miembro = Miembro.findMemberByNoDoc(noDocMiembro);
        if (miembro == null) {
            System.out.println("Error: Miembro con número de documento " + noDocMiembro + " no encontrado.");
            return;
        }

        System.out.println("\n--- LIBROS PRESTADOS POR " + miembro.getNombre().toUpperCase() + " " + miembro.getApellido().toUpperCase() + " ---");
        List<Prestamo> prestamosDelMiembro = Prestamo.findPrestamosByMember(noDocMiembro);

        if (prestamosDelMiembro.isEmpty()) {
            System.out.println(miembro.getNombre() + " no tiene ningún libro prestado o devuelto.");
        } else {
            for (Prestamo prestamo : prestamosDelMiembro) {
                Libro libro = Libro.findBookByCode(prestamo.getCodigoLibro());
                String infoLibro = (libro != null) ? libro.getNombre() + " (Código: " + libro.getCodigo() + ")" : "Libro Desconocido";
                System.out.println("  - " + infoLibro + " | Fecha Préstamo: " + Fecha.formatDate(prestamo.getFechaPrestamo()) + " | Fecha Devolución Estimada: " + Fecha.formatDate(prestamo.getFechaDevolucionEstimada()) + " | Estado: " + prestamo.getEstado());
            }
        }
        System.out.println("-------------------------------------------------------------------\n");
    }

    @Override
    public String toString() {
        return "Admin {"
                + "\n  " + super.toString().replace("Miembro {", "").replaceFirst("\\n}$", "") // Reemplazar el inicio y el final para integrar mejor
                + "\n}";
    }
}
```

**5. `Libro.java`**

```java
package com.mycompany.sistemabiblioteca;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID; // For generating unique codes if needed
import java.util.stream.Collectors;

/**
 * Clase para representar un libro en la biblioteca.
 *
 * @author edrui
 */
public class Libro {
    private String nombre;
    private String autor;
    private String codigo; // ID del libro
    private int unLibres; // Unidades libres disponibles
    private int unPrestadas; // Unidades actualmente prestadas
    private int idBiblioteca; // ID de la biblioteca a la que pertenece el libro
    private String sedeBiblioteca; // Sede de la biblioteca a la que pertenece el libro

    public Libro() {
    }

    // Constructor para la creación inicial de un libro
    public Libro(String nombre, String autor, String codigo, int unLibres, int idBiblioteca, String sedeBiblioteca) {
        this.nombre = nombre;
        this.autor = autor;
        this.codigo = codigo != null && !codigo.isEmpty() ? codigo : generateUniqueCode(); // Si no se provee, generar
        this.unLibres = unLibres;
        this.unPrestadas = 0; // Inicialmente 0 unidades prestadas
        this.idBiblioteca = idBiblioteca;
        this.sedeBiblioteca = sedeBiblioteca;
    }

    // Constructor para cargar un libro desde archivo (incluye unPrestadas)
    public Libro(String nombre, String autor, String codigo, int unLibres, int unPrestadas, int idBiblioteca, String sedeBiblioteca) {
        this.nombre = nombre;
        this.autor = autor;
        this.codigo = codigo;
        this.unLibres = unLibres;
        this.unPrestadas = unPrestadas;
        this.idBiblioteca = idBiblioteca;
        this.sedeBiblioteca = sedeBiblioteca;
    }


    // Método para generar un código único si es necesario
    private String generateUniqueCode() {
        return UUID.randomUUID().toString();
    }

    // Getters
    public String getNombre() {
        return nombre;
    }

    public String getAutor() {
        return autor;
    }

    public String getCodigo() {
        return codigo;
    }

    public int getUnLibres() {
        return unLibres;
    }

    public int getUnPrestadas() {
        return unPrestadas;
    }

    public int getIdBiblioteca() {
        return idBiblioteca;
    }

    public String getSedeBiblioteca() {
        return sedeBiblioteca;
    }

    // Setters (que invocan save() si modifican un atributo persistente)
    public void setNombre(String nombre) {
        this.nombre = nombre;
        save();
    }

    public void setAutor(String autor) {
        this.autor = autor;
        save();
    }

    // El código no debería cambiar una vez asignado, pero si fuera necesario:
    // Este setter tiene un manejo especial para la persistencia.
    public void setCodigo(String codigo) {
        String oldCodigo = this.codigo;
        this.codigo = codigo;
        saveInternal(oldCodigo); // Maneja el cambio de ID en el archivo
    }

    public void setUnLibres(int unLibres) {
        this.unLibres = unLibres;
        save();
    }

    public void setUnPrestadas(int unPrestadas) {
        this.unPrestadas = unPrestadas;
        save();
    }

    public void setIdBiblioteca(int idBiblioteca) {
        this.idBiblioteca = idBiblioteca;
        save();
    }

    public void setSedeBiblioteca(String sedeBiblioteca) {
        this.sedeBiblioteca = sedeBiblioteca;
        save();
    }

    // --- Persistence methods ---

    // Resuelve la ruta del archivo Libros.txt
    protected static Path resolveLibrosPath() {
        try {
            java.net.URL res = Libro.class.getClassLoader().getResource("Libros.txt");
            if (res != null && "file".equalsIgnoreCase(res.getProtocol())) {
                try {
                    return Paths.get(res.toURI());
                } catch (Exception ex) {
                    // fallback to user.dir
                }
            }
        } catch (Exception ex) {
            // ignore and fallback
        }
        return Paths.get(System.getProperty("user.dir"), "Libros.txt");
    }

    // Construye la línea de texto para guardar el libro
    private String buildLine() {
        return String.format("%s\\%s\\%s\\%d\\%d\\%d\\%s",
                codigo, nombre, autor, unLibres, unPrestadas, idBiblioteca, sedeBiblioteca);
    }

    // Guarda o actualiza el registro del libro.
    public synchronized void save() {
        saveInternal(null);
    }

    // Método interno para guardar/actualizar, maneja cambios de código o eliminaciones.
    // oldCodigo se usa cuando el código del libro ha cambiado o para encontrar la línea original.
    protected synchronized void saveInternal(String oldCodigo) {
        try {
            Path path = resolveLibrosPath();
            List<String> lines = new ArrayList<>();
            if (Files.exists(path)) {
                lines = new ArrayList<>(Files.readAllLines(path, java.nio.charset.StandardCharsets.UTF_8));
            }

            List<String> normalized = new ArrayList<>();
            for (String l : lines) {
                if (l != null && !l.trim().isEmpty()) normalized.add(l);
            }
            lines = normalized;

            boolean found = false;
            String searchCodigo = (oldCodigo != null && !oldCodigo.trim().isEmpty()) ? oldCodigo : this.codigo;

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                String[] parts = line.split("\\\\");
                if (parts.length >= 7) { // Asegura que haya suficientes partes para un libro
                    String libroCodigo = parts[0].trim();
                    if (libroCodigo.equals(searchCodigo)) {
                        if (this.codigo == null || this.codigo.trim().isEmpty()) { // Si el código es null, significa eliminación
                            lines.remove(i);
                            i--;
                        } else {
                            lines.set(i, buildLine()); // Actualizar la línea
                        }
                        found = true;
                        break;
                    }
                }
            }

            if (!found) {
                if (this.codigo != null && !this.codigo.trim().isEmpty()) {
                    lines.add(buildLine()); // Agregar nuevo libro
                }
            }

            Path parent = path.getParent();
            if (parent != null && !Files.exists(parent)) {
                try {
                    Files.createDirectories(parent);
                } catch (IOException ex) {
                    System.err.println("Error creating directory for Libros.txt: " + ex.getMessage());
                }
            }
            Files.write(path, lines, java.nio.charset.StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // Elimina el libro del archivo de persistencia.
    public synchronized void delete() {
        String originalCodigo = this.codigo; // Guardar el código original para la búsqueda
        this.codigo = null; // Marcar para eliminación en saveInternal
        saveInternal(originalCodigo); // Buscar el libro con el código original y eliminar
    }

    // Carga todos los libros del archivo Libros.txt
    public static List<Libro> loadAllBooks() {
        List<Libro> libros = new ArrayList<>();
        Path path = resolveLibrosPath();
        if (!Files.exists(path)) {
            return libros;
        }

        try (BufferedReader reader = Files.newBufferedReader(path, java.nio.charset.StandardCharsets.UTF_8)) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] parts = linea.split("\\\\");
                if (parts.length >= 7) {
                    try {
                        String codigo = parts[0].trim();
                        String nombre = parts[1].trim();
                        String autor = parts[2].trim();
                        int unLibres = Integer.parseInt(parts[3].trim());
                        int unPrestadas = Integer.parseInt(parts[4].trim());
                        int idBiblioteca = Integer.parseInt(parts[5].trim());
                        String sedeBiblioteca = parts[6].trim();

                        libros.add(new Libro(nombre, autor, codigo, unLibres, unPrestadas, idBiblioteca, sedeBiblioteca));
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing book line: " + linea + " - " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return libros;
    }

    // Encuentra un libro por su código.
    public static Libro findBookByCode(String code) {
        List<Libro> allBooks = loadAllBooks();
        for (Libro libro : allBooks) {
            if (libro.getCodigo().equalsIgnoreCase(code.trim())) {
                return libro;
            }
        }
        return null;
    }


    @Override
    public String toString() {
        return "Libro {" +
                "  Nombre='" + nombre + '\'' +
                ", Autor='" + autor + '\'' +
                ", Codigo='" + codigo + '\'' +
                ", Unidades Libres=" + unLibres +
                ", Unidades Prestadas=" + unPrestadas +
                ", ID Biblioteca=" + idBiblioteca +
                ", Sede Biblioteca='" + sedeBiblioteca + '\'' +
                '}';
    }
}
```

**6. `Biblioteca.java`**

```java
package com.mycompany.sistemabiblioteca;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author edrui
 */
public class Biblioteca {

    private int id;
    private String sede;
    private String nombreBiblioteca;

    // Constante para el valor de la multa por día
    public static final double MULTA_POR_DIA = 4.750;
    // Constante para el máximo de días de préstamo
    public static final int DIAS_MAX_PRESTAMO = 30;


    // Constructor para la clase Biblioteca
    public Biblioteca(int id, String sede, String nombreBiblioteca) {
        this.id = id;
        this.sede = sede;
        this.nombreBiblioteca = nombreBiblioteca;
    }

    public Biblioteca() {
        // Constructor vacío si se necesita para inicialización diferida o carga
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getSede() {
        return sede;
    }

    public String getNombreBiblioteca() {
        return nombreBiblioteca;
    }

    // Setters (no implican guardar directamente al archivo de bibliotecas, ya que este es más estático)
    public void setId(int id) {
        this.id = id;
    }

    public void setSede(String sede) {
        this.sede = sede;
    }

    public void setNombreBiblioteca(String nombreBiblioteca) {
        this.nombreBiblioteca = nombreBiblioteca;
    }

    // --- Persistence methods for Biblioteca itself (reading Bibliotecas.txt) ---
    // Resuelve la ruta del archivo Bibliotecas.txt
    protected static Path resolveBibliotecasPath() {
        try {
            java.net.URL res = Biblioteca.class.getClassLoader().getResource("Bibliotecas.txt");
            if (res != null && "file".equalsIgnoreCase(res.getProtocol())) {
                try {
                    return Paths.get(res.toURI());
                } catch (Exception ex) {
                    // fallback to user.dir
                }
            }
        } catch (Exception ex) {
            // ignore and fallback
        }
        return Paths.get(System.getProperty("user.dir"), "Bibliotecas.txt");
    }

    // Carga todas las bibliotecas del archivo Bibliotecas.txt
    public static List<Biblioteca> loadAllBibliotecas() {
        List<Biblioteca> bibliotecas = new ArrayList<>();
        Path path = resolveBibliotecasPath();
        if (!Files.exists(path)) {
            System.err.println("File not found: " + path.toAbsolutePath());
            return bibliotecas;
        }

        try (BufferedReader reader = Files.newBufferedReader(path, java.nio.charset.StandardCharsets.UTF_8)) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] parts = linea.split("\\\\");
                if (parts.length >= 3) { // id\sede\nombreBiblioteca
                    try {
                        int id = Integer.parseInt(parts[0].trim());
                        String sede = parts[1].trim();
                        String nombre = parts[2].trim();
                        bibliotecas.add(new Biblioteca(id, sede, nombre));
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing Biblioteca line: " + linea + " - " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bibliotecas;
    }

    // Encuentra una biblioteca por su ID.
    public static Biblioteca findBibliotecaById(int id) {
        return loadAllBibliotecas().stream()
                .filter(b -> b.getId() == id)
                .findFirst()
                .orElse(null);
    }

    // Encuentra una biblioteca por su sede.
    public static Biblioteca findBibliotecaBySede(String sede) {
        return loadAllBibliotecas().stream()
                .filter(b -> b.getSede().equalsIgnoreCase(sede))
                .findFirst()
                .orElse(null);
    }

    // Métodos de la funcionalidad de la biblioteca

    public void seleccionarSede() {
        System.out.println("Sedes de Bibliotecas disponibles:");
        List<Biblioteca> bibliotecas = loadAllBibliotecas();
        if (bibliotecas.isEmpty()) {
            System.out.println("No hay bibliotecas registradas.");
            return;
        }

        for (Biblioteca b : bibliotecas) {
            System.out.println("ID: " + b.getId() + ", Sede: " + b.getSede() + ", Nombre: " + b.getNombreBiblioteca());
        }

        System.out.println("La selección de la sede debe ser manejada por la lógica de la interfaz de usuario.");
        System.out.println("El objeto Biblioteca actual (this) representa la sede con ID: " + this.id + " y Sede: " + this.sede);
    }

    // Método para prestar un libro
    public String prestarLibro(Miembro miembro, String codigoLibro) {
        if (miembro == null) {
            return "Error: Miembro no válido.";
        }
        if (miembro.getRol() == 0) { // Admin no puede pedir libros para sí mismo como un usuario normal
             return "Error: Los administradores no pueden pedir libros prestados para sí mismos.";
        }

        // Verificar si el usuario tiene deuda pendiente
        if (miembro instanceof Usuario) {
            Usuario user = (Usuario) miembro;
            if (user.getDeuda() > 0) {
                return "Error: No puedes pedir prestado un libro. Tienes una deuda pendiente de " + String.format("%.2f", user.getDeuda()) + " pesos.";
            }
        }

        Libro libro = Libro.findBookByCode(codigoLibro);
        if (libro == null) {
            return "Error: Libro con código " + codigoLibro + " no encontrado.";
        }

        // Verificar que el libro pertenece a esta biblioteca
        if (libro.getIdBiblioteca() != this.id) {
            return "Error: El libro " + codigoLibro + " no se encuentra en esta biblioteca (" + this.nombreBiblioteca + ").";
        }

        if (libro.getUnLibres() <= 0) {
            return "Error: El libro '" + libro.getNombre() + "' no tiene unidades disponibles.";
        }

        List<Prestamo> prestamosActivosDelMiembro = Prestamo.findPrestamosByMember(miembro.getNoDoc()).stream()
                                                    .filter(p -> "PRESTADO".equals(p.getEstado()))
                                                    .collect(Collectors.toList());

        int limitePrestamos;
        String tipoMiembro;
        switch (miembro.getRol()) {
            case 1: // Estudiante
                limitePrestamos = 5;
                tipoMiembro = "Estudiante";
                break;
            case 2: // Profesor
                limitePrestamos = 3;
                tipoMiembro = "Profesor";
                break;
            case 3: // Administrativo de la Universidad
                limitePrestamos = 1;
                tipoMiembro = "Administrativo de la Universidad";
                break;
            default:
                return "Error: Rol de miembro desconocido.";
        }

        if (prestamosActivosDelMiembro.size() >= limitePrestamos) {
            return "Error: El " + tipoMiembro + " " + miembro.getNombre() + " ya ha alcanzado su límite de " + limitePrestamos + " libros prestados.";
        }

        // Verificar si el miembro ya tiene este mismo libro prestado
        boolean yaTieneLibro = prestamosActivosDelMiembro.stream()
                                .anyMatch(p -> p.getCodigoLibro().equals(codigoLibro));
        if (yaTieneLibro) {
            return "Error: El miembro ya tiene este libro prestado.";
        }

        // Realizar el préstamo
        libro.setUnLibres(libro.getUnLibres() - 1);
        libro.setUnPrestadas(libro.getUnPrestadas() + 1);
        libro.save(); // Actualizar el estado del libro en el archivo

        Date fechaPrestamo = Fecha.getToday();
        Date fechaDevolucionEstimada = Fecha.addDays(fechaPrestamo, DIAS_MAX_PRESTAMO); // 30 días de préstamo

        Prestamo nuevoPrestamo = new Prestamo(codigoLibro, miembro.getNoDoc(), fechaPrestamo, fechaDevolucionEstimada, this.id);
        nuevoPrestamo.save(); // Guardar el registro de préstamo

        return "Éxito: Libro '" + libro.getNombre() + "' prestado a " + miembro.getNombre() + " " + miembro.getApellido() + ". Debe ser devuelto antes de " + Fecha.formatDate(fechaDevolucionEstimada) + ".";
    }

    // Método para devolver un libro
    public String devolverLibro(Miembro miembro, String codigoLibro) {
        if (miembro == null) {
            return "Error: Miembro no válido.";
        }

        Libro libro = Libro.findBookByCode(codigoLibro);
        if (libro == null) {
            return "Error: Libro con código " + codigoLibro + " no encontrado.";
        }

        // Verificar que el libro pertenece a esta biblioteca
        if (libro.getIdBiblioteca() != this.id) {
            return "Error: El libro " + codigoLibro + " no se encuentra en esta biblioteca (" + this.nombreBiblioteca + ").";
        }

        Prestamo prestamoActivo = Prestamo.findActivePrestamo(miembro.getNoDoc(), codigoLibro);

        if (prestamoActivo == null) {
            return "Error: El miembro " + miembro.getNombre() + " no tiene prestado el libro '" + libro.getNombre() + "'.";
        }

        // Marcar el préstamo como devuelto
        prestamoActivo.setFechaDevolucionReal(Fecha.getToday());
        prestamoActivo.setEstado("DEVUELTO");
        prestamoActivo.save(); // Actualizar el registro de préstamo

        // Actualizar el estado del libro
        libro.setUnLibres(libro.getUnLibres() + 1);
        libro.setUnPrestadas(libro.getUnPrestadas() - 1);
        libro.save(); // Actualizar el estado del libro en el archivo

        String multaInfo = "";
        // Verificar si hay multa y aplicarla
        if (prestamoActivo.getFechaDevolucionReal().after(prestamoActivo.getFechaDevolucionEstimada())) {
            multaInfo = multar(miembro, libro, prestamoActivo);
        }

        return "Éxito: Libro '" + libro.getNombre() + "' devuelto por " + miembro.getNombre() + " " + miembro.getApellido() + ". " + multaInfo;
    }

    // Método para ver libros disponibles por sede
    public List<Libro> librosDisponibles(String sedeBusqueda) {
        return Libro.loadAllBooks().stream()
                .filter(libro -> libro.getSedeBiblioteca().equalsIgnoreCase(sedeBusqueda) && libro.getUnLibres() > 0)
                .collect(Collectors.toList());
    }

    // Sobrecarga para buscar por ID de biblioteca
    public List<Libro> librosDisponibles(int idBibliotecaBusqueda) {
        return Libro.loadAllBooks().stream()
                .filter(libro -> libro.getIdBiblioteca() == idBibliotecaBusqueda && libro.getUnLibres() > 0)
                .collect(Collectors.toList());
    }

    // Sobrecarga para buscar por sede e ID de biblioteca
    public List<Libro> librosDisponibles(String sedeBusqueda, int idBibliotecaBusqueda) {
        return Libro.loadAllBooks().stream()
                .filter(libro -> libro.getSedeBiblioteca().equalsIgnoreCase(sedeBusqueda)
                                && libro.getIdBiblioteca() == idBibliotecaBusqueda
                                && libro.getUnLibres() > 0)
                .collect(Collectors.toList());
    }

    // Método para buscar libros
    public List<Libro> buscarLibro(String query, String searchType) {
        List<Libro> allBooks = Libro.loadAllBooks();
        return allBooks.stream()
                .filter(libro -> {
                    switch (searchType.toLowerCase()) {
                        case "nombre":
                            return libro.getNombre().toLowerCase().contains(query.toLowerCase());
                        case "autor":
                            return libro.getAutor().toLowerCase().contains(query.toLowerCase());
                        case "codigo":
                            return libro.getCodigo().equalsIgnoreCase(query);
                        case "sede": // Buscar por sede
                            return libro.getSedeBiblioteca().equalsIgnoreCase(query);
                        case "idbiblioteca": // Buscar por ID de biblioteca
                            try {
                                return libro.getIdBiblioteca() == Integer.parseInt(query);
                            } catch (NumberFormatException e) {
                                return false;
                            }
                        default:
                            return false; // Tipo de búsqueda no válido
                    }
                })
                .collect(Collectors.toList());
    }

    public String multar(Miembro miembro, Libro libro, Prestamo prestamo) {
        if (prestamo != null && prestamo.getFechaDevolucionReal() != null &&
            prestamo.getFechaDevolucionReal().after(prestamo.getFechaDevolucionEstimada())) {

            long diasRetraso = Fecha.getDaysBetween(prestamo.getFechaDevolucionReal(), prestamo.getFechaDevolucionEstimada());

            // Asegurarse de que díasRetraso sea al menos 1 si hay retraso
            if (diasRetraso <= 0) diasRetraso = 1;

            double montoMulta = diasRetraso * MULTA_POR_DIA;

            // Si el miembro es un Usuario, se le aplica la deuda.
            if (miembro instanceof Usuario) {
                Usuario user = (Usuario) miembro;
                user.setDeuda(user.getDeuda() + montoMulta); // Acumula la deuda
                System.out.println("Deuda actualizada para " + user.getNombre() + ". Nueva deuda total: " + String.format("%.2f", user.getDeuda()));
            }

            return String.format("Se ha aplicado una multa a %s %s por el libro '%s'. Retraso: %d días. Monto: %.2f pesos. ",
                                 miembro.getNombre(), miembro.getApellido(), libro.getNombre(), diasRetraso, montoMulta);
        }
        return "No hay multa para este préstamo.";
    }

    @Override
    public String toString() {
        return "Biblioteca {" +
                "  ID=" + id +
                ", Sede='" + sede + '\'' +
                ", Nombre='" + nombreBiblioteca + '\'' +
                '}';
    }
}
```

**7. `Prestamo.java`**

```java
package com.mycompany.sistemabiblioteca;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Clase para representar un préstamo de libro.
 */
public class Prestamo {
    private String codigoLibro;
    private long noDocMiembro;
    private Date fechaPrestamo;
    private Date fechaDevolucionEstimada;
    private Date fechaDevolucionReal; // Null si no se ha devuelto
    private int idBiblioteca;
    private String estado; // Ej: "PRESTADO", "DEVUELTO", "VENCIDO"

    public Prestamo() {
    }

    public Prestamo(String codigoLibro, long noDocMiembro, Date fechaPrestamo, Date fechaDevolucionEstimada, int idBiblioteca) {
        this.codigoLibro = codigoLibro;
        this.noDocMiembro = noDocMiembro;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaDevolucionEstimada = fechaDevolucionEstimada;
        this.fechaDevolucionReal = null; // No devuelto inicialmente
        this.idBiblioteca = idBiblioteca;
        this.estado = "PRESTADO"; // Estado inicial
    }

    // Constructor para cargar desde archivo
    public Prestamo(String codigoLibro, long noDocMiembro, Date fechaPrestamo, Date fechaDevolucionEstimada, Date fechaDevolucionReal, int idBiblioteca, String estado) {
        this.codigoLibro = codigoLibro;
        this.noDocMiembro = noDocMiembro;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaDevolucionEstimada = fechaDevolucionEstimada;
        this.fechaDevolucionReal = fechaDevolucionReal;
        this.idBiblioteca = idBiblioteca;
        this.estado = estado;
    }

    // Getters
    public String getCodigoLibro() {
        return codigoLibro;
    }

    public long getNoDocMiembro() {
        return noDocMiembro;
    }

    public Date getFechaPrestamo() {
        return fechaPrestamo;
    }

    public Date getFechaDevolucionEstimada() {
        return fechaDevolucionEstimada;
    }

    public Date getFechaDevolucionReal() {
        return fechaDevolucionReal;
    }

    public int getIdBiblioteca() {
        return idBiblioteca;
    }

    public String getEstado() {
        return estado;
    }

    // Setters (invocan save() si modifican un atributo persistente)
    public void setFechaDevolucionReal(Date fechaDevolucionReal) {
        this.fechaDevolucionReal = fechaDevolucionReal;
        save();
    }

    public void setEstado(String estado) {
        this.estado = estado;
        save();
    }

    // --- Persistence methods ---

    // Resuelve la ruta del archivo LibroPrestado.txt
    protected static Path resolveLibroPrestadoPath() {
        try {
            java.net.URL res = Prestamo.class.getClassLoader().getResource("LibroPrestado.txt");
            if (res != null && "file".equalsIgnoreCase(res.getProtocol())) {
                try {
                    return Paths.get(res.toURI());
                } catch (Exception ex) {
                    // fallback to user.dir
                }
            }
        } catch (Exception ex) {
            // ignore and fallback
        }
        return Paths.get(System.getProperty("user.dir"), "LibroPrestado.txt");
    }

    // Construye la línea de texto para guardar el préstamo
    private String buildLine() {
        return String.format("%s\\%d\\%s\\%s\\%s\\%d\\%s",
                codigoLibro,
                noDocMiembro,
                Fecha.formatDate(fechaPrestamo),
                Fecha.formatDate(fechaDevolucionEstimada),
                Fecha.formatDate(fechaDevolucionReal), // Usa Fecha.formatDate para null también
                idBiblioteca,
                estado);
    }

    // Guarda o actualiza el registro del préstamo.
    public synchronized void save() {
        try {
            Path path = resolveLibroPrestadoPath();
            List<String> lines = new ArrayList<>();
            if (Files.exists(path)) {
                lines = new ArrayList<>(Files.readAllLines(path, java.nio.charset.StandardCharsets.UTF_8));
            }

            List<String> normalized = new ArrayList<>();
            for (String l : lines) {
                if (l != null && !l.trim().isEmpty()) normalized.add(l);
            }
            lines = normalized;

            boolean found = false;
            // Usar un identificador único para el préstamo (libro, miembro, fecha de préstamo)
            // Se asume que no puede haber dos préstamos del mismo libro al mismo miembro en la misma fecha de préstamo.
            String uniqueLoanIdentifier = this.codigoLibro + "_" + this.noDocMiembro + "_" + Fecha.formatDate(this.fechaPrestamo);

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                String[] parts = line.split("\\\\");
                if (parts.length >= 7) {
                    try {
                        String lineCodigoLibro = parts[0].trim();
                        long lineNoDocMiembro = Long.parseLong(parts[1].trim());
                        Date lineFechaPrestamo = Fecha.parseDate(parts[2].trim());

                        String lineIdentifier = lineCodigoLibro + "_" + lineNoDocMiembro + "_" + Fecha.formatDate(lineFechaPrestamo);

                        if (lineIdentifier.equals(uniqueLoanIdentifier)) {
                            lines.set(i, buildLine()); // Actualizar la línea
                            found = true;
                            break;
                        }
                    } catch (NumberFormatException | ParseException e) {
                        System.err.println("Error parsing loan line during save: " + line + " - " + e.getMessage());
                    }
                }
            }

            if (!found) {
                lines.add(buildLine()); // Agregar nuevo préstamo
            }

            Path parent = path.getParent();
            if (parent != null && !Files.exists(parent)) {
                try {
                    Files.createDirectories(parent);
                } catch (IOException ex) {
                    System.err.println("Error creating directory for LibroPrestado.txt: " + ex.getMessage());
                }
            }
            Files.write(path, lines, java.nio.charset.StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // Elimina un registro de préstamo.
    public synchronized void delete() {
         try {
            Path path = resolveLibroPrestadoPath();
            List<String> lines = new ArrayList<>();
            if (Files.exists(path)) {
                lines = new ArrayList<>(Files.readAllLines(path, java.nio.charset.StandardCharsets.UTF_8));
            }

            List<String> normalized = new ArrayList<>();
            for (String l : lines) {
                if (l != null && !l.trim().isEmpty()) normalized.add(l);
            }
            lines = normalized;

            String uniqueLoanIdentifier = this.codigoLibro + "_" + this.noDocMiembro + "_" + Fecha.formatDate(this.fechaPrestamo);

            List<String> updatedLines = new ArrayList<>();
            for (String line : lines) {
                String[] parts = line.split("\\\\");
                if (parts.length >= 7) {
                    try {
                        String lineCodigoLibro = parts[0].trim();
                        long lineNoDocMiembro = Long.parseLong(parts[1].trim());
                        Date lineFechaPrestamo = Fecha.parseDate(parts[2].trim());
                        String lineIdentifier = lineCodigoLibro + "_" + lineNoDocMiembro + "_" + Fecha.formatDate(lineFechaPrestamo);

                        if (!lineIdentifier.equals(uniqueLoanIdentifier)) {
                            updatedLines.add(line);
                        }
                    } catch (NumberFormatException | ParseException e) {
                        System.err.println("Error parsing loan line during delete: " + line + " - " + e.getMessage());
                        updatedLines.add(line); // Mantener la línea si el parsing falla para evitar pérdida de datos
                    }
                } else {
                    updatedLines.add(line); // Mantener líneas mal formadas para inspección manual
                }
            }

            Path parent = path.getParent();
            if (parent != null && !Files.exists(parent)) {
                try {
                    Files.createDirectories(parent);
                } catch (IOException ex) {
                    System.err.println("Error creating directory for LibroPrestado.txt: " + ex.getMessage());
                }
            }
            Files.write(path, updatedLines, java.nio.charset.StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // Carga todos los préstamos del archivo LibroPrestado.txt
    public static List<Prestamo> loadAllPrestamos() {
        List<Prestamo> prestamos = new ArrayList<>();
        Path path = resolveLibroPrestadoPath();
        if (!Files.exists(path)) {
            return prestamos;
        }

        try (BufferedReader reader = Files.newBufferedReader(path, java.nio.charset.StandardCharsets.UTF_8)) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] parts = linea.split("\\\\");
                if (parts.length >= 7) {
                    try {
                        String codigoLibro = parts[0].trim();
                        long noDocMiembro = Long.parseLong(parts[1].trim());
                        Date fechaPrestamo = Fecha.parseDate(parts[2].trim());
                        Date fechaDevolucionEstimada = Fecha.parseDate(parts[3].trim());
                        Date fechaDevolucionReal = Fecha.parseDate(parts[4].trim()); // Usa Fecha.parseDate
                        int idBiblioteca = Integer.parseInt(parts[5].trim());
                        String estado = parts[6].trim();

                        prestamos.add(new Prestamo(codigoLibro, noDocMiembro, fechaPrestamo, fechaDevolucionEstimada, fechaDevolucionReal, idBiblioteca, estado));
                    } catch (NumberFormatException | ParseException e) {
                        System.err.println("Error parsing loan line: " + linea + " - " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return prestamos;
    }

    // Encuentra todos los préstamos de un miembro específico.
    public static List<Prestamo> findPrestamosByMember(long noDocMiembro) {
        return loadAllPrestamos().stream()
                .filter(p -> p.getNoDocMiembro() == noDocMiembro)
                .collect(Collectors.toList());
    }

    // Encuentra un préstamo activo de un libro por un miembro.
    public static Prestamo findActivePrestamo(long noDocMiembro, String codigoLibro) {
        return loadAllPrestamos().stream()
                .filter(p -> p.getNoDocMiembro() == noDocMiembro && p.getCodigoLibro().equals(codigoLibro) && "PRESTADO".equals(p.getEstado()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String toString() {
        return "Prestamo {" +
                "  Libro Código='" + codigoLibro + '\'' +
                ", Miembro Doc=" + noDocMiembro +
                ", Fecha Prestamo=" + Fecha.formatDate(fechaPrestamo) +
                ", Fecha Devolución Estimada=" + Fecha.formatDate(fechaDevolucionEstimada) +
                ", Fecha Devolución Real=" + Fecha.formatDate(fechaDevolucionReal) +
                ", ID Biblioteca=" + idBiblioteca +
                ", Estado='" + estado + '\'' +
                '}';
    }
}
```

**8. `SistemaBiblioteca.java`**

```java
package com.mycompany.sistemabiblioteca;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

/**
 * Clase principal que orquesta el sistema de la biblioteca.
 * Maneja el inicio de sesión y la navegación por los menús de administrador y usuario.
 */
public class SistemaBiblioteca {

    private static Miembro miembroActual = null; // Miembro logueado en el sistema
    private static Biblioteca bibliotecaSeleccionada = null; // Biblioteca con la que se interactúa
    private static Scanner scanner = new Scanner(System.in);

    // Para generar usuarios/contraseñas aleatorios
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private static String randomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        System.out.println("¡Bienvenido al Sistema de Gestión de Bibliotecas!");

        // 1. Inicializar datos: 1 admin y 3 usuarios aleatorios
        setupInitialData();

        // Cargar las bibliotecas disponibles al inicio
        List<Biblioteca> bibliotecasDisponibles = Biblioteca.loadAllBibliotecas();
        if (bibliotecasDisponibles.isEmpty()) {
            System.out.println("No se encontraron bibliotecas configuradas. Por favor, añada bibliotecas a 'Bibliotecas.txt'.");
            return;
        }

        // Seleccionar una biblioteca inicial para interactuar
        System.out.println("\n--- SELECCIÓN DE BIBLIOTECA ---");
        System.out.println("Seleccione una biblioteca para iniciar:");
        for (int i = 0; i < bibliotecasDisponibles.size(); i++) {
            System.out.println((i + 1) + ". " + bibliotecasDisponibles.get(i).getNombreBiblioteca() + " (" + bibliotecasDisponibles.get(i).getSede() + ")");
        }
        int opcionBib;
        while (true) {
            System.out.print("Ingrese el número de la biblioteca: ");
            try {
                opcionBib = scanner.nextInt();
                scanner.nextLine(); // Consumir el salto de línea
                if (opcionBib > 0 && opcionBib <= bibliotecasDisponibles.size()) {
                    bibliotecaSeleccionada = bibliotecasDisponibles.get(opcionBib - 1);
                    System.out.println("Ha seleccionado la biblioteca: " + bibliotecaSeleccionada.getNombreBiblioteca());
                    break;
                } else {
                    System.out.println("Opción no válida. Intente de nuevo.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Entrada inválida. Por favor, ingrese un número.");
                scanner.nextLine(); // Limpiar el buffer del scanner
            }
        }

        // Proceso de Login
        login();

        if (miembroActual != null) {
            if (miembroActual.getRol() == 0) { // Administrador
                mostrarMenuAdmin();
            } else { // Usuario (Estudiante, Profesor, Administrativo)
                mostrarMenuUsuario();
            }
        }

        System.out.println("Gracias por usar el Sistema de Bibliotecas. ¡Hasta pronto!");
        scanner.close();
    }

    private static void setupInitialData() {
        System.out.println("\n--- CONFIGURANDO DATOS INICIALES ---");

        // Crear el archivo Bibliotecas.txt si no existe con datos de ejemplo
        Path bibliotecasPath = Biblioteca.resolveBibliotecasPath();
        if (!Files.exists(bibliotecasPath)) {
            System.out.println("Creando 'Bibliotecas.txt' con datos de ejemplo...");
            try {
                Files.createDirectories(bibliotecasPath.getParent());
                List<String> defaultBibliotecas = new ArrayList<>();
                defaultBibliotecas.add("1\\Medellin\\Biblioteca Central");
                defaultBibliotecas.add("2\\Bogota\\Biblioteca Norte");
                Files.write(bibliotecasPath, defaultBibliotecas, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException e) {
                System.err.println("Error al crear Bibliotecas.txt: " + e.getMessage());
            }
        }

        // Crear 1 admin si no existe
        Admin adminExistente = null;
        for (Miembro m : Miembro.loadAllMembers()) {
            if (m.getRol() == 0) {
                adminExistente = (Admin) m;
                break;
            }
        }

        if (adminExistente == null) {
            System.out.println("Creando un nuevo administrador...");
            String adminUser = "admin_" + randomString(4);
            String adminPass = randomString(10);
            Admin nuevoAdmin = new Admin((byte)1, 1000000000L + RANDOM.nextInt(100000000), (byte)0, "Sys", "Admin", adminUser, adminPass);
            nuevoAdmin.save(); // Esto hasheará la contraseña y la guardará
            System.out.println("Admin creado (¡Use estas credenciales para iniciar sesión!):");
            System.out.println("  Usuario: " + adminUser);
            System.out.println("  Contraseña: " + adminPass); // Se muestra la contraseña en texto plano para prueba
        } else {
            System.out.println("Administrador existente encontrado: " + adminExistente.getUsuario());
            System.out.println("Para obtener las credenciales de un admin existente, debería cambiarle la contraseña a una conocida.");
            System.out.println("Para fines de prueba, si desea un nuevo admin aleatorio, borre el archivo 'Miembros.txt' antes de iniciar.");
        }

        // Crear 3 usuarios aleatorios si hay menos de 3 usuarios con rol != 0
        long currentUsers = Miembro.loadAllMembers().stream().filter(m -> m.getRol() != 0).count();
        int usersToCreate = 3 - (int) currentUsers;
        if (usersToCreate > 0) {
            System.out.println("Creando " + usersToCreate + " usuarios aleatorios...");
            String[] roles = {"Estudiante", "Profesor", "Administrativo"};
            byte[] rolCodes = {1, 2, 3};
            String[] sedes = {"Medellin", "Bogota", "Cali"};
            String[] carreras = {"Ingenieria de Sistemas", "Derecho", "Medicina", "Diseño Grafico"};

            for (int i = 0; i < usersToCreate; i++) {
                String user = "user" + (1000 + RANDOM.nextInt(9000));
                String pass = randomString(8);
                byte docType = (byte) (1 + RANDOM.nextInt(2)); // 1 or 2 (CC or TI)
                long docNum = 100000000L + RANDOM.nextInt(900000000); // 9-digit number
                byte rol = rolCodes[RANDOM.nextInt(rolCodes.length)];
                String name = randomString(5);
                String lastName = randomString(7);
                String sedeUni = sedes[RANDOM.nextInt(sedes.length)];
                String carreraUser = carreras[RANDOM.nextInt(carreras.length)];

                Usuario nuevoUsuario = new Usuario(docType, docNum, rol, name, lastName, user, pass, 0.0, sedeUni, carreraUser);
                nuevoUsuario.save();
                System.out.println("  Usuario creado: " + user + " (Rol: " + roles[rol-1] + ", Pass: " + pass + ")"); // Para debug
            }
        } else {
            System.out.println("Suficientes usuarios existentes.");
        }
        System.out.println("--- CONFIGURACIÓN INICIAL COMPLETA ---\n");
    }

    private static void login() {
        System.out.println("\n--- INICIO DE SESIÓN ---");
        System.out.print("Usuario: ");
        String usuario = scanner.nextLine();
        System.out.print("Contraseña: ");
        String contrasena = scanner.nextLine();

        Miembro tempMiembro = new Miembro();
        String lineaMiembro = tempMiembro.login(usuario, contrasena);

        if (!lineaMiembro.isEmpty()) {
            String[] parts = lineaMiembro.split("\\\\");
            byte tipoDoc = Byte.parseByte(parts[1]);
            long noDoc = Long.parseLong(parts[0]);
            byte rol = Byte.parseByte(parts[2]);
            String nombre = parts[3];
            String apellido = parts[4];
            String user = parts[5];
            String passHash = parts[6]; // Contraseña ya hasheada

            if (rol == 0) {
                miembroActual = new Admin(tipoDoc, noDoc, rol, nombre, apellido, user, passHash);
            } else {
                // Al cargar un Usuario, hay que intentar leer los campos adicionales
                double deuda = 0.0;
                String sedeUniversidad = "";
                String carrera = "";
                // Un Usuario debe tener al menos 10 partes si se guardan todos los campos.
                if (parts.length >= 10) {
                    try {
                        deuda = Double.parseDouble(parts[7].trim());
                        sedeUniversidad = parts[8].trim();
                        carrera = parts[9].trim();
                    } catch (NumberFormatException e) {
                        System.err.println("Error al parsear campos de Usuario en línea de login: " + lineaMiembro + " - " + e.getMessage());
                    }
                }
                miembroActual = new Usuario(tipoDoc, noDoc, rol, nombre, apellido, user, passHash, deuda, sedeUniversidad, carrera);
            }
            System.out.println("Inicio de sesión exitoso. ¡Bienvenido, " + miembroActual.getNombre() + "!");
        } else {
            System.out.println("Usuario o contraseña incorrectos.");
            miembroActual = null;
        }
    }

    private static void mostrarMenuAdmin() {
        Admin admin = (Admin) miembroActual;
        int opcion;
        do {
            System.out.println("\n--- MENÚ ADMINISTRADOR ---");
            System.out.println("1. Administrar Libros");
            System.out.println("2. Administrar Usuarios");
            System.out.println("3. Ver Libros Prestados (Todos)");
            System.out.println("4. Ver Libros Prestados por un Usuario Específico");
            System.out.println("5. Ver Todos los Libros");
            System.out.println("6. Ver un Libro Específico"); // Nuevo método
            System.out.println("7. Ver Todos los Usuarios");
            System.out.println("8. Cambiar información personal de Admin");
            System.out.println("9. Cambiar contraseña de Admin");
            System.out.println("0. Cerrar Sesión");
            System.out.print("Seleccione una opción: ");

            try {
                opcion = scanner.nextInt();
                scanner.nextLine(); // Consumir salto de línea

                switch (opcion) {
                    case 1:
                        menuAdminLibros(admin);
                        break;
                    case 2:
                        menuAdminUsuarios(admin);
                        break;
                    case 3:
                        admin.verLibrosPrestados();
                        break;
                    case 4:
                        System.out.print("Ingrese el número de documento del usuario: ");
                        long noDocUsuario = scanner.nextLong();
                        scanner.nextLine();
                        admin.verLibrosPrestadosPorUsuario(noDocUsuario);
                        break;
                    case 5:
                        admin.verTodosLibros();
                        break;
                    case 6: // Ver un libro específico
                        System.out.print("Ingrese el código del libro a ver: ");
                        String codVerLibro = scanner.nextLine();
                        admin.verLibro(codVerLibro);
                        break;
                    case 7:
                        admin.verTodosUsuarios();
                        break;
                    case 8:
                        cambiarInfoPersonalAdmin(admin);
                        break;
                    case 9:
                        cambiarContrasenaAdmin(admin);
                        break;
                    case 0:
                        System.out.println("Cerrando sesión de administrador.");
                        break;
                    default:
                        System.out.println("Opción no válida. Intente de nuevo.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Entrada inválida. Por favor, ingrese un número.");
                scanner.nextLine(); // Limpiar el buffer
                opcion = -1; // Para asegurar que el bucle continúe
            }
        } while (opcion != 0);
    }

    private static void menuAdminLibros(Admin admin) {
        int opcion;
        do {
            System.out.println("\n--- ADMINISTRAR LIBROS ---");
            System.out.println("1. Agregar Libro");
            System.out.println("2. Editar Libro");
            System.out.println("3. Eliminar Unidades de Libro / Eliminar Libro Completo");
            System.out.println("0. Volver al Menú Principal");
            System.out.print("Seleccione una opción: ");

            try {
                opcion = scanner.nextInt();
                scanner.nextLine(); // Consumir salto de línea

                switch (opcion) {
                    case 1:
                        System.out.print("Nombre: "); String nombre = scanner.nextLine();
                        System.out.print("Autor: "); String autor = scanner.nextLine();
                        System.out.print("Código (ID único, dejar vacío para generar): "); String codigo = scanner.nextLine();
                        System.out.print("Unidades Totales: "); int unidades = scanner.nextInt();
                        scanner.nextLine();
                        // Asignamos a la biblioteca seleccionada por el admin
                        admin.agregarLibro(nombre, autor, codigo, unidades, bibliotecaSeleccionada.getId(), bibliotecaSeleccionada.getSede());
                        break;
                    case 2:
                        System.out.print("Código del libro a editar: "); String codEdit = scanner.nextLine();
                        Libro libroAEditar = Libro.findBookByCode(codEdit);
                        if(libroAEditar == null) {
                            System.out.println("Libro no encontrado."); break;
                        }
                        System.out.println("Editando libro: " + libroAEditar.getNombre());
                        System.out.print("Nuevo Nombre (actual: " + libroAEditar.getNombre() + ", dejar vacío para no cambiar): "); String newNombre = scanner.nextLine(); if(newNombre.isEmpty()) newNombre = libroAEditar.getNombre();
                        System.out.print("Nuevo Autor (actual: " + libroAEditar.getAutor() + ", dejar vacío para no cambiar): "); String newAutor = scanner.nextLine(); if(newAutor.isEmpty()) newAutor = libroAEditar.getAutor();
                        System.out.print("Nuevas Unidades Totales (actual: " + (libroAEditar.getUnLibres() + libroAEditar.getUnPrestadas()) + ", dejar vacío para no cambiar): ");
                        String newUnidadesStr = scanner.nextLine();
                        int newUnidades = (newUnidadesStr.isEmpty() ? (libroAEditar.getUnLibres() + libroAEditar.getUnPrestadas()) : Integer.parseInt(newUnidadesStr));

                        // Podríamos pedir cambiar la biblioteca, pero por simplicidad usamos la seleccionada
                        admin.editarLibro(codEdit, newNombre, newAutor, newUnidades, libroAEditar.getIdBiblioteca(), libroAEditar.getSedeBiblioteca());
                        break;
                    case 3:
                        System.out.print("Código del libro a eliminar: "); String codDel = scanner.nextLine();
                        System.out.print("¿Cuántas unidades desea eliminar? (0 para eliminar todas las unidades libres, o un número específico): ");
                        String unidadesDelStr = scanner.nextLine();
                        if (unidadesDelStr.isEmpty()) { // Si deja vacío, elimina todas las unidades libres
                            admin.eliminarLibro(codDel);
                        } else {
                            int unidadesAeliminar = Integer.parseInt(unidadesDelStr);
                            admin.eliminarLibro(codDel, unidadesAeliminar);
                        }
                        break;
                    case 0:
                        break;
                    default:
                        System.out.println("Opción no válida.");
                }
            } catch (InputMismatchException | NumberFormatException e) {
                System.out.println("Entrada inválida. Asegúrese de ingresar el tipo de dato correcto.");
                scanner.nextLine();
                opcion = -1;
            }
        } while (opcion != 0);
    }

    private static void menuAdminUsuarios(Admin admin) {
        int opcion;
        do {
            System.out.println("\n--- ADMINISTRAR USUARIOS ---");
            System.out.println("1. Agregar Usuario");
            System.out.println("2. Editar Usuario");
            System.out.println("3. Eliminar Usuario");
            System.out.println("0. Volver al Menú Principal");
            System.out.print("Seleccione una opción: ");

            try {
                opcion = scanner.nextInt();
                scanner.nextLine(); // Consumir salto de línea

                switch (opcion) {
                    case 1:
                        System.out.print("Tipo de Documento (1:CC, 2:TI, etc.): "); byte tipoDoc = scanner.nextByte();
                        System.out.print("Número de Documento: "); long noDoc = scanner.nextLong();
                        System.out.print("Rol (1:Estudiante, 2:Profesor, 3:Administrativo Universidad): "); byte rol = scanner.nextByte();
                        scanner.nextLine(); // Consumir salto de línea
                        System.out.print("Nombre: "); String nombre = scanner.nextLine();
                        System.out.print("Apellido: "); String apellido = scanner.nextLine();
                        System.out.print("Usuario: "); String usuario = scanner.nextLine();
                        System.out.print("Contraseña: "); String contrasena = scanner.nextLine();
                        System.out.print("Deuda inicial (0.0 por defecto): "); String deudaStr = scanner.nextLine(); double deuda = (deudaStr.isEmpty() ? 0.0 : Double.parseDouble(deudaStr));
                        System.out.print("Sede Universidad (Ej. Medellin): "); String sedeUni = scanner.nextLine();
                        System.out.print("Carrera (Ej. Ingenieria de Sistemas): "); String carrera = scanner.nextLine();

                        admin.crearUsuario(tipoDoc, noDoc, rol, nombre, apellido, usuario, contrasena, deuda, sedeUni, carrera);
                        break;
                    case 2:
                        System.out.print("Usuario a editar: "); String userEdit = scanner.nextLine();
                        Miembro miembroAEditar = Miembro.findMemberByUsername(userEdit);
                        if(miembroAEditar == null) {
                            System.out.println("Usuario no encontrado."); break;
                        }
                        System.out.println("Editando miembro: " + miembroAEditar.getNombre() + " " + miembroAEditar.getApellido());

                        System.out.print("Nuevo Tipo Doc (actual: " + miembroAEditar.getTipoDoc() + ", dejar vacío para no cambiar): "); String newTipoDocStr = scanner.nextLine(); byte newTipoDoc = (newTipoDocStr.isEmpty() ? miembroAEditar.getTipoDoc() : Byte.parseByte(newTipoDocStr));
                        System.out.print("Nuevo No. Doc (actual: " + miembroAEditar.getNoDoc() + ", dejar vacío para no cambiar): "); String newNoDocStr = scanner.nextLine(); long newNoDoc = (newNoDocStr.isEmpty() ? miembroAEditar.getNoDoc() : Long.parseLong(newNoDocStr));
                        System.out.print("Nuevo Rol (actual: " + miembroAEditar.getRol() + ", 0:Admin, 1:Estudiante, 2:Profesor, 3:Admin Uni, dejar vacío para no cambiar): "); String newRolStr = scanner.nextLine(); byte newRol = (newRolStr.isEmpty() ? miembroAEditar.getRol() : Byte.parseByte(newRolStr));
                        System.out.print("Nuevo Nombre (actual: " + miembroAEditar.getNombre() + ", dejar vacío para no cambiar): "); String newNombre = scanner.nextLine(); if(newNombre.isEmpty()) newNombre = miembroAEditar.getNombre();
                        System.out.print("Nuevo Apellido (actual: " + miembroAEditar.getApellido() + ", dejar vacío para no cambiar): "); String newApellido = scanner.nextLine(); if(newApellido.isEmpty()) newApellido = miembroAEditar.getApellido();
                        System.out.print("Nuevo Usuario (actual: " + miembroAEditar.getUsuario() + ", dejar vacío para no cambiar): "); String newUsuario = scanner.nextLine(); if(newUsuario.isEmpty()) newUsuario = miembroAEditar.getUsuario();
                        System.out.print("Nueva Contraseña (dejar vacío para no cambiar): "); String newContrasena = scanner.nextLine();

                        double nuevaDeuda = 0.0;
                        String nuevaSedeUni = "";
                        String nuevaCarrera = "";

                        // Solo preguntar/aplicar campos específicos si el rol es de Usuario (no Admin)
                        if (newRol != 0) {
                            // Si el miembro actual es un Usuario, pre-cargar sus valores. Si no lo es (p. ej. era Admin), se usarán por defecto o vacíos.
                            if (miembroAEditar instanceof Usuario) {
                                Usuario currentUsuario = (Usuario) miembroAEditar;
                                System.out.print("Nueva Deuda (actual: " + String.format("%.2f", currentUsuario.getDeuda()) + ", dejar vacío para no cambiar): "); String newDeudaStr = scanner.nextLine(); nuevaDeuda = (newDeudaStr.isEmpty() ? currentUsuario.getDeuda() : Double.parseDouble(newDeudaStr));
                                System.out.print("Nueva Sede Universidad (actual: " + currentUsuario.getSedeUniversidad() + ", dejar vacío para no cambiar): "); String newSedeUniStr = scanner.nextLine(); nuevaSedeUni = (newSedeUniStr.isEmpty() ? currentUsuario.getSedeUniversidad() : newSedeUniStr);
                                System.out.print("Nueva Carrera (actual: " + currentUsuario.getCarrera() + ", dejar vacío para no cambiar): "); String newCarreraStr = scanner.nextLine(); nuevaCarrera = (newCarreraStr.isEmpty() ? currentUsuario.getCarrera() : newCarreraStr);
                            } else {
                                // Si era un Admin y se cambia a rol de Usuario, se pedirán estos campos
                                System.out.println("Este miembro pasará a ser un Usuario. Por favor, ingrese sus datos específicos:");
                                System.out.print("Nueva Deuda (0.0 por defecto): "); String newDeudaStr = scanner.nextLine(); nuevaDeuda = (newDeudaStr.isEmpty() ? 0.0 : Double.parseDouble(newDeudaStr));
                                System.out.print("Nueva Sede Universidad: "); nuevaSedeUni = scanner.nextLine();
                                System.out.print("Nueva Carrera: "); nuevaCarrera = scanner.nextLine();
                            }
                        } else {
                            System.out.println("Este miembro es o seguirá siendo un Administrador; los campos de deuda, sede y carrera no aplican.");
                        }

                        admin.editarUsuario(userEdit, newTipoDoc, newNoDoc, newRol, newNombre, newApellido, newUsuario, newContrasena, nuevaDeuda, nuevaSedeUni, nuevaCarrera);
                        break;
                    case 3:
                        System.out.print("Usuario a eliminar: "); String userDel = scanner.nextLine();
                        admin.eliminarUsuario(userDel);
                        break;
                    case 0:
                        break;
                    default:
                        System.out.println("Opción no válida.");
                }
            } catch (InputMismatchException | NumberFormatException e) {
                System.out.println("Entrada inválida. Asegúrese de ingresar el tipo de dato correcto.");
                scanner.nextLine();
                opcion = -1;
            }
        } while (opcion != 0);
    }

    private static void cambiarInfoPersonalAdmin(Admin admin) {
        System.out.println("\n--- CAMBIAR INFORMACIÓN PERSONAL DE ADMIN ---");
        System.out.print("Nuevo Tipo Doc (actual: " + admin.getTipoDoc() + ", dejar vacío para no cambiar): "); String newTipoDocStr = scanner.nextLine(); byte newTipoDoc = (newTipoDocStr.isEmpty() ? admin.getTipoDoc() : Byte.parseByte(newTipoDocStr));
        System.out.print("Nuevo Nombre (actual: " + admin.getNombre() + ", dejar vacío para no cambiar): "); String newNombre = scanner.nextLine(); if(newNombre.isEmpty()) newNombre = admin.getNombre();
        System.out.print("Nuevo Apellido (actual: " + admin.getApellido() + ", dejar vacío para no cambiar): "); String newApellido = scanner.nextLine(); if(newApellido.isEmpty()) newApellido = admin.getApellido();

        admin.cambiarInfoPersonal(newTipoDoc, newNombre, newApellido);
        System.out.println("Información personal actualizada con éxito.");
    }

    private static void cambiarContrasenaAdmin(Admin admin) {
        System.out.println("\n--- CAMBIAR CONTRASEÑA DE ADMIN ---");
        System.out.print("Ingrese la nueva contraseña: "); String newPass = scanner.nextLine();
        admin.cambiarContrasena(newPass);
        System.out.println("Contraseña actualizada con éxito.");
    }

    private static void mostrarMenuUsuario() {
        Usuario usuario = (Usuario) miembroActual;
        int opcion;
        do {
            System.out.println("\n--- MENÚ USUARIO (" + usuario.getNombre() + ") ---");
            System.out.println("Deuda Actual: " + String.format("%.2f", usuario.getDeuda()) + " pesos");
            System.out.println("1. Buscar Libro");
            System.out.println("2. Ver Libros Disponibles en " + bibliotecaSeleccionada.getNombreBiblioteca());
            System.out.println("3. Prestar Libro");
            System.out.println("4. Devolver Libro");
            System.out.println("5. Ver Mis Libros Prestados");
            System.out.println("6. Cambiar información de contacto");
            System.out.println("7. Cambiar Contraseña");
            System.out.println("0. Cerrar Sesión");
            System.out.print("Seleccione una opción: ");

            try {
                opcion = scanner.nextInt();
                scanner.nextLine(); // Consumir salto de línea

                switch (opcion) {
                    case 1:
                        System.out.print("Ingrese el término de búsqueda: "); String query = scanner.nextLine();
                        System.out.print("Buscar por (nombre/autor/codigo/sede/idbiblioteca): "); String searchType = scanner.nextLine();
                        List<Libro> resultados = bibliotecaSeleccionada.buscarLibro(query, searchType);
                        if (resultados.isEmpty()) {
                            System.out.println("No se encontraron libros.");
                        } else {
                            resultados.forEach(System.out::println);
                        }
                        break;
                    case 2:
                        List<Libro> disponibles = bibliotecaSeleccionada.librosDisponibles(bibliotecaSeleccionada.getSede(), bibliotecaSeleccionada.getId());
                        if (disponibles.isEmpty()) {
                            System.out.println("No hay libros disponibles en " + bibliotecaSeleccionada.getNombreBiblioteca() + ".");
                        } else {
                            disponibles.forEach(System.out::println);
                        }
                        break;
                    case 3:
                        System.out.print("Ingrese el código del libro a prestar: "); String codPrestamo = scanner.nextLine();
                        System.out.println(usuario.solicitarPrestamo(codPrestamo, bibliotecaSeleccionada));
                        break;
                    case 4:
                        System.out.print("Ingrese el código del libro a devolver: "); String codDevolucion = scanner.nextLine();
                        System.out.println(usuario.realizarDevolucion(codDevolucion, bibliotecaSeleccionada));
                        break;
                    case 5:
                        usuario.verMisLibrosPrestados();
                        break;
                    case 6:
                        cambiarInfoPersonalUsuario(usuario);
                        break;
                    case 7:
                        cambiarContrasenaUsuario(usuario);
                        break;
                    case 0:
                        System.out.println("Cerrando sesión de usuario.");
                        break;
                    default:
                        System.out.println("Opción no válida. Intente de nuevo.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Entrada inválida. Por favor, ingrese un número.");
                scanner.nextLine(); // Limpiar el buffer
                opcion = -1;
            }
        } while (opcion != 0);
    }

    private static void cambiarInfoPersonalUsuario(Usuario usuario) {
        System.out.println("\n--- CAMBIAR INFORMACIÓN PERSONAL DE USUARIO ---");
        System.out.print("Nuevo Tipo Doc (actual: " + usuario.getTipoDoc() + ", dejar vacío para no cambiar): "); String newTipoDocStr = scanner.nextLine(); byte newTipoDoc = (newTipoDocStr.isEmpty() ? usuario.getTipoDoc() : Byte.parseByte(newTipoDocStr));
        System.out.print("Nuevo Nombre (actual: " + usuario.getNombre() + ", dejar vacío para no cambiar): "); String newNombre = scanner.nextLine(); if(newNombre.isEmpty()) newNombre = usuario.getNombre();
        System.out.print("Nuevo Apellido (actual: " + usuario.getApellido() + ", dejar vacío para no cambiar): "); String newApellido = scanner.nextLine(); if(newApellido.isEmpty()) newApellido = usuario.getApellido();
        System.out.print("Nueva Sede Universidad (actual: " + usuario.getSedeUniversidad() + ", dejar vacío para no cambiar): "); String newSedeUni = scanner.nextLine(); if(newSedeUni.isEmpty()) newSedeUni = usuario.getSedeUniversidad();
        System.out.print("Nueva Carrera (actual: " + usuario.getCarrera() + ", dejar vacío para no cambiar): "); String newCarrera = scanner.nextLine(); if(newCarrera.isEmpty()) newCarrera = usuario.getCarrera();

        usuario.cambiarInfoPersonal(newTipoDoc, newNombre, newApellido, newSedeUni, newCarrera);
        System.out.println("Información personal actualizada con éxito.");
    }

    private static void cambiarContrasenaUsuario(Usuario usuario) {
        System.out.println("\n--- CAMBIAR CONTRASEÑA DE USUARIO ---");
        System.out.print("Ingrese la nueva contraseña: "); String newPass = scanner.nextLine();
        usuario.cambiarContrasena(newPass);
        System.out.println("Contraseña actualizada con éxito.");
    }
}
```

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.sistemabiblioteca; // Carpeta donde está organizado nuestro código.

import java.io.BufferedReader; // Para leer archivos de texto.
import java.io.IOException; // Para manejar errores de archivo.
import java.nio.file.Files; // Para operar con archivos.
import java.nio.file.Path; // Para rutas de archivos.
import java.nio.file.StandardOpenOption; // Para opciones de guardado.
import java.util.ArrayList; // Para listas dinámicas.
import java.util.Base64; // Para codificar/decodificar a Base64 (usado en contraseñas).
import java.util.List; // Para usar listas.

// Esta clase es la base para cualquier persona en el sistema (Admin o Usuario).
// Contiene la información común y la lógica para el login seguro.
// Implementa 'IPersistible' para guardarse y eliminarse de archivos.
public class Miembro implements IPersistible {

    protected long numeroDocumento; // Número de identificación del miembro.
    protected byte tipoDocumento; // Tipo de documento (ej. CC, TI).
    protected byte rol; // Rol del miembro: 0=Admin, 1=Estudiante, etc.
    protected String nombre; // Nombre del miembro.
    protected String apellido; // Apellido del miembro.
    protected String usuario; // Nombre de usuario para iniciar sesión.
    protected String contrasena; // Contraseña, guardada de forma segura (hasheada).

    public Miembro() { // Constructor vacío.
    }

    // Constructor para crear un miembro con todos sus datos.
    public Miembro(byte tipoDocumento, long numeroDocumento, byte rol, String nombre, String apellido, String usuario,
            String contrasena) {
        this.tipoDocumento = tipoDocumento;
        this.numeroDocumento = numeroDocumento;
        this.rol = rol;
        this.nombre = nombre;
        this.apellido = apellido;
        this.usuario = usuario;

        String hashed = hashPassword(contrasena); // Hashea la contraseña para guardarla segura.
        if (hashed != null && !hashed.isEmpty()) { // Si el hash se generó correctamente.
            this.contrasena = hashed; // Guarda el hash.
        } else {
            this.contrasena = contrasena; // **ADVERTENCIA**: Si falla, guarda la contraseña sin hashear (NO seguro en
                                          // producción).
        }
    }

    // Getters para los atributos.
    public long getNumeroDocumento() {
        return numeroDocumento;
    }

    // Setter para el número de documento. Guarda el cambio en el archivo.
    public void setNumeroDocumento(long numeroDocumento) {
        long oldNumeroDocumento = this.numeroDocumento; // Guarda el valor antiguo para buscar el registro.
        this.numeroDocumento = numeroDocumento;
        guardarInterno(null, oldNumeroDocumento); // Llama al método interno para actualizar el archivo.
    }

    public byte getTipoDocumento() {
        return tipoDocumento;
    }

    // Setter para el tipo de documento. Guarda el cambio.
    public void setTipoDocumento(byte tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
        guardar();
    }

    public byte getRol() {
        return rol;
    }

    // Setter para el rol. Guarda el cambio.
    public void setRol(byte rol) {
        this.rol = rol;
        guardar();
    }

    public String getNombre() {
        return nombre;
    }

    // Setter para el nombre. Guarda el cambio.
    public void setNombre(String nombre) {
        this.nombre = nombre;
        guardar();
    }

    public String getApellido() {
        return apellido;
    }

    // Setter para el apellido. Guarda el cambio.
    public void setApellido(String apellido) {
        this.apellido = apellido;
        guardar();
    }

    public String getUsuario() {
        return usuario;
    }

    // Setter para el nombre de usuario. Guarda el cambio.
    public void setUsuario(String usuario) {
        String oldUsuario = this.usuario; // Guarda el nombre de usuario antiguo para buscar el registro.
        this.usuario = usuario;
        guardarInterno(oldUsuario, this.numeroDocumento); // Llama al método interno para actualizar el archivo.
    }

    public String getContrasena() {
        return contrasena;
    }

    // Setter para la contraseña (ya hasheada). Guarda el cambio.
    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
        guardar();
    }

    // Permite a cualquier miembro cambiar su tipo de documento, nombre y apellido.
    public void cambiarInfoPersonal(byte nuevoTipoDocumento, String nuevoNombre, String nuevoApellido) {
        this.setTipoDocumento(nuevoTipoDocumento); // Actualiza y guarda el tipo de documento.
        this.setNombre(nuevoNombre); // Actualiza y guarda el nombre.
        this.setApellido(nuevoApellido); // Actualiza y guarda el apellido.
    }

    // Permite a cualquier miembro cambiar su contraseña. Hashea la nueva contraseña
    // antes de guardarla.
    public void cambiarContrasena(String nuevaContrasena) {
        String hashed = hashPassword(nuevaContrasena); // Hashea la nueva contraseña.
        if (hashed != null && !hashed.isEmpty()) { // Si el hash se generó bien.
            this.setContrasena(hashed); // Guarda la contraseña hasheada.
        } else {
            System.err.println("Error: No se pudo hashear la contrasena. No se realizaron cambios."); // Error si falla
                                                                                                      // el hasheo.
        }
    }

    // --- Metodos de Persistencia ---

    // Devuelve la ruta donde se guarda el archivo de miembros.
    protected static Path resolverRutaMiembros() {
        return PathManager.resolverRutaArchivo("Miembros.txt");
    }

    // Guarda el miembro en el archivo. Es un método seguro (synchronized).
    @Override
    public synchronized void guardar() {
        guardarInterno(null, this.numeroDocumento); // Llama al método interno para guardar.
    }

    // Método interno para guardar/actualizar/eliminar un miembro en el archivo.
    // Maneja si el nombre de usuario o número de documento cambiaron, o si el
    // miembro se va a eliminar.
    protected synchronized void guardarInterno(String oldUsuario, long oldNumeroDocumento) {
        try {
            Path path = resolverRutaMiembros();
            List<String> lines = new ArrayList<>();
            if (Files.exists(path)) { // Si el archivo existe, lee todas sus líneas.
                lines = new ArrayList<>(Files.readAllLines(path, java.nio.charset.StandardCharsets.UTF_8));
            }

            // Limpia las líneas vacías o nulas.
            List<String> normalized = new ArrayList<>();
            for (String l : lines) {
                if (l != null && !l.trim().isEmpty())
                    normalized.add(l);
            }
            lines = normalized;

            boolean found = false; // Bandera para saber si encontramos el miembro.
            for (int i = 0; i < lines.size(); i++) { // Recorre todas las líneas.
                String line = lines.get(i);
                String[] parts = line.split("\\\\");
                if (parts.length >= 7) {
                    String usuarioLinea = parts[5].trim(); // Nombre de usuario de la línea.
                    long numeroDocumentoLinea = -1;
                    try {
                        numeroDocumentoLinea = Long.parseLong(parts[0].trim()); // Número de documento de la línea.
                    } catch (NumberFormatException ex) {
                        // Ignora errores de conversión de número.
                    }
                    boolean match = false; // Bandera para indicar si la línea actual coincide.
                    if (oldUsuario != null && !oldUsuario.trim().isEmpty()) { // Si hay un nombre de usuario antiguo
                                                                              // (posible cambio o eliminación).
                        match = usuarioLinea.equals(oldUsuario.trim()); // Busca por el nombre de usuario antiguo.
                    } else if (this.usuario != null && !this.usuario.trim().isEmpty()) { // Si no hay antiguo, pero sí
                                                                                         // un nombre de usuario actual.
                        match = usuarioLinea.equals(this.usuario.trim()); // Busca por el nombre de usuario actual.
                        // También considera un cambio si el número de documento coincide pero el
                        // usuario es diferente (caso de cambio de solo documento).
                        match = match || (numeroDocumentoLinea == this.numeroDocumento
                                && !usuarioLinea.equals(this.usuario.trim()));
                    } else { // Si el usuario actual es nulo (señal de eliminación).
                        match = (numeroDocumentoLinea == oldNumeroDocumento); // Busca por el número de documento
                                                                              // antiguo.
                    }

                    if (match) { // Si la línea coincide con el miembro que buscamos.
                        if (this.usuario == null || this.usuario.trim().isEmpty()) { // Si el usuario actual es nulo, se
                                                                                     // elimina.
                            lines.remove(i);
                            i--; // Ajusta el índice.
                        } else {
                            lines.set(i, this.construirLinea()); // Si no, actualiza la línea.
                        }
                        found = true;
                        break; // Sale del bucle.
                    }
                }
            }

            if (!found) { // Si el miembro no se encontró (es nuevo).
                if (this.usuario != null && !this.usuario.trim().isEmpty()) { // Y si el usuario tiene un nombre válido.
                    lines.add(this.construirLinea()); // Añade el nuevo miembro al final.
                }
            }

            // Filtra líneas nulas o vacías y reescribe el archivo.
            List<String> toWrite = new ArrayList<>();
            for (String l : lines) {
                if (l != null && !l.trim().isEmpty())
                    toWrite.add(l);
            }
            Files.write(path, toWrite, java.nio.charset.StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // Elimina el miembro del archivo. Es un método seguro (synchronized).
    @Override
    public synchronized void eliminar() {
        String originalUsuario = this.usuario; // Guarda el usuario original.
        this.usuario = null; // Marca el usuario como nulo para que sea eliminado.
        guardarInterno(originalUsuario, this.numeroDocumento); // Llama al método interno para eliminarlo.
    }

    // Convierte el objeto Miembro a una línea de texto para guardarlo en el
    // archivo.
    @Override
    public String construirLinea() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.numeroDocumento).append("\\") // Añade el número de documento.
                .append(this.tipoDocumento).append("\\") // Añade el tipo de documento.
                .append(this.rol).append("\\") // Añade el rol.
                .append(this.nombre == null ? "" : this.nombre).append("\\") // Añade el nombre (o vacío si es nulo).
                .append(this.apellido == null ? "" : this.apellido).append("\\") // Añade el apellido.
                .append(this.usuario == null ? "" : this.usuario).append("\\") // Añade el usuario.
                .append(this.contrasena == null ? "" : this.contrasena); // Añade la contraseña hasheada.
        return sb.toString();
    }

    // Intenta iniciar sesión con el nombre de usuario y contraseña dados.
    public String login(String usuarioIngresado, String contrasenaIngresada) {
        Path path = resolverRutaMiembros();
        if (!Files.exists(path)) { // Si el archivo de miembros no existe.
            System.err.println("Advertencia: Archivo Miembros.txt no encontrado en " + path.toAbsolutePath());
            return ""; // Falla el login.
        }

        try (BufferedReader reader = Files.newBufferedReader(path, java.nio.charset.StandardCharsets.UTF_8)) {
            String linea;
            while ((linea = reader.readLine()) != null) { // Lee línea por línea.
                String[] parts = linea.split("\\\\");
                if (parts.length >= 7) {
                    String usuario = parts[5].trim(); // Nombre de usuario guardado.
                    String contrasenaAlmacenada = parts[6].trim(); // Contraseña hasheada guardada.
                    if (usuario.equals(usuarioIngresado.trim()) // Compara el nombre de usuario.
                            && verificarContrasena(contrasenaIngresada, contrasenaAlmacenada)) { // Verifica la
                                                                                                 // contraseña.
                        return linea; // Si coincide, login exitoso, devuelve la línea completa.
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ""; // Si no se encuentra, login falla.
    }

    // --- Metodos estaticos para cargar miembros ---
    // Carga todos los miembros (Admins y Usuarios) del archivo "Miembros.txt".
    public static List<Miembro> cargarTodosLosMiembros() {
        List<Miembro> miembros = new ArrayList<>(); // Lista para guardar los miembros.
        Path path = resolverRutaMiembros();
        if (!Files.exists(path)) { // Si el archivo no existe.
            return miembros;
        }

        try (BufferedReader reader = Files.newBufferedReader(path, java.nio.charset.StandardCharsets.UTF_8)) {
            String linea;
            while ((linea = reader.readLine()) != null) { // Lee línea por línea.
                String[] parts = linea.split("\\\\");
                if (parts.length >= 7) {
                    try {
                        long numeroDocumento = Long.parseLong(parts[0].trim());
                        byte tipoDocumento = Byte.parseByte(parts[1].trim());
                        byte rol = Byte.parseByte(parts[2].trim());
                        String nombre = parts[3].trim();
                        String apellido = parts[4].trim();
                        String usuario = parts[5].trim();
                        String contrasena = parts[6].trim();

                        if (rol == 0) { // Si el rol es 0, es un Admin.
                            miembros.add(new Admin(tipoDocumento, numeroDocumento, rol, nombre, apellido, usuario,
                                    contrasena)); // Crea un objeto Admin.
                        } else { // Si el rol es diferente de 0, es un Usuario.
                            double deuda = 0.0;
                            String sedeUniversidad = "";
                            String carrera = "";
                            if (parts.length >= 10) { // Si la línea tiene datos adicionales de Usuario.
                                try {
                                    deuda = Double.parseDouble(parts[7].trim().replace(',', '.')); // Parsea la deuda
                                                                                                   // (cambia ',' por
                                                                                                   // '.').
                                    sedeUniversidad = parts[8].trim();
                                    carrera = parts[9].trim();
                                } catch (NumberFormatException e) {
                                    System.err.println("Error de parseo en campos de Usuario en linea: " + linea + " - "
                                            + e.getMessage()); // Error si los datos de Usuario no son válidos.
                                }
                            }
                            miembros.add(new Usuario(tipoDocumento, numeroDocumento, rol, nombre, apellido, usuario,
                                    contrasena, deuda,
                                    sedeUniversidad, carrera)); // Crea un objeto Usuario.
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Error de parseo en linea de miembro: " + linea + " - " + e.getMessage()); // Error
                                                                                                                      // si
                                                                                                                      // los
                                                                                                                      // datos
                                                                                                                      // de
                                                                                                                      // Miembro
                                                                                                                      // no
                                                                                                                      // son
                                                                                                                      // válidos.
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return miembros; // Devuelve la lista de todos los miembros.
    }

    // Busca un miembro por su nombre de usuario.
    public static Miembro encontrarMiembroPorUsuario(String username) {
        List<Miembro> allMembers = cargarTodosLosMiembros();
        for (Miembro miembro : allMembers) {
            if (miembro.getUsuario().equalsIgnoreCase(username.trim())) { // Compara el nombre de usuario.
                return miembro; // Devuelve el miembro si lo encuentra.
            }
        }
        return null; // Si no lo encuentra.
    }

    // Busca un miembro por su número de documento.
    public static Miembro encontrarMiembroPorNumeroDocumento(long numeroDocumento) {
        List<Miembro> allMembers = cargarTodosLosMiembros();
        for (Miembro miembro : allMembers) {
            if (miembro.getNumeroDocumento() == numeroDocumento) { // Compara el número de documento.
                return miembro; // Devuelve el miembro si lo encuentra.
            }
        }
        return null; // Si no lo encuentra.
    }

    // --- Helpers para hashing/verificacion PBKDF2 ---
    // Genera un hash seguro para la contraseña usando el algoritmo PBKDF2.
    // El formato es: iteraciones:saltBase64:hashBase64.
    private String hashPassword(String password) {
        return Miembro.hashPasswordEstatico(password); // Delega el trabajo a un método estático.
    }

    // Método estático para hashear contraseñas, útil para crear usuarios nuevos.
    public static String hashPasswordEstatico(String password) {
        try {
            int iterations = 65536; // Número de veces que se repite el proceso (más seguro).
            int keyLength = 256; // Longitud del hash resultante.
            byte[] salt = new byte[16]; // Valor aleatorio único para cada contraseña (salt).
            new java.security.SecureRandom().nextBytes(salt); // Genera el salt seguro.

            byte[] hash = pbkdf2(password.toCharArray(), salt, iterations, keyLength); // Genera el hash.
            if (hash == null)
                return null;

            String saltB64 = Base64.getEncoder().encodeToString(salt); // Codifica el salt a Base64 (texto).
            String hashB64 = Base64.getEncoder().encodeToString(hash); // Codifica el hash a Base64 (texto).
            return iterations + ":" + saltB64 + ":" + hashB64; // Devuelve el hash completo en formato String.
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    // Verifica si la contraseña ingresada coincide con la guardada (hasheada).
    // Espera que 'stored' esté en el formato iteraciones:saltBase64:hashBase64.
    private boolean verificarContrasena(String password, String stored) {
        try {
            String[] parts = stored.split(":"); // Divide la contraseña guardada en partes.
            if (parts.length != 3) // Si no tiene 3 partes, el formato es incorrecto.
                return false;

            int iterations = Integer.parseInt(parts[0]); // Obtiene las iteraciones.
            byte[] salt = Base64.getDecoder().decode(parts[1]); // Decodifica el salt.
            byte[] hashStored = Base64.getDecoder().decode(parts[2]); // Decodifica el hash guardado.

            byte[] hashAttempt = pbkdf2(password.toCharArray(), salt, iterations, hashStored.length * 8); // Hashea la
                                                                                                          // contraseña
                                                                                                          // ingresada
                                                                                                          // con el
                                                                                                          // mismo salt.
            if (hashAttempt == null)
                return false;

            // Compara los hashes de forma segura para evitar ataques de tiempo.
            int diff = hashStored.length ^ hashAttempt.length;
            for (int i = 0; i < Math.min(hashStored.length, hashAttempt.length); i++) {
                diff |= hashStored[i] ^ hashAttempt[i];
            }
            return diff == 0; // Si son iguales (diff es 0), la contraseña es correcta.
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // Implementación del algoritmo PBKDF2 para generar el hash de la contraseña.
    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLength) {
        try {
            javax.crypto.spec.PBEKeySpec spec = new javax.crypto.spec.PBEKeySpec(password, salt, iterations, keyLength);
            javax.crypto.SecretKeyFactory skf = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return skf.generateSecret(spec).getEncoded(); // Genera y devuelve el hash como bytes.
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    // Devuelve una cadena de texto que representa el objeto Miembro de forma
    // legible.
    @Override
    public String toString() {
        return "Miembro {"
                + "\n  Tipo de Documento: " + tipoDocumento
                + "\n  Numero de Documento: " + numeroDocumento
                + "\n  Rol: " + rol
                + "\n  Nombre: " + nombre
                + "\n  Apellido: " + apellido
                + "\n  Usuario: " + usuario
                + "\n  Contrasena: " + (contrasena != null && !contrasena.isEmpty() ? "[HASHED]" : "[EMPTY]") // Muestra
                                                                                                              // "[HASHED]"
                                                                                                              // por
                                                                                                              // seguridad.
                + "\n}";
    }
}
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.sistemabiblioteca;

/**
 *
 * @author edrui
 */

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

    public Miembro(byte tipoDoc, long noDoc, byte rol, String nombre, String apellido, String usuario,
            String contrasena) {
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

    // Guarda/actualiza utilizando información extra sobre usuario antiguo o nro
    // documento antiguo.
    // oldUsuario puede ser null; oldNoDoc se usa como respaldo para encontrar la
    // línea si oldUsuario es null/empty.
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
                if (l != null && !l.trim().isEmpty())
                    normalized.add(l);
            }
            lines = normalized;

            boolean found = false;
            // Buscamos coincidencias por usuario (preferible) o por número de documento
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                String[] parts = line.split("\\\\");
                if (parts.length >= 7) {
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
                        // si no tenemos oldUsuario, intentar emparejar por el usuario actual
                        // (actualización directa)
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
                            // Reemplazar línea por la representación actual
                            String nuevaLinea = buildLine();
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
                    lines.add(buildLine());
                }
                // Si no se encontró y usuario está vacío -> nada que eliminar
            }

            // Escribir archivo sin líneas vacías
            List<String> toWrite = new ArrayList<>();
            for (String l : lines) {
                if (l != null && !l.trim().isEmpty())
                    toWrite.add(l);
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
            Files.write(path, toWrite, java.nio.charset.StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // Construye la representación en el formato esperado por login:
    // noDoc\tipoDoc\rol\nombre\apellido\ usuario\contrasena
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
            return ""; // File doesn't exist
        }

        try (BufferedReader reader = Files.newBufferedReader(path, java.nio.charset.StandardCharsets.UTF_8)) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                // Separa por '\' (en Java hay que escapar la barra invertida)
                String[] parts = linea.split("\\\\");
                // Según el orden: noDoc(0), tipoDoc(1), rol(2), nombre(3), apellido(4),
                // usuario(5), contrasena(6)
                if (parts.length >= 7) {
                    String usuario = parts[5].trim();
                    String contrasenaAlmacenada = parts[6].trim();
                    if (usuario.equals(usuarioIngresado.trim())
                            && verifyPassword(contrasenaIngresada, contrasenaAlmacenada)) {
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
                if (parts.length >= 7) {
                    try {
                        long noDoc = Long.parseLong(parts[0].trim());
                        byte tipoDoc = Byte.parseByte(parts[1].trim());
                        byte rol = Byte.parseByte(parts[2].trim());
                        String nombre = parts[3].trim();
                        String apellido = parts[4].trim();
                        String usuario = parts[5].trim();
                        String contrasena = parts[6].trim(); // This is the hashed password

                        // Construye la instancia correcta basada en el rol
                        if (rol == 0) {
                            miembros.add(new Admin(tipoDoc, noDoc, rol, nombre, apellido, usuario, contrasena));
                        } else {
                            miembros.add(new Usuario(tipoDoc, noDoc, rol, nombre, apellido, usuario, contrasena));
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
            if (hash == null)
                return null;

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
            if (parts.length != 3)
                return false;
            // Recupera las iteraciones y decodifica el salt y el hash almacenado
            int iterations = Integer.parseInt(parts[0]);
            byte[] salt = Base64.getDecoder().decode(parts[1]);
            byte[] hashStored = Base64.getDecoder().decode(parts[2]);

            // Calcula el hash de intento con los mismos parámetros (longitud en bits)
            byte[] hashAttempt = pbkdf2(password.toCharArray(), salt, iterations, hashStored.length * 8);
            if (hashAttempt == null)
                return false;

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
    // - password: contraseña como array de caracteres
    // - salt: salt aleatorio
    // - iterations: número de iteraciones del algoritmo
    // - keyLength: longitud de la clave en bits
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
                + "\n  Contraseña: " + (contrasena != null && !contrasena.isEmpty() ? "[HASHED]" : "[EMPTY]") // Avoid
                                                                                                              // printing
                                                                                                              // raw
                                                                                                              // hash
                + "\n}";
    }
}
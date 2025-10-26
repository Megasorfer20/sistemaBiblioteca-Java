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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Clase base para representar a un miembro del sistema (Admin o Usuario).
 * Implementa IPersistible para el manejo de guardado y eliminacion.
 *
 * @author edrui
 */
public class Miembro implements IPersistible {

    protected long numeroDocumento;
    protected byte tipoDocumento;
    protected byte rol; // 0: Admin, 1: Estudiante, 2: Profesor, 3: Administrativo Universidad
    protected String nombre;
    protected String apellido;
    protected String usuario;
    protected String contrasena; // Almacena el hash de la contrasena

    public Miembro() {
    }

    public Miembro(byte tipoDocumento, long numeroDocumento, byte rol, String nombre, String apellido, String usuario,
            String contrasena) {
        this.tipoDocumento = tipoDocumento;
        this.numeroDocumento = numeroDocumento;
        this.rol = rol;
        this.nombre = nombre;
        this.apellido = apellido;
        this.usuario = usuario;

        String hashed = hashPassword(contrasena);
        if (hashed != null && !hashed.isEmpty()) {
            this.contrasena = hashed;
        } else {
            this.contrasena = contrasena; // Fallback: si falla el hash, guarda la original (no recomendado para
                                          // produccion)
        }
    }

    public long getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(long numeroDocumento) {
        long oldNumeroDocumento = this.numeroDocumento;
        this.numeroDocumento = numeroDocumento;
        guardarInterno(null, oldNumeroDocumento);
    }

    public byte getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(byte tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
        guardar();
    }

    public byte getRol() {
        return rol;
    }

    public void setRol(byte rol) {
        this.rol = rol;
        guardar();
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
        guardar();
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
        guardar();
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        String oldUsuario = this.usuario;
        this.usuario = usuario;
        guardarInterno(oldUsuario, this.numeroDocumento);
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) { // Este setter espera el hash o no lo cambia
        this.contrasena = contrasena;
        guardar();
    }

    /**
     * Permite a cualquier miembro cambiar su informacion personal.
     * No permite cambiar el rol.
     * 
     * @param nuevoTipoDocumento El nuevo tipo de documento.
     * @param nuevoNombre        El nuevo nombre.
     * @param nuevoApellido      El nuevo apellido.
     */
    public void cambiarInfoPersonal(byte nuevoTipoDocumento, String nuevoNombre, String nuevoApellido) {
        this.setTipoDocumento(nuevoTipoDocumento); // Esto llama a guardar()
        this.setNombre(nuevoNombre); // Esto llama a guardar()
        this.setApellido(nuevoApellido); // Esto llama a guardar()
    }

    /**
     * Permite a cualquier miembro cambiar su contrasena.
     * 
     * @param nuevaContrasena La nueva contrasena en texto plano.
     */
    public void cambiarContrasena(String nuevaContrasena) {
        String hashed = hashPassword(nuevaContrasena);
        if (hashed != null && !hashed.isEmpty()) {
            this.setContrasena(hashed); // setContrasena ya llama a guardar()
        } else {
            System.err.println("Error: No se pudo hashear la contrasena. No se realizaron cambios.");
        }
    }

    // --- Metodos de Persistencia ---

    protected static Path resolverRutaMiembros() {
        return PathManager.resolverRutaArchivo("Miembros.txt");
    }

    @Override
    public synchronized void guardar() {
        guardarInterno(null, this.numeroDocumento);
    }

    /**
     * Metodo interno para guardar/actualizar, maneja cambios de usuario o
     * numeroDocumento o eliminaciones.
     */
    protected synchronized void guardarInterno(String oldUsuario, long oldNumeroDocumento) {
        try {
            Path path = resolverRutaMiembros();
            List<String> lines = new ArrayList<>();
            if (Files.exists(path)) {
                lines = new ArrayList<>(Files.readAllLines(path, java.nio.charset.StandardCharsets.UTF_8));
            }

            List<String> normalized = new ArrayList<>();
            for (String l : lines) {
                if (l != null && !l.trim().isEmpty())
                    normalized.add(l);
            }
            lines = normalized;

            boolean found = false;
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                String[] parts = line.split("\\\\");
                if (parts.length >= 7) {
                    String usuarioLinea = parts[5].trim();
                    long numeroDocumentoLinea = -1;
                    try {
                        numeroDocumentoLinea = Long.parseLong(parts[0].trim());
                    } catch (NumberFormatException ex) {
                        // ignorar parseo si falla
                    }
                    boolean match = false;
                    if (oldUsuario != null && !oldUsuario.trim().isEmpty()) {
                        match = usuarioLinea.equals(oldUsuario.trim());
                    } else if (this.usuario != null && !this.usuario.trim().isEmpty()) {
                        match = usuarioLinea.equals(this.usuario.trim());
                        // Tambien para el caso de cambio de numero de documento donde el usuario no ha
                        // cambiado
                        match = match || (numeroDocumentoLinea == this.numeroDocumento
                                && !usuarioLinea.equals(this.usuario.trim()));
                    } else { // Si el usuario actual es null/vacio, buscar por numero de documento
                        match = (numeroDocumentoLinea == oldNumeroDocumento);
                    }

                    if (match) {
                        if (this.usuario == null || this.usuario.trim().isEmpty()) {
                            lines.remove(i);
                            i--;
                        } else {
                            // Reemplazar linea por la representacion actual.
                            lines.set(i, this.construirLinea());
                        }
                        found = true;
                        break;
                    }
                }
            }

            if (!found) {
                if (this.usuario != null && !this.usuario.trim().isEmpty()) {
                    lines.add(this.construirLinea());
                }
            }

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

    @Override
    public synchronized void eliminar() {
        String originalUsuario = this.usuario;
        this.usuario = null;
        guardarInterno(originalUsuario, this.numeroDocumento);
    }

    @Override
    public String construirLinea() {
        // Usa `this` para que las subclases puedan sobrescribir y anadir mas campos
        StringBuilder sb = new StringBuilder();
        sb.append(this.numeroDocumento).append("\\")
                .append(this.tipoDocumento).append("\\")
                .append(this.rol).append("\\")
                .append(this.nombre == null ? "" : this.nombre).append("\\")
                .append(this.apellido == null ? "" : this.apellido).append("\\")
                .append(this.usuario == null ? "" : this.usuario).append("\\")
                .append(this.contrasena == null ? "" : this.contrasena);
        return sb.toString();
    }

    /**
     * Intenta loguear a un usuario.
     * 
     * @param usuarioIngresado    El nombre de usuario.
     * @param contrasenaIngresada La contrasena en texto plano.
     * @return La linea completa del miembro si el login es exitoso, cadena vacia en
     *         caso contrario.
     */
    public String login(String usuarioIngresado, String contrasenaIngresada) {
        Path path = resolverRutaMiembros();
        if (!Files.exists(path)) {
            System.err.println("Advertencia: Archivo Miembros.txt no encontrado en " + path.toAbsolutePath());
            return "";
        }

        try (BufferedReader reader = Files.newBufferedReader(path, java.nio.charset.StandardCharsets.UTF_8)) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] parts = linea.split("\\\\");
                if (parts.length >= 7) {
                    String usuario = parts[5].trim();
                    String contrasenaAlmacenada = parts[6].trim();
                    if (usuario.equals(usuarioIngresado.trim())
                            && verificarContrasena(contrasenaIngresada, contrasenaAlmacenada)) {
                        return linea;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    // --- Metodos estaticos para cargar miembros ---
    public static List<Miembro> cargarTodosLosMiembros() {
        List<Miembro> miembros = new ArrayList<>();
        Path path = resolverRutaMiembros();
        if (!Files.exists(path)) {
            return miembros;
        }

        try (BufferedReader reader = Files.newBufferedReader(path, java.nio.charset.StandardCharsets.UTF_8)) {
            String linea;
            while ((linea = reader.readLine()) != null) {
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

                        if (rol == 0) {
                            miembros.add(new Admin(tipoDocumento, numeroDocumento, rol, nombre, apellido, usuario,
                                    contrasena));
                        } else {
                            double deuda = 0.0;
                            String sedeUniversidad = "";
                            String carrera = "";
                            if (parts.length >= 10) {
                                try {
                                    deuda = Double.parseDouble(parts[7].trim());
                                    sedeUniversidad = parts[8].trim();
                                    carrera = parts[9].trim();
                                } catch (NumberFormatException e) {
                                    System.err.println("Error de parseo en campos de Usuario en linea: " + linea + " - "
                                            + e.getMessage());
                                }
                            }
                            miembros.add(new Usuario(tipoDocumento, numeroDocumento, rol, nombre, apellido, usuario,
                                    contrasena, deuda,
                                    sedeUniversidad, carrera));
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Error de parseo en linea de miembro: " + linea + " - " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return miembros;
    }

    public static Miembro encontrarMiembroPorUsuario(String username) {
        List<Miembro> allMembers = cargarTodosLosMiembros();
        for (Miembro miembro : allMembers) {
            if (miembro.getUsuario().equalsIgnoreCase(username.trim())) {
                return miembro;
            }
        }
        return null;
    }

    public static Miembro encontrarMiembroPorNumeroDocumento(long numeroDocumento) {
        List<Miembro> allMembers = cargarTodosLosMiembros();
        for (Miembro miembro : allMembers) {
            if (miembro.getNumeroDocumento() == numeroDocumento) {
                return miembro;
            }
        }
        return null;
    }

    // --- Helpers para hashing/verificacion PBKDF2 ---
    /**
     * Genera un hash seguro para la contrasena usando PBKDF2WithHmacSHA256.
     * Formato: iteraciones:saltBase64:hashBase64
     */
    private String hashPassword(String password) {
        return Miembro.hashPasswordEstatico(password); // Delega a un metodo estatico para reusabilidad
    }

    /**
     * Metodo estatico para hashear contrasenas, util para la creacion inicial de
     * usuarios.
     */
    public static String hashPasswordEstatico(String password) {
        try {
            int iterations = 65536;
            int keyLength = 256;
            byte[] salt = new byte[16];
            new java.security.SecureRandom().nextBytes(salt);

            byte[] hash = pbkdf2(password.toCharArray(), salt, iterations, keyLength);
            if (hash == null)
                return null;

            String saltB64 = Base64.getEncoder().encodeToString(salt);
            String hashB64 = Base64.getEncoder().encodeToString(hash);
            return iterations + ":" + saltB64 + ":" + hashB64;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Verifica si la contrasena indicada coincide con la representacion almacenada.
     * Espera que 'stored' tenga el formato iteraciones:saltBase64:hashBase64
     */
    private boolean verificarContrasena(String password, String stored) {
        try {
            String[] parts = stored.split(":");
            if (parts.length != 3)
                return false;

            int iterations = Integer.parseInt(parts[0]);
            byte[] salt = Base64.getDecoder().decode(parts[1]);
            byte[] hashStored = Base64.getDecoder().decode(parts[2]);

            byte[] hashAttempt = pbkdf2(password.toCharArray(), salt, iterations, hashStored.length * 8);
            if (hashAttempt == null)
                return false;

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

    /**
     * Implementacion de PBKDF2 que devuelve los bytes derivados.
     */
    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLength) {
        try {
            javax.crypto.spec.PBEKeySpec spec = new javax.crypto.spec.PBEKeySpec(password, salt, iterations, keyLength);
            javax.crypto.SecretKeyFactory skf = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return skf.generateSecret(spec).getEncoded();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        return "Miembro {"
                + "\n  Tipo de Documento: " + tipoDocumento
                + "\n  Numero de Documento: " + numeroDocumento
                + "\n  Rol: " + rol
                + "\n  Nombre: " + nombre
                + "\n  Apellido: " + apellido
                + "\n  Usuario: " + usuario
                + "\n  Contrasena: " + (contrasena != null && !contrasena.isEmpty() ? "[HASHED]" : "[EMPTY]")
                + "\n}";
    }
}
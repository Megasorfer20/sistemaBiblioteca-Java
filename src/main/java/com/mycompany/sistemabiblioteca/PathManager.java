package com.mycompany.sistemabiblioteca;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author edrui
 */

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Clase de utilidad para resolver las rutas absolutas de los archivos de persistencia.
 * Esto asegura que los archivos se encuentren correctamente tanto en entorno de desarrollo
 * como si la aplicacion se empaqueta en un JAR.
 *
 * @author edrui
 */
public class PathManager {

    private static final String ARCHIVOS_DIR = "datos"; // Directorio dentro del cual se guardaran los .txt

    /**
     * Resuelve la ruta para un nombre de archivo dado.
     * Intenta encontrar el archivo en el classpath primero (util para JARs).
     * Si no, lo crea en un subdirectorio "datos" en el directorio de trabajo del usuario.
     *
     * @param fileName El nombre del archivo (e.g., "Miembros.txt").
     * @return La ruta absoluta al archivo.
     */
    public static Path resolverRutaArchivo(String fileName) {
        // Intenta obtener el recurso desde el classpath (ej. dentro de un JAR)
        try {
            java.net.URL res = PathManager.class.getClassLoader().getResource(ARCHIVOS_DIR + "/" + fileName);
            if (res != null) {
                if ("file".equalsIgnoreCase(res.getProtocol())) {
                    return Paths.get(res.toURI());
                } else {
                    // Si no es un archivo del sistema (ej. dentro de un JAR), no podemos escribir directamente
                    // Asi que se intentara crear en el directorio de trabajo
                    return crearRutaEnDirectorioDeTrabajo(ARCHIVOS_DIR, fileName);
                }
            }
        } catch (Exception ex) {
            // Ignorar y caer al fallback de directorio de trabajo
        }
        // Fallback: Si no se encuentra en el classpath o hay error, usar el directorio de trabajo
        return crearRutaEnDirectorioDeTrabajo(ARCHIVOS_DIR, fileName);
    }

    /**
     * Crea y devuelve una ruta a un archivo dentro de un subdirectorio
     * en el directorio de trabajo del usuario. Asegura que el subdirectorio exista.
     *
     * @param subDirectory El nombre del subdirectorio (e.g., "datos").
     * @param fileName     El nombre del archivo.
     * @return La ruta absoluta al archivo.
     */
    private static Path crearRutaEnDirectorioDeTrabajo(String subDirectory, String fileName) {
        Path path = Paths.get(System.getProperty("user.dir"), subDirectory, fileName);
        Path parent = path.getParent();
        if (parent != null && !Files.exists(parent)) {
            try {
                Files.createDirectories(parent);
                // System.out.println("Directorio creado: " + parent.toAbsolutePath()); // Solo para depuracion
            } catch (IOException ex) {
                System.err.println("Error al crear el directorio " + parent.toAbsolutePath() + ": " + ex.getMessage());
            }
        }
        return path;
    }
}

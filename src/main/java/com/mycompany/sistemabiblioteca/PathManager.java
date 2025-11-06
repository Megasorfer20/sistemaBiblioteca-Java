package com.mycompany.sistemabiblioteca; // Carpeta donde está organizado nuestro código.

import java.io.IOException; // Para manejar errores al trabajar con archivos.
import java.nio.file.Files; // Para operar con archivos y directorios.
import java.nio.file.Path; // Para representar la ruta de un archivo.
import java.nio.file.Paths; // Para construir rutas de archivos.

// Esta clase es una herramienta para encontrar las rutas de los archivos de datos.
// Asegura que los archivos se encuentren bien, tanto si el programa está en desarrollo
// como si está empaquetado en un archivo JAR.
public class PathManager {

    private static final String ARCHIVOS_DIR = "datos"; // Nombre de la carpeta donde se guardarán los archivos de
                                                        // texto.

    // Encuentra la ruta de un archivo. Primero busca dentro del programa (si es un
    // JAR).
    // Si no lo encuentra o no se puede escribir allí, lo crea en una carpeta
    // "datos"
    // en la misma ubicación donde se está ejecutando el programa.
    public static Path resolverRutaArchivo(String fileName) {
        // Intenta encontrar el archivo como un "recurso" del programa (útil en JARs).
        try {
            java.net.URL res = PathManager.class.getClassLoader().getResource(ARCHIVOS_DIR + "/" + fileName);
            if (res != null) { // Si se encontró el recurso.
                if ("file".equalsIgnoreCase(res.getProtocol())) { // Si es un archivo directamente accesible.
                    return Paths.get(res.toURI()); // Devuelve esa ruta.
                } else {
                    // Si el recurso está dentro de un JAR y no se puede escribir, intenta crear el
                    // archivo externo.
                    return crearRutaEnDirectorioDeTrabajo(ARCHIVOS_DIR, fileName);
                }
            }
        } catch (Exception ex) {
            // Si ocurre un error al buscar el recurso, lo ignora y pasa a la siguiente
            // opción.
        }
        // Si no se encontró el recurso o hubo un error, crea el archivo en el
        // directorio de trabajo.
        return crearRutaEnDirectorioDeTrabajo(ARCHIVOS_DIR, fileName);
    }

    // Crea y devuelve la ruta a un archivo dentro de una subcarpeta
    // en el directorio donde se ejecuta el programa. Se asegura de que la
    // subcarpeta exista.
    private static Path crearRutaEnDirectorioDeTrabajo(String subDirectory, String fileName) {
        // Construye la ruta: directorio actual + subcarpeta + nombre del archivo.
        Path path = Paths.get(System.getProperty("user.dir"), subDirectory, fileName);
        Path parent = path.getParent(); // Obtiene la carpeta contenedora (ej. "datos").
        if (parent != null && !Files.exists(parent)) { // Si la carpeta no existe.
            try {
                Files.createDirectories(parent); // Crea la carpeta (y las carpetas intermedias si es necesario).
                // System.out.println("Directorio creado: " + parent.toAbsolutePath()); // Línea
                // para depuración.
            } catch (IOException ex) {
                System.err.println("Error al crear el directorio " + parent.toAbsolutePath() + ": " + ex.getMessage()); // Muestra
                                                                                                                        // error
                                                                                                                        // si
                                                                                                                        // no
                                                                                                                        // se
                                                                                                                        // pudo
                                                                                                                        // crear.
            }
        }
        return path; // Devuelve la ruta completa del archivo.
    }
}
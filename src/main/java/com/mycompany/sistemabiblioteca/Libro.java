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
    public Libro(String nombre, String autor, String codigo, int unLibres, int unPrestadas, int idBiblioteca,
            String sedeBiblioteca) {
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

    // Método interno para guardar/actualizar, maneja cambios de código o
    // eliminaciones.
    // oldCodigo se usa cuando el código del libro ha cambiado o para encontrar la
    // línea original.
    protected synchronized void saveInternal(String oldCodigo) {
        try {
            Path path = resolveLibrosPath();
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
            String searchCodigo = (oldCodigo != null && !oldCodigo.trim().isEmpty()) ? oldCodigo : this.codigo;

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                String[] parts = line.split("\\\\");
                if (parts.length >= 7) { // Asegura que haya suficientes partes para un libro
                    String libroCodigo = parts[0].trim();
                    if (libroCodigo.equals(searchCodigo)) {
                        if (this.codigo == null || this.codigo.trim().isEmpty()) { // Si el código es null, significa
                                                                                   // eliminación
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
            Files.write(path, lines, java.nio.charset.StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

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

                        libros.add(
                                new Libro(nombre, autor, codigo, unLibres, unPrestadas, idBiblioteca, sedeBiblioteca));
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
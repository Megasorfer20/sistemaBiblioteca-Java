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
import java.util.List;
import java.util.stream.Collectors;

/**
 * Clase para representar un libro en la biblioteca.
 * Implementa IPersistible para el manejo de guardado y eliminacion.
 *
 * @author edrui
 */
public class Libro implements IPersistible {
    private String nombre;
    private String autor;
    private String codigo; // ID del libro
    private int unidadesLibres;
    private int unidadesPrestadas;
    private int idBiblioteca; // ID de la biblioteca a la que pertenece el libro
    private String sedeBiblioteca; // Sede de la biblioteca a la que pertenece el libro

    public Libro() {
    }

    // Constructor para la creacion inicial de un libro (con codigo generado o
    // provisto)
    public Libro(String nombre, String autor, String codigo, int unidadesTotales, int idBiblioteca,
            String sedeBiblioteca) {
        this.nombre = nombre;
        this.autor = autor;
        this.codigo = codigo;
        this.unidadesLibres = unidadesTotales;
        this.unidadesPrestadas = 0;
        this.idBiblioteca = idBiblioteca;
        this.sedeBiblioteca = sedeBiblioteca;
    }

    // Constructor para cargar un libro desde archivo
    public Libro(String nombre, String autor, String codigo, int unidadesLibres, int unidadesPrestadas,
            int idBiblioteca,
            String sedeBiblioteca) {
        this.nombre = nombre;
        this.autor = autor;
        this.codigo = codigo;
        this.unidadesLibres = unidadesLibres;
        this.unidadesPrestadas = unidadesPrestadas;
        this.idBiblioteca = idBiblioteca;
        this.sedeBiblioteca = sedeBiblioteca;
    }

    // --- Metodo para generar un codigo unico y auto-incremental ---
    /**
     * Genera el siguiente codigo de libro unico y auto-incremental para una
     * biblioteca dada.
     * Formato: ID_BIB-INICIAL_SEDE-NUMERO_SECUENCIAL
     */
    public static String generarSiguienteCodigoLibro(int idBiblioteca, String sedeBiblioteca) {
        List<Libro> todosLosLibros = cargarTodosLosLibros();
        long maxNumeroSecuencial = 0;

        // El codigo debe ser "Id de la biblioteca - Inicial de la localidad de la
        // Biblioteca - Cantidad de libros - Id atoincremental del libro"
        // Interpreto "Cantidad de libros" como parte del numero secuencial o
        // simplemente el numero secuencial mismo.
        // Para que sea un ID estable, usaremos: ID_BIB-INICIAL_SEDE-NUMERO_SECUENCIAL
        // Donde el numero secuencial es incremental y unico dentro de esa biblioteca (o
        // global si se desea).
        // Optare por un numero secuencial global para simplificar, pero filtrando por
        // el prefijo para mantener el formato.

        String prefijoLibro = String.valueOf(idBiblioteca) + "-" + sedeBiblioteca.substring(0, 1).toUpperCase();

        for (Libro libro : todosLosLibros) {
            if (libro.getCodigo().startsWith(prefijoLibro)) {
                String[] parts = libro.getCodigo().split("-");
                if (parts.length == 3) {
                    try {
                        long currentNum = Long.parseLong(parts[2]);
                        if (currentNum > maxNumeroSecuencial) {
                            maxNumeroSecuencial = currentNum;
                        }
                    } catch (NumberFormatException e) {
                        // Ignorar codigos mal formados
                    }
                }
            }
        }

        // Para ser mas robustos y globales, podriamos revisar todos los codigos si el
        // formato siempre tiene 3 partes
        // y el ultimo es el numerico. Para el ejemplo, asumiremos que los codigos
        // generados seguiran el formato.
        // Para una maxima robustez global independiente de la biblioteca, solo tomar el
        // maximo de todos los numeros finales.
        if (maxNumeroSecuencial == 0) { // Si no hay libros, o no hay libros con el prefijo, buscar el maximo de todos.
            for (Libro libro : todosLosLibros) {
                String[] parts = libro.getCodigo().split("-");
                if (parts.length >= 2) { // Asumimos que al menos el ultimo es numerico
                    try {
                        // Intentamos tomar la ultima parte como ID secuencial, si no, un UUID.
                        long currentNum = Long.parseLong(parts[parts.length - 1]);
                        if (currentNum > maxNumeroSecuencial) {
                            maxNumeroSecuencial = currentNum;
                        }
                    } catch (NumberFormatException e) {
                        // Ignorar codigos mal formados o que no son numericos al final.
                    }
                }
            }
        }

        return prefijoLibro + "-" + (maxNumeroSecuencial + 1);
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

    public int getUnidadesLibres() {
        return unidadesLibres;
    }

    public int getUnidadesPrestadas() {
        return unidadesPrestadas;
    }

    public int getIdBiblioteca() {
        return idBiblioteca;
    }

    public String getSedeBiblioteca() {
        return sedeBiblioteca;
    }

    // Setters (que invocan guardar() si modifican un atributo persistente)
    public void setNombre(String nombre) {
        this.nombre = nombre;
        guardar();
    }

    public void setAutor(String autor) {
        this.autor = autor;
        guardar();
    }

    public void setCodigo(String codigo) {
        String oldCodigo = this.codigo;
        this.codigo = codigo;
        guardarInterno(oldCodigo);
    }

    public void setUnidadesLibres(int unidadesLibres) {
        this.unidadesLibres = unidadesLibres;
        guardar();
    }

    public void setUnidadesPrestadas(int unidadesPrestadas) {
        this.unidadesPrestadas = unidadesPrestadas;
        guardar();
    }

    public void setIdBiblioteca(int idBiblioteca) {
        this.idBiblioteca = idBiblioteca;
        guardar();
    }

    public void setSedeBiblioteca(String sedeBiblioteca) {
        this.sedeBiblioteca = sedeBiblioteca;
        guardar();
    }

    // --- Metodos de Persistencia ---

    protected static Path resolverRutaLibros() {
        return PathManager.resolverRutaArchivo("Libros.txt");
    }

    @Override
    public String construirLinea() {
        return String.format("%s\\%s\\%s\\%d\\%d\\%d\\%s",
                codigo, nombre, autor, unidadesLibres, unidadesPrestadas, idBiblioteca, sedeBiblioteca);
    }

    @Override
    public synchronized void guardar() {
        guardarInterno(null);
    }

    /**
     * Metodo interno para guardar/actualizar, maneja cambios de codigo o
     * eliminaciones.
     * oldCodigo se usa cuando el codigo del libro ha cambiado o para encontrar la
     * linea original.
     */
    protected synchronized void guardarInterno(String oldCodigo) {
        try {
            Path path = resolverRutaLibros();
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
                if (parts.length >= 7) {
                    String libroCodigo = parts[0].trim();
                    if (libroCodigo.equals(searchCodigo)) {
                        if (this.codigo == null || this.codigo.trim().isEmpty()) {
                            lines.remove(i);
                            i--;
                        } else {
                            lines.set(i, construirLinea());
                        }
                        found = true;
                        break;
                    }
                }
            }

            if (!found) {
                if (this.codigo != null && !this.codigo.trim().isEmpty()) {
                    lines.add(construirLinea());
                }
            }

            Files.write(path, lines, java.nio.charset.StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public synchronized void eliminar() {
        String originalCodigo = this.codigo;
        this.codigo = null;
        guardarInterno(originalCodigo);
    }

    /**
     * Carga todos los libros del archivo Libros.txt.
     */
    public static List<Libro> cargarTodosLosLibros() {
        List<Libro> libros = new ArrayList<>();
        Path path = resolverRutaLibros();
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
                        int uniLibres = Integer.parseInt(parts[3].trim());
                        int uniPrestadas = Integer.parseInt(parts[4].trim());
                        int idBiblioteca = Integer.parseInt(parts[5].trim());
                        String sedeBiblioteca = parts[6].trim();

                        libros.add(
                                new Libro(nombre, autor, codigo, uniLibres, uniPrestadas, idBiblioteca,
                                        sedeBiblioteca));
                    } catch (NumberFormatException e) {
                        System.err.println("Error de parseo en linea de libro: " + linea + " - " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return libros;
    }

    /**
     * Encuentra un libro por su codigo.
     */
    public static Libro encontrarLibroPorCodigo(String code) {
        List<Libro> allBooks = cargarTodosLosLibros();
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
                ", Unidades Libres=" + unidadesLibres +
                ", Unidades Prestadas=" + unidadesPrestadas +
                ", ID Biblioteca=" + idBiblioteca +
                ", Sede Biblioteca='" + sedeBiblioteca + '\'' +
                '}';
    }
}
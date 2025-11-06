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

// Esta clase representa un libro en la biblioteca.
// Sabe cómo guardarse y eliminarse porque implementa 'IPersistible'.
public class Libro implements IPersistible {
    private String nombre; // Título del libro.
    private String autor; // Autor del libro.
    private String codigo; // Código único del libro (como un ID).
    private int unidadesLibres; // Copias disponibles para prestar.
    private int unidadesPrestadas; // Copias que están prestadas.
    private int idBiblioteca; // ID de la biblioteca a la que pertenece.
    private String sedeBiblioteca; // Sede de la biblioteca a la que pertenece.

    public Libro() { // Constructor vacío.
    }

    // Constructor para crear un libro nuevo.
    public Libro(String nombre, String autor, String codigo, int unidadesTotales, int idBiblioteca,
            String sedeBiblioteca) {
        this.nombre = nombre;
        this.autor = autor;
        this.codigo = codigo;
        this.unidadesLibres = unidadesTotales; // Todas las unidades son libres al inicio.
        this.unidadesPrestadas = 0; // Ninguna prestada al inicio.
        this.idBiblioteca = idBiblioteca;
        this.sedeBiblioteca = sedeBiblioteca;
    }

    // Constructor para cargar un libro desde un archivo.
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
    // Genera un código único para un libro, siguiendo el formato:
    // ID_BIB-INICIAL_SEDE-NUMERO_SECUENCIAL.
    public static String generarSiguienteCodigoLibro(int idBiblioteca, String sedeBiblioteca) {
        List<Libro> todosLosLibros = cargarTodosLosLibros(); // Carga todos los libros existentes.
        long maxNumeroSecuencial = 0; // Empieza a contar desde 0.

        // Crea el prefijo del código (ej. "1-M" para ID 1 y sede "Medellin").
        String prefijoLibro = String.valueOf(idBiblioteca) + "-" + sedeBiblioteca.substring(0, 1).toUpperCase();

        // Busca el número secuencial más alto usado con este prefijo.
        for (Libro libro : todosLosLibros) {
            if (libro.getCodigo().startsWith(prefijoLibro)) { // Si el código del libro empieza con el prefijo.
                String[] parts = libro.getCodigo().split("-"); // Divide el código por los guiones.
                if (parts.length == 3) { // Si tiene 3 partes (ej. ID-SEDE-NUMERO).
                    try {
                        long currentNum = Long.parseLong(parts[2]); // La tercera parte es el número secuencial.
                        if (currentNum > maxNumeroSecuencial) { // Si es mayor al máximo encontrado.
                            maxNumeroSecuencial = currentNum; // Lo actualiza.
                        }
                    } catch (NumberFormatException e) {
                        // Ignora códigos mal formados.
                    }
                }
            }
        }

        // Si no se encontró un número secuencial con el prefijo, busca el máximo entre
        // todos los libros.
        if (maxNumeroSecuencial == 0) {
            for (Libro libro : todosLosLibros) {
                String[] parts = libro.getCodigo().split("-");
                if (parts.length >= 2) { // Asume que la última parte es un número.
                    try {
                        long currentNum = Long.parseLong(parts[parts.length - 1]);
                        if (currentNum > maxNumeroSecuencial) {
                            maxNumeroSecuencial = currentNum;
                        }
                    } catch (NumberFormatException e) {
                        // Ignora códigos que no terminan en número.
                    }
                }
            }
        }

        return prefijoLibro + "-" + (maxNumeroSecuencial + 1); // Devuelve el nuevo código (prefijo + siguiente número).
    }

    // Getters para los atributos del libro.
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

    // Setters que guardan el libro automáticamente después de un cambio.
    public void setNombre(String nombre) {
        this.nombre = nombre;
        guardar();
    }

    public void setAutor(String autor) {
        this.autor = autor;
        guardar();
    }

    public void setCodigo(String codigo) {
        String oldCodigo = this.codigo; // Guarda el código antiguo.
        this.codigo = codigo;
        guardarInterno(oldCodigo); // Usa el método interno para manejar el cambio de código en el archivo.
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

    // Devuelve la ruta donde se guarda el archivo de libros.
    protected static Path resolverRutaLibros() {
        return PathManager.resolverRutaArchivo("Libros.txt");
    }

    // Convierte el objeto Libro a una línea de texto para guardarlo en el archivo.
    @Override
    public String construirLinea() {
        return String.format("%s\\%s\\%s\\%d\\%d\\%d\\%s",
                codigo, nombre, autor, unidadesLibres, unidadesPrestadas, idBiblioteca, sedeBiblioteca);
    }

    // Guarda el libro en el archivo. Es un método seguro (synchronized).
    @Override
    public synchronized void guardar() {
        guardarInterno(null); // Llama al método interno sin un código antiguo (asume que el código no
                              // cambió).
    }

    // Método interno para guardar/actualizar/eliminar un libro en el archivo.
    // Maneja si el código del libro cambió o si el libro se va a eliminar.
    protected synchronized void guardarInterno(String oldCodigo) {
        try {
            Path path = resolverRutaLibros();
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

            boolean found = false; // Bandera para saber si encontramos el libro.
            // Usa el código antiguo (si se dio) o el actual para buscar el libro en el
            // archivo.
            String searchCodigo = (oldCodigo != null && !oldCodigo.trim().isEmpty()) ? oldCodigo : this.codigo;

            for (int i = 0; i < lines.size(); i++) { // Recorre todas las líneas.
                String line = lines.get(i);
                String[] parts = line.split("\\\\");
                if (parts.length >= 7) {
                    String libroCodigo = parts[0].trim();
                    if (libroCodigo.equals(searchCodigo)) { // Si encuentra el libro.
                        if (this.codigo == null || this.codigo.trim().isEmpty()) { // Si el código actual es nulo,
                                                                                   // significa que hay que eliminarlo.
                            lines.remove(i); // Elimina la línea.
                            i--; // Ajusta el índice.
                        } else {
                            lines.set(i, construirLinea()); // Si no es nulo, actualiza la línea con la nueva
                                                            // información.
                        }
                        found = true; // Marca que se encontró.
                        break; // Sale del bucle.
                    }
                }
            }

            if (!found) { // Si el libro no se encontró (es nuevo).
                if (this.codigo != null && !this.codigo.trim().isEmpty()) { // Y si tiene un código válido.
                    lines.add(construirLinea()); // Añade el nuevo libro al final.
                }
            }

            // Vuelve a escribir todas las líneas (actualizadas o nuevas) en el archivo,
            // sobrescribiendo el anterior.
            Files.write(path, lines, java.nio.charset.StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

        } catch (IOException ex) { // Si hay un error al leer/escribir.
            ex.printStackTrace(); // Imprime el error.
        }
    }

    // Elimina el libro del archivo. Es un método seguro (synchronized).
    @Override
    public synchronized void eliminar() {
        String originalCodigo = this.codigo; // Guarda el código antes de cambiarlo a nulo.
        this.codigo = null; // Pone el código a nulo, señalando que debe eliminarse.
        guardarInterno(originalCodigo); // Llama al método interno para que borre el registro con el código original.
    }

    // Carga todos los libros del archivo "Libros.txt" y los devuelve en una lista.
    public static List<Libro> cargarTodosLosLibros() {
        List<Libro> libros = new ArrayList<>(); // Lista para guardar los libros.
        Path path = resolverRutaLibros(); // Obtiene la ruta del archivo.
        if (!Files.exists(path)) { // Si el archivo no existe.
            return libros; // Devuelve una lista vacía.
        }

        try (BufferedReader reader = Files.newBufferedReader(path, java.nio.charset.StandardCharsets.UTF_8)) { // Abre
                                                                                                               // el
                                                                                                               // archivo.
            String linea;
            while ((linea = reader.readLine()) != null) { // Lee línea por línea.
                String[] parts = linea.split("\\\\"); // Divide la línea por '\'.
                if (parts.length >= 7) { // Si tiene suficientes partes.
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
                                        sedeBiblioteca)); // Crea el libro y lo añade a la lista.
                    } catch (NumberFormatException e) {
                        System.err.println("Error de parseo en linea de libro: " + linea + " - " + e.getMessage()); // Error
                                                                                                                    // si
                                                                                                                    // un
                                                                                                                    // número
                                                                                                                    // está
                                                                                                                    // mal.
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return libros; // Devuelve la lista de libros cargados.
    }

    // Busca un libro por su código en la lista de todos los libros.
    public static Libro encontrarLibroPorCodigo(String code) {
        List<Libro> allBooks = cargarTodosLosLibros(); // Carga todos los libros.
        for (Libro libro : allBooks) { // Recorre cada libro.
            if (libro.getCodigo().equalsIgnoreCase(code.trim())) { // Si el código coincide (ignorando
                                                                   // mayúsculas/minúsculas y espacios).
                return libro; // Devuelve el libro encontrado.
            }
        }
        return null; // Si no lo encuentra, devuelve 'null'.
    }

    // Devuelve una cadena de texto que representa el objeto Libro de forma legible.
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
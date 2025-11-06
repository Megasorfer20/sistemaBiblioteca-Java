/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.sistemabiblioteca; // Carpeta donde está organizado nuestro código.

import java.io.BufferedReader; // Para leer archivos de texto.
import java.io.IOException; // Para manejar errores de archivo.
import java.nio.file.Files; // Para operar con archivos.
import java.nio.file.Path; // Para rutas de archivos.
import java.text.ParseException; // Para manejar errores al convertir texto a fecha.
import java.util.ArrayList; // Para listas dinámicas.
import java.util.Date; // Para manejar fechas.
import java.util.List; // Para usar listas.
import java.util.stream.Collectors; // Para operar con listas de forma avanzada.
import java.nio.file.StandardOpenOption; // Para opciones de guardado.

// Esta clase representa un registro de préstamo de un libro.
// Guarda todos los detalles de cuándo un libro fue prestado y devuelto.
// Implementa 'IPersistible' para guardarse y eliminarse de archivos.
public class Prestamo implements IPersistible {
    private String codigoLibro; // Código único del libro prestado.
    private long numeroDocumentoMiembro; // Documento del miembro que pidió el libro.
    private Date fechaPrestamo; // Fecha en que se prestó el libro.
    private Date fechaDevolucionEstimada; // Fecha límite para devolver el libro.
    private Date fechaDevolucionReal; // Fecha real en que se devolvió (es nula si no se ha devuelto).
    private int idBiblioteca; // ID de la biblioteca de donde se prestó.
    private String estado; // Estado del préstamo (ej. "PRESTADO", "DEVUELTO", "VENCIDO").

    public Prestamo() { // Constructor vacío.
    }

    // Constructor para crear un nuevo préstamo.
    public Prestamo(String codigoLibro, long numeroDocumentoMiembro, Date fechaPrestamo, Date fechaDevolucionEstimada,
            int idBiblioteca) {
        this.codigoLibro = codigoLibro;
        this.numeroDocumentoMiembro = numeroDocumentoMiembro;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaDevolucionEstimada = fechaDevolucionEstimada;
        this.fechaDevolucionReal = null; // No hay fecha real al inicio.
        this.idBiblioteca = idBiblioteca;
        this.estado = "PRESTADO"; // El estado inicial es "PRESTADO".
    }

    // Constructor para cargar un préstamo desde un archivo.
    public Prestamo(String codigoLibro, long numeroDocumentoMiembro, Date fechaPrestamo, Date fechaDevolucionEstimada,
            Date fechaDevolucionReal, int idBiblioteca, String estado) {
        this.codigoLibro = codigoLibro;
        this.numeroDocumentoMiembro = numeroDocumentoMiembro;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaDevolucionEstimada = fechaDevolucionEstimada;
        this.fechaDevolucionReal = fechaDevolucionReal;
        this.idBiblioteca = idBiblioteca;
        this.estado = estado;
    }

    // Getters para los atributos.
    public String getCodigoLibro() {
        return codigoLibro;
    }

    public long getNumeroDocumentoMiembro() {
        return numeroDocumentoMiembro;
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

    // Setters que guardan el préstamo automáticamente después de un cambio.
    public void setFechaDevolucionReal(Date fechaDevolucionReal) {
        this.fechaDevolucionReal = fechaDevolucionReal;
        guardar();
    }

    public void setEstado(String estado) {
        this.estado = estado;
        guardar();
    }

    // --- Metodos de Persistencia ---

    // Devuelve la ruta donde se guarda el archivo de préstamos.
    protected static Path resolverRutaLibroPrestado() {
        return PathManager.resolverRutaArchivo("LibroPrestado.txt");
    }

    // Convierte el objeto Prestamo a una línea de texto para guardarlo en el
    // archivo.
    @Override
    public String construirLinea() {
        return String.format("%s\\%d\\%s\\%s\\%s\\%d\\%s", // Formato:
                                                           // código\documento\fechaPrestamo\fechaEstimada\fechaReal\idBiblioteca\estado.
                codigoLibro,
                numeroDocumentoMiembro,
                Fecha.formatDate(fechaPrestamo), // Convierte la fecha a texto.
                Fecha.formatDate(fechaDevolucionEstimada),
                Fecha.formatDate(fechaDevolucionReal), // Será "null" si el libro no se ha devuelto.
                idBiblioteca,
                estado);
    }

    // Guarda o actualiza el préstamo en el archivo. Es un método seguro
    // (synchronized).
    @Override
    public synchronized void guardar() {
        try {
            Path path = resolverRutaLibroPrestado();
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

            boolean found = false; // Bandera para saber si encontramos el préstamo.
            // Crea un identificador único para el préstamo (libro, miembro, fecha de
            // préstamo).
            String uniqueLoanIdentifier = this.codigoLibro + "_" + this.numeroDocumentoMiembro + "_"
                    + Fecha.formatDate(this.fechaPrestamo);

            for (int i = 0; i < lines.size(); i++) { // Recorre todas las líneas.
                String line = lines.get(i);
                String[] parts = line.split("\\\\");
                if (parts.length >= 7) {
                    try {
                        String lineCodigoLibro = parts[0].trim();
                        long lineNumeroDocumentoMiembro = Long.parseLong(parts[1].trim());
                        Date lineFechaPrestamo = Fecha.parseDate(parts[2].trim());

                        String lineIdentifier = lineCodigoLibro + "_" + lineNumeroDocumentoMiembro + "_"
                                + Fecha.formatDate(lineFechaPrestamo);

                        if (lineIdentifier.equals(uniqueLoanIdentifier)) { // Si el préstamo ya existe en el archivo.
                            lines.set(i, construirLinea()); // Actualiza la línea con la nueva información.
                            found = true;
                            break; // Sale del bucle.
                        }
                    } catch (NumberFormatException | ParseException e) {
                        System.err.println("Error de parseo en linea de prestamo durante el guardado: " + line + " - "
                                + e.getMessage()); // Error si los datos de la línea están mal.
                    }
                }
            }

            if (!found) { // Si el préstamo no se encontró (es nuevo).
                lines.add(construirLinea()); // Añade el nuevo préstamo al final.
            }

            // Vuelve a escribir todas las líneas (actualizadas o nuevas) en el archivo,
            // sobrescribiendo el anterior.
            Files.write(path, lines, java.nio.charset.StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // Elimina el préstamo del archivo. Es un método seguro (synchronized).
    @Override
    public synchronized void eliminar() {
        try {
            Path path = resolverRutaLibroPrestado();
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

            // Crea el identificador único para el préstamo que se quiere eliminar.
            String uniqueLoanIdentifier = this.codigoLibro + "_" + this.numeroDocumentoMiembro + "_"
                    + Fecha.formatDate(this.fechaPrestamo);

            List<String> updatedLines = new ArrayList<>(); // Lista para guardar las líneas que *no* se van a eliminar.
            for (String line : lines) { // Recorre todas las líneas del archivo.
                String[] parts = line.split("\\\\");
                if (parts.length >= 7) {
                    try {
                        String lineCodigoLibro = parts[0].trim();
                        long lineNumeroDocumentoMiembro = Long.parseLong(parts[1].trim());
                        Date lineFechaPrestamo = Fecha.parseDate(parts[2].trim());
                        String lineIdentifier = lineCodigoLibro + "_" + lineNumeroDocumentoMiembro + "_"
                                + Fecha.formatDate(lineFechaPrestamo);

                        if (!lineIdentifier.equals(uniqueLoanIdentifier)) { // Si esta línea NO es el préstamo a
                                                                            // eliminar.
                            updatedLines.add(line); // La añade a la lista de líneas que se mantendrán.
                        }
                    } catch (NumberFormatException | ParseException e) {
                        System.err.println("Error de parseo en linea de prestamo durante la eliminacion: " + line
                                + " - " + e.getMessage()); // Error si los datos de la línea están mal.
                        updatedLines.add(line); // Si hay un error, mantiene la línea para no perderla.
                    }
                } else {
                    updatedLines.add(line); // Mantiene las líneas que no tienen el formato esperado.
                }
            }

            // Reescribe el archivo con la lista de préstamos actualizada (sin el
            // eliminado).
            Files.write(path, updatedLines, java.nio.charset.StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // Carga todos los préstamos del archivo "LibroPrestado.txt" y los devuelve en
    // una lista.
    public static List<Prestamo> cargarTodosLosPrestamos() {
        List<Prestamo> prestamos = new ArrayList<>(); // Lista para guardar los préstamos.
        Path path = resolverRutaLibroPrestado();
        if (!Files.exists(path)) { // Si el archivo no existe.
            return prestamos; // Devuelve una lista vacía.
        }

        try (BufferedReader reader = Files.newBufferedReader(path, java.nio.charset.StandardCharsets.UTF_8)) {
            String linea;
            while ((linea = reader.readLine()) != null) { // Lee línea por línea.
                String[] parts = linea.split("\\\\");
                if (parts.length >= 7) { // Si tiene suficientes partes.
                    try {
                        String codigoLibro = parts[0].trim();
                        long numeroDocumentoMiembro = Long.parseLong(parts[1].trim());
                        Date fechaPrestamo = Fecha.parseDate(parts[2].trim());
                        Date fechaDevolucionEstimada = Fecha.parseDate(parts[3].trim());
                        Date fechaDevolucionReal = Fecha.parseDate(parts[4].trim());
                        int idBiblioteca = Integer.parseInt(parts[5].trim());
                        String estado = parts[6].trim();

                        prestamos.add(new Prestamo(codigoLibro, numeroDocumentoMiembro, fechaPrestamo,
                                fechaDevolucionEstimada,
                                fechaDevolucionReal, idBiblioteca, estado)); // Crea el préstamo y lo añade a la lista.
                    } catch (NumberFormatException | ParseException e) {
                        System.err.println("Error de parseo en linea de prestamo: " + linea + " - " + e.getMessage()); // Error
                                                                                                                       // si
                                                                                                                       // los
                                                                                                                       // datos
                                                                                                                       // están
                                                                                                                       // mal.
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return prestamos; // Devuelve la lista de préstamos.
    }

    // Busca y devuelve todos los préstamos de un miembro específico, por su número
    // de documento.
    public static List<Prestamo> encontrarPrestamosPorMiembro(long numeroDocumentoMiembro) {
        return cargarTodosLosPrestamos().stream()
                .filter(p -> p.getNumeroDocumentoMiembro() == numeroDocumentoMiembro) // Filtra por el número de
                                                                                      // documento.
                .collect(Collectors.toList()); // Devuelve los préstamos que coinciden.
    }

    // Busca un préstamo *activo* específico (un libro prestado por un miembro y que
    // no ha sido devuelto).
    public static Prestamo encontrarPrestamoActivo(long numeroDocumentoMiembro, String codigoLibro) {
        return cargarTodosLosPrestamos().stream()
                .filter(p -> p.getNumeroDocumentoMiembro() == numeroDocumentoMiembro // Filtra por el miembro.
                        && p.getCodigoLibro().equals(codigoLibro) // Filtra por el libro.
                        && "PRESTADO".equals(p.getEstado())) // Filtra por estado "PRESTADO".
                .findFirst() // Toma el primer resultado.
                .orElse(null); // Si no lo encuentra, devuelve 'null'.
    }

    // Devuelve una cadena de texto que representa el objeto Prestamo de forma
    // legible.
    @Override
    public String toString() {
        return "Prestamo {" +
                "  Libro Codigo='" + codigoLibro + '\'' +
                ", Miembro Doc=" + numeroDocumentoMiembro +
                ", Fecha Prestamo=" + Fecha.formatDate(fechaPrestamo) +
                ", Fecha Devolucion Estimada=" + Fecha.formatDate(fechaDevolucionEstimada) +
                ", Fecha Devolucion Real=" + Fecha.formatDate(fechaDevolucionReal) +
                ", ID Biblioteca=" + idBiblioteca +
                ", Estado='" + estado + '\'' +
                '}';
    }
}
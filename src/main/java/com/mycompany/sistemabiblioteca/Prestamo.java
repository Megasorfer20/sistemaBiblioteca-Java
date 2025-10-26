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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.nio.file.StandardOpenOption;

/**
 * Clase para representar un prestamo de libro.
 * Implementa IPersistible para el manejo de guardado y eliminacion.
 */
public class Prestamo implements IPersistible {
    private String codigoLibro;
    private long numeroDocumentoMiembro;
    private Date fechaPrestamo;
    private Date fechaDevolucionEstimada;
    private Date fechaDevolucionReal; // Null si no se ha devuelto
    private int idBiblioteca;
    private String estado; // Ej: "PRESTADO", "DEVUELTO", "VENCIDO"

    public Prestamo() {
    }

    public Prestamo(String codigoLibro, long numeroDocumentoMiembro, Date fechaPrestamo, Date fechaDevolucionEstimada,
            int idBiblioteca) {
        this.codigoLibro = codigoLibro;
        this.numeroDocumentoMiembro = numeroDocumentoMiembro;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaDevolucionEstimada = fechaDevolucionEstimada;
        this.fechaDevolucionReal = null;
        this.idBiblioteca = idBiblioteca;
        this.estado = "PRESTADO";
    }

    // Constructor para cargar desde archivo
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

    // Getters
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

    // Setters (invocan guardar() si modifican un atributo persistente)
    public void setFechaDevolucionReal(Date fechaDevolucionReal) {
        this.fechaDevolucionReal = fechaDevolucionReal;
        guardar();
    }

    public void setEstado(String estado) {
        this.estado = estado;
        guardar();
    }

    // --- Metodos de Persistencia ---

    protected static Path resolverRutaLibroPrestado() {
        return PathManager.resolverRutaArchivo("LibroPrestado.txt");
    }

    @Override
    public String construirLinea() {
        return String.format("%s\\%d\\%s\\%s\\%s\\%d\\%s",
                codigoLibro,
                numeroDocumentoMiembro,
                Fecha.formatDate(fechaPrestamo),
                Fecha.formatDate(fechaDevolucionEstimada),
                Fecha.formatDate(fechaDevolucionReal),
                idBiblioteca,
                estado);
    }

    @Override
    public synchronized void guardar() {
        try {
            Path path = resolverRutaLibroPrestado();
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
            // Usar un identificador unico para el prestamo (libro, miembro, fecha de
            // prestamo)
            String uniqueLoanIdentifier = this.codigoLibro + "_" + this.numeroDocumentoMiembro + "_"
                    + Fecha.formatDate(this.fechaPrestamo);

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                String[] parts = line.split("\\\\");
                if (parts.length >= 7) {
                    try {
                        String lineCodigoLibro = parts[0].trim();
                        long lineNumeroDocumentoMiembro = Long.parseLong(parts[1].trim());
                        Date lineFechaPrestamo = Fecha.parseDate(parts[2].trim());

                        String lineIdentifier = lineCodigoLibro + "_" + lineNumeroDocumentoMiembro + "_"
                                + Fecha.formatDate(lineFechaPrestamo);

                        if (lineIdentifier.equals(uniqueLoanIdentifier)) {
                            lines.set(i, construirLinea());
                            found = true;
                            break;
                        }
                    } catch (NumberFormatException | ParseException e) {
                        System.err.println("Error de parseo en linea de prestamo durante el guardado: " + line + " - "
                                + e.getMessage());
                    }
                }
            }

            if (!found) {
                lines.add(construirLinea());
            }

            Files.write(path, lines, java.nio.charset.StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public synchronized void eliminar() {
        try {
            Path path = resolverRutaLibroPrestado();
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

            String uniqueLoanIdentifier = this.codigoLibro + "_" + this.numeroDocumentoMiembro + "_"
                    + Fecha.formatDate(this.fechaPrestamo);

            List<String> updatedLines = new ArrayList<>();
            for (String line : lines) {
                String[] parts = line.split("\\\\");
                if (parts.length >= 7) {
                    try {
                        String lineCodigoLibro = parts[0].trim();
                        long lineNumeroDocumentoMiembro = Long.parseLong(parts[1].trim());
                        Date lineFechaPrestamo = Fecha.parseDate(parts[2].trim());
                        String lineIdentifier = lineCodigoLibro + "_" + lineNumeroDocumentoMiembro + "_"
                                + Fecha.formatDate(lineFechaPrestamo);

                        if (!lineIdentifier.equals(uniqueLoanIdentifier)) {
                            updatedLines.add(line);
                        }
                    } catch (NumberFormatException | ParseException e) {
                        System.err.println("Error de parseo en linea de prestamo durante la eliminacion: " + line
                                + " - " + e.getMessage());
                        updatedLines.add(line);
                    }
                } else {
                    updatedLines.add(line);
                }
            }

            Files.write(path, updatedLines, java.nio.charset.StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Carga todos los prestamos del archivo LibroPrestado.txt.
     */
    public static List<Prestamo> cargarTodosLosPrestamos() {
        List<Prestamo> prestamos = new ArrayList<>();
        Path path = resolverRutaLibroPrestado();
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
                        long numeroDocumentoMiembro = Long.parseLong(parts[1].trim());
                        Date fechaPrestamo = Fecha.parseDate(parts[2].trim());
                        Date fechaDevolucionEstimada = Fecha.parseDate(parts[3].trim());
                        Date fechaDevolucionReal = Fecha.parseDate(parts[4].trim());
                        int idBiblioteca = Integer.parseInt(parts[5].trim());
                        String estado = parts[6].trim();

                        prestamos.add(new Prestamo(codigoLibro, numeroDocumentoMiembro, fechaPrestamo,
                                fechaDevolucionEstimada,
                                fechaDevolucionReal, idBiblioteca, estado));
                    } catch (NumberFormatException | ParseException e) {
                        System.err.println("Error de parseo en linea de prestamo: " + linea + " - " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return prestamos;
    }

    /**
     * Encuentra todos los prestamos de un miembro especifico.
     */
    public static List<Prestamo> encontrarPrestamosPorMiembro(long numeroDocumentoMiembro) {
        return cargarTodosLosPrestamos().stream()
                .filter(p -> p.getNumeroDocumentoMiembro() == numeroDocumentoMiembro)
                .collect(Collectors.toList());
    }

    /**
     * Encuentra un prestamo activo de un libro por un miembro.
     */
    public static Prestamo encontrarPrestamoActivo(long numeroDocumentoMiembro, String codigoLibro) {
        return cargarTodosLosPrestamos().stream()
                .filter(p -> p.getNumeroDocumentoMiembro() == numeroDocumentoMiembro
                        && p.getCodigoLibro().equals(codigoLibro)
                        && "PRESTADO".equals(p.getEstado()))
                .findFirst()
                .orElse(null);
    }

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
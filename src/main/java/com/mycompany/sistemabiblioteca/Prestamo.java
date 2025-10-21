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

/**
 * Clase para representar un préstamo de libro.
 */
public class Prestamo {
    private String codigoLibro;
    private long noDocMiembro;
    private Date fechaPrestamo;
    private Date fechaDevolucionEstimada;
    private Date fechaDevolucionReal; // Null si no se ha devuelto
    private int idBiblioteca;
    private String estado; // Ej: "PRESTADO", "DEVUELTO", "VENCIDO"

    public Prestamo() {
    }

    public Prestamo(String codigoLibro, long noDocMiembro, Date fechaPrestamo, Date fechaDevolucionEstimada,
            int idBiblioteca) {
        this.codigoLibro = codigoLibro;
        this.noDocMiembro = noDocMiembro;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaDevolucionEstimada = fechaDevolucionEstimada;
        this.fechaDevolucionReal = null; // No devuelto inicialmente
        this.idBiblioteca = idBiblioteca;
        this.estado = "PRESTADO"; // Estado inicial
    }

    // Constructor para cargar desde archivo
    public Prestamo(String codigoLibro, long noDocMiembro, Date fechaPrestamo, Date fechaDevolucionEstimada,
            Date fechaDevolucionReal, int idBiblioteca, String estado) {
        this.codigoLibro = codigoLibro;
        this.noDocMiembro = noDocMiembro;
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

    public long getNoDocMiembro() {
        return noDocMiembro;
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

    // Setters (invocan save() si modifican un atributo persistente)
    public void setFechaDevolucionReal(Date fechaDevolucionReal) {
        this.fechaDevolucionReal = fechaDevolucionReal;
        save();
    }

    public void setEstado(String estado) {
        this.estado = estado;
        save();
    }

    // --- Persistence methods ---

    // Resuelve la ruta del archivo LibroPrestado.txt
    protected static Path resolveLibroPrestadoPath() {
        try {
            java.net.URL res = Prestamo.class.getClassLoader().getResource("LibroPrestado.txt");
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
        return Paths.get(System.getProperty("user.dir"), "LibroPrestado.txt");
    }

    // Construye la línea de texto para guardar el préstamo
    private String buildLine() {
        return String.format("%s\\%d\\%s\\%s\\%s\\%d\\%s",
                codigoLibro,
                noDocMiembro,
                Fecha.formatDate(fechaPrestamo),
                Fecha.formatDate(fechaDevolucionEstimada),
                Fecha.formatDate(fechaDevolucionReal), // Usa Fecha.formatDate para null también
                idBiblioteca,
                estado);
    }

    // Guarda o actualiza el registro del préstamo.
    public synchronized void save() {
        try {
            Path path = resolveLibroPrestadoPath();
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
            // Usar un identificador único para el préstamo (libro, miembro, fecha de
            // préstamo)
            // Se asume que no puede haber dos préstamos del mismo libro al mismo miembro en
            // la misma fecha de préstamo.
            String uniqueLoanIdentifier = this.codigoLibro + "_" + this.noDocMiembro + "_"
                    + Fecha.formatDate(this.fechaPrestamo);

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                String[] parts = line.split("\\\\");
                if (parts.length >= 7) {
                    try {
                        String lineCodigoLibro = parts[0].trim();
                        long lineNoDocMiembro = Long.parseLong(parts[1].trim());
                        Date lineFechaPrestamo = Fecha.parseDate(parts[2].trim());

                        String lineIdentifier = lineCodigoLibro + "_" + lineNoDocMiembro + "_"
                                + Fecha.formatDate(lineFechaPrestamo);

                        if (lineIdentifier.equals(uniqueLoanIdentifier)) {
                            lines.set(i, buildLine()); // Actualizar la línea
                            found = true;
                            break;
                        }
                    } catch (NumberFormatException | ParseException e) {
                        System.err.println("Error parsing loan line during save: " + line + " - " + e.getMessage());
                    }
                }
            }

            if (!found) {
                lines.add(buildLine()); // Agregar nuevo préstamo
            }

            Path parent = path.getParent();
            if (parent != null && !Files.exists(parent)) {
                try {
                    Files.createDirectories(parent);
                } catch (IOException ex) {
                    System.err.println("Error creating directory for LibroPrestado.txt: " + ex.getMessage());
                }
            }
            Files.write(path, lines, java.nio.charset.StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // Elimina un registro de préstamo.
    public synchronized void delete() {
        try {
            Path path = resolveLibroPrestadoPath();
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

            String uniqueLoanIdentifier = this.codigoLibro + "_" + this.noDocMiembro + "_"
                    + Fecha.formatDate(this.fechaPrestamo);

            List<String> updatedLines = new ArrayList<>();
            for (String line : lines) {
                String[] parts = line.split("\\\\");
                if (parts.length >= 7) {
                    try {
                        String lineCodigoLibro = parts[0].trim();
                        long lineNoDocMiembro = Long.parseLong(parts[1].trim());
                        Date lineFechaPrestamo = Fecha.parseDate(parts[2].trim());
                        String lineIdentifier = lineCodigoLibro + "_" + lineNoDocMiembro + "_"
                                + Fecha.formatDate(lineFechaPrestamo);

                        if (!lineIdentifier.equals(uniqueLoanIdentifier)) {
                            updatedLines.add(line);
                        }
                    } catch (NumberFormatException | ParseException e) {
                        System.err.println("Error parsing loan line during delete: " + line + " - " + e.getMessage());
                        updatedLines.add(line); // Mantener la línea si el parsing falla para evitar pérdida de datos
                    }
                } else {
                    updatedLines.add(line); // Mantener líneas mal formadas para inspección manual
                }
            }

            Path parent = path.getParent();
            if (parent != null && !Files.exists(parent)) {
                try {
                    Files.createDirectories(parent);
                } catch (IOException ex) {
                    System.err.println("Error creating directory for LibroPrestado.txt: " + ex.getMessage());
                }
            }
            Files.write(path, updatedLines, java.nio.charset.StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // Carga todos los préstamos del archivo LibroPrestado.txt
    public static List<Prestamo> loadAllPrestamos() {
        List<Prestamo> prestamos = new ArrayList<>();
        Path path = resolveLibroPrestadoPath();
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
                        long noDocMiembro = Long.parseLong(parts[1].trim());
                        Date fechaPrestamo = Fecha.parseDate(parts[2].trim());
                        Date fechaDevolucionEstimada = Fecha.parseDate(parts[3].trim());
                        Date fechaDevolucionReal = Fecha.parseDate(parts[4].trim()); // Usa Fecha.parseDate
                        int idBiblioteca = Integer.parseInt(parts[5].trim());
                        String estado = parts[6].trim();

                        prestamos.add(new Prestamo(codigoLibro, noDocMiembro, fechaPrestamo, fechaDevolucionEstimada,
                                fechaDevolucionReal, idBiblioteca, estado));
                    } catch (NumberFormatException | ParseException e) {
                        System.err.println("Error parsing loan line: " + linea + " - " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return prestamos;
    }

    // Encuentra todos los préstamos de un miembro específico.
    public static List<Prestamo> findPrestamosByMember(long noDocMiembro) {
        return loadAllPrestamos().stream()
                .filter(p -> p.getNoDocMiembro() == noDocMiembro)
                .collect(Collectors.toList());
    }

    // Encuentra un préstamo activo de un libro por un miembro.
    public static Prestamo findActivePrestamo(long noDocMiembro, String codigoLibro) {
        return loadAllPrestamos().stream()
                .filter(p -> p.getNoDocMiembro() == noDocMiembro && p.getCodigoLibro().equals(codigoLibro)
                        && "PRESTADO".equals(p.getEstado()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String toString() {
        return "Prestamo {" +
                "  Libro Código='" + codigoLibro + '\'' +
                ", Miembro Doc=" + noDocMiembro +
                ", Fecha Prestamo=" + Fecha.formatDate(fechaPrestamo) +
                ", Fecha Devolución Estimada=" + Fecha.formatDate(fechaDevolucionEstimada) +
                ", Fecha Devolución Real=" + Fecha.formatDate(fechaDevolucionReal) +
                ", ID Biblioteca=" + idBiblioteca +
                ", Estado='" + estado + '\'' +
                '}';
    }
}
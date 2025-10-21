/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.sistemabiblioteca;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author edrui
 */
public class Biblioteca {

    private int id;
    private String sede;
    private String nombreBiblioteca;

    // Constante para el valor de la multa por día
    public static final double MULTA_POR_DIA = 4.750;
    // Constante para el máximo de días de préstamo
    public static final int DIAS_MAX_PRESTAMO = 30;

    // Constructor para la clase Biblioteca
    public Biblioteca(int id, String sede, String nombreBiblioteca) {
        this.id = id;
        this.sede = sede;
        this.nombreBiblioteca = nombreBiblioteca;
    }

    public Biblioteca() {
        // Constructor vacío si se necesita para inicialización diferida o carga
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getSede() {
        return sede;
    }

    public String getNombreBiblioteca() {
        return nombreBiblioteca;
    }

    // Setters (no implican guardar directamente al archivo de bibliotecas, ya que
    // este es más estático)
    public void setId(int id) {
        this.id = id;
    }

    public void setSede(String sede) {
        this.sede = sede;
    }

    public void setNombreBiblioteca(String nombreBiblioteca) {
        this.nombreBiblioteca = nombreBiblioteca;
    }

    // --- Persistence methods for Biblioteca itself (reading Bibliotecas.txt) ---
    // Resuelve la ruta del archivo Bibliotecas.txt
    protected static Path resolveBibliotecasPath() {
        try {
            java.net.URL res = Biblioteca.class.getClassLoader().getResource("Bibliotecas.txt");
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
        return Paths.get(System.getProperty("user.dir"), "Bibliotecas.txt");
    }

    // Carga todas las bibliotecas del archivo Bibliotecas.txt
    public static List<Biblioteca> loadAllBibliotecas() {
        List<Biblioteca> bibliotecas = new ArrayList<>();
        Path path = resolveBibliotecasPath();
        if (!Files.exists(path)) {
            System.err.println("File not found: " + path.toAbsolutePath());
            return bibliotecas;
        }

        try (BufferedReader reader = Files.newBufferedReader(path, java.nio.charset.StandardCharsets.UTF_8)) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] parts = linea.split("\\\\");
                if (parts.length >= 3) { // id\sede\nombreBiblioteca
                    try {
                        int id = Integer.parseInt(parts[0].trim());
                        String sede = parts[1].trim();
                        String nombre = parts[2].trim();
                        bibliotecas.add(new Biblioteca(id, sede, nombre));
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing Biblioteca line: " + linea + " - " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bibliotecas;
    }

    // Encuentra una biblioteca por su ID.
    public static Biblioteca findBibliotecaById(int id) {
        return loadAllBibliotecas().stream()
                .filter(b -> b.getId() == id)
                .findFirst()
                .orElse(null);
    }

    // Encuentra una biblioteca por su sede.
    public static Biblioteca findBibliotecaBySede(String sede) {
        return loadAllBibliotecas().stream()
                .filter(b -> b.getSede().equalsIgnoreCase(sede))
                .findFirst()
                .orElse(null);
    }

    // Métodos de la funcionalidad de la biblioteca

    public void seleccionarSede() {
        System.out.println("Sedes de Bibliotecas disponibles:");
        List<Biblioteca> bibliotecas = loadAllBibliotecas();
        if (bibliotecas.isEmpty()) {
            System.out.println("No hay bibliotecas registradas.");
            return;
        }

        for (Biblioteca b : bibliotecas) {
            System.out.println("ID: " + b.getId() + ", Sede: " + b.getSede() + ", Nombre: " + b.getNombreBiblioteca());
        }

        System.out.println("La selección de la sede debe ser manejada por la lógica de la interfaz de usuario.");
        System.out.println(
                "El objeto Biblioteca actual (this) representa la sede con ID: " + this.id + " y Sede: " + this.sede);
    }

    // Método para prestar un libro
    public String prestarLibro(Miembro miembro, String codigoLibro) {
        if (miembro == null) {
            return "Error: Miembro no válido.";
        }
        if (miembro.getRol() == 0) { // Admin no puede pedir libros para sí mismo como un usuario normal
            return "Error: Los administradores no pueden pedir libros prestados para sí mismos.";
        }

        // Verificar si el usuario tiene deuda pendiente
        if (miembro instanceof Usuario) {
            Usuario user = (Usuario) miembro;
            if (user.getDeuda() > 0) {
                return "Error: No puedes pedir prestado un libro. Tienes una deuda pendiente de "
                        + String.format("%.2f", user.getDeuda()) + " pesos.";
            }
        }

        Libro libro = Libro.findBookByCode(codigoLibro);
        if (libro == null) {
            return "Error: Libro con código " + codigoLibro + " no encontrado.";
        }

        // Verificar que el libro pertenece a esta biblioteca
        if (libro.getIdBiblioteca() != this.id) {
            return "Error: El libro " + codigoLibro + " no se encuentra en esta biblioteca (" + this.nombreBiblioteca
                    + ").";
        }

        if (libro.getUnLibres() <= 0) {
            return "Error: El libro '" + libro.getNombre() + "' no tiene unidades disponibles.";
        }

        List<Prestamo> prestamosActivosDelMiembro = Prestamo.findPrestamosByMember(miembro.getNoDoc()).stream()
                .filter(p -> "PRESTADO".equals(p.getEstado()))
                .collect(Collectors.toList());

        int limitePrestamos;
        String tipoMiembro;
        switch (miembro.getRol()) {
            case 1: // Estudiante
                limitePrestamos = 5;
                tipoMiembro = "Estudiante";
                break;
            case 2: // Profesor
                limitePrestamos = 3;
                tipoMiembro = "Profesor";
                break;
            case 3: // Administrativo de la Universidad
                limitePrestamos = 1;
                tipoMiembro = "Administrativo de la Universidad";
                break;
            default:
                return "Error: Rol de miembro desconocido.";
        }

        if (prestamosActivosDelMiembro.size() >= limitePrestamos) {
            return "Error: El " + tipoMiembro + " " + miembro.getNombre() + " ya ha alcanzado su límite de "
                    + limitePrestamos + " libros prestados.";
        }

        // Verificar si el miembro ya tiene este mismo libro prestado
        boolean yaTieneLibro = prestamosActivosDelMiembro.stream()
                .anyMatch(p -> p.getCodigoLibro().equals(codigoLibro));
        if (yaTieneLibro) {
            return "Error: El miembro ya tiene este libro prestado.";
        }

        // Realizar el préstamo
        libro.setUnLibres(libro.getUnLibres() - 1);
        libro.setUnPrestadas(libro.getUnPrestadas() + 1);
        libro.save(); // Actualizar el estado del libro en el archivo

        Date fechaPrestamo = Fecha.getToday();
        Date fechaDevolucionEstimada = Fecha.addDays(fechaPrestamo, DIAS_MAX_PRESTAMO); // 30 días de préstamo

        Prestamo nuevoPrestamo = new Prestamo(codigoLibro, miembro.getNoDoc(), fechaPrestamo, fechaDevolucionEstimada,
                this.id);
        nuevoPrestamo.save(); // Guardar el registro de préstamo

        return "Éxito: Libro '" + libro.getNombre() + "' prestado a " + miembro.getNombre() + " "
                + miembro.getApellido() + ". Debe ser devuelto antes de " + Fecha.formatDate(fechaDevolucionEstimada)
                + ".";
    }

    // Método para devolver un libro
    public String devolverLibro(Miembro miembro, String codigoLibro) {
        if (miembro == null) {
            return "Error: Miembro no válido.";
        }

        Libro libro = Libro.findBookByCode(codigoLibro);
        if (libro == null) {
            return "Error: Libro con código " + codigoLibro + " no encontrado.";
        }

        // Verificar que el libro pertenece a esta biblioteca
        if (libro.getIdBiblioteca() != this.id) {
            return "Error: El libro " + codigoLibro + " no se encuentra en esta biblioteca (" + this.nombreBiblioteca
                    + ").";
        }

        Prestamo prestamoActivo = Prestamo.findActivePrestamo(miembro.getNoDoc(), codigoLibro);

        if (prestamoActivo == null) {
            return "Error: El miembro " + miembro.getNombre() + " no tiene prestado el libro '" + libro.getNombre()
                    + "'.";
        }

        // Marcar el préstamo como devuelto
        prestamoActivo.setFechaDevolucionReal(Fecha.getToday());
        prestamoActivo.setEstado("DEVUELTO");
        prestamoActivo.save(); // Actualizar el registro de préstamo

        // Actualizar el estado del libro
        libro.setUnLibres(libro.getUnLibres() + 1);
        libro.setUnPrestadas(libro.getUnPrestadas() - 1);
        libro.save(); // Actualizar el estado del libro en el archivo

        String multaInfo = "";
        // Verificar si hay multa y aplicarla
        if (prestamoActivo.getFechaDevolucionReal().after(prestamoActivo.getFechaDevolucionEstimada())) {
            multaInfo = multar(miembro, libro, prestamoActivo);
        }

        return "Éxito: Libro '" + libro.getNombre() + "' devuelto por " + miembro.getNombre() + " "
                + miembro.getApellido() + ". " + multaInfo;
    }

    // Método para ver libros disponibles por sede
    public List<Libro> librosDisponibles(String sedeBusqueda) {
        return Libro.loadAllBooks().stream()
                .filter(libro -> libro.getSedeBiblioteca().equalsIgnoreCase(sedeBusqueda) && libro.getUnLibres() > 0)
                .collect(Collectors.toList());
    }

    // Sobrecarga para buscar por ID de biblioteca
    public List<Libro> librosDisponibles(int idBibliotecaBusqueda) {
        return Libro.loadAllBooks().stream()
                .filter(libro -> libro.getIdBiblioteca() == idBibliotecaBusqueda && libro.getUnLibres() > 0)
                .collect(Collectors.toList());
    }

    // Sobrecarga para buscar por sede e ID de biblioteca
    public List<Libro> librosDisponibles(String sedeBusqueda, int idBibliotecaBusqueda) {
        return Libro.loadAllBooks().stream()
                .filter(libro -> libro.getSedeBiblioteca().equalsIgnoreCase(sedeBusqueda)
                        && libro.getIdBiblioteca() == idBibliotecaBusqueda
                        && libro.getUnLibres() > 0)
                .collect(Collectors.toList());
    }

    // Método para buscar libros
    public List<Libro> buscarLibro(String query, String searchType) {
        List<Libro> allBooks = Libro.loadAllBooks();
        return allBooks.stream()
                .filter(libro -> {
                    switch (searchType.toLowerCase()) {
                        case "nombre":
                            return libro.getNombre().toLowerCase().contains(query.toLowerCase());
                        case "autor":
                            return libro.getAutor().toLowerCase().contains(query.toLowerCase());
                        case "codigo":
                            return libro.getCodigo().equalsIgnoreCase(query);
                        case "sede": // Buscar por sede
                            return libro.getSedeBiblioteca().equalsIgnoreCase(query);
                        case "idbiblioteca": // Buscar por ID de biblioteca
                            try {
                                return libro.getIdBiblioteca() == Integer.parseInt(query);
                            } catch (NumberFormatException e) {
                                return false;
                            }
                        default:
                            return false; // Tipo de búsqueda no válido
                    }
                })
                .collect(Collectors.toList());
    }

    public String multar(Miembro miembro, Libro libro, Prestamo prestamo) {
        if (prestamo != null && prestamo.getFechaDevolucionReal() != null &&
                prestamo.getFechaDevolucionReal().after(prestamo.getFechaDevolucionEstimada())) {

            long diasRetraso = Fecha.getDaysBetween(prestamo.getFechaDevolucionReal(),
                    prestamo.getFechaDevolucionEstimada());

            // Asegurarse de que díasRetraso sea al menos 1 si hay retraso
            if (diasRetraso <= 0)
                diasRetraso = 1;

            double montoMulta = diasRetraso * MULTA_POR_DIA;

            // Si el miembro es un Usuario, se le aplica la deuda.
            if (miembro instanceof Usuario) {
                Usuario user = (Usuario) miembro;
                user.setDeuda(user.getDeuda() + montoMulta); // Acumula la deuda
                System.out.println("Deuda actualizada para " + user.getNombre() + ". Nueva deuda total: "
                        + String.format("%.2f", user.getDeuda()));
            }

            return String.format(
                    "Se ha aplicado una multa a %s %s por el libro '%s'. Retraso: %d días. Monto: %.2f pesos. ",
                    miembro.getNombre(), miembro.getApellido(), libro.getNombre(), diasRetraso, montoMulta);
        }
        return "No hay multa para este préstamo.";
    }

    @Override
    public String toString() {
        return "Biblioteca {" +
                "  ID=" + id +
                ", Sede='" + sede + '\'' +
                ", Nombre='" + nombreBiblioteca + '\'' +
                '}';
    }
}
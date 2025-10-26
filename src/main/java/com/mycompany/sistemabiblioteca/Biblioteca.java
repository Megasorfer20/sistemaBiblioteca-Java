/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.sistemabiblioteca;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class Biblioteca implements IPersistible {

    private int id;
    private String sede;
    private String nombreBiblioteca;

    public static final double MULTA_POR_DIA = 4.750;
    public static final int DIAS_MAX_PRESTAMO = 30;

    public Biblioteca(int id, String sede, String nombreBiblioteca) {
        this.id = id;
        this.sede = sede;
        this.nombreBiblioteca = nombreBiblioteca;
    }

    public Biblioteca() {
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

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setSede(String sede) {
        this.sede = sede;
    }

    public void setNombreBiblioteca(String nombreBiblioteca) {
        this.nombreBiblioteca = nombreBiblioteca;
    }

    // --- Metodos de Persistencia para Biblioteca ---
    protected static Path resolverRutaBibliotecas() {
        return PathManager.resolverRutaArchivo("Bibliotecas.txt");
    }

    /**
     * Carga todas las bibliotecas del archivo Bibliotecas.txt.
     */
    public static List<Biblioteca> cargarTodasLasBibliotecas() {
        List<Biblioteca> bibliotecas = new ArrayList<>();
        Path path = resolverRutaBibliotecas();
        if (!Files.exists(path)) {
            System.err.println("Advertencia: Archivo Bibliotecas.txt no encontrado en " + path.toAbsolutePath());
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
                        System.err.println("Error de parseo en linea de Biblioteca: " + linea + " - " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bibliotecas;
    }

    /**
     * Guarda una lista completa de bibliotecas en el archivo Bibliotecas.txt,
     * sobrescribiendo el contenido existente.
     */
    public static synchronized void guardarTodasLasBibliotecas(List<Biblioteca> bibliotecas) {
        Path path = resolverRutaBibliotecas();
        List<String> lineas = bibliotecas.stream()
                .map(Biblioteca::construirLinea)
                .collect(Collectors.toList());
        try {
            Files.write(path, lineas, java.nio.charset.StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Encuentra una biblioteca por su ID.
     */
    public static Biblioteca encontrarBibliotecaPorId(int id) {
        return cargarTodasLasBibliotecas().stream()
                .filter(b -> b.getId() == id)
                .findFirst()
                .orElse(null);
    }

    /**
     * Encuentra una biblioteca por su sede.
     */
    public static Biblioteca encontrarBibliotecaPorSede(String sede) {
        return cargarTodasLasBibliotecas().stream()
                .filter(b -> b.getSede().equalsIgnoreCase(sede))
                .findFirst()
                .orElse(null);
    }

    // Implementacion de IPersistible (en este caso, guardar/eliminar es gestionado
    // por la lista estatica)
    @Override
    public void guardar() {
        // En este diseño, la persistencia de Bibliotecas se maneja a nivel de lista
        // estatica.
        // Un solo objeto Biblioteca no se "auto-guarda", sino que se guarda la lista
        // completa.
        // Si se editara un campo, se cargaria la lista, se editara el objeto y se
        // guardaria la lista completa.
        // Dejamos este metodo para cumplir la interfaz, pero no hace nada aqui.
        System.out.println(
                "Advertencia: El metodo guardar() de Biblioteca no guarda instancias individuales. Use guardarTodasLasBibliotecas().");
    }

    @Override
    public void eliminar() {
        // Similar a guardar(), la eliminacion se maneja a nivel de lista estatica.
        System.out.println(
                "Advertencia: El metodo eliminar() de Biblioteca no elimina instancias individuales. Use la logica del Admin para eliminar de la lista.");
    }

    @Override
    public String construirLinea() {
        return String.format("%d\\%s\\%s", id, sede, nombreBiblioteca);
    }

    // --- Metodos de la funcionalidad de la biblioteca ---

    /**
     * Permite seleccionar una sede, aunque la logica de seleccion es externa a esta
     * clase.
     */
    public void seleccionarSede() {
        System.out.println("Sedes de Bibliotecas disponibles:");
        List<Biblioteca> bibliotecas = cargarTodasLasBibliotecas();
        if (bibliotecas.isEmpty()) {
            System.out.println("No hay bibliotecas registradas.");
            return;
        }

        for (Biblioteca b : bibliotecas) {
            System.out.println("ID: " + b.getId() + ", Sede: " + b.getSede() + ", Nombre: " + b.getNombreBiblioteca());
        }

        System.out.println("La seleccion de la sede debe ser manejada por la logica de la interfaz de usuario.");
        System.out.println(
                "El objeto Biblioteca actual (this) representa la sede con ID: " + this.id + " y Sede: " + this.sede);
    }

    /**
     * Realiza el prestamo de un libro a un miembro.
     * Incorpora las reglas de limites por tipo de usuario y la verificacion de
     * deuda.
     */
    public String prestarLibro(Miembro miembro, String codigoLibro) {
        if (miembro == null) {
            return "Error: Miembro no valido.";
        }
        if (miembro.getRol() == 0) {
            return "Error: Los administradores no pueden pedir libros prestados para si mismos.";
        }

        // Verificar si el usuario tiene deuda pendiente
        if (miembro instanceof Usuario) {
            Usuario user = (Usuario) miembro;
            if (user.getDeuda() > 0) {
                return "Error: No puedes pedir prestado un libro. Tienes una deuda pendiente de "
                        + String.format("%.2f", user.getDeuda()) + " pesos.";
            }
        }

        Libro libro = Libro.encontrarLibroPorCodigo(codigoLibro);
        if (libro == null) {
            return "Error: Libro con codigo " + codigoLibro + " no encontrado.";
        }

        // Verificar que el libro pertenece a esta biblioteca
        if (libro.getIdBiblioteca() != this.id) {
            return "Error: El libro " + codigoLibro + " no se encuentra en esta biblioteca (" + this.nombreBiblioteca
                    + ").";
        }

        if (libro.getUnidadesLibres() <= 0) {
            return "Error: El libro '" + libro.getNombre() + "' no tiene unidades disponibles.";
        }

        List<Prestamo> prestamosActivosDelMiembro = Prestamo.encontrarPrestamosPorMiembro(miembro.getNumeroDocumento())
                .stream()
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
            return "Error: El " + tipoMiembro + " " + miembro.getNombre() + " ya ha alcanzado su limite de "
                    + limitePrestamos + " libros prestados.";
        }

        // Verificar si el miembro ya tiene este mismo libro prestado
        boolean yaTieneLibro = prestamosActivosDelMiembro.stream()
                .anyMatch(p -> p.getCodigoLibro().equals(codigoLibro));
        if (yaTieneLibro) {
            return "Error: El miembro ya tiene este libro prestado.";
        }

        // Realizar el prestamo
        libro.setUnidadesLibres(libro.getUnidadesLibres() - 1);
        libro.setUnidadesPrestadas(libro.getUnidadesPrestadas() + 1);
        libro.guardar();

        Date fechaPrestamo = Fecha.getToday();
        Date fechaDevolucionEstimada = Fecha.addDays(fechaPrestamo, DIAS_MAX_PRESTAMO);

        Prestamo nuevoPrestamo = new Prestamo(codigoLibro, miembro.getNumeroDocumento(), fechaPrestamo,
                fechaDevolucionEstimada,
                this.id);
        nuevoPrestamo.guardar();

        return "Exito: Libro '" + libro.getNombre() + "' prestado a " + miembro.getNombre() + " "
                + miembro.getApellido() + ". Debe ser devuelto antes de " + Fecha.formatDate(fechaDevolucionEstimada)
                + ".";
    }

    /**
     * Realiza la devolucion de un libro por parte de un miembro.
     * Calcula multas si aplica.
     */
    public String devolverLibro(Miembro miembro, String codigoLibro) {
        if (miembro == null) {
            return "Error: Miembro no valido.";
        }

        Libro libro = Libro.encontrarLibroPorCodigo(codigoLibro);
        if (libro == null) {
            return "Error: Libro con codigo " + codigoLibro + " no encontrado.";
        }

        // Verificar que el libro pertenece a esta biblioteca
        if (libro.getIdBiblioteca() != this.id) {
            return "Error: El libro " + codigoLibro + " no se encuentra en esta biblioteca (" + this.nombreBiblioteca
                    + ").";
        }

        Prestamo prestamoActivo = Prestamo.encontrarPrestamoActivo(miembro.getNumeroDocumento(), codigoLibro);

        if (prestamoActivo == null) {
            return "Error: El miembro " + miembro.getNombre() + " no tiene prestado el libro '" + libro.getNombre()
                    + "'.";
        }

        // Marcar el prestamo como devuelto
        prestamoActivo.setFechaDevolucionReal(Fecha.getToday());
        prestamoActivo.setEstado("DEVUELTO");
        prestamoActivo.guardar();

        libro.setUnidadesLibres(libro.getUnidadesLibres() + 1);
        libro.setUnidadesPrestadas(libro.getUnidadesPrestadas() - 1);
        libro.guardar();

        String multaInfo = "";
        // Verificar si hay multa y aplicarla
        if (prestamoActivo.getFechaDevolucionReal().after(prestamoActivo.getFechaDevolucionEstimada())) {
            multaInfo = multar(miembro, libro, prestamoActivo);
        }

        return "Exito: Libro '" + libro.getNombre() + "' devuelto por " + miembro.getNombre() + " "
                + miembro.getApellido() + ". " + multaInfo;
    }

    /**
     * Busca los libros disponibles en una sede especifica.
     */
    public List<Libro> librosDisponibles(String sedeBusqueda) {
        return Libro.cargarTodosLosLibros().stream()
                .filter(libro -> libro.getSedeBiblioteca().equalsIgnoreCase(sedeBusqueda)
                        && libro.getUnidadesLibres() > 0)
                .collect(Collectors.toList());
    }

    /**
     * Busca los libros disponibles por ID de biblioteca.
     */
    public List<Libro> librosDisponibles(int idBibliotecaBusqueda) {
        return Libro.cargarTodosLosLibros().stream()
                .filter(libro -> libro.getIdBiblioteca() == idBibliotecaBusqueda && libro.getUnidadesLibres() > 0)
                .collect(Collectors.toList());
    }

    /**
     * Busca los libros disponibles en una sede y por ID de biblioteca.
     */
    public List<Libro> librosDisponibles(String sedeBusqueda, int idBibliotecaBusqueda) {
        return Libro.cargarTodosLosLibros().stream()
                .filter(libro -> libro.getSedeBiblioteca().equalsIgnoreCase(sedeBusqueda)
                        && libro.getIdBiblioteca() == idBibliotecaBusqueda
                        && libro.getUnidadesLibres() > 0)
                .collect(Collectors.toList());
    }

    /**
     * Realiza una busqueda generica de libros por nombre, autor, codigo, sede o ID
     * de biblioteca.
     */
    public List<Libro> buscarLibro(String query, String searchType) {
        List<Libro> allBooks = Libro.cargarTodosLosLibros();
        return allBooks.stream()
                .filter(libro -> {
                    switch (searchType.toLowerCase()) {
                        case "nombre":
                            return libro.getNombre().toLowerCase().contains(query.toLowerCase());
                        case "autor":
                            return libro.getAutor().toLowerCase().contains(query.toLowerCase());
                        case "codigo":
                            return libro.getCodigo().equalsIgnoreCase(query);
                        case "sede":
                            return libro.getSedeBiblioteca().equalsIgnoreCase(query);
                        case "idbiblioteca":
                            try {
                                return libro.getIdBiblioteca() == Integer.parseInt(query);
                            } catch (NumberFormatException e) {
                                return false;
                            }
                        default:
                            return false;
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * Calcula y aplica multas por retraso en la devolucion de libros.
     */
    public String multar(Miembro miembro, Libro libro, Prestamo prestamo) {
        if (prestamo != null && prestamo.getFechaDevolucionReal() != null &&
                prestamo.getFechaDevolucionReal().after(prestamo.getFechaDevolucionEstimada())) {

            long diasRetraso = Fecha.getDaysBetween(prestamo.getFechaDevolucionReal(),
                    prestamo.getFechaDevolucionEstimada());

            if (diasRetraso <= 0)
                diasRetraso = 1;

            double montoMulta = diasRetraso * MULTA_POR_DIA;

            if (miembro instanceof Usuario) {
                Usuario user = (Usuario) miembro;
                user.setDeuda(user.getDeuda() + montoMulta);
                System.out.println("Deuda actualizada para " + user.getNombre() + ". Nueva deuda total: "
                        + String.format("%.2f", user.getDeuda()));
            }

            return String.format(
                    "Se ha aplicado una multa a %s %s por el libro '%s'. Retraso: %d dias. Monto: %.2f pesos. ",
                    miembro.getNombre(), miembro.getApellido(), libro.getNombre(), diasRetraso, montoMulta);
        }
        return "No hay multa para este prestamo.";
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
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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class Usuario extends Miembro {

    private double deuda;
    private String sedeUniversidad; // Renamed to clarify: sede de la universidad del usuario
    private String carrera;

    public Usuario() {
        super();
        this.deuda = 0.0;
        this.sedeUniversidad = "";
        this.carrera = "";
    }

    public Usuario(byte tipoDoc, long noDoc, byte rol, String nombre, String apellido, String usuario,
            String contrasena,
            double deuda, String sedeUniversidad, String carrera) {
        super(tipoDoc, noDoc, rol, nombre, apellido, usuario, contrasena);
        // Asegúrate de que el rol no sea 0 (Admin)
        if (rol == 0) {
            System.err.println(
                    "Advertencia: Se intentó crear un Usuario con rol de Administrador. Estableciendo rol a Estudiante (1).");
            this.rol = 1; // Default to student if trying to create admin as user
        }
        this.deuda = deuda;
        this.sedeUniversidad = sedeUniversidad;
        this.carrera = carrera;
    }

    // Constructor para cuando se carga desde Miembros.txt y no tiene los atributos
    // extra.
    // Se usará al cargar un Miembro y luego castearlo si es Usuario
    public Usuario(Miembro miembroBase, double deuda, String sedeUniversidad, String carrera) {
        super(miembroBase.getTipoDoc(), miembroBase.getNoDoc(), miembroBase.getRol(),
                miembroBase.getNombre(), miembroBase.getApellido(), miembroBase.getUsuario(),
                miembroBase.getContrasena());
        this.deuda = deuda;
        this.sedeUniversidad = sedeUniversidad;
        this.carrera = carrera;
    }

    // Getters para nuevos atributos
    public double getDeuda() {
        return deuda;
    }

    public String getSedeUniversidad() {
        return sedeUniversidad;
    }

    public String getCarrera() {
        return carrera;
    }

    // Setters para nuevos atributos (que invocan save() para persistir cambios)
    public void setDeuda(double deuda) {
        this.deuda = deuda;
        save();
    }

    public void setSedeUniversidad(String sedeUniversidad) {
        this.sedeUniversidad = sedeUniversidad;
        save();
    }

    public void setCarrera(String carrera) {
        this.carrera = carrera;
        save();
    }

    // Override cambiarInfoPersonal para incluir los nuevos atributos
    // Ojo: El rol no se cambia aquí, solo en el admin. La deuda no se cambia aquí.
    public void cambiarInfoPersonal(byte newTipoDoc, String newNombre, String newApellido, String newSedeUniversidad,
            String newCarrera) {
        super.cambiarInfoPersonal(newTipoDoc, newNombre, newApellido); // Llama al método del padre
        this.setSedeUniversidad(newSedeUniversidad);
        this.setCarrera(newCarrera);
        // Los setters individuales ya llaman a save().
    }

    /**
     * Permite al usuario ver la lista de libros que tiene prestados actualmente.
     * Corregido para usar `Fecha.formatDate`.
     *
     * @return Una lista de objetos Prestamo que el usuario tiene activos.
     */
    public List<Prestamo> verMisLibrosPrestados() {
        System.out.println("\n--- MIS LIBROS PRESTADOS (" + this.getNombre() + " " + this.getApellido() + ") ---");
        List<Prestamo> misPrestamos = Prestamo.findPrestamosByMember(this.getNoDoc());

        if (misPrestamos.isEmpty()) {
            System.out.println("No tienes libros prestados o devueltos.");
            return misPrestamos;
        } else {
            for (Prestamo prestamo : misPrestamos) {
                Libro libro = Libro.findBookByCode(prestamo.getCodigoLibro());
                String infoLibro = (libro != null) ? libro.getNombre() + " (Código: " + libro.getCodigo() + ")"
                        : "Libro Desconocido";
                System.out.println("  - " + infoLibro + " | Fecha Préstamo: "
                        + Fecha.formatDate(prestamo.getFechaPrestamo()) + " | Fecha Devolución Estimada: "
                        + Fecha.formatDate(prestamo.getFechaDevolucionEstimada()) + " | Estado: " + prestamo.getEstado()
                        + " | Biblioteca ID: " + prestamo.getIdBiblioteca());
            }
        }
        System.out.println("-----------------------------------------------------------------------------------\n");
        return misPrestamos;
    }

    // Este es el método "verListaLibros" solicitado
    public List<Prestamo> verListaLibros() {
        return verMisLibrosPrestados(); // Delega en el método ya existente
    }

    /**
     * Permite al usuario solicitar el préstamo de un libro.
     * La lógica de validación (límites de libros, disponibilidad, DEUDA) está en la
     * clase Biblioteca.
     *
     * @param codigoLibro El código del libro que se desea prestar.
     * @param biblioteca  La instancia de la biblioteca desde la que se presta el
     *                    libro.
     * @return Un mensaje de éxito o error del préstamo.
     */
    public String solicitarPrestamo(String codigoLibro, Biblioteca biblioteca) {
        if (this.deuda > 0) {
            return "Error: No puedes pedir prestado un libro nuevo. Tienes una deuda pendiente de "
                    + String.format("%.2f", this.deuda) + " pesos.";
        }
        if (biblioteca == null) {
            return "Error: No se ha seleccionado una biblioteca para realizar el préstamo.";
        }
        System.out.println("Intentando prestar libro '" + codigoLibro + "' para " + this.getUsuario()
                + " desde biblioteca " + biblioteca.getNombreBiblioteca());
        return biblioteca.prestarLibro(this, codigoLibro);
    }

    /**
     * Permite al usuario devolver un libro que tiene prestado.
     * La lógica de actualización de estados y multas está en la clase Biblioteca.
     *
     * @param codigoLibro El código del libro que se desea devolver.
     * @param biblioteca  La instancia de la biblioteca a la que se devuelve el
     *                    libro.
     * @return Un mensaje de éxito o error de la devolución.
     */
    public String realizarDevolucion(String codigoLibro, Biblioteca biblioteca) {
        if (biblioteca == null) {
            return "Error: No se ha seleccionado una biblioteca para realizar la devolución.";
        }
        System.out.println("Intentando devolver libro '" + codigoLibro + "' para " + this.getUsuario()
                + " en biblioteca " + biblioteca.getNombreBiblioteca());
        return biblioteca.devolverLibro(this, codigoLibro);
    }

    // Override de buildLine para incluir los nuevos atributos de Usuario
    @Override
    protected String buildLine() {
        // Llama al buildLine del padre y le concatena los nuevos atributos
        return super.buildLine() + "\\" +
                String.format("%.2f", this.deuda) + "\\" + // Formato de deuda
                (this.sedeUniversidad == null ? "" : this.sedeUniversidad) + "\\" +
                (this.carrera == null ? "" : this.carrera);
    }

    // El toString sobrescrito en la versión anterior es bueno y se mantiene.
    @Override
    public String toString() {
        String rolText;
        switch (this.rol) {
            case 1:
                rolText = "Estudiante";
                break;
            case 2:
                rolText = "Profesor";
                break;
            case 3:
                rolText = "Administrativo Universidad";
                break;
            default:
                rolText = "Usuario (Rol desconocido: " + this.rol + ")";
                break;
        }
        return "Usuario {"
                + "\n  Tipo de Documento: " + tipoDoc
                + "\n  Número de Documento: " + noDoc
                + "\n  Rol: " + rolText
                + "\n  Nombre: " + nombre
                + "\n  Apellido: " + apellido
                + "\n  Usuario: " + usuario
                + "\n  Deuda: " + String.format("%.2f", deuda) + " pesos"
                + "\n  Sede Universidad: " + sedeUniversidad
                + "\n  Carrera: " + carrera
                + "\n}";
    }
}
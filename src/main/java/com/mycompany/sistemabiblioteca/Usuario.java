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
    private String sedeUniversidad;
    private String carrera;

    public Usuario() {
        super();
        this.deuda = 0.0;
        this.sedeUniversidad = "";
        this.carrera = "";
    }

    public Usuario(byte tipoDocumento, long numeroDocumento, byte rol, String nombre, String apellido, String usuario,
            String contrasena, double deuda, String sedeUniversidad, String carrera) {
        super(tipoDocumento, numeroDocumento, rol, nombre, apellido, usuario, contrasena);

        if (rol == 0) {
            System.err.println(
                    "Advertencia: Se intento crear un Usuario con rol de Administrador. Estableciendo rol a Estudiante (1).");
            this.rol = 1;
        }
        this.deuda = deuda;
        this.sedeUniversidad = sedeUniversidad;
        this.carrera = carrera;
    }

    public double getDeuda() {
        return deuda;
    }

    public String getSedeUniversidad() {
        return sedeUniversidad;
    }

    public String getCarrera() {
        return carrera;
    }

    // Setters (que invocan guardar() para persistir cambios)
    public void setDeuda(double deuda) {
        this.deuda = deuda;
        guardar();
    }

    public void setSedeUniversidad(String sedeUniversidad) {
        this.sedeUniversidad = sedeUniversidad;
        guardar();
    }

    public void setCarrera(String carrera) {
        this.carrera = carrera;
        guardar();
    }

    /**
     * Permite al usuario ver la lista de libros que tiene prestados actualmente.
     * 
     * @return Una lista de objetos Prestamo que el usuario tiene activos.
     */
    public List<Prestamo> verMisLibrosPrestados() {
        System.out.println("\n--- MIS LIBROS PRESTADOS (" + this.getNombre() + " " + this.getApellido() + ") ---");
        List<Prestamo> misPrestamos = Prestamo.encontrarPrestamosPorMiembro(this.getNumeroDocumento());

        if (misPrestamos.isEmpty()) {
            System.out.println("No tienes libros prestados o devueltos.");
            return misPrestamos;
        } else {
            for (Prestamo prestamo : misPrestamos) {
                Libro libro = Libro.encontrarLibroPorCodigo(prestamo.getCodigoLibro());
                String infoLibro = (libro != null) ? libro.getNombre() + " (Codigo: " + libro.getCodigo() + ")"
                        : "Libro Desconocido";
                System.out.println("  - " + infoLibro + " | Fecha Prestamo: "
                        + Fecha.formatDate(prestamo.getFechaPrestamo()) + " | Fecha Devolucion Estimada: "
                        + Fecha.formatDate(prestamo.getFechaDevolucionEstimada()) + " | Estado: " + prestamo.getEstado()
                        + " | Biblioteca ID: " + prestamo.getIdBiblioteca());
            }
        }
        System.out.println("-----------------------------------------------------------------------------------\n");
        return misPrestamos;
    }

    /**
     * Permite al usuario solicitar el prestamo de un libro.
     * La logica de validacion (limites de libros, disponibilidad, DEUDA) esta en la
     * clase Biblioteca.
     *
     * @param codigoLibro El codigo del libro que se desea prestar.
     * @param biblioteca  La instancia de la biblioteca desde la que se presta el
     *                    libro.
     * @return Un mensaje de exito o error del prestamo.
     */
    public String solicitarPrestamo(String codigoLibro, Biblioteca biblioteca) {
        if (this.deuda > 0) {
            return "Error: No puedes pedir prestado un libro nuevo. Tienes una deuda pendiente de "
                    + String.format("%.2f", this.deuda) + " pesos.";
        }
        if (biblioteca == null) {
            return "Error: No se ha seleccionado una biblioteca para realizar el prestamo.";
        }
        System.out.println("Intentando prestar libro '" + codigoLibro + "' para " + this.getUsuario()
                + " desde biblioteca " + biblioteca.getNombreBiblioteca());
        return biblioteca.prestarLibro(this, codigoLibro);
    }

    /**
     * Permite al usuario devolver un libro que tiene prestado.
     * La logica de actualizacion de estados y multas esta en la clase Biblioteca.
     *
     * @param codigoLibro El codigo del libro que se desea devolver.
     * @param biblioteca  La instancia de la biblioteca a la que se devuelve el
     *                    libro.
     * @return Un mensaje de exito o error de la devolucion.
     */
    public String realizarDevolucion(String codigoLibro, Biblioteca biblioteca) {
        if (biblioteca == null) {
            return "Error: No se ha seleccionado una biblioteca para realizar la devolucion.";
        }
        System.out.println("Intentando devolver libro '" + codigoLibro + "' para " + this.getUsuario()
                + " en biblioteca " + biblioteca.getNombreBiblioteca());
        return biblioteca.devolverLibro(this, codigoLibro);
    }

    /**
     * Sobrecarga del metodo cambiarInfoPersonal para incluir los atributos
     * especificos de Usuario.
     * Llama al metodo del padre para los campos comunes y actualiza los propios.
     *
     * @param nuevoTipoDocumento   El nuevo tipo de documento del usuario.
     * @param nuevoNombre          El nuevo nombre del usuario.
     * @param nuevoApellido        El nuevo apellido del usuario.
     * @param nuevaSedeUniversidad La nueva sede de la universidad del usuario.
     * @param nuevaCarrera         La nueva carrera del usuario.
     */
    public void cambiarInfoPersonal(byte nuevoTipoDocumento, String nuevoNombre, String nuevoApellido,
            String nuevaSedeUniversidad, String nuevaCarrera) {
        super.cambiarInfoPersonal(nuevoTipoDocumento, nuevoNombre, nuevoApellido); // Llama al metodo del padre
                                                                                   // (Miembro)
        this.setSedeUniversidad(nuevaSedeUniversidad); // El setter ya llama a guardar()
        this.setCarrera(nuevaCarrera); // El setter ya llama a guardar()
        // No es necesario llamar a guardar() de nuevo aqui ya que cada setter lo hace
        // al final
    }

    // Override de construirLinea para incluir los nuevos atributos de Usuario
    @Override
    public String construirLinea() {
        return super.construirLinea() + "\\" +
                String.format("%.2f", this.deuda) + "\\" +
                (this.sedeUniversidad == null ? "" : this.sedeUniversidad) + "\\" +
                (this.carrera == null ? "" : this.carrera);
    }

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
                + "\n  Tipo de Documento: " + tipoDocumento
                + "\n  Numero de Documento: " + numeroDocumento
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
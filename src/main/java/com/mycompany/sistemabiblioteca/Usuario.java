/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.sistemabiblioteca;

/**
 *
 * @author edrui
 */

import java.util.List;
import java.util.stream.Collectors;

public class Usuario extends Miembro {

    public Usuario() {
        super();
        // El rol por defecto para un Usuario podría ser Estudiante (1) si no se especifica.
        // O este constructor podría no ser usado directamente si siempre se especifica el rol.
    }

    public Usuario(byte tipoDoc, long noDoc, byte rol, String nombre, String apellido, String usuario, String contrasena) {
        super(tipoDoc, noDoc, rol, nombre, apellido, usuario, contrasena);
        // Asegúrate de que el rol no sea 0 (Admin)
        if (rol == 0) {
            System.err.println("Advertencia: Se intentó crear un Usuario con rol de Administrador. Estableciendo rol a Estudiante (1).");
            this.rol = 1; // Default to student if trying to create admin as user
        }
    }

    // Métodos específicos para un usuario (no admin)

    /**
     * Permite al usuario ver la lista de libros que tiene prestados actualmente.
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
                String infoLibro = (libro != null) ? libro.getNombre() + " (Código: " + libro.getCodigo() + ")" : "Libro Desconocido";
                System.out.println("  - " + infoLibro + " | Fecha Préstamo: " + Fecha.formatDate(prestamo.getFechaPrestamo()) + " | Fecha Devolución Estimada: " + Fecha.formatDate(prestamo.getFechaDevolucionEstimada()) + " | Estado: " + prestamo.getEstado() + " | Biblioteca ID: " + prestamo.getIdBiblioteca());
            }
        }
        System.out.println("-----------------------------------------------------------------------------------\n");
        return misPrestamos;
    }

    /**
     * Permite al usuario solicitar el préstamo de un libro.
     * La lógica de validación (límites de libros, disponibilidad) está en la clase Biblioteca.
     *
     * @param codigoLibro El código del libro que se desea prestar.
     * @param biblioteca  La instancia de la biblioteca desde la que se presta el libro.
     * @return Un mensaje de éxito o error del préstamo.
     */
    public String solicitarPrestamo(String codigoLibro, Biblioteca biblioteca) {
        if (biblioteca == null) {
            return "Error: No se ha seleccionado una biblioteca para realizar el préstamo.";
        }
        System.out.println("Intentando prestar libro '" + codigoLibro + "' para " + this.getUsuario() + " desde biblioteca " + biblioteca.getNombreBiblioteca());
        return biblioteca.prestarLibro(this, codigoLibro);
    }

    /**
     * Permite al usuario devolver un libro que tiene prestado.
     * La lógica de actualización de estados y multas está en la clase Biblioteca.
     *
     * @param codigoLibro El código del libro que se desea devolver.
     * @param biblioteca  La instancia de la biblioteca a la que se devuelve el libro.
     * @return Un mensaje de éxito o error de la devolución.
     */
    public String realizarDevolucion(String codigoLibro, Biblioteca biblioteca) {
        if (biblioteca == null) {
            return "Error: No se ha seleccionado una biblioteca para realizar la devolución.";
        }
        System.out.println("Intentando devolver libro '" + codigoLibro + "' para " + this.getUsuario() + " en biblioteca " + biblioteca.getNombreBiblioteca());
        return biblioteca.devolverLibro(this, codigoLibro);
    }
    
    // Sobreescribe toString para que se distinga de un Miembro genérico si es necesario
    @Override
    public String toString() {
        String rolText;
        switch (this.rol) {
            case 1: rolText = "Estudiante"; break;
            case 2: rolText = "Profesor"; break;
            case 3: rolText = "Administrativo Universidad"; break;
            default: rolText = "Usuario (Rol desconocido: " + this.rol + ")"; break;
        }
        return "Usuario {"
                + "\n  Tipo de Documento: " + tipoDoc
                + "\n  Número de Documento: " + noDoc
                + "\n  Rol: " + rolText
                + "\n  Nombre: " + nombre
                + "\n  Apellido: " + apellido
                + "\n  Usuario: " + usuario
                + "\n}";
    }
}
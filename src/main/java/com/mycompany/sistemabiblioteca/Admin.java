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
import java.util.Scanner;
import java.util.Date;
import java.util.stream.Collectors;

public class Admin extends Miembro {
    
    // El atributo bibliotecaActual se moverá a SistemaBiblioteca para un manejo más centralizado
    // Un admin puede interactuar con múltiples bibliotecas, el "contexto" de la biblioteca actual
    // lo puede manejar la clase que orqueste la interacción (SistemaBiblioteca).

    public Admin() {
        super();
        this.rol = 0; // Asegura que el rol sea Admin
    }

    public Admin(byte tipoDoc, long noDoc, byte rol, String nombre, String apellido, String usuario, String contrasena) {
        super(tipoDoc, noDoc, (byte) 0, nombre, apellido, usuario, contrasena); // Rol siempre 0 para Admin
    }

    // --- Métodos de Administración de Libros ---

    public void agregarLibro(String nombre, String autor, String codigo, int totalUnidades, int idBiblioteca, String sedeBiblioteca) {
        if (Libro.findBookByCode(codigo) != null) {
            System.out.println("Error: Ya existe un libro con el código " + codigo + ".");
            return;
        }
        Libro nuevoLibro = new Libro(nombre, autor, codigo, totalUnidades, idBiblioteca, sedeBiblioteca);
        nuevoLibro.save();
        System.out.println("Libro '" + nombre + "' agregado con éxito.");
    }

    public void editarLibro(String codigoLibro, String nuevoNombre, String nuevoAutor, int nuevasUnidadesTotales, int nuevaIdBiblioteca, String nuevaSedeBiblioteca) {
        Libro libro = Libro.findBookByCode(codigoLibro);
        if (libro == null) {
            System.out.println("Error: Libro con código " + codigoLibro + " no encontrado.");
            return;
        }

        int unidadesPrestadasActuales = libro.getUnPrestadas();
        if (nuevasUnidadesTotales < unidadesPrestadasActuales) {
            System.out.println("Error: Las nuevas unidades totales (" + nuevasUnidadesTotales + ") no pueden ser menores que las unidades actualmente prestadas (" + unidadesPrestadasActuales + ").");
            return;
        }

        libro.setNombre(nuevoNombre);
        libro.setAutor(nuevoAutor);
        libro.setUnLibres(nuevasUnidadesTotales - unidadesPrestadasActuales); // Recalcular unidades libres
        libro.setIdBiblioteca(nuevaIdBiblioteca);
        libro.setSedeBiblioteca(nuevaSedeBiblioteca);
        
        // Las llamadas a los setters ya invocan save(), pero una llamada final asegura la consistencia.
        libro.save(); 
        System.out.println("Libro '" + codigoLibro + "' actualizado con éxito.");
    }

    public void eliminarLibro(String codigoLibro) {
        Libro libro = Libro.findBookByCode(codigoLibro);
        if (libro == null) {
            System.out.println("Error: Libro con código " + codigoLibro + " no encontrado.");
            return;
        }
        if (libro.getUnPrestadas() > 0) {
            System.out.println("Error: No se puede eliminar el libro '" + libro.getNombre() + "' porque tiene " + libro.getUnPrestadas() + " unidades prestadas.");
            return;
        }
        libro.delete();
        System.out.println("Libro '" + libro.getNombre() + "' eliminado con éxito.");
    }

    // --- Métodos de Administración de Usuarios ---

    public void agregarUsuario(byte tipoDoc, long noDoc, byte rol, String nombre, String apellido, String usuario, String contrasena) {
        if (Miembro.findMemberByUsername(usuario) != null) {
            System.out.println("Error: Ya existe un usuario con el nombre de usuario '" + usuario + "'.");
            return;
        }
        if (Miembro.findMemberByNoDoc(noDoc) != null) {
             System.out.println("Error: Ya existe un usuario con el número de documento '" + noDoc + "'.");
             return;
        }
        // Creamos una instancia de Miembro directamente, y luego será cargada como Admin o Usuario
        Miembro nuevoMiembro = new Miembro(tipoDoc, noDoc, rol, nombre, apellido, usuario, contrasena);
        nuevoMiembro.save();
        System.out.println("Usuario '" + usuario + "' agregado con éxito (Rol: " + rol + ").");
    }

    public void editarUsuario(String usuarioExistente, byte nuevoTipoDoc, long nuevoNoDoc, byte nuevoRol, String nuevoNombre, String nuevoApellido, String nuevoUsuario, String nuevaContrasena) {
        Miembro miembro = Miembro.findMemberByUsername(usuarioExistente);
        if (miembro == null) {
            System.out.println("Error: Usuario '" + usuarioExistente + "' no encontrado.");
            return;
        }
        
        // Verificar si el nuevo usuario ya existe y no es el mismo
        if (!usuarioExistente.equalsIgnoreCase(nuevoUsuario) && Miembro.findMemberByUsername(nuevoUsuario) != null) {
            System.out.println("Error: El nuevo nombre de usuario '" + nuevoUsuario + "' ya está en uso.");
            return;
        }
        
        // Verificar si el nuevo número de documento ya existe y no es el mismo
        if (miembro.getNoDoc() != nuevoNoDoc && Miembro.findMemberByNoDoc(nuevoNoDoc) != null) {
             System.out.println("Error: El nuevo número de documento '" + nuevoNoDoc + "' ya está en uso.");
             return;
        }

        miembro.setTipoDoc(nuevoTipoDoc);
        miembro.setNoDoc(nuevoNoDoc); // Esto llama a saveInternal() en Miembro
        miembro.setRol(nuevoRol);
        miembro.setNombre(nuevoNombre);
        miembro.setApellido(nuevoApellido);
        miembro.setUsuario(nuevoUsuario); // Esto llama a saveInternal() en Miembro
        if (nuevaContrasena != null && !nuevaContrasena.isEmpty()) {
            miembro.setContrasena(nuevaContrasena);
        } else {
             // Si no se cambia la contraseña, asegúrate de guardar los otros cambios
             miembro.save(); 
        }
       
        System.out.println("Usuario '" + usuarioExistente + "' actualizado con éxito.");
    }

    public void eliminarUsuario(String usuarioAEliminar) {
        Miembro miembro = Miembro.findMemberByUsername(usuarioAEliminar);
        if (miembro == null) {
            System.out.println("Error: Usuario '" + usuarioAEliminar + "' no encontrado.");
            return;
        }
        
        // Verificar si el usuario tiene libros prestados
        List<Prestamo> prestamosActivos = Prestamo.findPrestamosByMember(miembro.getNoDoc()).stream()
                                                    .filter(p -> "PRESTADO".equals(p.getEstado()))
                                                    .collect(Collectors.toList());
        if (!prestamosActivos.isEmpty()) {
            System.out.println("Error: No se puede eliminar al usuario '" + usuarioAEliminar + "' porque tiene " + prestamosActivos.size() + " libros prestados.");
            return;
        }

        miembro.setUsuario(null); // Marcar para eliminación (internamente en saveInternal, si usuario es null, se elimina)
        miembro.save(); // saveInternal() eliminará la línea
        System.out.println("Usuario '" + usuarioAEliminar + "' eliminado con éxito.");
    }

    // --- Métodos de Visualización ---

    public void verTodosUsuarios() {
        System.out.println("\n--- LISTADO DE TODOS LOS USUARIOS ---");
        List<Miembro> miembros = Miembro.loadAllMembers();
        if (miembros.isEmpty()) {
            System.out.println("No hay usuarios registrados en el sistema.");
        } else {
            miembros.forEach(m -> {
                if (m.getRol() == 0) {
                    System.out.println(m.toString()); // Ya es un Admin y su toString lo reflejará
                } else {
                    // Cargar como Usuario para que su toString sea específico
                    Usuario u = new Usuario(m.getTipoDoc(), m.getNoDoc(), m.getRol(), m.getNombre(), m.getApellido(), m.getUsuario(), m.getContrasena());
                    System.out.println(u.toString());
                }
            });
        }
        System.out.println("-------------------------------------\n");
    }

    public void verTodosLibros() {
        System.out.println("\n--- LISTADO DE TODOS LOS LIBROS ---");
        List<Libro> libros = Libro.loadAllBooks();
        if (libros.isEmpty()) {
            System.out.println("No hay libros registrados en el sistema.");
        } else {
            libros.forEach(System.out::println);
        }
        System.out.println("-----------------------------------\n");
    }

    public void verLibrosPrestados() {
        System.out.println("\n--- LIBROS ACTUALMENTE PRESTADOS ---");
        List<Prestamo> prestamos = Prestamo.loadAllPrestamos().stream()
                                    .filter(p -> "PRESTADO".equals(p.getEstado()))
                                    .collect(Collectors.toList());
        
        if (prestamos.isEmpty()) {
            System.out.println("No hay libros prestados actualmente.");
        } else {
            for (Prestamo prestamo : prestamos) {
                Miembro miembro = Miembro.findMemberByNoDoc(prestamo.getNoDocMiembro());
                Libro libro = Libro.findBookByCode(prestamo.getCodigoLibro());
                String infoMiembro = (miembro != null) ? miembro.getNombre() + " " + miembro.getApellido() + " (Usuario: " + miembro.getUsuario() + ")" : "Miembro Desconocido";
                String infoLibro = (libro != null) ? libro.getNombre() + " (Código: " + libro.getCodigo() + ")" : "Libro Desconocido";
                System.out.println("Prestamo: " + infoLibro + " a " + infoMiembro + " | Fecha Préstamo: " + Fecha.formatDate(prestamo.getFechaPrestamo()) + " | Fecha Devolución Estimada: " + Fecha.formatDate(prestamo.getFechaDevolucionEstimada()) + " | Biblioteca ID: " + prestamo.getIdBiblioteca());
            }
        }
        System.out.println("------------------------------------\n");
    }
    
    // Método para que el admin pueda ver los libros prestados por un usuario específico
    public void verLibrosPrestadosPorUsuario(long noDocMiembro) {
        Miembro miembro = Miembro.findMemberByNoDoc(noDocMiembro);
        if (miembro == null) {
            System.out.println("Error: Miembro con número de documento " + noDocMiembro + " no encontrado.");
            return;
        }

        System.out.println("\n--- LIBROS PRESTADOS POR " + miembro.getNombre().toUpperCase() + " " + miembro.getApellido().toUpperCase() + " ---");
        List<Prestamo> prestamosDelMiembro = Prestamo.findPrestamosByMember(noDocMiembro);

        if (prestamosDelMiembro.isEmpty()) {
            System.out.println(miembro.getNombre() + " no tiene ningún libro prestado o devuelto.");
        } else {
            for (Prestamo prestamo : prestamosDelMiembro) {
                Libro libro = Libro.findBookByCode(prestamo.getCodigoLibro());
                String infoLibro = (libro != null) ? libro.getNombre() + " (Código: " + libro.getCodigo() + ")" : "Libro Desconocido";
                System.out.println("  - " + infoLibro + " | Fecha Préstamo: " + Fecha.formatDate(prestamo.getFechaPrestamo()) + " | Fecha Devolución Estimada: " + Fecha.formatDate(prestamo.getFechaDevolucionEstimada()) + " | Estado: " + prestamo.getEstado());
            }
        }
        System.out.println("-------------------------------------------------------------------\n");
    }

    @Override
    public String toString() {
        return "Admin {"
                + "\n  " + super.toString().replace("Miembro {", "").replaceFirst("\\n}$", "") // Reemplazar el inicio y el final para integrar mejor
                + "\n}";
    }
}
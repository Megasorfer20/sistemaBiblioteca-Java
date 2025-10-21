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

    public Admin() {
        super();
        this.rol = 0; // Asegura que el rol sea Admin
    }

    public Admin(byte tipoDoc, long noDoc, byte rol, String nombre, String apellido, String usuario,
            String contrasena) {
        super(tipoDoc, noDoc, (byte) 0, nombre, apellido, usuario, contrasena); // Rol siempre 0 para Admin
    }

    // --- Métodos de Administración de Libros ---

    // Este es el método "agregarLibro" solicitado
    public void agregarLibro(String nombre, String autor, String codigo, int totalUnidades, int idBiblioteca,
            String sedeBiblioteca) {
        if (Libro.findBookByCode(codigo) != null) {
            System.out.println("Error: Ya existe un libro con el código " + codigo + ".");
            return;
        }
        Libro nuevoLibro = new Libro(nombre, autor, codigo, totalUnidades, idBiblioteca, sedeBiblioteca);
        nuevoLibro.save();
        System.out.println("Libro '" + nombre + "' agregado con éxito.");
    }

    // Este es el método "editarLibro" solicitado
    public void editarLibro(String codigoLibro, String nuevoNombre, String nuevoAutor, int nuevasUnidadesTotales,
            int nuevaIdBiblioteca, String nuevaSedeBiblioteca) {
        Libro libro = Libro.findBookByCode(codigoLibro);
        if (libro == null) {
            System.out.println("Error: Libro con código " + codigoLibro + " no encontrado.");
            return;
        }

        int unidadesPrestadasActuales = libro.getUnPrestadas();
        if (nuevasUnidadesTotales < unidadesPrestadasActuales) {
            System.out.println("Error: Las nuevas unidades totales (" + nuevasUnidadesTotales
                    + ") no pueden ser menores que las unidades actualmente prestadas (" + unidadesPrestadasActuales
                    + ").");
            return;
        }

        libro.setNombre(nuevoNombre);
        libro.setAutor(nuevoAutor);
        libro.setUnLibres(nuevasUnidadesTotales - unidadesPrestadasActuales); // Recalcular unidades libres
        libro.setIdBiblioteca(nuevaIdBiblioteca);
        libro.setSedeBiblioteca(nuevaSedeBiblioteca);

        // Las llamadas a los setters ya invocan save(), pero una llamada final asegura
        // la consistencia.
        libro.save();
        System.out.println("Libro '" + codigoLibro + "' actualizado con éxito.");
    }

    // Este es el método "eliminarLibro" solicitado, con la lógica de unidades
    // prestadas.
    // "Puede eliminar los libros que no están prestados, si están prestados se
    // eliminan los no prestados,
    // y las copias que tiene el usuario se conservan aún pero los que no han
    // prestado se eliminan"
    public void eliminarLibro(String codigoLibro, int unidadesAeliminar) {
        Libro libro = Libro.findBookByCode(codigoLibro);
        if (libro == null) {
            System.out.println("Error: Libro con código " + codigoLibro + " no encontrado.");
            return;
        }

        if (unidadesAeliminar <= 0) {
            System.out.println("Error: El número de unidades a eliminar debe ser mayor que 0.");
            return;
        }

        // Si se intenta eliminar más unidades de las libres, ajustamos a las libres
        if (unidadesAeliminar > libro.getUnLibres()) {
            System.out.println("Advertencia: Se intenta eliminar " + unidadesAeliminar + " unidades, pero solo hay "
                    + libro.getUnLibres() + " unidades libres. Se eliminarán solo las unidades libres.");
            unidadesAeliminar = libro.getUnLibres();
        }

        if (unidadesAeliminar == 0 && libro.getUnPrestadas() > 0) {
            System.out.println("No hay unidades libres para eliminar del libro '" + libro.getNombre()
                    + "'. Las unidades prestadas (" + libro.getUnPrestadas()
                    + ") no se pueden eliminar hasta que sean devueltas.");
            return;
        }

        if (unidadesAeliminar > 0) {
            libro.setUnLibres(libro.getUnLibres() - unidadesAeliminar);
            // Si todas las unidades (libres y prestadas) se eliminaran y quedan 0, entonces
            // eliminar el registro completo del libro.
            // La lógica "las copias que tiene el usuario se conservan aún" implica que no
            // debemos eliminar el registro del libro
            // si unPrestadas es > 0, solo reducir las unLibres. Si unPrestadas es 0 y
            // unLibres llega a 0, entonces sí.
            if (libro.getUnLibres() == 0 && libro.getUnPrestadas() == 0) {
                libro.delete(); // Esto elimina el registro del archivo
                System.out.println("Libro '" + libro.getNombre() + "' completamente eliminado (todas las unidades).");
            } else {
                libro.save(); // Solo actualiza las unidades
                System.out.println(unidadesAeliminar + " unidades del libro '" + libro.getNombre()
                        + "' eliminadas con éxito. Quedan " + libro.getUnLibres() + " libres y "
                        + libro.getUnPrestadas() + " prestadas.");
            }
        } else {
            System.out.println("No se realizaron cambios en las unidades del libro '" + libro.getNombre() + "'.");
        }
    }

    // Sobrecarga para eliminar todas las unidades libres si no se especifica
    // cantidad
    public void eliminarLibro(String codigoLibro) {
        Libro libro = Libro.findBookByCode(codigoLibro);
        if (libro == null) {
            System.out.println("Error: Libro con código " + codigoLibro + " no encontrado.");
            return;
        }
        // Llamar al método con la cantidad de unidades libres
        eliminarLibro(codigoLibro, libro.getUnLibres());
    }

    // --- Métodos de Administración de Usuarios ---

    // Este es el método "crearUsuario" solicitado
    public void crearUsuario(byte tipoDoc, long noDoc, byte rol, String nombre, String apellido, String usuario,
            String contrasena,
            double deuda, String sedeUniversidad, String carrera) {
        if (Miembro.findMemberByUsername(usuario) != null) {
            System.out.println("Error: Ya existe un usuario con el nombre de usuario '" + usuario + "'.");
            return;
        }
        if (Miembro.findMemberByNoDoc(noDoc) != null) {
            System.out.println("Error: Ya existe un usuario con el número de documento '" + noDoc + "'.");
            return;
        }
        // Creamos una instancia de Usuario para que los atributos específicos se
        // guarden correctamente
        Usuario nuevoUsuario = new Usuario(tipoDoc, noDoc, rol, nombre, apellido, usuario, contrasena, deuda,
                sedeUniversidad, carrera);
        nuevoUsuario.save();
        System.out.println("Usuario '" + usuario + "' agregado con éxito (Rol: " + rol + ").");
    }

    // Este es el método "editarUsuario" solicitado
    public void editarUsuario(String usuarioExistente, byte nuevoTipoDoc, long nuevoNoDoc, byte nuevoRol,
            String nuevoNombre, String nuevoApellido, String nuevoUsuario, String nuevaContrasena,
            double nuevaDeuda, String nuevaSedeUniversidad, String nuevaCarrera) {
        Miembro miembroBase = Miembro.findMemberByUsername(usuarioExistente);
        if (miembroBase == null) {
            System.out.println("Error: Usuario '" + usuarioExistente + "' no encontrado.");
            return;
        }

        // Si el usuario es un Admin, no debe tener campos de deuda, sede, carrera.
        if (miembroBase.getRol() == 0 && nuevoRol != 0) {
            System.out.println(
                    "Advertencia: Se está intentando cambiar el rol de un Admin a un rol de usuario normal. Los campos específicos de usuario se aplicarán.");
        } else if (miembroBase.getRol() != 0 && nuevoRol == 0) {
            System.out.println(
                    "Advertencia: Se está intentando cambiar el rol de un usuario normal a Admin. Los campos específicos de usuario serán ignorados en esta instancia.");
        }

        // Verificar si el nuevo usuario ya existe y no es el mismo
        if (!usuarioExistente.equalsIgnoreCase(nuevoUsuario) && Miembro.findMemberByUsername(nuevoUsuario) != null) {
            System.out.println("Error: El nuevo nombre de usuario '" + nuevoUsuario + "' ya está en uso.");
            return;
        }

        // Verificar si el nuevo número de documento ya existe y no es el mismo
        if (miembroBase.getNoDoc() != nuevoNoDoc && Miembro.findMemberByNoDoc(nuevoNoDoc) != null) {
            System.out.println("Error: El nuevo número de documento '" + nuevoNoDoc + "' ya está en uso.");
            return;
        }

        miembroBase.setTipoDoc(nuevoTipoDoc);
        miembroBase.setNoDoc(nuevoNoDoc); // Esto llama a saveInternal() en Miembro
        miembroBase.setRol(nuevoRol); // El rol sí se puede cambiar por el admin
        miembroBase.setNombre(nuevoNombre);
        miembroBase.setApellido(nuevoApellido);
        miembroBase.setUsuario(nuevoUsuario); // Esto llama a saveInternal() en Miembro
        if (nuevaContrasena != null && !nuevaContrasena.isEmpty()) {
            miembroBase.setContrasena(nuevaContrasena);
        }

        // Campos específicos de Usuario (solo aplicar si el miembro es o se convierte
        // en un Usuario)
        if (miembroBase.getRol() != 0) { // Si el rol final no es Admin, se trata como Usuario
            Usuario usuarioEdit;
            if (miembroBase instanceof Usuario) {
                usuarioEdit = (Usuario) miembroBase; // Si ya es Usuario, lo casteamos
            } else {
                // Si era Admin y su rol fue cambiado, creamos una nueva instancia de Usuario
                usuarioEdit = new Usuario(miembroBase.getTipoDoc(), miembroBase.getNoDoc(), miembroBase.getRol(),
                        miembroBase.getNombre(), miembroBase.getApellido(), miembroBase.getUsuario(),
                        miembroBase.getContrasena(), 0.0, "", "");
                System.out.println("El miembro fue convertido de Admin a Usuario. Se inicializan campos de Usuario.");
            }
            usuarioEdit.setDeuda(nuevaDeuda);
            usuarioEdit.setSedeUniversidad(nuevaSedeUniversidad);
            usuarioEdit.setCarrera(nuevaCarrera);
            usuarioEdit.save(); // Guarda el objeto Usuario completo
        } else {
            miembroBase.save(); // Si es Admin o se mantiene como Admin, solo guardar los campos base
        }

        System.out.println("Miembro '" + usuarioExistente + "' actualizado con éxito.");
    }

    // Este es el método "eliminarUsuario" solicitado
    public void eliminarUsuario(String usuarioAEliminar) {
        Miembro miembro = Miembro.findMemberByUsername(usuarioAEliminar);
        if (miembro == null) {
            System.out.println("Error: Usuario '" + usuarioAEliminar + "' no encontrado.");
            return;
        }

        // No se puede eliminar un Admin (rol 0) a menos que sea una función muy
        // específica.
        // Asumimos que no se puede eliminar el propio admin o un admin sin otra lógica.
        if (miembro.getRol() == 0) {
            System.out.println("Error: No se puede eliminar a un usuario con rol de Administrador directamente.");
            return;
        }

        // Verificar si el usuario tiene libros prestados
        List<Prestamo> prestamosActivos = Prestamo.findPrestamosByMember(miembro.getNoDoc()).stream()
                .filter(p -> "PRESTADO".equals(p.getEstado()))
                .collect(Collectors.toList());
        if (!prestamosActivos.isEmpty()) {
            System.out.println("Error: No se puede eliminar al usuario '" + usuarioAEliminar + "' porque tiene "
                    + prestamosActivos.size() + " libros prestados.");
            return;
        }

        // Verificar si el usuario tiene deuda
        if (miembro instanceof Usuario) { // Asegurarse de que es un objeto Usuario para acceder a la deuda
            Usuario user = (Usuario) miembro;
            if (user.getDeuda() > 0) {
                System.out.println("Error: No se puede eliminar al usuario '" + usuarioAEliminar
                        + "' porque tiene una deuda pendiente de " + String.format("%.2f", user.getDeuda())
                        + " pesos.");
                return;
            }
        }

        miembro.setUsuario(null); // Marcar para eliminación (internamente en saveInternal, si usuario es null, se
                                  // elimina)
        miembro.save(); // saveInternal() eliminará la línea
        System.out.println("Usuario '" + usuarioAEliminar + "' eliminado con éxito.");
    }

    // --- Métodos de Visualización ---

    // Este es el método "verUsuarios" solicitado (o "verTodosUsuarios")
    public void verTodosUsuarios() {
        System.out.println("\n--- LISTADO DE TODOS LOS MIEMBROS DEL SISTEMA (incluye Admins y Usuarios) ---");
        List<Miembro> miembros = Miembro.loadAllMembers();
        if (miembros.isEmpty()) {
            System.out.println("No hay miembros registrados en el sistema.");
        } else {
            miembros.forEach(System.out::println); // toString() de cada clase se encargará del formato
        }
        System.out.println("-------------------------------------------------------------------\n");
    }

    // Este es el método "verLibros" solicitado (o "verTodosLibros")
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

    // Este es el método "verLibro" solicitado (para un libro específico)
    public void verLibro(String codigoLibro) {
        Libro libro = Libro.findBookByCode(codigoLibro);
        if (libro == null) {
            System.out.println("Error: Libro con código " + codigoLibro + " no encontrado.");
            return;
        }
        System.out.println("\n--- INFORMACIÓN DEL LIBRO ---");
        System.out.println(libro);

        // También mostrar los préstamos asociados a este libro si es relevante para el
        // admin
        List<Prestamo> prestamosLibro = Prestamo.loadAllPrestamos().stream()
                .filter(p -> p.getCodigoLibro().equals(codigoLibro))
                .collect(Collectors.toList());
        if (!prestamosLibro.isEmpty()) {
            System.out.println("\n--- PRÉSTAMOS ASOCIADOS AL LIBRO " + libro.getNombre() + " ---");
            for (Prestamo p : prestamosLibro) {
                Miembro m = Miembro.findMemberByNoDoc(p.getNoDocMiembro());
                System.out
                        .println("  - Miembro: " + (m != null ? m.getNombre() + " " + m.getApellido() : "Desconocido") +
                                ", Fecha Prestamo: " + Fecha.formatDate(p.getFechaPrestamo()) +
                                ", Fecha Devolución Estimada: " + Fecha.formatDate(p.getFechaDevolucionEstimada()) +
                                ", Estado: " + p.getEstado());
            }
        } else {
            System.out.println("No hay préstamos registrados para este libro.");
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
                String infoMiembro = (miembro != null)
                        ? miembro.getNombre() + " " + miembro.getApellido() + " (Usuario: " + miembro.getUsuario() + ")"
                        : "Miembro Desconocido";
                String infoLibro = (libro != null) ? libro.getNombre() + " (Código: " + libro.getCodigo() + ")"
                        : "Libro Desconocido";
                System.out.println("Prestamo: " + infoLibro + " a " + infoMiembro + " | Fecha Préstamo: "
                        + Fecha.formatDate(prestamo.getFechaPrestamo()) + " | Fecha Devolución Estimada: "
                        + Fecha.formatDate(prestamo.getFechaDevolucionEstimada()) + " | Biblioteca ID: "
                        + prestamo.getIdBiblioteca());
            }
        }
        System.out.println("------------------------------------\n");
    }

    // Método para que el admin pueda ver los libros prestados por un usuario
    // específico
    public void verLibrosPrestadosPorUsuario(long noDocMiembro) {
        Miembro miembro = Miembro.findMemberByNoDoc(noDocMiembro);
        if (miembro == null) {
            System.out.println("Error: Miembro con número de documento " + noDocMiembro + " no encontrado.");
            return;
        }

        System.out.println("\n--- LIBROS PRESTADOS POR " + miembro.getNombre().toUpperCase() + " "
                + miembro.getApellido().toUpperCase() + " ---");
        List<Prestamo> prestamosDelMiembro = Prestamo.findPrestamosByMember(noDocMiembro);

        if (prestamosDelMiembro.isEmpty()) {
            System.out.println(miembro.getNombre() + " no tiene ningún libro prestado o devuelto.");
        } else {
            for (Prestamo prestamo : prestamosDelMiembro) {
                Libro libro = Libro.findBookByCode(prestamo.getCodigoLibro());
                String infoLibro = (libro != null) ? libro.getNombre() + " (Código: " + libro.getCodigo() + ")"
                        : "Libro Desconocido";
                System.out.println("  - " + infoLibro + " | Fecha Préstamo: "
                        + Fecha.formatDate(prestamo.getFechaPrestamo()) + " | Fecha Devolución Estimada: "
                        + Fecha.formatDate(prestamo.getFechaDevolucionEstimada()) + " | Estado: "
                        + prestamo.getEstado());
            }
        }
        System.out.println("-------------------------------------------------------------------\n");
    }

    @Override
    public String toString() {
        return "Admin {"
                + "\n  " + super.toString().replace("Miembro {", "").replaceFirst("\\n}$", "") // Reemplazar el inicio y
                                                                                               // el final para integrar
                                                                                               // mejor
                + "\n}";
    }
}
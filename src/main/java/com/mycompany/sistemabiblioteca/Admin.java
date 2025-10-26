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

    public Admin(byte tipoDocumento, long numeroDocumento, byte rol, String nombre, String apellido, String usuario,
            String contrasena) {
        super(tipoDocumento, numeroDocumento, (byte) 0, nombre, apellido, usuario, contrasena); // Rol siempre 0 para
                                                                                                // Admin
    }

    // --- Metodos de Administracion de Libros ---

    /**
     * Agrega un nuevo libro al sistema.
     * Genera el codigo del libro automaticamente si no se provee uno valido.
     */
    public String agregarLibro(String nombre, String autor, String codigoPropuesto, int unidadesTotales,
            int idBiblioteca,
            String sedeBiblioteca) {
        // Si no se provee codigo, generar uno
        String codigoFinal = codigoPropuesto;
        if (codigoFinal == null || codigoFinal.trim().isEmpty() || codigoFinal.equalsIgnoreCase("auto")) {
            codigoFinal = Libro.generarSiguienteCodigoLibro(idBiblioteca, sedeBiblioteca);
        } else {
            // Verificar si el codigo propuesto ya existe
            if (Libro.encontrarLibroPorCodigo(codigoFinal) != null) {
                return "Error: Ya existe un libro con el codigo " + codigoFinal + ". Intente con otro.";
            }
        }

        Libro nuevoLibro = new Libro(nombre, autor, codigoFinal, unidadesTotales, idBiblioteca, sedeBiblioteca);
        nuevoLibro.guardar();
        return "Exito: Libro '" + nombre + "' agregado con exito. Codigo: " + codigoFinal + ".";
    }

    /**
     * Edita la informacion de un libro existente.
     */
    public String editarLibro(String codigoLibro, String nuevoNombre, String nuevoAutor, int nuevasUnidadesTotales,
            int nuevaIdBiblioteca, String nuevaSedeBiblioteca) {
        Libro libro = Libro.encontrarLibroPorCodigo(codigoLibro);
        if (libro == null) {
            return "Error: Libro con codigo " + codigoLibro + " no encontrado.";
        }

        int unidadesPrestadasActuales = libro.getUnidadesPrestadas();
        if (nuevasUnidadesTotales < unidadesPrestadasActuales) {
            return "Error: Las nuevas unidades totales (" + nuevasUnidadesTotales
                    + ") no pueden ser menores que las unidades actualmente prestadas (" + unidadesPrestadasActuales
                    + ").";
        }

        libro.setNombre(nuevoNombre);
        libro.setAutor(nuevoAutor);
        libro.setUnidadesLibres(nuevasUnidadesTotales - unidadesPrestadasActuales); // Recalcular unidades libres
        libro.setIdBiblioteca(nuevaIdBiblioteca);
        libro.setSedeBiblioteca(nuevaSedeBiblioteca);

        libro.guardar();
        return "Exito: Libro '" + codigoLibro + "' actualizado con exito.";
    }

    /**
     * Elimina unidades de un libro.
     * Si el libro tiene unidades prestadas, solo se pueden eliminar las unidades
     * libres.
     * Si no hay unidades prestadas y las unidades libres llegan a 0, el registro
     * del libro se elimina.
     */
    public String eliminarLibro(String codigoLibro, int unidadesAeliminar) {
        Libro libro = Libro.encontrarLibroPorCodigo(codigoLibro);
        if (libro == null) {
            return "Error: Libro con codigo " + codigoLibro + " no encontrado.";
        }

        if (unidadesAeliminar <= 0) {
            return "Error: El numero de unidades a eliminar debe ser mayor que 0.";
        }

        // Si se intenta eliminar mas unidades de las libres, ajustamos a las libres
        if (unidadesAeliminar > libro.getUnidadesLibres()) {
            System.out.println("Advertencia: Se intenta eliminar " + unidadesAeliminar + " unidades, pero solo hay "
                    + libro.getUnidadesLibres() + " unidades libres. Se eliminaran solo las unidades libres.");
            unidadesAeliminar = libro.getUnidadesLibres();
        }

        if (unidadesAeliminar == 0 && libro.getUnidadesPrestadas() > 0) {
            return "No hay unidades libres para eliminar del libro '" + libro.getNombre()
                    + "'. Las unidades prestadas (" + libro.getUnidadesPrestadas()
                    + ") no se pueden eliminar hasta que sean devueltas.";
        }

        if (unidadesAeliminar > 0) {
            libro.setUnidadesLibres(libro.getUnidadesLibres() - unidadesAeliminar);

            // Si todas las unidades (libres y prestadas) se eliminaran y quedan 0, entonces
            // eliminar el registro completo del libro.
            if (libro.getUnidadesLibres() == 0 && libro.getUnidadesPrestadas() == 0) {
                libro.eliminar();
                return "Exito: Libro '" + libro.getNombre() + "' completamente eliminado (todas las unidades).";
            } else {
                libro.guardar();
                return "Exito: " + unidadesAeliminar + " unidades del libro '" + libro.getNombre()
                        + "' eliminadas. Quedan " + libro.getUnidadesLibres() + " libres y "
                        + libro.getUnidadesPrestadas() + " prestadas.";
            }
        } else {
            return "Error: No se realizaron cambios en las unidades del libro '" + libro.getNombre() + "'.";
        }
    }

    // Sobrecarga para eliminar todas las unidades libres si no se especifica
    // cantidad
    public String eliminarLibro(String codigoLibro) {
        Libro libro = Libro.encontrarLibroPorCodigo(codigoLibro);
        if (libro == null) {
            return "Error: Libro con codigo " + codigoLibro + " no encontrado.";
        }
        // Llamar al metodo con la cantidad de unidades libres
        return eliminarLibro(codigoLibro, libro.getUnidadesLibres());
    }

    // --- Metodos de Administracion de Usuarios ---

    /**
     * Crea un nuevo usuario en el sistema.
     */
    public String crearUsuario(byte tipoDocumento, long numeroDocumento, byte rol, String nombre, String apellido,
            String usuario,
            String contrasena, double deuda, String sedeUniversidad, String carrera) {
        if (Miembro.encontrarMiembroPorUsuario(usuario) != null) {
            return "Error: Ya existe un usuario con el nombre de usuario '" + usuario + "'.";
        }
        if (Miembro.encontrarMiembroPorNumeroDocumento(numeroDocumento) != null) {
            return "Error: Ya existe un usuario con el numero de documento '" + numeroDocumento + "'.";
        }

        Usuario nuevoUsuario = new Usuario(tipoDocumento, numeroDocumento, rol, nombre, apellido, usuario, contrasena,
                deuda,
                sedeUniversidad, carrera);
        nuevoUsuario.guardar();
        return "Exito: Usuario '" + usuario + "' agregado con exito (Rol: " + rol + ").";
    }

    /**
     * Edita la informacion de un usuario existente.
     */
    public String editarUsuario(String usuarioExistente, byte nuevoTipoDocumento, long nuevoNumeroDocumento,
            byte nuevoRol,
            String nuevoNombre, String nuevoApellido, String nuevoUsuario, String nuevaContrasena,
            double nuevaDeuda, String nuevaSedeUniversidad, String nuevaCarrera) {
        Miembro miembroBase = Miembro.encontrarMiembroPorUsuario(usuarioExistente);
        if (miembroBase == null) {
            return "Error: Usuario '" + usuarioExistente + "' no encontrado.";
        }

        // Verificar si el nuevo usuario ya existe y no es el mismo
        if (!usuarioExistente.equalsIgnoreCase(nuevoUsuario)
                && Miembro.encontrarMiembroPorUsuario(nuevoUsuario) != null) {
            return "Error: El nuevo nombre de usuario '" + nuevoUsuario + "' ya esta en uso.";
        }

        // Verificar si el nuevo numero de documento ya existe y no es el mismo
        if (miembroBase.getNumeroDocumento() != nuevoNumeroDocumento
                && Miembro.encontrarMiembroPorNumeroDocumento(nuevoNumeroDocumento) != null) {
            return "Error: El nuevo numero de documento '" + nuevoNumeroDocumento + "' ya esta en uso.";
        }

        miembroBase.setTipoDocumento(nuevoTipoDocumento);
        miembroBase.setNumeroDocumento(nuevoNumeroDocumento);
        miembroBase.setRol(nuevoRol); // El rol si se puede cambiar por el admin
        miembroBase.setNombre(nuevoNombre);
        miembroBase.setApellido(nuevoApellido);
        miembroBase.setUsuario(nuevoUsuario);
        if (nuevaContrasena != null && !nuevaContrasena.isEmpty()) {
            miembroBase.cambiarContrasena(nuevaContrasena); // Usa el metodo de cambiarContrasena que ya hashea
        }

        // Campos especificos de Usuario (solo aplicar si el miembro es o se convierte
        // en un Usuario)
        if (miembroBase.getRol() != 0) { // Si el rol final no es Admin, se trata como Usuario
            Usuario usuarioEdit;
            if (miembroBase instanceof Usuario) {
                usuarioEdit = (Usuario) miembroBase; // Si ya es Usuario, lo casteamos
            } else {
                // Si era Admin y su rol fue cambiado, creamos una nueva instancia de Usuario
                // AVISO: Esto en ciertos escenarios podria borrar el antiguo admin si los
                // usuarios son el mismo.
                // Lo ideal es tener un mecanismo de actualizacion mas robusto que cambie de
                // tipo
                // sin eliminar el registro previo del Miembro.
                System.out.println(
                        "Advertencia: Un Admin fue cambiado a rol de Usuario. Se creara un nuevo objeto de Usuario y se eliminara el registro anterior.");
                miembroBase.setUsuario(null); // Marcar el objeto Admin base para eliminacion
                miembroBase.guardar(); // Eliminar el registro de Admin

                usuarioEdit = new Usuario(miembroBase.getTipoDocumento(), miembroBase.getNumeroDocumento(), nuevoRol,
                        miembroBase.getNombre(), miembroBase.getApellido(), miembroBase.getUsuario(),
                        nuevaContrasena != null && !nuevaContrasena.isEmpty()
                                ? Miembro.hashPasswordEstatico(nuevaContrasena)
                                : miembroBase.getContrasena(),
                        0.0, "", "");
            }
            usuarioEdit.setDeuda(nuevaDeuda);
            usuarioEdit.setSedeUniversidad(nuevaSedeUniversidad);
            usuarioEdit.setCarrera(nuevaCarrera);
            usuarioEdit.guardar(); // Guarda el objeto Usuario completo
        } else {
            miembroBase.guardar(); // Si es Admin o se mantiene como Admin, solo guardar los campos base
        }

        return "Exito: Miembro '" + usuarioExistente + "' actualizado con exito.";
    }

    /**
     * Elimina un usuario del sistema, sujeto a ciertas condiciones.
     */
    public String eliminarUsuario(String usuarioAEliminar) {
        Miembro miembro = Miembro.encontrarMiembroPorUsuario(usuarioAEliminar);
        if (miembro == null) {
            return "Error: Usuario '" + usuarioAEliminar + "' no encontrado.";
        }

        if (miembro.getRol() == 0) {
            return "Error: No se puede eliminar a un usuario con rol de Administrador directamente.";
        }

        // Verificar si el usuario tiene libros prestados
        List<Prestamo> prestamosActivos = Prestamo.encontrarPrestamosPorMiembro(miembro.getNumeroDocumento()).stream()
                .filter(p -> "PRESTADO".equals(p.getEstado()))
                .collect(Collectors.toList());
        if (!prestamosActivos.isEmpty()) {
            return "Error: No se puede eliminar al usuario '" + usuarioAEliminar + "' porque tiene "
                    + prestamosActivos.size() + " libros prestados.";
        }

        // Verificar si el usuario tiene deuda
        if (miembro instanceof Usuario) {
            Usuario user = (Usuario) miembro;
            if (user.getDeuda() > 0) {
                return "Error: No se puede eliminar al usuario '" + usuarioAEliminar
                        + "' porque tiene una deuda pendiente de " + String.format("%.2f", user.getDeuda())
                        + " pesos.";
            }
        }
        miembro.setUsuario(null); // Marcar para eliminacion (internamente en guardarInterno, si usuario es null,
                                  // se elimina)
        miembro.guardar(); // guardarInterno() eliminara la linea
        return "Exito: Usuario '" + usuarioAEliminar + "' eliminado con exito.";
    }

    // --- Metodos de Administracion de Bibliotecas (NUEVO) ---
    /**
     * Agrega una nueva biblioteca al sistema.
     * 
     * @param id               El ID unico de la biblioteca.
     * @param sede             La sede de la biblioteca.
     * @param nombreBiblioteca El nombre de la biblioteca.
     * @return Mensaje de exito o error.
     */
    public String agregarBiblioteca(int id, String sede, String nombreBiblioteca) {
        if (Biblioteca.encontrarBibliotecaPorId(id) != null) {
            return "Error: Ya existe una biblioteca con el ID " + id + ".";
        }
        if (Biblioteca.encontrarBibliotecaPorSede(sede) != null) {
            return "Error: Ya existe una biblioteca en la sede " + sede + ".";
        }
        Biblioteca nuevaBiblioteca = new Biblioteca(id, sede, nombreBiblioteca);
        List<Biblioteca> bibliotecas = Biblioteca.cargarTodasLasBibliotecas();
        bibliotecas.add(nuevaBiblioteca);
        Biblioteca.guardarTodasLasBibliotecas(bibliotecas);
        return "Exito: Biblioteca '" + nombreBiblioteca + "' agregada con exito.";
    }

    /**
     * Edita los detalles de una biblioteca existente.
     * 
     * @param id          El ID de la biblioteca a editar.
     * @param nuevaSede   La nueva sede de la biblioteca.
     * @param nuevoNombre El nuevo nombre de la biblioteca.
     * @return Mensaje de exito o error.
     */
    public String editarBiblioteca(int id, String nuevaSede, String nuevoNombre) {
        List<Biblioteca> bibliotecas = Biblioteca.cargarTodasLasBibliotecas();
        boolean encontrada = false;
        for (int i = 0; i < bibliotecas.size(); i++) {
            if (bibliotecas.get(i).getId() == id) {
                bibliotecas.get(i).setSede(nuevaSede);
                bibliotecas.get(i).setNombreBiblioteca(nuevoNombre);
                encontrada = true;
                break;
            }
        }
        if (encontrada) {
            Biblioteca.guardarTodasLasBibliotecas(bibliotecas);
            return "Exito: Biblioteca con ID " + id + " actualizada con exito.";
        }
        return "Error: Biblioteca con ID " + id + " no encontrada.";
    }

    /**
     * Elimina una biblioteca del sistema.
     * 
     * @param id El ID de la biblioteca a eliminar.
     * @return Mensaje de exito o error.
     */
    public String eliminarBiblioteca(int id) {
        List<Biblioteca> bibliotecas = Biblioteca.cargarTodasLasBibliotecas();
        boolean eliminada = bibliotecas.removeIf(b -> b.getId() == id);
        if (eliminada) {
            Biblioteca.guardarTodasLasBibliotecas(bibliotecas);
            return "Exito: Biblioteca con ID " + id + " eliminada con exito.";
        }
        return "Error: Biblioteca con ID " + id + " no encontrada.";
    }

    // --- Metodos de Visualizacion ---

    /**
     * Muestra todos los miembros registrados en el sistema (incluye Admins y
     * Usuarios).
     */
    public void verTodosUsuarios() {
        System.out.println("\n--- LISTADO DE TODOS LOS MIEMBROS DEL SISTEMA (incluye Admins y Usuarios) ---");
        List<Miembro> miembros = Miembro.cargarTodosLosMiembros();
        if (miembros.isEmpty()) {
            System.out.println("No hay miembros registrados en el sistema.");
        } else {
            miembros.forEach(System.out::println);
        }
        System.out.println("-------------------------------------------------------------------\n");
    }

    /**
     * Muestra todos los libros registrados en el sistema.
     */
    public void verTodosLibros() {
        System.out.println("\n--- LISTADO DE TODOS LOS LIBROS ---");
        List<Libro> libros = Libro.cargarTodosLosLibros();
        if (libros.isEmpty()) {
            System.out.println("No hay libros registrados en el sistema.");
        } else {
            libros.forEach(System.out::println);
        }
        System.out.println("-----------------------------------\n");
    }

    /**
     * Muestra la informacion de un libro especifico y sus prestamos asociados.
     */
    public void verLibro(String codigoLibro) {
        Libro libro = Libro.encontrarLibroPorCodigo(codigoLibro);
        if (libro == null) {
            System.out.println("Error: Libro con codigo " + codigoLibro + " no encontrado.");
            return;
        }
        System.out.println("\n--- INFORMACION DEL LIBRO ---");
        System.out.println(libro);

        List<Prestamo> prestamosLibro = Prestamo.cargarTodosLosPrestamos().stream()
                .filter(p -> p.getCodigoLibro().equals(codigoLibro))
                .collect(Collectors.toList());
        if (!prestamosLibro.isEmpty()) {
            System.out.println("\n--- PRESTAMOS ASOCIADOS AL LIBRO " + libro.getNombre() + " ---");
            for (Prestamo p : prestamosLibro) {
                Miembro m = Miembro.encontrarMiembroPorNumeroDocumento(p.getNumeroDocumentoMiembro());
                System.out
                        .println("  - Miembro: " + (m != null ? m.getNombre() + " " + m.getApellido() : "Desconocido") +
                                ", Fecha Prestamo: " + Fecha.formatDate(p.getFechaPrestamo()) +
                                ", Fecha Devolucion Estimada: " + Fecha.formatDate(p.getFechaDevolucionEstimada()) +
                                ", Estado: " + p.getEstado());
            }
        } else {
            System.out.println("No hay prestamos registrados para este libro.");
        }
        System.out.println("-----------------------------------\n");
    }

    /**
     * Muestra todos los libros que estan actualmente en estado de prestamo.
     */
    public void verLibrosPrestados() {
        System.out.println("\n--- LIBROS ACTUALMENTE PRESTADOS ---");
        List<Prestamo> prestamos = Prestamo.cargarTodosLosPrestamos().stream()
                .filter(p -> "PRESTADO".equals(p.getEstado()))
                .collect(Collectors.toList());

        if (prestamos.isEmpty()) {
            System.out.println("No hay libros prestados actualmente.");
        } else {
            for (Prestamo prestamo : prestamos) {
                Miembro miembro = Miembro.encontrarMiembroPorNumeroDocumento(prestamo.getNumeroDocumentoMiembro());
                Libro libro = Libro.encontrarLibroPorCodigo(prestamo.getCodigoLibro());
                String infoMiembro = (miembro != null)
                        ? miembro.getNombre() + " " + miembro.getApellido() + " (Usuario: " + miembro.getUsuario() + ")"
                        : "Miembro Desconocido";
                String infoLibro = (libro != null) ? libro.getNombre() + " (Codigo: " + libro.getCodigo() + ")"
                        : "Libro Desconocido";
                System.out.println("Prestamo: " + infoLibro + " a " + infoMiembro + " | Fecha Prestamo: "
                        + Fecha.formatDate(prestamo.getFechaPrestamo()) + " | Fecha Devolucion Estimada: "
                        + Fecha.formatDate(prestamo.getFechaDevolucionEstimada()) + " | Biblioteca ID: "
                        + prestamo.getIdBiblioteca());
            }
        }
        System.out.println("------------------------------------\n");
    }

    /**
     * Muestra los libros prestados por un usuario especificado por su numero de
     * documento.
     */
    public void verLibrosPrestadosPorUsuario(long numeroDocumentoMiembro) {
        Miembro miembro = Miembro.encontrarMiembroPorNumeroDocumento(numeroDocumentoMiembro);
        if (miembro == null) {
            System.out.println("Error: Miembro con numero de documento " + numeroDocumentoMiembro + " no encontrado.");
            return;
        }

        System.out.println("\n--- LIBROS PRESTADOS POR " + miembro.getNombre().toUpperCase() + " "
                + miembro.getApellido().toUpperCase() + " ---");
        List<Prestamo> prestamosDelMiembro = Prestamo.encontrarPrestamosPorMiembro(numeroDocumentoMiembro);

        if (prestamosDelMiembro.isEmpty()) {
            System.out.println(miembro.getNombre() + " no tiene ningun libro prestado o devuelto.");
        } else {
            for (Prestamo prestamo : prestamosDelMiembro) {
                Libro libro = Libro.encontrarLibroPorCodigo(prestamo.getCodigoLibro());
                String infoLibro = (libro != null) ? libro.getNombre() + " (Codigo: " + libro.getCodigo() + ")"
                        : "Libro Desconocido";
                System.out.println("  - " + infoLibro + " | Fecha Prestamo: "
                        + Fecha.formatDate(prestamo.getFechaPrestamo()) + " | Fecha Devolucion Estimada: "
                        + Fecha.formatDate(prestamo.getFechaDevolucionEstimada()) + " | Estado: "
                        + prestamo.getEstado());
            }
        }
        System.out.println("-------------------------------------------------------------------\n");
    }

    @Override
    public String toString() {
        return "Admin {"
                + "\n  " + super.toString().replace("Miembro {", "").replaceFirst("\\n}$", "")
                + "\n}";
    }
}
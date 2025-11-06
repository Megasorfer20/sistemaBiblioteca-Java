/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.sistemabiblioteca; // Carpeta donde está organizado nuestro código.

import java.util.List; // Necesario para manejar listas de objetos.
import java.util.stream.Collectors; // Herramienta para trabajar con listas de forma avanzada, como filtrarlas.

public class Admin extends Miembro { // La clase 'Admin' es un tipo especial de 'Miembro'.

    public Admin() { // Constructor básico para crear un administrador.
        super(); // Llama al constructor de la clase 'Miembro' (el padre).
        this.rol = 0; // Establece el rol como 0, que significa Administrador.
    }

    public Admin(byte tipoDocumento, long numeroDocumento, byte rol, String nombre, String apellido, String usuario,
            String contrasena) { // Constructor completo para crear un administrador con todos sus datos.
        super(tipoDocumento, numeroDocumento, (byte) 0, nombre, apellido, usuario, contrasena); // Llama al constructor
                                                                                                // del 'Miembro',
                                                                                                // forzando el rol a 0
                                                                                                // (Administrador).
    }

    // --- Metodos de Administracion de Libros ---

    // Este método permite al administrador agregar un libro nuevo al sistema.
    public String agregarLibro(String nombre, String autor, String codigoPropuesto, int unidadesTotales,
            int idBiblioteca,
            String sedeBiblioteca) {
        String codigoFinal = codigoPropuesto; // Toma el código que se propuso.

        // Si el código está vacío o se pidió "auto", el sistema genera uno.
        if (codigoFinal == null || codigoFinal.trim().isEmpty() || codigoFinal.equalsIgnoreCase("auto")) {
            codigoFinal = Libro.generarSiguienteCodigoLibro(idBiblioteca, sedeBiblioteca); // Genera un código único
                                                                                           // para el libro.
        } else {
            // Si se propuso un código, verifica que no exista ya.
            if (Libro.encontrarLibroPorCodigo(codigoFinal) != null) {
                return "Error: Ya existe un libro con el codigo " + codigoFinal + ". Intente con otro."; // Si existe,
                                                                                                         // da un error.
            }
        }

        Libro nuevoLibro = new Libro(nombre, autor, codigoFinal, unidadesTotales, idBiblioteca, sedeBiblioteca); // Crea
                                                                                                                 // el
                                                                                                                 // nuevo
                                                                                                                 // libro.
        nuevoLibro.guardar(); // Guarda el libro en el sistema (lo escribe en un archivo).
        return "Exito: Libro '" + nombre + "' agregado con exito. Codigo: " + codigoFinal + "."; // Mensaje de éxito.
    }

    // Este método permite al administrador cambiar la información de un libro
    // existente.
    public String editarLibro(String codigoLibro, String nuevoNombre, String nuevoAutor, int nuevasUnidadesTotales,
            int nuevaIdBiblioteca, String nuevaSedeBiblioteca) {
        Libro libro = Libro.encontrarLibroPorCodigo(codigoLibro); // Busca el libro por su código.
        if (libro == null) { // Si no encuentra el libro.
            return "Error: Libro con codigo " + codigoLibro + " no encontrado."; // Da un error.
        }

        int unidadesPrestadasActuales = libro.getUnidadesPrestadas(); // Obtiene cuántas unidades están prestadas.
        // Impide que las nuevas unidades totales sean menos que las ya prestadas.
        if (nuevasUnidadesTotales < unidadesPrestadasActuales) {
            return "Error: Las nuevas unidades totales (" + nuevasUnidadesTotales
                    + ") no pueden ser menores que las unidades actualmente prestadas (" + unidadesPrestadasActuales
                    + ").";
        }

        libro.setNombre(nuevoNombre); // Actualiza el nombre del libro.
        libro.setAutor(nuevoAutor); // Actualiza el autor del libro.
        libro.setUnidadesLibres(nuevasUnidadesTotales - unidadesPrestadasActuales); // Recalcula las unidades libres.
        libro.setIdBiblioteca(nuevaIdBiblioteca); // Actualiza la ID de la biblioteca.
        libro.setSedeBiblioteca(nuevaSedeBiblioteca); // Actualiza la sede de la biblioteca.

        libro.guardar(); // Guarda los cambios del libro.
        return "Exito: Libro '" + codigoLibro + "' actualizado con exito."; // Mensaje de éxito.
    }

    // Este método elimina unidades de un libro, o el libro completo si se quedan
    // sin unidades.
    public String eliminarLibro(String codigoLibro, int unidadesAeliminar) {
        Libro libro = Libro.encontrarLibroPorCodigo(codigoLibro); // Busca el libro.
        if (libro == null) { // Si no encuentra el libro.
            return "Error: Libro con codigo " + codigoLibro + " no encontrado."; // Da un error.
        }

        if (unidadesAeliminar <= 0) { // Verifica que la cantidad a eliminar sea mayor a cero.
            return "Error: El numero de unidades a eliminar debe ser mayor que 0."; // Da un error.
        }

        // Si se intenta eliminar más de las unidades libres, ajusta la cantidad a las
        // unidades libres.
        if (unidadesAeliminar > libro.getUnidadesLibres()) {
            System.out.println("Advertencia: Se intenta eliminar " + unidadesAeliminar + " unidades, pero solo hay "
                    + libro.getUnidadesLibres() + " unidades libres. Se eliminaran solo las unidades libres.");
            unidadesAeliminar = libro.getUnidadesLibres(); // Ajusta la cantidad a eliminar.
        }

        // Si no hay unidades libres para eliminar y todavía hay prestadas, no se puede
        // eliminar.
        if (unidadesAeliminar == 0 && libro.getUnidadesPrestadas() > 0) {
            return "No hay unidades libres para eliminar del libro '" + libro.getNombre()
                    + "'. Las unidades prestadas (" + libro.getUnidadesPrestadas()
                    + ") no se pueden eliminar hasta que sean devueltas.";
        }

        // Si hay unidades para eliminar.
        if (unidadesAeliminar > 0) {
            libro.setUnidadesLibres(libro.getUnidadesLibres() - unidadesAeliminar); // Resta las unidades eliminadas de
                                                                                    // las libres.

            // Si el libro se queda sin unidades libres y sin prestadas, se elimina por
            // completo.
            if (libro.getUnidadesLibres() == 0 && libro.getUnidadesPrestadas() == 0) {
                libro.eliminar(); // Elimina el registro completo del libro.
                return "Exito: Libro '" + libro.getNombre() + "' completamente eliminado (todas las unidades)."; // Mensaje
                                                                                                                 // de
                                                                                                                 // éxito.
            } else {
                libro.guardar(); // Si aún quedan unidades, guarda los cambios.
                return "Exito: " + unidadesAeliminar + " unidades del libro '" + libro.getNombre()
                        + "' eliminadas. Quedan " + libro.getUnidadesLibres() + " libres y "
                        + libro.getUnidadesPrestadas() + " prestadas."; // Mensaje de éxito con detalles.
            }
        } else {
            return "Error: No se realizaron cambios en las unidades del libro '" + libro.getNombre() + "'."; // Mensaje
                                                                                                             // si no
                                                                                                             // hubo
                                                                                                             // cambios.
        }
    }

    // Versión simplificada para eliminar todas las unidades libres de un libro.
    public String eliminarLibro(String codigoLibro) {
        Libro libro = Libro.encontrarLibroPorCodigo(codigoLibro); // Busca el libro.
        if (libro == null) { // Si no lo encuentra.
            return "Error: Libro con codigo " + codigoLibro + " no encontrado."; // Da un error.
        }
        return eliminarLibro(codigoLibro, libro.getUnidadesLibres()); // Llama al método completo para eliminar todas
                                                                      // las unidades libres.
    }

    // --- Metodos de Administracion de Usuarios ---

    // Este método permite al administrador crear un nuevo usuario.
    public String crearUsuario(byte tipoDocumento, long numeroDocumento, byte rol, String nombre, String apellido,
            String usuario,
            String contrasena, double deuda, String sedeUniversidad, String carrera) {
        if (Miembro.encontrarMiembroPorUsuario(usuario) != null) { // Verifica si el nombre de usuario ya existe.
            return "Error: Ya existe un usuario con el nombre de usuario '" + usuario + "'."; // Da un error.
        }
        if (Miembro.encontrarMiembroPorNumeroDocumento(numeroDocumento) != null) { // Verifica si el número de documento
                                                                                   // ya existe.
            return "Error: Ya existe un usuario con el numero de documento '" + numeroDocumento + "'."; // Da un error.
        }

        Usuario nuevoUsuario = new Usuario(tipoDocumento, numeroDocumento, rol, nombre, apellido, usuario, contrasena,
                deuda,
                sedeUniversidad, carrera); // Crea el nuevo usuario.
        nuevoUsuario.guardar(); // Guarda el usuario en el sistema.
        return "Exito: Usuario '" + usuario + "' agregado con exito (Rol: " + rol + ")."; // Mensaje de éxito.
    }

    // Este método permite al administrador editar la información de un usuario
    // existente.
    public String editarUsuario(String usuarioExistente, byte nuevoTipoDocumento, long nuevoNumeroDocumento,
            byte nuevoRol,
            String nuevoNombre, String nuevoApellido, String nuevoUsuario, String nuevaContrasena,
            double nuevaDeuda, String nuevaSedeUniversidad, String nuevaCarrera) {
        Miembro miembroBase = Miembro.encontrarMiembroPorUsuario(usuarioExistente); // Busca el miembro a editar.
        if (miembroBase == null) { // Si no lo encuentra.
            return "Error: Usuario '" + usuarioExistente + "' no encontrado."; // Da un error.
        }

        // Verifica si el nuevo nombre de usuario ya existe y es diferente al actual.
        if (!usuarioExistente.equalsIgnoreCase(nuevoUsuario)
                && Miembro.encontrarMiembroPorUsuario(nuevoUsuario) != null) {
            return "Error: El nuevo nombre de usuario '" + nuevoUsuario + "' ya esta en uso."; // Da un error.
        }

        // Verifica si el nuevo número de documento ya existe y es diferente al actual.
        if (miembroBase.getNumeroDocumento() != nuevoNumeroDocumento
                && Miembro.encontrarMiembroPorNumeroDocumento(nuevoNumeroDocumento) != null) {
            return "Error: El nuevo numero de documento '" + nuevoNumeroDocumento + "' ya esta en uso."; // Da un error.
        }

        // Actualiza los datos básicos del miembro.
        miembroBase.setTipoDocumento(nuevoTipoDocumento);
        miembroBase.setNumeroDocumento(nuevoNumeroDocumento);
        miembroBase.setRol(nuevoRol); // Permite cambiar el rol.
        miembroBase.setNombre(nuevoNombre);
        miembroBase.setApellido(nuevoApellido);
        miembroBase.setUsuario(nuevoUsuario);
        if (nuevaContrasena != null && !nuevaContrasena.isEmpty()) { // Si se dio una nueva contraseña.
            miembroBase.cambiarContrasena(nuevaContrasena); // La cambia, hasheándola.
        }

        // Maneja los campos específicos del 'Usuario' si el rol final no es
        // Administrador.
        if (miembroBase.getRol() != 0) { // Si el rol es de Usuario (no Admin).
            Usuario usuarioEdit;
            if (miembroBase instanceof Usuario) { // Si ya era un Usuario.
                usuarioEdit = (Usuario) miembroBase; // Simplemente lo trata como tal.
            } else {
                // Si era un Admin y su rol se cambió a Usuario, se crea un nuevo objeto Usuario
                // y se elimina el Admin original.
                System.out.println(
                        "Advertencia: Un Admin fue cambiado a rol de Usuario. Se creara un nuevo objeto de Usuario y se eliminara el registro anterior.");
                miembroBase.setUsuario(null); // Marca el Admin para eliminación.
                miembroBase.guardar(); // Elimina el registro del Admin.

                usuarioEdit = new Usuario(miembroBase.getTipoDocumento(), miembroBase.getNumeroDocumento(), nuevoRol,
                        miembroBase.getNombre(), miembroBase.getApellido(), miembroBase.getUsuario(),
                        nuevaContrasena != null && !nuevaContrasena.isEmpty()
                                ? Miembro.hashPasswordEstatico(nuevaContrasena) // Si hay nueva contraseña, la hashea.
                                : miembroBase.getContrasena(), // Si no, usa la que tenía el Admin.
                        0.0, "", ""); // Inicializa los campos de Usuario por defecto.
            }
            usuarioEdit.setDeuda(nuevaDeuda); // Actualiza la deuda.
            usuarioEdit.setSedeUniversidad(nuevaSedeUniversidad); // Actualiza la sede de la universidad.
            usuarioEdit.setCarrera(nuevaCarrera); // Actualiza la carrera.
            usuarioEdit.guardar(); // Guarda el objeto Usuario completo.
        } else {
            miembroBase.guardar(); // Si sigue siendo Admin, solo guarda los campos básicos del Miembro.
        }

        return "Exito: Miembro '" + usuarioExistente + "' actualizado con exito."; // Mensaje de éxito.
    }

    // Este método permite al administrador eliminar un usuario, pero con
    // condiciones.
    public String eliminarUsuario(String usuarioAEliminar) {
        Miembro miembro = Miembro.encontrarMiembroPorUsuario(usuarioAEliminar); // Busca el miembro.
        if (miembro == null) { // Si no lo encuentra.
            return "Error: Usuario '" + usuarioAEliminar + "' no encontrado."; // Da un error.
        }

        if (miembro.getRol() == 0) { // No permite eliminar un administrador.
            return "Error: No se puede eliminar a un usuario con rol de Administrador directamente."; // Da un error.
        }

        // Verifica si el usuario tiene libros prestados actualmente.
        List<Prestamo> prestamosActivos = Prestamo.encontrarPrestamosPorMiembro(miembro.getNumeroDocumento()).stream()
                .filter(p -> "PRESTADO".equals(p.getEstado())) // Filtra solo los préstamos activos.
                .collect(Collectors.toList());
        if (!prestamosActivos.isEmpty()) { // Si tiene préstamos activos.
            return "Error: No se puede eliminar al usuario '" + usuarioAEliminar + "' porque tiene "
                    + prestamosActivos.size() + " libros prestados."; // Da un error.
        }

        // Verifica si el usuario tiene alguna deuda.
        if (miembro instanceof Usuario) { // Si es un objeto de tipo Usuario.
            Usuario user = (Usuario) miembro;
            if (user.getDeuda() > 0) { // Si la deuda es mayor a cero.
                return "Error: No se puede eliminar al usuario '" + usuarioAEliminar
                        + "' porque tiene una deuda pendiente de " + String.format("%.2f", user.getDeuda())
                        + " pesos."; // Da un error.
            }
        }
        miembro.setUsuario(null); // Marca el usuario para eliminación (poniendo su nombre de usuario a nulo).
        miembro.guardar(); // Llama a guardar, que detecta el usuario nulo y lo elimina del archivo.
        return "Exito: Usuario '" + usuarioAEliminar + "' eliminado con exito."; // Mensaje de éxito.
    }

    // --- Metodos de Administracion de Bibliotecas (NUEVO) ---
    // Este método permite al administrador añadir una nueva biblioteca al sistema.
    public String agregarBiblioteca(int id, String sede, String nombreBiblioteca) {
        if (Biblioteca.encontrarBibliotecaPorId(id) != null) { // Verifica si ya existe una biblioteca con esa ID.
            return "Error: Ya existe una biblioteca con el ID " + id + "."; // Da un error.
        }
        if (Biblioteca.encontrarBibliotecaPorSede(sede) != null) { // Verifica si ya existe una biblioteca en esa sede.
            return "Error: Ya existe una biblioteca en la sede " + sede + "."; // Da un error.
        }
        Biblioteca nuevaBiblioteca = new Biblioteca(id, sede, nombreBiblioteca); // Crea la nueva biblioteca.
        List<Biblioteca> bibliotecas = Biblioteca.cargarTodasLasBibliotecas(); // Carga todas las bibliotecas
                                                                               // existentes.
        bibliotecas.add(nuevaBiblioteca); // Añade la nueva a la lista.
        Biblioteca.guardarTodasLasBibliotecas(bibliotecas); // Guarda toda la lista actualizada en el archivo.
        return "Exito: Biblioteca '" + nombreBiblioteca + "' agregada con exito."; // Mensaje de éxito.
    }

    // Este método permite al administrador editar los detalles de una biblioteca.
    public String editarBiblioteca(int id, String nuevaSede, String nuevoNombre) {
        List<Biblioteca> bibliotecas = Biblioteca.cargarTodasLasBibliotecas(); // Carga todas las bibliotecas.
        boolean encontrada = false; // Bandera para saber si se encontró la biblioteca.
        for (int i = 0; i < bibliotecas.size(); i++) { // Recorre la lista de bibliotecas.
            if (bibliotecas.get(i).getId() == id) { // Si encuentra la biblioteca por su ID.
                bibliotecas.get(i).setSede(nuevaSede); // Actualiza la sede.
                bibliotecas.get(i).setNombreBiblioteca(nuevoNombre); // Actualiza el nombre.
                encontrada = true; // Marca como encontrada.
                break; // Sale del bucle.
            }
        }
        if (encontrada) { // Si la biblioteca fue encontrada y editada.
            Biblioteca.guardarTodasLasBibliotecas(bibliotecas); // Guarda la lista completa y actualizada.
            return "Exito: Biblioteca con ID " + id + " actualizada con exito."; // Mensaje de éxito.
        }
        return "Error: Biblioteca con ID " + id + " no encontrada."; // Da un error si no se encontró.
    }

    // Este método permite al administrador eliminar una biblioteca.
    public String eliminarBiblioteca(int id) {
        List<Biblioteca> bibliotecas = Biblioteca.cargarTodasLasBibliotecas(); // Carga todas las bibliotecas.
        boolean eliminada = bibliotecas.removeIf(b -> b.getId() == id); // Elimina la biblioteca por su ID de la lista.
        if (eliminada) { // Si se eliminó de la lista.
            Biblioteca.guardarTodasLasBibliotecas(bibliotecas); // Guarda la lista actualizada.
            return "Exito: Biblioteca con ID " + id + " eliminada con exito."; // Mensaje de éxito.
        }
        return "Error: Biblioteca con ID " + id + " no encontrada."; // Da un error si no se encontró.
    }

    // --- Metodos de Visualizacion ---

    // Este método muestra por consola una lista de todos los miembros del sistema
    // (Admins y Usuarios).
    public void verTodosUsuarios() {
        System.out.println("\n--- LISTADO DE TODOS LOS MIEMBROS DEL SISTEMA (incluye Admins y Usuarios) ---");
        List<Miembro> miembros = Miembro.cargarTodosLosMiembros(); // Carga todos los miembros.
        if (miembros.isEmpty()) { // Si no hay miembros.
            System.out.println("No hay miembros registrados en el sistema.");
        } else {
            miembros.forEach(System.out::println); // Imprime la información de cada miembro.
        }
        System.out.println("-------------------------------------------------------------------\n");
    }

    // Este método muestra por consola una lista de todos los libros registrados en
    // el sistema.
    public void verTodosLibros() {
        System.out.println("\n--- LISTADO DE TODOS LOS LIBROS ---");
        List<Libro> libros = Libro.cargarTodosLosLibros(); // Carga todos los libros.
        if (libros.isEmpty()) { // Si no hay libros.
            System.out.println("No hay libros registrados en el sistema.");
        } else {
            libros.forEach(System.out::println); // Imprime la información de cada libro.
        }
        System.out.println("-----------------------------------\n");
    }

    // Este método muestra la información de un libro específico y sus préstamos
    // asociados.
    public void verLibro(String codigoLibro) {
        Libro libro = Libro.encontrarLibroPorCodigo(codigoLibro); // Busca el libro.
        if (libro == null) { // Si no lo encuentra.
            System.out.println("Error: Libro con codigo " + codigoLibro + " no encontrado."); // Da un error.
            return;
        }
        System.out.println("\n--- INFORMACION DEL LIBRO ---");
        System.out.println(libro); // Imprime la información básica del libro.

        // Filtra todos los préstamos para encontrar los que corresponden a este libro.
        List<Prestamo> prestamosLibro = Prestamo.cargarTodosLosPrestamos().stream()
                .filter(p -> p.getCodigoLibro().equals(codigoLibro))
                .collect(Collectors.toList());
        if (!prestamosLibro.isEmpty()) { // Si hay préstamos asociados al libro.
            System.out.println("\n--- PRESTAMOS ASOCIADOS AL LIBRO " + libro.getNombre() + " ---");
            for (Prestamo p : prestamosLibro) { // Recorre cada préstamo.
                Miembro m = Miembro.encontrarMiembroPorNumeroDocumento(p.getNumeroDocumentoMiembro()); // Busca el
                                                                                                       // miembro que
                                                                                                       // hizo el
                                                                                                       // préstamo.
                System.out
                        .println("  - Miembro: " + (m != null ? m.getNombre() + " " + m.getApellido() : "Desconocido") + // Imprime
                                                                                                                         // el
                                                                                                                         // miembro
                                                                                                                         // (o
                                                                                                                         // "Desconocido").
                                ", Fecha Prestamo: " + Fecha.formatDate(p.getFechaPrestamo()) + // Imprime la fecha de
                                                                                                // préstamo.
                                ", Fecha Devolucion Estimada: " + Fecha.formatDate(p.getFechaDevolucionEstimada()) + // Imprime
                                                                                                                     // la
                                                                                                                     // fecha
                                                                                                                     // de
                                                                                                                     // devolución
                                                                                                                     // estimada.
                                ", Estado: " + p.getEstado()); // Imprime el estado del préstamo.
            }
        } else {
            System.out.println("No hay prestamos registrados para este libro."); // Mensaje si no hay préstamos.
        }
        System.out.println("-----------------------------------\n");
    }

    // Este método muestra por consola todos los libros que están actualmente
    // prestados.
    public void verLibrosPrestados() {
        System.out.println("\n--- LIBROS ACTUALMENTE PRESTADOS ---");
        // Filtra todos los préstamos para encontrar solo los que están "PRESTADO".
        List<Prestamo> prestamos = Prestamo.cargarTodosLosPrestamos().stream()
                .filter(p -> "PRESTADO".equals(p.getEstado()))
                .collect(Collectors.toList());

        if (prestamos.isEmpty()) { // Si no hay libros prestados.
            System.out.println("No hay libros prestados actualmente.");
        } else {
            for (Prestamo prestamo : prestamos) { // Para cada préstamo.
                Miembro miembro = Miembro.encontrarMiembroPorNumeroDocumento(prestamo.getNumeroDocumentoMiembro()); // Busca
                                                                                                                    // el
                                                                                                                    // miembro.
                Libro libro = Libro.encontrarLibroPorCodigo(prestamo.getCodigoLibro()); // Busca el libro.
                String infoMiembro = (miembro != null) // Formatea la información del miembro.
                        ? miembro.getNombre() + " " + miembro.getApellido() + " (Usuario: " + miembro.getUsuario() + ")"
                        : "Miembro Desconocido";
                String infoLibro = (libro != null) ? libro.getNombre() + " (Codigo: " + libro.getCodigo() + ")" // Formatea
                                                                                                                // la
                                                                                                                // información
                                                                                                                // del
                                                                                                                // libro.
                        : "Libro Desconocido";
                System.out.println("Prestamo: " + infoLibro + " a " + infoMiembro + " | Fecha Prestamo: "
                        + Fecha.formatDate(prestamo.getFechaPrestamo()) + " | Fecha Devolucion Estimada: "
                        + Fecha.formatDate(prestamo.getFechaDevolucionEstimada()) + " | Biblioteca ID: "
                        + prestamo.getIdBiblioteca()); // Imprime los detalles del préstamo.
            }
        }
        System.out.println("------------------------------------\n");
    }

    // Este método muestra los libros prestados por un usuario específico, dado su
    // número de documento.
    public void verLibrosPrestadosPorUsuario(long numeroDocumentoMiembro) {
        Miembro miembro = Miembro.encontrarMiembroPorNumeroDocumento(numeroDocumentoMiembro); // Busca el miembro.
        if (miembro == null) { // Si no lo encuentra.
            System.out.println("Error: Miembro con numero de documento " + numeroDocumentoMiembro + " no encontrado."); // Da
                                                                                                                        // un
                                                                                                                        // error.
            return;
        }

        System.out.println("\n--- LIBROS PRESTADOS POR " + miembro.getNombre().toUpperCase() + " "
                + miembro.getApellido().toUpperCase() + " ---"); // Encabezado.
        List<Prestamo> prestamosDelMiembro = Prestamo.encontrarPrestamosPorMiembro(numeroDocumentoMiembro); // Obtiene
                                                                                                            // todos los
                                                                                                            // préstamos
                                                                                                            // del
                                                                                                            // miembro.

        if (prestamosDelMiembro.isEmpty()) { // Si no tiene préstamos.
            System.out.println(miembro.getNombre() + " no tiene ningun libro prestado o devuelto.");
        } else {
            for (Prestamo prestamo : prestamosDelMiembro) { // Para cada préstamo.
                Libro libro = Libro.encontrarLibroPorCodigo(prestamo.getCodigoLibro()); // Busca el libro.
                String infoLibro = (libro != null) ? libro.getNombre() + " (Codigo: " + libro.getCodigo() + ")" // Formatea
                                                                                                                // la
                                                                                                                // información
                                                                                                                // del
                                                                                                                // libro.
                        : "Libro Desconocido";
                System.out.println("  - " + infoLibro + " | Fecha Prestamo: "
                        + Fecha.formatDate(prestamo.getFechaPrestamo()) + " | Fecha Devolucion Estimada: "
                        + Fecha.formatDate(prestamo.getFechaDevolucionEstimada()) + " | Estado: "
                        + prestamo.getEstado()); // Imprime los detalles.
            }
        }
        System.out.println("-------------------------------------------------------------------\n");
    }

    // Este método devuelve una cadena de texto que representa al objeto Admin de
    // forma legible.
    @Override
    public String toString() {
        // Usa el 'toString' de la clase padre 'Miembro' y le da un formato para Admin.
        return "Admin {"
                + "\n  " + super.toString().replace("Miembro {", "").replaceFirst("\\n}$", "")
                + "\n}";
    }
}
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.sistemabiblioteca; // Carpeta donde está organizado nuestro código.

import java.io.BufferedReader; // Herramienta para leer archivos de texto.
import java.io.IOException; // Para manejar errores al trabajar con archivos.
import java.nio.file.Files; // Para interactuar con archivos y directorios.
import java.nio.file.Path; // Para representar la ruta de un archivo.
import java.nio.file.StandardOpenOption; // Opciones para abrir/guardar archivos.
import java.util.ArrayList; // Para crear listas dinámicas.
import java.util.Date; // Para manejar fechas.
import java.util.List; // Para usar listas.
import java.util.stream.Collectors; // Para operar con listas de forma avanzada.

public class Biblioteca implements IPersistible { // La clase 'Biblioteca' representa una sede de la biblioteca y puede
                                                  // guardarse/cargarse.

    private int id; // Número único de la biblioteca.
    private String sede; // Ubicación de la biblioteca (ej. "Medellin").
    private String nombreBiblioteca; // Nombre completo de la biblioteca.

    public static final double MULTA_POR_DIA = 4.750; // Costo de multa por día de retraso (es fijo para todas las
                                                      // bibliotecas).
    public static final int DIAS_MAX_PRESTAMO = 30; // Días máximos para un préstamo sin multa (es fijo).

    public Biblioteca(int id, String sede, String nombreBiblioteca) { // Constructor para crear una biblioteca con
                                                                      // datos.
        this.id = id;
        this.sede = sede;
        this.nombreBiblioteca = nombreBiblioteca;
    }

    public Biblioteca() { // Constructor vacío.
    }

    // Métodos para obtener los valores de los atributos (Getters)
    public int getId() {
        return id;
    }

    public String getSede() {
        return sede;
    }

    public String getNombreBiblioteca() {
        return nombreBiblioteca;
    }

    // Métodos para cambiar los valores de los atributos (Setters)
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
    // Devuelve la ruta donde se guarda el archivo de las bibliotecas.
    protected static Path resolverRutaBibliotecas() {
        return PathManager.resolverRutaArchivo("Bibliotecas.txt"); // Pide a 'PathManager' la ruta de "Bibliotecas.txt".
    }

    // Carga todas las bibliotecas del archivo "Bibliotecas.txt" y las devuelve en
    // una lista.
    public static List<Biblioteca> cargarTodasLasBibliotecas() {
        List<Biblioteca> bibliotecas = new ArrayList<>(); // Lista donde se guardarán las bibliotecas.
        Path path = resolverRutaBibliotecas(); // Obtiene la ruta del archivo.
        if (!Files.exists(path)) { // Si el archivo no existe.
            System.err.println("Advertencia: Archivo Bibliotecas.txt no encontrado en " + path.toAbsolutePath()); // Muestra
                                                                                                                  // una
                                                                                                                  // advertencia.
            return bibliotecas; // Devuelve una lista vacía.
        }

        try (BufferedReader reader = Files.newBufferedReader(path, java.nio.charset.StandardCharsets.UTF_8)) { // Abre
                                                                                                               // el
                                                                                                               // archivo
                                                                                                               // para
                                                                                                               // leer.
            String linea; // Variable para cada línea.
            while ((linea = reader.readLine()) != null) { // Lee el archivo línea por línea.
                String[] parts = linea.split("\\\\"); // Divide la línea por '\' para obtener los datos.
                if (parts.length >= 3) { // Si la línea tiene al menos 3 partes (ID, sede, nombre).
                    try {
                        int id = Integer.parseInt(parts[0].trim()); // Convierte la primera parte a ID.
                        String sede = parts[1].trim(); // La segunda parte es la sede.
                        String nombre = parts[2].trim(); // La tercera parte es el nombre.
                        bibliotecas.add(new Biblioteca(id, sede, nombre)); // Crea una Biblioteca y la añade a la lista.
                    } catch (NumberFormatException e) {
                        System.err.println("Error de parseo en linea de Biblioteca: " + linea + " - " + e.getMessage()); // Error
                                                                                                                         // si
                                                                                                                         // el
                                                                                                                         // ID
                                                                                                                         // no
                                                                                                                         // es
                                                                                                                         // número.
                    }
                }
            }
        } catch (IOException e) { // Si hay un problema al leer el archivo.
            e.printStackTrace(); // Imprime el error.
        }
        return bibliotecas; // Devuelve la lista de bibliotecas.
    }

    // Guarda una lista completa de bibliotecas en el archivo "Bibliotecas.txt",
    // reemplazando el contenido.
    public static synchronized void guardarTodasLasBibliotecas(List<Biblioteca> bibliotecas) {
        Path path = resolverRutaBibliotecas(); // Obtiene la ruta del archivo.
        List<String> lineas = bibliotecas.stream()
                .map(Biblioteca::construirLinea) // Convierte cada objeto Biblioteca a una línea de texto.
                .collect(Collectors.toList()); // Recolecta todas las líneas en una lista.
        try {
            Files.write(path, lineas, java.nio.charset.StandardCharsets.UTF_8, StandardOpenOption.CREATE, // Crea el
                                                                                                          // archivo si
                                                                                                          // no existe.
                    StandardOpenOption.TRUNCATE_EXISTING); // Borra el contenido existente antes de escribir.
        } catch (IOException e) { // Si hay un problema al escribir.
            e.printStackTrace(); // Imprime el error.
        }
    }

    // Busca una biblioteca por su ID en la lista de todas las bibliotecas.
    public static Biblioteca encontrarBibliotecaPorId(int id) {
        return cargarTodasLasBibliotecas().stream()
                .filter(b -> b.getId() == id) // Filtra para encontrar la que coincide con el ID.
                .findFirst() // Toma la primera que encuentre.
                .orElse(null); // Si no encuentra, devuelve 'null'.
    }

    // Busca una biblioteca por el nombre de su sede (sin importar
    // mayúsculas/minúsculas).
    public static Biblioteca encontrarBibliotecaPorSede(String sede) {
        return cargarTodasLasBibliotecas().stream()
                .filter(b -> b.getSede().equalsIgnoreCase(sede)) // Filtra por la sede, ignorando mayúsculas/minúsculas.
                .findFirst()
                .orElse(null);
    }

    // Este método no hace nada aquí, porque las bibliotecas se guardan/eliminan
    // como una lista completa.
    @Override
    public void guardar() {
        System.out.println(
                "Advertencia: El metodo guardar() de Biblioteca no guarda instancias individuales. Use guardarTodasLasBibliotecas().");
    }

    // Este método tampoco hace nada, por la misma razón que 'guardar()'.
    @Override
    public void eliminar() {
        System.out.println(
                "Advertencia: El metodo eliminar() de Biblioteca no elimina instancias individuales. Use la logica del Admin para eliminar de la lista.");
    }

    // Convierte los datos de la biblioteca a una línea de texto para guardarla en
    // el archivo.
    @Override
    public String construirLinea() {
        return String.format("%d\\%s\\%s", id, sede, nombreBiblioteca); // Formato: ID\Sede\Nombre.
    }

    // --- Metodos de la funcionalidad de la biblioteca ---

    // Este método solo muestra las sedes disponibles, la selección se hace en otra
    // parte del programa.
    public void seleccionarSede() {
        System.out.println("Sedes de Bibliotecas disponibles:");
        List<Biblioteca> bibliotecas = cargarTodasLasBibliotecas(); // Carga todas las bibliotecas para mostrarlas.
        if (bibliotecas.isEmpty()) { // Si no hay bibliotecas.
            System.out.println("No hay bibliotecas registradas.");
            return;
        }

        for (Biblioteca b : bibliotecas) { // Recorre y muestra cada biblioteca.
            System.out.println("ID: " + b.getId() + ", Sede: " + b.getSede() + ", Nombre: " + b.getNombreBiblioteca());
        }

        System.out.println("La seleccion de la sede debe ser manejada por la logica de la interfaz de usuario.");
        System.out.println(
                "El objeto Biblioteca actual (this) representa la sede con ID: " + this.id + " y Sede: " + this.sede);
    }

    // Permite prestar un libro a un miembro, aplicando reglas de límite y deuda.
    public String prestarLibro(Miembro miembro, String codigoLibro) {
        if (miembro == null) { // Verifica que el miembro exista.
            return "Error: Miembro no valido.";
        }
        if (miembro.getRol() == 0) { // Los administradores no pueden pedir libros prestados.
            return "Error: Los administradores no pueden pedir libros prestados para si mismos.";
        }

        // Si el miembro es un Usuario, verifica si tiene deuda.
        if (miembro instanceof Usuario) { // 'instanceof' comprueba si 'miembro' es un 'Usuario'.
            Usuario user = (Usuario) miembro; // Lo convierte a 'Usuario' para acceder a su deuda.
            if (user.getDeuda() > 0) { // Si tiene deuda.
                return "Error: No puedes pedir prestado un libro. Tienes una deuda pendiente de "
                        + String.format("%.2f", user.getDeuda()) + " pesos."; // Da un error.
            }
        }

        Libro libro = Libro.encontrarLibroPorCodigo(codigoLibro); // Busca el libro.
        if (libro == null) { // Si el libro no se encuentra.
            return "Error: Libro con codigo " + codigoLibro + " no encontrado."; // Da un error.
        }

        // Verifica que el libro pertenezca a esta biblioteca.
        if (libro.getIdBiblioteca() != this.id) {
            return "Error: El libro " + codigoLibro + " no se encuentra en esta biblioteca (" + this.nombreBiblioteca
                    + ").";
        }

        if (libro.getUnidadesLibres() <= 0) { // Verifica si hay unidades disponibles del libro.
            return "Error: El libro '" + libro.getNombre() + "' no tiene unidades disponibles."; // Da un error.
        }

        // Obtiene la lista de libros que este miembro ya tiene prestados.
        List<Prestamo> prestamosActivosDelMiembro = Prestamo.encontrarPrestamosPorMiembro(miembro.getNumeroDocumento())
                .stream()
                .filter(p -> "PRESTADO".equals(p.getEstado())) // Solo los que están activos ("PRESTADO").
                .collect(Collectors.toList());

        int limitePrestamos; // Límite de préstamos según el rol.
        String tipoMiembro; // Descripción del tipo de miembro.
        switch (miembro.getRol()) { // Asigna límites según el rol del miembro.
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
                return "Error: Rol de miembro desconocido."; // Error si el rol no es válido.
        }

        // Si el miembro ya alcanzó su límite de préstamos.
        if (prestamosActivosDelMiembro.size() >= limitePrestamos) {
            return "Error: El " + tipoMiembro + " " + miembro.getNombre() + " ya ha alcanzado su limite de "
                    + limitePrestamos + " libros prestados."; // Da un error.
        }

        // Verifica si el miembro ya tiene este mismo libro prestado.
        boolean yaTieneLibro = prestamosActivosDelMiembro.stream()
                .anyMatch(p -> p.getCodigoLibro().equals(codigoLibro));
        if (yaTieneLibro) { // Si ya lo tiene.
            return "Error: El miembro ya tiene este libro prestado."; // Da un error.
        }

        // Si todas las condiciones son buenas, se realiza el préstamo.
        libro.setUnidadesLibres(libro.getUnidadesLibres() - 1); // Disminuye una unidad libre.
        libro.setUnidadesPrestadas(libro.getUnidadesPrestadas() + 1); // Aumenta una unidad prestada.
        libro.guardar(); // Guarda los cambios del libro.

        Date fechaPrestamo = Fecha.getToday(); // Obtiene la fecha actual.
        Date fechaDevolucionEstimada = Fecha.addDays(fechaPrestamo, DIAS_MAX_PRESTAMO); // Calcula la fecha límite de
                                                                                        // devolución.

        Prestamo nuevoPrestamo = new Prestamo(codigoLibro, miembro.getNumeroDocumento(), fechaPrestamo,
                fechaDevolucionEstimada,
                this.id); // Crea un nuevo objeto Prestamo.
        nuevoPrestamo.guardar(); // Guarda el registro del préstamo.

        return "Exito: Libro '" + libro.getNombre() + "' prestado a " + miembro.getNombre() + " "
                + miembro.getApellido() + ". Debe ser devuelto antes de " + Fecha.formatDate(fechaDevolucionEstimada)
                + "."; // Mensaje de éxito.
    }

    // Permite devolver un libro y calcula multas si hay retraso.
    public String devolverLibro(Miembro miembro, String codigoLibro) {
        if (miembro == null) { // Verifica que el miembro exista.
            return "Error: Miembro no valido.";
        }

        Libro libro = Libro.encontrarLibroPorCodigo(codigoLibro); // Busca el libro.
        if (libro == null) { // Si no lo encuentra.
            return "Error: Libro con codigo " + codigoLibro + " no encontrado."; // Da un error.
        }

        // Verifica que el libro pertenezca a esta biblioteca.
        if (libro.getIdBiblioteca() != this.id) {
            return "Error: El libro " + codigoLibro + " no se encuentra en esta biblioteca (" + this.nombreBiblioteca
                    + ").";
        }

        Prestamo prestamoActivo = Prestamo.encontrarPrestamoActivo(miembro.getNumeroDocumento(), codigoLibro); // Busca
                                                                                                               // el
                                                                                                               // préstamo
                                                                                                               // activo
                                                                                                               // del
                                                                                                               // libro
                                                                                                               // por
                                                                                                               // este
                                                                                                               // miembro.

        if (prestamoActivo == null) { // Si no se encuentra un préstamo activo.
            return "Error: El miembro " + miembro.getNombre() + " no tiene prestado el libro '" + libro.getNombre()
                    + "'."; // Da un error.
        }

        // Actualiza el préstamo como devuelto.
        prestamoActivo.setFechaDevolucionReal(Fecha.getToday()); // Establece la fecha real de devolución (hoy).
        prestamoActivo.setEstado("DEVUELTO"); // Cambia el estado a "DEVUELTO".
        prestamoActivo.guardar(); // Guarda los cambios del préstamo.

        libro.setUnidadesLibres(libro.getUnidadesLibres() + 1); // Aumenta una unidad libre del libro.
        libro.setUnidadesPrestadas(libro.getUnidadesPrestadas() - 1); // Disminuye una unidad prestada.
        libro.guardar(); // Guarda los cambios del libro.

        String multaInfo = ""; // Mensaje de multa (vacío si no hay multa).
        // Verifica si hubo retraso en la devolución para aplicar una multa.
        if (prestamoActivo.getFechaDevolucionReal().after(prestamoActivo.getFechaDevolucionEstimada())) {
            multaInfo = multar(miembro, libro, prestamoActivo); // Calcula y aplica la multa.
        }

        return "Exito: Libro '" + libro.getNombre() + "' devuelto por " + miembro.getNombre() + " "
                + miembro.getApellido() + ". " + multaInfo; // Mensaje de éxito.
    }

    // Busca libros disponibles en una sede específica.
    public List<Libro> librosDisponibles(String sedeBusqueda) {
        return Libro.cargarTodosLosLibros().stream()
                .filter(libro -> libro.getSedeBiblioteca().equalsIgnoreCase(sedeBusqueda) // Filtra por sede (ignorando
                                                                                          // mayúsculas/minúsculas).
                        && libro.getUnidadesLibres() > 0) // Solo si hay unidades libres.
                .collect(Collectors.toList()); // Devuelve la lista de libros.
    }

    // Busca libros disponibles por el ID de una biblioteca.
    public List<Libro> librosDisponibles(int idBibliotecaBusqueda) {
        return Libro.cargarTodosLosLibros().stream()
                .filter(libro -> libro.getIdBiblioteca() == idBibliotecaBusqueda && libro.getUnidadesLibres() > 0) // Filtra
                                                                                                                   // por
                                                                                                                   // ID
                                                                                                                   // de
                                                                                                                   // biblioteca
                                                                                                                   // y
                                                                                                                   // unidades
                                                                                                                   // libres.
                .collect(Collectors.toList());
    }

    // Busca libros disponibles por sede Y por ID de biblioteca.
    public List<Libro> librosDisponibles(String sedeBusqueda, int idBibliotecaBusqueda) {
        return Libro.cargarTodosLosLibros().stream()
                .filter(libro -> libro.getSedeBiblioteca().equalsIgnoreCase(sedeBusqueda) // Filtra por sede.
                        && libro.getIdBiblioteca() == idBibliotecaBusqueda // Filtra por ID de biblioteca.
                        && libro.getUnidadesLibres() > 0) // Y solo si hay unidades libres.
                .collect(Collectors.toList());
    }

    // Realiza una búsqueda flexible de libros por varios criterios.
    public List<Libro> buscarLibro(String query, String searchType) {
        List<Libro> allBooks = Libro.cargarTodosLosLibros(); // Carga todos los libros.
        return allBooks.stream()
                .filter(libro -> { // Para cada libro, aplica un filtro según el tipo de búsqueda.
                    switch (searchType.toLowerCase()) {
                        case "nombre":
                            return libro.getNombre().toLowerCase().contains(query.toLowerCase()); // Busca si el nombre
                                                                                                  // contiene el texto.
                        case "autor":
                            return libro.getAutor().toLowerCase().contains(query.toLowerCase()); // Busca si el autor
                                                                                                 // contiene el texto.
                        case "codigo":
                            return libro.getCodigo().equalsIgnoreCase(query); // Busca por código exacto.
                        case "sede":
                            return libro.getSedeBiblioteca().equalsIgnoreCase(query); // Busca por sede exacta.
                        case "idbiblioteca":
                            try {
                                return libro.getIdBiblioteca() == Integer.parseInt(query); // Busca por ID de biblioteca
                                                                                           // (numérico).
                            } catch (NumberFormatException e) {
                                return false; // Si el texto no es un número, no coincide.
                            }
                        default:
                            return false; // Si el tipo de búsqueda no es válido.
                    }
                })
                .collect(Collectors.toList()); // Devuelve la lista de libros encontrados.
    }

    // Calcula y aplica multas por retraso en la devolución de libros.
    public String multar(Miembro miembro, Libro libro, Prestamo prestamo) {
        // Verifica si el préstamo existe, si se devolvió y si fue después de la fecha
        // estimada.
        if (prestamo != null && prestamo.getFechaDevolucionReal() != null &&
                prestamo.getFechaDevolucionReal().after(prestamo.getFechaDevolucionEstimada())) {

            long diasRetraso = Fecha.getDaysBetween(prestamo.getFechaDevolucionReal(),
                    prestamo.getFechaDevolucionEstimada()); // Calcula los días de retraso.

            if (diasRetraso <= 0) // Asegura que al menos sea 1 día de retraso si la devolución fue tardía.
                diasRetraso = 1;

            double montoMulta = diasRetraso * MULTA_POR_DIA; // Calcula el monto total de la multa.

            if (miembro instanceof Usuario) { // Si el miembro es un Usuario (solo los usuarios acumulan deuda).
                Usuario user = (Usuario) miembro;
                user.setDeuda(user.getDeuda() + montoMulta); // Añade el monto al total de la deuda del usuario.
                System.out.println("Deuda actualizada para " + user.getNombre() + ". Nueva deuda total: "
                        + String.format("%.2f", user.getDeuda())); // Muestra la nueva deuda.
            }

            return String.format(
                    "Se ha aplicado una multa a %s %s por el libro '%s'. Retraso: %d dias. Monto: %.2f pesos. ",
                    miembro.getNombre(), miembro.getApellido(), libro.getNombre(), diasRetraso, montoMulta); // Mensaje
                                                                                                             // de
                                                                                                             // multa.
        }
        return "No hay multa para este prestamo."; // Mensaje si no aplica multa.
    }

    // Devuelve una cadena de texto que representa el objeto Biblioteca de forma
    // legible.
    @Override
    public String toString() {
        return "Biblioteca {" +
                "  ID=" + id +
                ", Sede='" + sede + '\'' +
                ", Nombre='" + nombreBiblioteca + '\'' +
                '}';
    }
}
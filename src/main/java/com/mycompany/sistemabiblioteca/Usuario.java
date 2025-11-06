/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.sistemabiblioteca; // Carpeta donde está organizado nuestro código.

import java.util.List; // Para usar listas.
import java.util.Locale; // Para formatear números (como la deuda) de forma consistente.

// Esta clase representa a un Usuario normal en el sistema de biblioteca.
// Hereda de 'Miembro' y añade información específica como deuda, sede de la universidad y carrera.
public class Usuario extends Miembro { // 'Usuario' es un tipo especial de 'Miembro'.

    private double deuda; // Cantidad de dinero que el usuario debe a la biblioteca.
    private String sedeUniversidad; // Sede de la universidad del usuario.
    private String carrera; // Carrera o programa de estudios del usuario.

    public Usuario() { // Constructor vacío.
        super(); // Llama al constructor de la clase padre 'Miembro'.
        this.deuda = 0.0; // Deuda inicial es 0.
        this.sedeUniversidad = ""; // Sede y carrera vacías al inicio.
        this.carrera = "";
    }

    // Constructor para crear un usuario con todos sus datos.
    public Usuario(byte tipoDocumento, long numeroDocumento, byte rol, String nombre, String apellido, String usuario,
            String contrasena, double deuda, String sedeUniversidad, String carrera) {
        super(tipoDocumento, numeroDocumento, rol, nombre, apellido, usuario, contrasena); // Llama al constructor de
                                                                                           // 'Miembro'.

        if (rol == 0) { // Si se intenta crear un Usuario con rol de Administrador.
            System.err.println(
                    "Advertencia: Se intento crear un Usuario con rol de Administrador. Estableciendo rol a Estudiante (1).");
            this.rol = 1; // Cambia el rol a 1 (Estudiante) por seguridad.
        }
        this.deuda = deuda;
        this.sedeUniversidad = sedeUniversidad;
        this.carrera = carrera;
    }

    // Getters para los atributos específicos del Usuario.
    public double getDeuda() {
        return deuda;
    }

    public String getSedeUniversidad() {
        return sedeUniversidad;
    }

    public String getCarrera() {
        return carrera;
    }

    // Setters que guardan el usuario automáticamente después de un cambio.
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

    // Permite al usuario ver la lista de todos los libros que tiene o ha tenido
    // prestados.
    public List<Prestamo> verMisLibrosPrestados() {
        System.out.println("\n--- MIS LIBROS PRESTADOS (" + this.getNombre() + " " + this.getApellido() + ") ---");
        // Obtiene todos los préstamos asociados a este usuario.
        List<Prestamo> misPrestamos = Prestamo.encontrarPrestamosPorMiembro(this.getNumeroDocumento());

        if (misPrestamos.isEmpty()) { // Si el usuario no tiene préstamos.
            System.out.println("No tienes libros prestados o devueltos.");
            return misPrestamos;
        } else {
            for (Prestamo prestamo : misPrestamos) { // Para cada préstamo.
                Libro libro = Libro.encontrarLibroPorCodigo(prestamo.getCodigoLibro()); // Busca la información del
                                                                                        // libro.
                String infoLibro = (libro != null) ? libro.getNombre() + " (Codigo: " + libro.getCodigo() + ")" // Formatea
                                                                                                                // la
                                                                                                                // información
                                                                                                                // del
                                                                                                                // libro.
                        : "Libro Desconocido";
                System.out.println("  - " + infoLibro + " | Fecha Prestamo: "
                        + Fecha.formatDate(prestamo.getFechaPrestamo()) + " | Fecha Devolucion Estimada: "
                        + Fecha.formatDate(prestamo.getFechaDevolucionEstimada()) + " | Estado: " + prestamo.getEstado()
                        + " | Biblioteca ID: " + prestamo.getIdBiblioteca()); // Imprime los detalles del préstamo.
            }
        }
        System.out.println("-----------------------------------------------------------------------------------\n");
        return misPrestamos;
    }

    // Permite al usuario solicitar un libro prestado.
    // Verifica si tiene deuda y si la biblioteca es válida.
    public String solicitarPrestamo(String codigoLibro, Biblioteca biblioteca) {
        if (this.deuda > 0) { // Si el usuario tiene deuda.
            return "Error: No puedes pedir prestado un libro nuevo. Tienes una deuda pendiente de "
                    + String.format(Locale.US, "%.2f", this.deuda) + " pesos."; // No puede pedir más libros.
        }
        if (biblioteca == null) { // Si no se ha seleccionado una biblioteca.
            return "Error: No se ha seleccionado una biblioteca para realizar el prestamo.";
        }
        System.out.println("Intentando prestar libro '" + codigoLibro + "' para " + this.getUsuario()
                + " desde biblioteca " + biblioteca.getNombreBiblioteca());
        return biblioteca.prestarLibro(this, codigoLibro); // Delega la lógica del préstamo a la clase 'Biblioteca'.
    }

    // Permite al usuario devolver un libro prestado.
    public String realizarDevolucion(String codigoLibro, Biblioteca biblioteca) {
        if (biblioteca == null) { // Si no se ha seleccionado una biblioteca.
            return "Error: No se ha seleccionado una biblioteca para realizar la devolucion.";
        }
        System.out.println("Intentando devolver libro '" + codigoLibro + "' para " + this.getUsuario()
                + " en biblioteca " + biblioteca.getNombreBiblioteca());
        return biblioteca.devolverLibro(this, codigoLibro); // Delega la lógica de la devolución a la clase
                                                            // 'Biblioteca'.
    }

    // Permite al usuario cambiar su información personal (básica y específica de
    // Usuario).
    public void cambiarInfoPersonal(byte nuevoTipoDocumento, String nuevoNombre, String nuevoApellido,
            String nuevaSedeUniversidad, String nuevaCarrera) {
        super.cambiarInfoPersonal(nuevoTipoDocumento, nuevoNombre, nuevoApellido); // Llama al método de la clase padre
                                                                                   // (Miembro).
        this.setSedeUniversidad(nuevaSedeUniversidad); // Actualiza la sede de la universidad.
        this.setCarrera(nuevaCarrera); // Actualiza la carrera.
        // Los setters ya llaman a 'guardar()', no es necesario hacerlo de nuevo aquí.
    }

    // Convierte el objeto Usuario a una línea de texto para guardarlo en el
    // archivo.
    // Añade los campos específicos de Usuario a la línea de Miembro.
    @Override
    public String construirLinea() {
        return super.construirLinea() + "\\" + // Obtiene la línea de la clase padre.
                String.format(Locale.US, "%.2f", this.deuda) + "\\" + // Añade la deuda (con punto decimal).
                (this.sedeUniversidad == null ? "" : this.sedeUniversidad) + "\\" + // Añade la sede.
                (this.carrera == null ? "" : this.carrera); // Añade la carrera.
    }

    // Devuelve una cadena de texto que representa el objeto Usuario de forma
    // legible.
    @Override
    public String toString() {
        String rolText; // Variable para el nombre legible del rol.
        switch (this.rol) { // Convierte el número de rol a texto.
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
                + "\n  Deuda: " + String.format(Locale.US, "%.2f", deuda) + " pesos" + // Muestra la deuda.
                "\n  Sede Universidad: " + sedeUniversidad +
                "\n  Carrera: " + carrera +
                "\n}";
    }
}
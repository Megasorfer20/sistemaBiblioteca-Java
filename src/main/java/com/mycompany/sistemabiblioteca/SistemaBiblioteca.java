/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.sistemabiblioteca;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

/**
 * Clase principal que orquesta el sistema de la biblioteca.
 * Maneja el inicio de sesión y la navegación por los menús de administrador y
 * usuario.
 */
public class SistemaBiblioteca {

    private static Miembro miembroActual = null; // Miembro logueado en el sistema
    private static Biblioteca bibliotecaSeleccionada = null; // Biblioteca con la que se interactúa
    private static Scanner scanner = new Scanner(System.in);

    // Para generar usuarios/contraseñas aleatorios
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private static String randomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        System.out.println("¡Bienvenido al Sistema de Gestión de Bibliotecas!");

        // 1. Inicializar datos: 1 admin y 3 usuarios aleatorios
        setupInitialData();

        // Cargar las bibliotecas disponibles al inicio
        List<Biblioteca> bibliotecasDisponibles = Biblioteca.loadAllBibliotecas();
        if (bibliotecasDisponibles.isEmpty()) {
            System.out.println(
                    "No se encontraron bibliotecas configuradas. Por favor, añada bibliotecas a 'Bibliotecas.txt'.");
            return;
        }

        // Seleccionar una biblioteca inicial para interactuar
        System.out.println("\n--- SELECCIÓN DE BIBLIOTECA ---");
        System.out.println("Seleccione una biblioteca para iniciar:");
        for (int i = 0; i < bibliotecasDisponibles.size(); i++) {
            System.out.println((i + 1) + ". " + bibliotecasDisponibles.get(i).getNombreBiblioteca() + " ("
                    + bibliotecasDisponibles.get(i).getSede() + ")");
        }
        int opcionBib;
        while (true) {
            System.out.print("Ingrese el número de la biblioteca: ");
            try {
                opcionBib = scanner.nextInt();
                scanner.nextLine(); // Consumir el salto de línea
                if (opcionBib > 0 && opcionBib <= bibliotecasDisponibles.size()) {
                    bibliotecaSeleccionada = bibliotecasDisponibles.get(opcionBib - 1);
                    System.out
                            .println("Ha seleccionado la biblioteca: " + bibliotecaSeleccionada.getNombreBiblioteca());
                    break;
                } else {
                    System.out.println("Opción no válida. Intente de nuevo.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Entrada inválida. Por favor, ingrese un número.");
                scanner.nextLine(); // Limpiar el buffer del scanner
            }
        }

        // Proceso de Login
        login();

        if (miembroActual != null) {
            if (miembroActual.getRol() == 0) { // Administrador
                mostrarMenuAdmin();
            } else { // Usuario (Estudiante, Profesor, Administrativo)
                mostrarMenuUsuario();
            }
        }

        System.out.println("Gracias por usar el Sistema de Bibliotecas. ¡Hasta pronto!");
        scanner.close();
    }

    private static void setupInitialData() {
        System.out.println("\n--- CONFIGURANDO DATOS INICIALES ---");

        // Crear el archivo Bibliotecas.txt si no existe con datos de ejemplo
        Path bibliotecasPath = Biblioteca.resolveBibliotecasPath();
        if (!Files.exists(bibliotecasPath)) {
            System.out.println("Creando 'Bibliotecas.txt' con datos de ejemplo...");
            try {
                Files.createDirectories(bibliotecasPath.getParent());
                List<String> defaultBibliotecas = new ArrayList<>();
                defaultBibliotecas.add("1\\Medellin\\Biblioteca Central");
                defaultBibliotecas.add("2\\Bogota\\Biblioteca Norte");
                Files.write(bibliotecasPath, defaultBibliotecas, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException e) {
                System.err.println("Error al crear Bibliotecas.txt: " + e.getMessage());
            }
        }

        // Crear 1 admin si no existe
        Admin adminExistente = null;
        for (Miembro m : Miembro.loadAllMembers()) {
            if (m.getRol() == 0) {
                adminExistente = (Admin) m;
                break;
            }
        }

        if (adminExistente == null) {
            System.out.println("Creando un nuevo administrador...");
            String adminUser = "admin_" + randomString(4);
            String adminPass = randomString(10);
            Admin nuevoAdmin = new Admin((byte) 1, 1000000000L + RANDOM.nextInt(100000000), (byte) 0, "Sys", "Admin",
                    adminUser, adminPass);
            nuevoAdmin.save(); // Esto hasheará la contraseña y la guardará
            System.out.println("Admin creado (¡Use estas credenciales para iniciar sesión!):");
            System.out.println("  Usuario: " + adminUser);
            System.out.println("  Contraseña: " + adminPass); // Se muestra la contraseña en texto plano para prueba
        } else {
            System.out.println("Administrador existente encontrado: " + adminExistente.getUsuario());
            System.out.println(
                    "Para obtener las credenciales de un admin existente, debería cambiarle la contraseña a una conocida.");
            System.out.println(
                    "Para fines de prueba, si desea un nuevo admin aleatorio, borre el archivo 'Miembros.txt' antes de iniciar.");
        }

        // Crear 3 usuarios aleatorios si hay menos de 3 usuarios con rol != 0
        long currentUsers = Miembro.loadAllMembers().stream().filter(m -> m.getRol() != 0).count();
        int usersToCreate = 3 - (int) currentUsers;
        if (usersToCreate > 0) {
            System.out.println("Creando " + usersToCreate + " usuarios aleatorios...");
            String[] roles = { "Estudiante", "Profesor", "Administrativo" };
            byte[] rolCodes = { 1, 2, 3 };
            String[] sedes = { "Medellin", "Bogota", "Cali" };
            String[] carreras = { "Ingenieria de Sistemas", "Derecho", "Medicina", "Diseño Grafico" };

            for (int i = 0; i < usersToCreate; i++) {
                String user = "user" + (1000 + RANDOM.nextInt(9000));
                String pass = randomString(8);
                byte docType = (byte) (1 + RANDOM.nextInt(2)); // 1 or 2 (CC or TI)
                long docNum = 100000000L + RANDOM.nextInt(900000000); // 9-digit number
                byte rol = rolCodes[RANDOM.nextInt(rolCodes.length)];
                String name = randomString(5);
                String lastName = randomString(7);
                String sedeUni = sedes[RANDOM.nextInt(sedes.length)];
                String carreraUser = carreras[RANDOM.nextInt(carreras.length)];

                Usuario nuevoUsuario = new Usuario(docType, docNum, rol, name, lastName, user, pass, 0.0, sedeUni,
                        carreraUser);
                nuevoUsuario.save();
                System.out.println("  Usuario creado: " + user + " (Rol: " + roles[rol - 1] + ", Pass: " + pass + ")"); // Para
                                                                                                                        // debug
            }
        } else {
            System.out.println("Suficientes usuarios existentes.");
        }
        System.out.println("--- CONFIGURACIÓN INICIAL COMPLETA ---\n");
    }

    private static void login() {
        System.out.println("\n--- INICIO DE SESIÓN ---");
        System.out.print("Usuario: ");
        String usuario = scanner.nextLine();
        System.out.print("Contraseña: ");
        String contrasena = scanner.nextLine();

        Miembro tempMiembro = new Miembro();
        String lineaMiembro = tempMiembro.login(usuario, contrasena);

        if (!lineaMiembro.isEmpty()) {
            String[] parts = lineaMiembro.split("\\\\");
            byte tipoDoc = Byte.parseByte(parts[1]);
            long noDoc = Long.parseLong(parts[0]);
            byte rol = Byte.parseByte(parts[2]);
            String nombre = parts[3];
            String apellido = parts[4];
            String user = parts[5];
            String passHash = parts[6]; // Contraseña ya hasheada

            if (rol == 0) {
                miembroActual = new Admin(tipoDoc, noDoc, rol, nombre, apellido, user, passHash);
            } else {
                // Al cargar un Usuario, hay que intentar leer los campos adicionales
                double deuda = 0.0;
                String sedeUniversidad = "";
                String carrera = "";
                // Un Usuario debe tener al menos 10 partes si se guardan todos los campos.
                if (parts.length >= 10) {
                    try {
                        deuda = Double.parseDouble(parts[7].trim());
                        sedeUniversidad = parts[8].trim();
                        carrera = parts[9].trim();
                    } catch (NumberFormatException e) {
                        System.err.println("Error al parsear campos de Usuario en línea de login: " + lineaMiembro
                                + " - " + e.getMessage());
                    }
                }
                miembroActual = new Usuario(tipoDoc, noDoc, rol, nombre, apellido, user, passHash, deuda,
                        sedeUniversidad, carrera);
            }
            System.out.println("Inicio de sesión exitoso. ¡Bienvenido, " + miembroActual.getNombre() + "!");
        } else {
            System.out.println("Usuario o contraseña incorrectos.");
            miembroActual = null;
        }
    }

    private static void mostrarMenuAdmin() {
        Admin admin = (Admin) miembroActual;
        int opcion;
        do {
            System.out.println("\n--- MENÚ ADMINISTRADOR ---");
            System.out.println("1. Administrar Libros");
            System.out.println("2. Administrar Usuarios");
            System.out.println("3. Ver Libros Prestados (Todos)");
            System.out.println("4. Ver Libros Prestados por un Usuario Específico");
            System.out.println("5. Ver Todos los Libros");
            System.out.println("6. Ver un Libro Específico"); // Nuevo método
            System.out.println("7. Ver Todos los Usuarios");
            System.out.println("8. Cambiar información personal de Admin");
            System.out.println("9. Cambiar contraseña de Admin");
            System.out.println("0. Cerrar Sesión");
            System.out.print("Seleccione una opción: ");

            try {
                opcion = scanner.nextInt();
                scanner.nextLine(); // Consumir salto de línea

                switch (opcion) {
                    case 1:
                        menuAdminLibros(admin);
                        break;
                    case 2:
                        menuAdminUsuarios(admin);
                        break;
                    case 3:
                        admin.verLibrosPrestados();
                        break;
                    case 4:
                        System.out.print("Ingrese el número de documento del usuario: ");
                        long noDocUsuario = scanner.nextLong();
                        scanner.nextLine();
                        admin.verLibrosPrestadosPorUsuario(noDocUsuario);
                        break;
                    case 5:
                        admin.verTodosLibros();
                        break;
                    case 6: // Ver un libro específico
                        System.out.print("Ingrese el código del libro a ver: ");
                        String codVerLibro = scanner.nextLine();
                        admin.verLibro(codVerLibro);
                        break;
                    case 7:
                        admin.verTodosUsuarios();
                        break;
                    case 8:
                        cambiarInfoPersonalAdmin(admin);
                        break;
                    case 9:
                        cambiarContrasenaAdmin(admin);
                        break;
                    case 0:
                        System.out.println("Cerrando sesión de administrador.");
                        break;
                    default:
                        System.out.println("Opción no válida. Intente de nuevo.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Entrada inválida. Por favor, ingrese un número.");
                scanner.nextLine(); // Limpiar el buffer
                opcion = -1; // Para asegurar que el bucle continúe
            }
        } while (opcion != 0);
    }

    private static void menuAdminLibros(Admin admin) {
        int opcion;
        do {
            System.out.println("\n--- ADMINISTRAR LIBROS ---");
            System.out.println("1. Agregar Libro");
            System.out.println("2. Editar Libro");
            System.out.println("3. Eliminar Unidades de Libro / Eliminar Libro Completo");
            System.out.println("0. Volver al Menú Principal");
            System.out.print("Seleccione una opción: ");

            try {
                opcion = scanner.nextInt();
                scanner.nextLine(); // Consumir salto de línea

                switch (opcion) {
                    case 1:
                        System.out.print("Nombre: ");
                        String nombre = scanner.nextLine();
                        System.out.print("Autor: ");
                        String autor = scanner.nextLine();
                        System.out.print("Código (ID único, dejar vacío para generar): ");
                        String codigo = scanner.nextLine();
                        System.out.print("Unidades Totales: ");
                        int unidades = scanner.nextInt();
                        scanner.nextLine();
                        // Asignamos a la biblioteca seleccionada por el admin
                        admin.agregarLibro(nombre, autor, codigo, unidades, bibliotecaSeleccionada.getId(),
                                bibliotecaSeleccionada.getSede());
                        break;
                    case 2:
                        System.out.print("Código del libro a editar: ");
                        String codEdit = scanner.nextLine();
                        Libro libroAEditar = Libro.findBookByCode(codEdit);
                        if (libroAEditar == null) {
                            System.out.println("Libro no encontrado.");
                            break;
                        }
                        System.out.println("Editando libro: " + libroAEditar.getNombre());
                        System.out.print("Nuevo Nombre (actual: " + libroAEditar.getNombre()
                                + ", dejar vacío para no cambiar): ");
                        String newNombre = scanner.nextLine();
                        if (newNombre.isEmpty())
                            newNombre = libroAEditar.getNombre();
                        System.out.print(
                                "Nuevo Autor (actual: " + libroAEditar.getAutor() + ", dejar vacío para no cambiar): ");
                        String newAutor = scanner.nextLine();
                        if (newAutor.isEmpty())
                            newAutor = libroAEditar.getAutor();
                        System.out.print("Nuevas Unidades Totales (actual: "
                                + (libroAEditar.getUnLibres() + libroAEditar.getUnPrestadas())
                                + ", dejar vacío para no cambiar): ");
                        String newUnidadesStr = scanner.nextLine();
                        int newUnidades = (newUnidadesStr.isEmpty()
                                ? (libroAEditar.getUnLibres() + libroAEditar.getUnPrestadas())
                                : Integer.parseInt(newUnidadesStr));

                        // Podríamos pedir cambiar la biblioteca, pero por simplicidad usamos la
                        // seleccionada
                        admin.editarLibro(codEdit, newNombre, newAutor, newUnidades, libroAEditar.getIdBiblioteca(),
                                libroAEditar.getSedeBiblioteca());
                        break;
                    case 3:
                        System.out.print("Código del libro a eliminar: ");
                        String codDel = scanner.nextLine();
                        System.out.print(
                                "¿Cuántas unidades desea eliminar? (0 para eliminar todas las unidades libres, o un número específico): ");
                        String unidadesDelStr = scanner.nextLine();
                        if (unidadesDelStr.isEmpty()) { // Si deja vacío, elimina todas las unidades libres
                            admin.eliminarLibro(codDel);
                        } else {
                            int unidadesAeliminar = Integer.parseInt(unidadesDelStr);
                            admin.eliminarLibro(codDel, unidadesAeliminar);
                        }
                        break;
                    case 0:
                        break;
                    default:
                        System.out.println("Opción no válida.");
                }
            } catch (InputMismatchException | NumberFormatException e) {
                System.out.println("Entrada inválida. Asegúrese de ingresar el tipo de dato correcto.");
                scanner.nextLine();
                opcion = -1;
            }
        } while (opcion != 0);
    }

    private static void menuAdminUsuarios(Admin admin) {
        int opcion;
        do {
            System.out.println("\n--- ADMINISTRAR USUARIOS ---");
            System.out.println("1. Agregar Usuario");
            System.out.println("2. Editar Usuario");
            System.out.println("3. Eliminar Usuario");
            System.out.println("0. Volver al Menú Principal");
            System.out.print("Seleccione una opción: ");

            try {
                opcion = scanner.nextInt();
                scanner.nextLine(); // Consumir salto de línea

                switch (opcion) {
                    case 1:
                        System.out.print("Tipo de Documento (1:CC, 2:TI, etc.): ");
                        byte tipoDoc = scanner.nextByte();
                        System.out.print("Número de Documento: ");
                        long noDoc = scanner.nextLong();
                        System.out.print("Rol (1:Estudiante, 2:Profesor, 3:Administrativo Universidad): ");
                        byte rol = scanner.nextByte();
                        scanner.nextLine(); // Consumir salto de línea
                        System.out.print("Nombre: ");
                        String nombre = scanner.nextLine();
                        System.out.print("Apellido: ");
                        String apellido = scanner.nextLine();
                        System.out.print("Usuario: ");
                        String usuario = scanner.nextLine();
                        System.out.print("Contraseña: ");
                        String contrasena = scanner.nextLine();
                        System.out.print("Deuda inicial (0.0 por defecto): ");
                        String deudaStr = scanner.nextLine();
                        double deuda = (deudaStr.isEmpty() ? 0.0 : Double.parseDouble(deudaStr));
                        System.out.print("Sede Universidad (Ej. Medellin): ");
                        String sedeUni = scanner.nextLine();
                        System.out.print("Carrera (Ej. Ingenieria de Sistemas): ");
                        String carrera = scanner.nextLine();

                        admin.crearUsuario(tipoDoc, noDoc, rol, nombre, apellido, usuario, contrasena, deuda, sedeUni,
                                carrera);
                        break;
                    case 2:
                        System.out.print("Usuario a editar: ");
                        String userEdit = scanner.nextLine();
                        Miembro miembroAEditar = Miembro.findMemberByUsername(userEdit);
                        if (miembroAEditar == null) {
                            System.out.println("Usuario no encontrado.");
                            break;
                        }
                        System.out.println(
                                "Editando miembro: " + miembroAEditar.getNombre() + " " + miembroAEditar.getApellido());

                        System.out.print("Nuevo Tipo Doc (actual: " + miembroAEditar.getTipoDoc()
                                + ", dejar vacío para no cambiar): ");
                        String newTipoDocStr = scanner.nextLine();
                        byte newTipoDoc = (newTipoDocStr.isEmpty() ? miembroAEditar.getTipoDoc()
                                : Byte.parseByte(newTipoDocStr));
                        System.out.print("Nuevo No. Doc (actual: " + miembroAEditar.getNoDoc()
                                + ", dejar vacío para no cambiar): ");
                        String newNoDocStr = scanner.nextLine();
                        long newNoDoc = (newNoDocStr.isEmpty() ? miembroAEditar.getNoDoc()
                                : Long.parseLong(newNoDocStr));
                        System.out.print("Nuevo Rol (actual: " + miembroAEditar.getRol()
                                + ", 0:Admin, 1:Estudiante, 2:Profesor, 3:Admin Uni, dejar vacío para no cambiar): ");
                        String newRolStr = scanner.nextLine();
                        byte newRol = (newRolStr.isEmpty() ? miembroAEditar.getRol() : Byte.parseByte(newRolStr));
                        System.out.print("Nuevo Nombre (actual: " + miembroAEditar.getNombre()
                                + ", dejar vacío para no cambiar): ");
                        String newNombre = scanner.nextLine();
                        if (newNombre.isEmpty())
                            newNombre = miembroAEditar.getNombre();
                        System.out.print("Nuevo Apellido (actual: " + miembroAEditar.getApellido()
                                + ", dejar vacío para no cambiar): ");
                        String newApellido = scanner.nextLine();
                        if (newApellido.isEmpty())
                            newApellido = miembroAEditar.getApellido();
                        System.out.print("Nuevo Usuario (actual: " + miembroAEditar.getUsuario()
                                + ", dejar vacío para no cambiar): ");
                        String newUsuario = scanner.nextLine();
                        if (newUsuario.isEmpty())
                            newUsuario = miembroAEditar.getUsuario();
                        System.out.print("Nueva Contraseña (dejar vacío para no cambiar): ");
                        String newContrasena = scanner.nextLine();

                        double nuevaDeuda = 0.0;
                        String nuevaSedeUni = "";
                        String nuevaCarrera = "";

                        // Solo preguntar/aplicar campos específicos si el rol es de Usuario (no Admin)
                        if (newRol != 0) {
                            // Si el miembro actual es un Usuario, pre-cargar sus valores. Si no lo es (p.
                            // ej. era Admin), se usarán por defecto o vacíos.
                            if (miembroAEditar instanceof Usuario) {
                                Usuario currentUsuario = (Usuario) miembroAEditar;
                                System.out.print(
                                        "Nueva Deuda (actual: " + String.format("%.2f", currentUsuario.getDeuda())
                                                + ", dejar vacío para no cambiar): ");
                                String newDeudaStr = scanner.nextLine();
                                nuevaDeuda = (newDeudaStr.isEmpty() ? currentUsuario.getDeuda()
                                        : Double.parseDouble(newDeudaStr));
                                System.out.print("Nueva Sede Universidad (actual: "
                                        + currentUsuario.getSedeUniversidad() + ", dejar vacío para no cambiar): ");
                                String newSedeUniStr = scanner.nextLine();
                                nuevaSedeUni = (newSedeUniStr.isEmpty() ? currentUsuario.getSedeUniversidad()
                                        : newSedeUniStr);
                                System.out.print("Nueva Carrera (actual: " + currentUsuario.getCarrera()
                                        + ", dejar vacío para no cambiar): ");
                                String newCarreraStr = scanner.nextLine();
                                nuevaCarrera = (newCarreraStr.isEmpty() ? currentUsuario.getCarrera() : newCarreraStr);
                            } else {
                                // Si era un Admin y se cambia a rol de Usuario, se pedirán estos campos
                                System.out.println(
                                        "Este miembro pasará a ser un Usuario. Por favor, ingrese sus datos específicos:");
                                System.out.print("Nueva Deuda (0.0 por defecto): ");
                                String newDeudaStr = scanner.nextLine();
                                nuevaDeuda = (newDeudaStr.isEmpty() ? 0.0 : Double.parseDouble(newDeudaStr));
                                System.out.print("Nueva Sede Universidad: ");
                                nuevaSedeUni = scanner.nextLine();
                                System.out.print("Nueva Carrera: ");
                                nuevaCarrera = scanner.nextLine();
                            }
                        } else {
                            System.out.println(
                                    "Este miembro es o seguirá siendo un Administrador; los campos de deuda, sede y carrera no aplican.");
                        }

                        admin.editarUsuario(userEdit, newTipoDoc, newNoDoc, newRol, newNombre, newApellido, newUsuario,
                                newContrasena, nuevaDeuda, nuevaSedeUni, nuevaCarrera);
                        break;
                    case 3:
                        System.out.print("Usuario a eliminar: ");
                        String userDel = scanner.nextLine();
                        admin.eliminarUsuario(userDel);
                        break;
                    case 0:
                        break;
                    default:
                        System.out.println("Opción no válida.");
                }
            } catch (InputMismatchException | NumberFormatException e) {
                System.out.println("Entrada inválida. Asegúrese de ingresar el tipo de dato correcto.");
                scanner.nextLine();
                opcion = -1;
            }
        } while (opcion != 0);
    }

    private static void cambiarInfoPersonalAdmin(Admin admin) {
        System.out.println("\n--- CAMBIAR INFORMACIÓN PERSONAL DE ADMIN ---");
        System.out.print("Nuevo Tipo Doc (actual: " + admin.getTipoDoc() + ", dejar vacío para no cambiar): ");
        String newTipoDocStr = scanner.nextLine();
        byte newTipoDoc = (newTipoDocStr.isEmpty() ? admin.getTipoDoc() : Byte.parseByte(newTipoDocStr));
        System.out.print("Nuevo Nombre (actual: " + admin.getNombre() + ", dejar vacío para no cambiar): ");
        String newNombre = scanner.nextLine();
        if (newNombre.isEmpty())
            newNombre = admin.getNombre();
        System.out.print("Nuevo Apellido (actual: " + admin.getApellido() + ", dejar vacío para no cambiar): ");
        String newApellido = scanner.nextLine();
        if (newApellido.isEmpty())
            newApellido = admin.getApellido();

        admin.cambiarInfoPersonal(newTipoDoc, newNombre, newApellido);
        System.out.println("Información personal actualizada con éxito.");
    }

    private static void cambiarContrasenaAdmin(Admin admin) {
        System.out.println("\n--- CAMBIAR CONTRASEÑA DE ADMIN ---");
        System.out.print("Ingrese la nueva contraseña: ");
        String newPass = scanner.nextLine();
        admin.cambiarContrasena(newPass);
        System.out.println("Contraseña actualizada con éxito.");
    }

    private static void mostrarMenuUsuario() {
        Usuario usuario = (Usuario) miembroActual;
        int opcion;
        do {
            System.out.println("\n--- MENÚ USUARIO (" + usuario.getNombre() + ") ---");
            System.out.println("Deuda Actual: " + String.format("%.2f", usuario.getDeuda()) + " pesos");
            System.out.println("1. Buscar Libro");
            System.out.println("2. Ver Libros Disponibles en " + bibliotecaSeleccionada.getNombreBiblioteca());
            System.out.println("3. Prestar Libro");
            System.out.println("4. Devolver Libro");
            System.out.println("5. Ver Mis Libros Prestados");
            System.out.println("6. Cambiar información de contacto");
            System.out.println("7. Cambiar Contraseña");
            System.out.println("0. Cerrar Sesión");
            System.out.print("Seleccione una opción: ");

            try {
                opcion = scanner.nextInt();
                scanner.nextLine(); // Consumir salto de línea

                switch (opcion) {
                    case 1:
                        System.out.print("Ingrese el término de búsqueda: ");
                        String query = scanner.nextLine();
                        System.out.print("Buscar por (nombre/autor/codigo/sede/idbiblioteca): ");
                        String searchType = scanner.nextLine();
                        List<Libro> resultados = bibliotecaSeleccionada.buscarLibro(query, searchType);
                        if (resultados.isEmpty()) {
                            System.out.println("No se encontraron libros.");
                        } else {
                            resultados.forEach(System.out::println);
                        }
                        break;
                    case 2:
                        List<Libro> disponibles = bibliotecaSeleccionada
                                .librosDisponibles(bibliotecaSeleccionada.getSede(), bibliotecaSeleccionada.getId());
                        if (disponibles.isEmpty()) {
                            System.out.println("No hay libros disponibles en "
                                    + bibliotecaSeleccionada.getNombreBiblioteca() + ".");
                        } else {
                            disponibles.forEach(System.out::println);
                        }
                        break;
                    case 3:
                        System.out.print("Ingrese el código del libro a prestar: ");
                        String codPrestamo = scanner.nextLine();
                        System.out.println(usuario.solicitarPrestamo(codPrestamo, bibliotecaSeleccionada));
                        break;
                    case 4:
                        System.out.print("Ingrese el código del libro a devolver: ");
                        String codDevolucion = scanner.nextLine();
                        System.out.println(usuario.realizarDevolucion(codDevolucion, bibliotecaSeleccionada));
                        break;
                    case 5:
                        usuario.verMisLibrosPrestados();
                        break;
                    case 6:
                        cambiarInfoPersonalUsuario(usuario);
                        break;
                    case 7:
                        cambiarContrasenaUsuario(usuario);
                        break;
                    case 0:
                        System.out.println("Cerrando sesión de usuario.");
                        break;
                    default:
                        System.out.println("Opción no válida. Intente de nuevo.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Entrada inválida. Por favor, ingrese un número.");
                scanner.nextLine(); // Limpiar el buffer
                opcion = -1;
            }
        } while (opcion != 0);
    }

    private static void cambiarInfoPersonalUsuario(Usuario usuario) {
        System.out.println("\n--- CAMBIAR INFORMACIÓN PERSONAL DE USUARIO ---");
        System.out.print("Nuevo Tipo Doc (actual: " + usuario.getTipoDoc() + ", dejar vacío para no cambiar): ");
        String newTipoDocStr = scanner.nextLine();
        byte newTipoDoc = (newTipoDocStr.isEmpty() ? usuario.getTipoDoc() : Byte.parseByte(newTipoDocStr));
        System.out.print("Nuevo Nombre (actual: " + usuario.getNombre() + ", dejar vacío para no cambiar): ");
        String newNombre = scanner.nextLine();
        if (newNombre.isEmpty())
            newNombre = usuario.getNombre();
        System.out.print("Nuevo Apellido (actual: " + usuario.getApellido() + ", dejar vacío para no cambiar): ");
        String newApellido = scanner.nextLine();
        if (newApellido.isEmpty())
            newApellido = usuario.getApellido();
        System.out.print(
                "Nueva Sede Universidad (actual: " + usuario.getSedeUniversidad() + ", dejar vacío para no cambiar): ");
        String newSedeUni = scanner.nextLine();
        if (newSedeUni.isEmpty())
            newSedeUni = usuario.getSedeUniversidad();
        System.out.print("Nueva Carrera (actual: " + usuario.getCarrera() + ", dejar vacío para no cambiar): ");
        String newCarrera = scanner.nextLine();
        if (newCarrera.isEmpty())
            newCarrera = usuario.getCarrera();

        usuario.cambiarInfoPersonal(newTipoDoc, newNombre, newApellido, newSedeUni, newCarrera);
        System.out.println("Información personal actualizada con éxito.");
    }

    private static void cambiarContrasenaUsuario(Usuario usuario) {
        System.out.println("\n--- CAMBIAR CONTRASEÑA DE USUARIO ---");
        System.out.print("Ingrese la nueva contraseña: ");
        String newPass = scanner.nextLine();
        usuario.cambiarContrasena(newPass);
        System.out.println("Contraseña actualizada con éxito.");
    }
}
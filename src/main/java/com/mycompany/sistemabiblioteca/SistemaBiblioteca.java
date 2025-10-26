/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.sistemabiblioteca;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit; // Para el Thread.sleep

/**
 * Clase principal que orquesta el sistema de la biblioteca.
 * Maneja el inicio de sesion y la navegacion por los menus de administrador y
 * usuario, e incluye pruebas automaticas.
 *
 * @author edrui
 */
public class SistemaBiblioteca {

    private static Miembro miembroActual = null;
    private static Biblioteca bibliotecaSeleccionada = null;
    private static Scanner scanner = new Scanner(System.in);

    // Para generar usuarios/contrasenas aleatorios
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private static String stringAleatorio(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        System.out.println("¡Bienvenido al Sistema de Gestion de Bibliotecas!");

        System.out.println("\n--- INICIANDO CONFIGURACION Y PRUEBAS AUTOMATICAS ---");
        setupInitialData(); // Inicializar datos: admin, usuarios, bibliotecas
        ejecutarTestsAutomaticos(); // Ejecutar las pruebas
        System.out.println("--- PRUEBAS AUTOMATICAS COMPLETADAS ---\n");

        // Cargar las bibliotecas disponibles despues de posible adicion en setup
        List<Biblioteca> bibliotecasDisponibles = Biblioteca.cargarTodasLasBibliotecas();
        if (bibliotecasDisponibles.isEmpty()) {
            System.err.println(
                    "Error: No se encontraron bibliotecas configuradas. Por favor, asegurese de tener 'Bibliotecas.txt' con datos.");
            return;
        }

        seleccionarBibliotecaInicial(bibliotecasDisponibles);

        if (bibliotecaSeleccionada == null) {
            System.out.println("No se pudo seleccionar una biblioteca. Saliendo del sistema.");
            scanner.close();
            return;
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

    /**
     * Configura los datos iniciales del sistema (bibliotecas, admin, usuarios).
     * Si los archivos no existen, los crea con datos de ejemplo.
     */
    private static void setupInitialData() {
        System.out.println("\n--- CONFIGURANDO DATOS INICIALES ---");

        // 1. Crear el archivo Bibliotecas.txt si no existe con datos de ejemplo
        Path bibliotecasPath = Biblioteca.resolverRutaBibliotecas();
        if (!Files.exists(bibliotecasPath) || Biblioteca.cargarTodasLasBibliotecas().isEmpty()) {
            System.out.println("Creando 'Bibliotecas.txt' con datos de ejemplo...");
            try {
                List<String> defaultBibliotecas = new ArrayList<>();
                defaultBibliotecas.add("1\\Medellin\\Biblioteca Central");
                defaultBibliotecas.add("2\\Bogota\\Biblioteca Norte");
                Files.write(bibliotecasPath, defaultBibliotecas, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);
                System.out.println("Bibliotecas de ejemplo creadas.");
            } catch (IOException e) {
                System.err.println("Error al crear Bibliotecas.txt: " + e.getMessage());
            }
        } else {
            System.out.println("Bibliotecas existentes cargadas.");
        }

        // 2. Crear 1 admin si no existe
        Admin adminExistente = null;
        for (Miembro m : Miembro.cargarTodosLosMiembros()) {
            if (m instanceof Admin) {
                adminExistente = (Admin) m;
                break;
            }
        }

        if (adminExistente == null) {
            System.out.println("Creando un nuevo administrador...");
            String adminUser = "admin_" + stringAleatorio(4);
            String adminPass = stringAleatorio(10);
            Admin nuevoAdmin = new Admin((byte) 1, 1000000000L + RANDOM.nextInt(100000000), (byte) 0, "Sys", "Admin",
                    adminUser, adminPass);
            nuevoAdmin.guardar();
            System.out.println("Admin creado (¡Use estas credenciales para iniciar sesion!):");
            System.out.println("  Usuario: " + adminUser);
            System.out.println("  Contrasena: " + adminPass);
        } else {
            System.out.println("Administrador existente encontrado: " + adminExistente.getUsuario());
        }

        // 3. Crear 3 usuarios aleatorios si hay menos de 3 usuarios con rol != 0
        long currentUsers = Miembro.cargarTodosLosMiembros().stream().filter(m -> m.getRol() != 0).count();
        int usersToCreate = 3 - (int) currentUsers;
        if (usersToCreate > 0) {
            System.out.println("Creando " + usersToCreate + " usuarios aleatorios...");
            String[] roles = { "Estudiante", "Profesor", "Administrativo Universidad" };
            byte[] rolCodes = { 1, 2, 3 };
            String[] sedes = { "Medellin", "Bogota", "Cali" };
            String[] carreras = { "Ingenieria de Sistemas", "Derecho", "Medicina", "Diseno Grafico" };

            for (int i = 0; i < usersToCreate; i++) {
                String user = "user" + (1000 + RANDOM.nextInt(9000));
                String pass = stringAleatorio(8);
                byte docType = (byte) (1 + RANDOM.nextInt(2));
                long docNum = 100000000L + RANDOM.nextInt(900000000);
                byte rol = rolCodes[RANDOM.nextInt(rolCodes.length)];
                String name = stringAleatorio(5);
                String lastName = stringAleatorio(7);
                String sedeUni = sedes[RANDOM.nextInt(sedes.length)];
                String carreraUser = carreras[RANDOM.nextInt(carreras.length)];

                Usuario nuevoUsuario = new Usuario(docType, docNum, rol, name, lastName, user, pass, 0.0, sedeUni,
                        carreraUser);
                nuevoUsuario.guardar();
                System.out.println("  Usuario creado: " + user + " (Rol: " + roles[rol - 1] + ", Pass: " + pass + ")");
            }
        } else {
            System.out.println("Suficientes usuarios existentes.");
        }
        System.out.println("--- CONFIGURACION INICIAL COMPLETA ---\n");
    }

    /**
     * Permite al usuario seleccionar una biblioteca de las disponibles.
     * 
     * @param bibliotecasDisponibles Lista de bibliotecas para elegir.
     */
    private static void seleccionarBibliotecaInicial(List<Biblioteca> bibliotecasDisponibles) {
        System.out.println("\n--- SELECCION DE BIBLIOTECA ---");
        System.out.println("Seleccione una biblioteca para iniciar:");
        for (int i = 0; i < bibliotecasDisponibles.size(); i++) {
            System.out.println((i + 1) + ". " + bibliotecasDisponibles.get(i).getNombreBiblioteca() + " ("
                    + bibliotecasDisponibles.get(i).getSede() + ")");
        }
        int opcionBib;
        while (true) {
            System.out.print("Ingrese el numero de la biblioteca: ");
            try {
                opcionBib = scanner.nextInt();
                scanner.nextLine();
                if (opcionBib > 0 && opcionBib <= bibliotecasDisponibles.size()) {
                    bibliotecaSeleccionada = bibliotecasDisponibles.get(opcionBib - 1);
                    System.out
                            .println("Ha seleccionado la biblioteca: " + bibliotecaSeleccionada.getNombreBiblioteca());
                    break;
                } else {
                    System.out.println("Opcion no valida. Intente de nuevo.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Entrada invalida. Por favor, ingrese un numero.");
                scanner.nextLine();
            }
        }
    }

    /**
     * Maneja el proceso de inicio de sesion.
     */
    private static void login() {
        System.out.println("\n--- INICIO DE SESION ---");
        System.out.print("Usuario: ");
        String usuario = scanner.nextLine();
        System.out.print("Contrasena: ");
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
            String passHash = parts[6];

            if (rol == 0) {
                miembroActual = new Admin(tipoDoc, noDoc, rol, nombre, apellido, user, passHash);
            } else {
                double deuda = 0.0;
                String sedeUniversidad = "";
                String carrera = "";
                if (parts.length >= 10) {
                    try {
                        deuda = Double.parseDouble(parts[7].trim());
                        sedeUniversidad = parts[8].trim();
                        carrera = parts[9].trim();
                    } catch (NumberFormatException e) {
                        System.err.println("Error al parsear campos de Usuario en linea de login: " + lineaMiembro
                                + " - " + e.getMessage());
                    }
                }
                miembroActual = new Usuario(tipoDoc, noDoc, rol, nombre, apellido, user, passHash, deuda,
                        sedeUniversidad, carrera);
            }
            System.out.println("Inicio de sesion exitoso. ¡Bienvenido, " + miembroActual.getNombre() + "!");
        } else {
            System.out.println("Usuario o contrasena incorrectos.");
            miembroActual = null;
        }
    }

    // --- Implementacion de Menus (Admin y Usuario) ---
    private static void mostrarMenuAdmin() {
        Admin admin = (Admin) miembroActual;
        int opcion;
        do {
            System.out.println("\n--- MENU ADMINISTRADOR ---");
            System.out.println("1. Administrar Libros");
            System.out.println("2. Administrar Usuarios");
            System.out.println("3. Administrar Bibliotecas");
            System.out.println("4. Ver Libros Prestados (Todos)");
            System.out.println("5. Ver Libros Prestados por un Usuario Especifico");
            System.out.println("6. Ver Todos los Libros");
            System.out.println("7. Ver un Libro Especifico");
            System.out.println("8. Ver Todos los Usuarios");
            System.out.println("9. Cambiar informacion personal de Admin");
            System.out.println("10. Cambiar contrasena de Admin");
            System.out.println("0. Cerrar Sesion");
            System.out.print("Seleccione una opcion: ");

            try {
                opcion = scanner.nextInt();
                scanner.nextLine();

                switch (opcion) {
                    case 1:
                        menuAdminLibros(admin);
                        break;
                    case 2:
                        menuAdminUsuarios(admin);
                        break;
                    case 3:
                        menuAdminBibliotecas(admin);
                        break;
                    case 4:
                        admin.verLibrosPrestados();
                        break;
                    case 5:
                        System.out.print("Ingrese el numero de documento del usuario: ");
                        long noDocUsuario = scanner.nextLong();
                        scanner.nextLine();
                        admin.verLibrosPrestadosPorUsuario(noDocUsuario);
                        break;
                    case 6:
                        admin.verTodosLibros();
                        break;
                    case 7:
                        System.out.print("Ingrese el codigo del libro a ver: ");
                        String codVerLibro = scanner.nextLine();
                        admin.verLibro(codVerLibro);
                        break;
                    case 8:
                        admin.verTodosUsuarios();
                        break;
                    case 9:
                        cambiarInfoPersonalAdmin(admin);
                        break;
                    case 10:
                        cambiarContrasenaAdmin(admin);
                        break;
                    case 0:
                        System.out.println("Cerrando sesion de administrador.");
                        break;
                    default:
                        System.out.println("Opcion no valida. Intente de nuevo.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Entrada invalida. Por favor, ingrese un numero.");
                scanner.nextLine();
                opcion = -1;
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
            System.out.println("0. Volver al Menu Principal");
            System.out.print("Seleccione una opcion: ");

            try {
                opcion = scanner.nextInt();
                scanner.nextLine();

                switch (opcion) {
                    case 1:
                        System.out.print("Nombre: ");
                        String nombre = scanner.nextLine();
                        System.out.print("Autor: ");
                        String autor = scanner.nextLine();
                        // Dejamos que el admin pueda dar un codigo o pedir "auto"
                        System.out.print("Codigo (ID unico, teclear 'auto' para generar): ");
                        String codigo = scanner.nextLine();
                        System.out.print("Unidades Totales: ");
                        int unidades = scanner.nextInt();
                        scanner.nextLine();
                        System.out.println(
                                admin.agregarLibro(nombre, autor, codigo, unidades, bibliotecaSeleccionada.getId(),
                                        bibliotecaSeleccionada.getSede()));
                        break;
                    case 2:
                        System.out.print("Codigo del libro a editar: ");
                        String codEdit = scanner.nextLine();
                        Libro libroAEditar = Libro.encontrarLibroPorCodigo(codEdit);
                        if (libroAEditar == null) {
                            System.out.println("Libro no encontrado.");
                            break;
                        }
                        System.out.println("Editando libro: " + libroAEditar.getNombre());
                        System.out.print("Nuevo Nombre (actual: " + libroAEditar.getNombre()
                                + ", dejar vacio para no cambiar): ");
                        String newNombre = scanner.nextLine();
                        if (newNombre.isEmpty())
                            newNombre = libroAEditar.getNombre();
                        System.out.print(
                                "Nuevo Autor (actual: " + libroAEditar.getAutor() + ", dejar vacio para no cambiar): ");
                        String newAutor = scanner.nextLine();
                        if (newAutor.isEmpty())
                            newAutor = libroAEditar.getAutor();
                        System.out.print("Nuevas Unidades Totales (actual: "
                                + (libroAEditar.getUnidadesLibres() + libroAEditar.getUnidadesPrestadas())
                                + ", dejar vacio para no cambiar): ");
                        String newUnidadesStr = scanner.nextLine();
                        int newUnidades = (newUnidadesStr.isEmpty()
                                ? (libroAEditar.getUnidadesLibres() + libroAEditar.getUnidadesPrestadas())
                                : Integer.parseInt(newUnidadesStr));

                        System.out.println(admin.editarLibro(codEdit, newNombre, newAutor, newUnidades,
                                libroAEditar.getIdBiblioteca(),
                                libroAEditar.getSedeBiblioteca()));
                        break;
                    case 3:
                        System.out.print("Codigo del libro a eliminar: ");
                        String codDel = scanner.nextLine();
                        System.out.print(
                                "¿Cuantas unidades desea eliminar? (0 para eliminar todas las unidades libres, o un numero especifico): ");
                        String unidadesDelStr = scanner.nextLine();
                        if (unidadesDelStr.isEmpty() || unidadesDelStr.equals("0")) {
                            System.out.println(admin.eliminarLibro(codDel));
                        } else {
                            int unidadesAeliminar = Integer.parseInt(unidadesDelStr);
                            System.out.println(admin.eliminarLibro(codDel, unidadesAeliminar));
                        }
                        break;
                    case 0:
                        break;
                    default:
                        System.out.println("Opcion no valida.");
                }
            } catch (InputMismatchException | NumberFormatException e) {
                System.out.println("Entrada invalida. Asegurese de ingresar el tipo de dato correcto.");
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
            System.out.println("0. Volver al Menu Principal");
            System.out.print("Seleccione una opcion: ");

            try {
                opcion = scanner.nextInt();
                scanner.nextLine();

                switch (opcion) {
                    case 1:
                        System.out.print("Tipo de Documento (1:CC, 2:TI, etc.): ");
                        byte tipoDoc = scanner.nextByte();
                        System.out.print("Numero de Documento: ");
                        long noDoc = scanner.nextLong();
                        System.out.print("Rol (1:Estudiante, 2:Profesor, 3:Administrativo Universidad): ");
                        byte rol = scanner.nextByte();
                        scanner.nextLine();
                        System.out.print("Nombre: ");
                        String nombre = scanner.nextLine();
                        System.out.print("Apellido: ");
                        String apellido = scanner.nextLine();
                        System.out.print("Usuario: ");
                        String usuario = scanner.nextLine();
                        System.out.print("Contrasena: ");
                        String contrasena = scanner.nextLine();
                        System.out.print("Deuda inicial (0.0 por defecto): ");
                        String deudaStr = scanner.nextLine();
                        double deuda = (deudaStr.isEmpty() ? 0.0 : Double.parseDouble(deudaStr));
                        System.out.print("Sede Universidad (Ej. Medellin): ");
                        String sedeUni = scanner.nextLine();
                        System.out.print("Carrera (Ej. Ingenieria de Sistemas): ");
                        String carrera = scanner.nextLine();

                        System.out.println(admin.crearUsuario(tipoDoc, noDoc, rol, nombre, apellido, usuario,
                                contrasena, deuda, sedeUni,
                                carrera));
                        break;
                    case 2:
                        System.out.print("Usuario a editar: ");
                        String userEdit = scanner.nextLine();
                        Miembro miembroAEditar = Miembro.encontrarMiembroPorUsuario(userEdit);
                        if (miembroAEditar == null) {
                            System.out.println("Usuario no encontrado.");
                            break;
                        }
                        System.out.println(
                                "Editando miembro: " + miembroAEditar.getNombre() + " " + miembroAEditar.getApellido());

                        System.out.print("Nuevo Tipo Doc (actual: " + miembroAEditar.getTipoDocumento()
                                + ", dejar vacio para no cambiar): ");
                        String newTipoDocStr = scanner.nextLine();
                        byte newTipoDoc = (newTipoDocStr.isEmpty() ? miembroAEditar.getTipoDocumento()
                                : Byte.parseByte(newTipoDocStr));
                        System.out.print("Nuevo No. Doc (actual: " + miembroAEditar.getNumeroDocumento()
                                + ", dejar vacio para no cambiar): ");
                        String newNoDocStr = scanner.nextLine();
                        long newNoDoc = (newNoDocStr.isEmpty() ? miembroAEditar.getNumeroDocumento()
                                : Long.parseLong(newNoDocStr));
                        System.out.print("Nuevo Rol (actual: " + miembroAEditar.getRol()
                                + ", 0:Admin, 1:Estudiante, 2:Profesor, 3:Admin Uni, dejar vacio para no cambiar): ");
                        String newRolStr = scanner.nextLine();
                        byte newRol = (newRolStr.isEmpty() ? miembroAEditar.getRol() : Byte.parseByte(newRolStr));
                        System.out.print("Nuevo Nombre (actual: " + miembroAEditar.getNombre()
                                + ", dejar vacio para no cambiar): ");
                        String newNombre = scanner.nextLine();
                        if (newNombre.isEmpty())
                            newNombre = miembroAEditar.getNombre();
                        System.out.print("Nuevo Apellido (actual: " + miembroAEditar.getApellido()
                                + ", dejar vacio para no cambiar): ");
                        String newApellido = scanner.nextLine();
                        if (newApellido.isEmpty())
                            newApellido = miembroAEditar.getApellido();
                        System.out.print("Nuevo Usuario (actual: " + miembroAEditar.getUsuario()
                                + ", dejar vacio para no cambiar): ");
                        String newUsuario = scanner.nextLine();
                        if (newUsuario.isEmpty())
                            newUsuario = miembroAEditar.getUsuario();
                        System.out.print("Nueva Contrasena (dejar vacio para no cambiar): ");
                        String newContrasena = scanner.nextLine();

                        double nuevaDeuda = 0.0;
                        String nuevaSedeUni = "";
                        String nuevaCarrera = "";

                        if (newRol != 0) {
                            if (miembroAEditar instanceof Usuario) {
                                Usuario currentUsuario = (Usuario) miembroAEditar;
                                System.out.print(
                                        "Nueva Deuda (actual: " + String.format("%.2f", currentUsuario.getDeuda())
                                                + ", dejar vacio para no cambiar): ");
                                String newDeudaStr = scanner.nextLine();
                                nuevaDeuda = (newDeudaStr.isEmpty() ? currentUsuario.getDeuda()
                                        : Double.parseDouble(newDeudaStr));
                                System.out.print("Nueva Sede Universidad (actual: "
                                        + currentUsuario.getSedeUniversidad() + ", dejar vacio para no cambiar): ");
                                String newSedeUniStr = scanner.nextLine();
                                nuevaSedeUni = (newSedeUniStr.isEmpty() ? currentUsuario.getSedeUniversidad()
                                        : newSedeUniStr);
                                System.out.print("Nueva Carrera (actual: " + currentUsuario.getCarrera()
                                        + ", dejar vacio para no cambiar): ");
                                String newCarreraStr = scanner.nextLine();
                                nuevaCarrera = (newCarreraStr.isEmpty() ? currentUsuario.getCarrera() : newCarreraStr);
                            } else {
                                System.out.println(
                                        "Este miembro pasara a ser un Usuario. Por favor, ingrese sus datos especificos:");
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
                                    "Este miembro es o seguira siendo un Administrador; los campos de deuda, sede y carrera no aplican.");
                        }

                        System.out.println(admin.editarUsuario(userEdit, newTipoDoc, newNoDoc, newRol, newNombre,
                                newApellido, newUsuario,
                                newContrasena, nuevaDeuda, nuevaSedeUni, nuevaCarrera));
                        break;
                    case 3:
                        System.out.print("Usuario a eliminar: ");
                        String userDel = scanner.nextLine();
                        System.out.println(admin.eliminarUsuario(userDel));
                        break;
                    case 0:
                        break;
                    default:
                        System.out.println("Opcion no valida.");
                }
            } catch (InputMismatchException | NumberFormatException e) {
                System.out.println("Entrada invalida. Asegurese de ingresar el tipo de dato correcto.");
                scanner.nextLine();
                opcion = -1;
            }
        } while (opcion != 0);
    }

    private static void menuAdminBibliotecas(Admin admin) {
        int opcion;
        do {
            System.out.println("\n--- ADMINISTRAR BIBLIOTECAS ---");
            System.out.println("1. Agregar Nueva Biblioteca");
            System.out.println("2. Editar Biblioteca Existente");
            System.out.println("3. Eliminar Biblioteca");
            System.out.println("4. Ver Todas las Bibliotecas");
            System.out.println("0. Volver al Menu Principal");
            System.out.print("Seleccione una opcion: ");

            try {
                opcion = scanner.nextInt();
                scanner.nextLine(); // Consumir salto de linea

                switch (opcion) {
                    case 1:
                        System.out.print("ID de la nueva biblioteca: ");
                        int newId = scanner.nextInt();
                        scanner.nextLine();
                        System.out.print("Sede de la nueva biblioteca: ");
                        String newSede = scanner.nextLine();
                        System.out.print("Nombre de la nueva biblioteca: ");
                        String newName = scanner.nextLine();
                        System.out.println(admin.agregarBiblioteca(newId, newSede, newName));
                        break;
                    case 2:
                        System.out.print("ID de la biblioteca a editar: ");
                        int editId = scanner.nextInt();
                        scanner.nextLine();
                        Biblioteca bibAEditar = Biblioteca.encontrarBibliotecaPorId(editId);
                        if (bibAEditar == null) {
                            System.out.println("Biblioteca no encontrada.");
                            break;
                        }
                        System.out.print(
                                "Nueva Sede (actual: " + bibAEditar.getSede() + ", dejar vacio para no cambiar): ");
                        String newSedeEdit = scanner.nextLine();
                        if (newSedeEdit.isEmpty())
                            newSedeEdit = bibAEditar.getSede();
                        System.out.print("Nuevo Nombre (actual: " + bibAEditar.getNombreBiblioteca()
                                + ", dejar vacio para no cambiar): ");
                        String newNameEdit = scanner.nextLine();
                        if (newNameEdit.isEmpty())
                            newNameEdit = bibAEditar.getNombreBiblioteca();
                        System.out.println(admin.editarBiblioteca(editId, newSedeEdit, newNameEdit));
                        break;
                    case 3:
                        System.out.print("ID de la biblioteca a eliminar: ");
                        int delId = scanner.nextInt();
                        scanner.nextLine();
                        System.out.println(admin.eliminarBiblioteca(delId));
                        break;
                    case 4:
                        System.out.println("\n--- LISTADO DE BIBLIOTECAS ---");
                        List<Biblioteca> todasBib = Biblioteca.cargarTodasLasBibliotecas();
                        if (todasBib.isEmpty()) {
                            System.out.println("No hay bibliotecas registradas.");
                        } else {
                            todasBib.forEach(System.out::println);
                        }
                        System.out.println("------------------------------\n");
                        break;
                    case 0:
                        break;
                    default:
                        System.out.println("Opcion no valida.");
                }
            } catch (InputMismatchException | NumberFormatException e) {
                System.out.println("Entrada invalida. Asegurese de ingresar el tipo de dato correcto.");
                scanner.nextLine();
                opcion = -1;
            }
        } while (opcion != 0);
    }

    private static void cambiarInfoPersonalAdmin(Admin admin) {
        System.out.println("\n--- CAMBIAR INFORMACION PERSONAL DE ADMIN ---");
        System.out.print("Nuevo Tipo Doc (actual: " + admin.getTipoDocumento() + ", dejar vacio para no cambiar): ");
        String newTipoDocStr = scanner.nextLine();
        byte newTipoDoc = (newTipoDocStr.isEmpty() ? admin.getTipoDocumento() : Byte.parseByte(newTipoDocStr));
        System.out.print("Nuevo Nombre (actual: " + admin.getNombre() + ", dejar vacio para no cambiar): ");
        String newNombre = scanner.nextLine();
        if (newNombre.isEmpty())
            newNombre = admin.getNombre();
        System.out.print("Nuevo Apellido (actual: " + admin.getApellido() + ", dejar vacio para no cambiar): ");
        String newApellido = scanner.nextLine();
        if (newApellido.isEmpty())
            newApellido = admin.getApellido();

        admin.cambiarInfoPersonal(newTipoDoc, newNombre, newApellido);
        System.out.println("Informacion personal actualizada con exito.");
    }

    private static void cambiarContrasenaAdmin(Admin admin) {
        System.out.println("\n--- CAMBIAR CONTRASENA DE ADMIN ---");
        System.out.print("Ingrese la nueva contrasena: ");
        String newPass = scanner.nextLine();
        admin.cambiarContrasena(newPass);
        System.out.println("Contrasena actualizada con exito.");
    }

    private static void mostrarMenuUsuario() {
        Usuario usuario = (Usuario) miembroActual;
        int opcion;
        do {
            System.out.println("\n--- MENU USUARIO (" + usuario.getNombre() + ") ---");
            System.out.println("Deuda Actual: " + String.format("%.2f", usuario.getDeuda()) + " pesos");
            System.out.println("1. Buscar Libro");
            System.out.println("2. Ver Libros Disponibles en " + bibliotecaSeleccionada.getNombreBiblioteca());
            System.out.println("3. Prestar Libro");
            System.out.println("4. Devolver Libro");
            System.out.println("5. Ver Mis Libros Prestados");
            System.out.println("6. Cambiar informacion de contacto");
            System.out.println("7. Cambiar Contrasena");
            System.out.println("0. Cerrar Sesion");
            System.out.print("Seleccione una opcion: ");

            try {
                opcion = scanner.nextInt();
                scanner.nextLine();

                switch (opcion) {
                    case 1:
                        System.out.print("Ingrese el termino de busqueda: ");
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
                        System.out.print("Ingrese el codigo del libro a prestar: ");
                        String codPrestamo = scanner.nextLine();
                        System.out.println(usuario.solicitarPrestamo(codPrestamo, bibliotecaSeleccionada));
                        break;
                    case 4:
                        System.out.print("Ingrese el codigo del libro a devolver: ");
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
                        System.out.println("Cerrando sesion de usuario.");
                        break;
                    default:
                        System.out.println("Opcion no valida. Intente de nuevo.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Entrada invalida. Por favor, ingrese un numero.");
                scanner.nextLine();
                opcion = -1;
            }
        } while (opcion != 0);
    }

    private static void cambiarInfoPersonalUsuario(Usuario usuario) {
        System.out.println("\n--- CAMBIAR INFORMACION PERSONAL DE USUARIO ---");
        System.out.print("Nuevo Tipo Doc (actual: " + usuario.getTipoDocumento() + ", dejar vacio para no cambiar): ");
        String newTipoDocStr = scanner.nextLine();
        byte newTipoDoc = (newTipoDocStr.isEmpty() ? usuario.getTipoDocumento() : Byte.parseByte(newTipoDocStr));
        System.out.print("Nuevo Nombre (actual: " + usuario.getNombre() + ", dejar vacio para no cambiar): ");
        String newNombre = scanner.nextLine();
        if (newNombre.isEmpty())
            newNombre = usuario.getNombre();
        System.out.print("Nuevo Apellido (actual: " + usuario.getApellido() + ", dejar vacio para no cambiar): ");
        String newApellido = scanner.nextLine();
        if (newApellido.isEmpty())
            newApellido = usuario.getApellido();
        System.out.print(
                "Nueva Sede Universidad (actual: " + usuario.getSedeUniversidad() + ", dejar vacio para no cambiar): ");
        String newSedeUni = scanner.nextLine();
        if (newSedeUni.isEmpty())
            newSedeUni = usuario.getSedeUniversidad();
        System.out.print("Nueva Carrera (actual: " + usuario.getCarrera() + ", dejar vacio para no cambiar): ");
        String newCarrera = scanner.nextLine();
        if (newCarrera.isEmpty())
            newCarrera = usuario.getCarrera();

        usuario.cambiarInfoPersonal(newTipoDoc, newNombre, newApellido, newSedeUni, newCarrera);
        System.out.println("Informacion personal actualizada con exito.");
    }

    private static void cambiarContrasenaUsuario(Usuario usuario) {
        System.out.println("\n--- CAMBIAR CONTRASENA DE USUARIO ---");
        System.out.print("Ingrese la nueva contrasena: ");
        String newPass = scanner.nextLine();
        usuario.cambiarContrasena(newPass);
        System.out.println("Contrasena actualizada con exito.");
    }

    // --- SECCION DE TESTS AUTOMATICOS ---
    private static void ejecutarTestsAutomaticos() {
        pausar("Iniciando tests automaticos...");

        // Usamos la primera biblioteca disponible para los tests
        List<Biblioteca> bibliotecas = Biblioteca.cargarTodasLasBibliotecas();
        if (bibliotecas.isEmpty()) {
            System.err.println("No hay bibliotecas para ejecutar tests. Asegurese que Bibliotecas.txt tiene datos.");
            return;
        }
        Biblioteca testBiblioteca = bibliotecas.get(0);
        System.out.println("Usando Biblioteca para tests: " + testBiblioteca.getNombreBiblioteca() + " (ID: "
                + testBiblioteca.getId() + ")");

        // Encontrar un admin y un usuario para los tests
        Admin adminTest = null;
        Usuario userEstudianteTest = null;
        for (Miembro m : Miembro.cargarTodosLosMiembros()) {
            if (m instanceof Admin) {
                adminTest = (Admin) m;
            } else if (m instanceof Usuario && m.getRol() == 1) { // Estudiante
                userEstudianteTest = (Usuario) m;
            }
            if (adminTest != null && userEstudianteTest != null)
                break;
        }

        if (adminTest == null) {
            System.err.println("ERROR: No se encontro un administrador para ejecutar los tests.");
            return;
        }
        if (userEstudianteTest == null) {
            System.err.println("ERROR: No se encontro un usuario estudiante para ejecutar los tests.");
            return;
        }

        System.out.println("Admin para tests: " + adminTest.getUsuario());
        System.out.println("Usuario Estudiante para tests: " + userEstudianteTest.getUsuario());
        pausar("Preparacion de elementos para tests completada.");

        // --- Tests del Administrador ---
        System.out.println("\n--- TESTS DE ADMINISTRADOR ---");

        // Test 1: Agregar una nueva biblioteca
        System.out.println("\n--- Test Admin: Agregar Biblioteca ---");
        System.out.println(adminTest.agregarBiblioteca(3, "Cali", "Biblioteca Sede Oeste"));
        pausar("Verificando...");
        System.out.println("Bibliotecas actuales:");
        Biblioteca.cargarTodasLasBibliotecas().forEach(System.out::println);

        // Test 2: Agregar Libro
        System.out.println("\n--- Test Admin: Agregar Libro ---");
        String codLibro1 = Libro.generarSiguienteCodigoLibro(testBiblioteca.getId(), testBiblioteca.getSede());
        System.out.println(adminTest.agregarLibro("El senor de los anillos", "J.R.R. Tolkien", codLibro1, 3,
                testBiblioteca.getId(), testBiblioteca.getSede()));
        String codLibro2 = Libro.generarSiguienteCodigoLibro(testBiblioteca.getId(), testBiblioteca.getSede());
        System.out.println(adminTest.agregarLibro("Cien Anos de Soledad", "Gabriel Garcia Marquez", codLibro2, 2,
                testBiblioteca.getId(), testBiblioteca.getSede()));
        pausar("Verificando libros agregados...");
        adminTest.verLibro(codLibro1);
        adminTest.verLibro(codLibro2);

        // Test 3: Editar Libro
        System.out.println("\n--- Test Admin: Editar Libro ---");
        System.out.println(adminTest.editarLibro(codLibro1, "El senor de los anillos (Edicion Especial)",
                "J.R.R. Tolkien", 4, testBiblioteca.getId(), testBiblioteca.getSede()));
        pausar("Verificando libro editado...");
        adminTest.verLibro(codLibro1);

        // Test 4: Crear Usuario (otro estudiante)
        System.out.println("\n--- Test Admin: Crear Usuario ---");
        String userEstudiante2 = "estudTest" + stringAleatorio(3);
        String passEstudiante2 = stringAleatorio(6);
        System.out.println(adminTest.crearUsuario((byte) 1, 1122334455L, (byte) 1, "Ana", "Gomez", userEstudiante2,
                passEstudiante2, 0.0, "Medellin", "Ingenieria"));
        pausar("Verificando usuario creado...");
        adminTest.verTodosUsuarios();

        // Test 5: Editar Usuario
        System.out.println("\n--- Test Admin: Editar Usuario ---");
        System.out.println(adminTest.editarUsuario(userEstudiante2, (byte) 1, 1122334455L, (byte) 1, "Maria", "Gomez",
                userEstudiante2, "nuevaPass123", 0.0, "Medellin", "Arquitectura"));
        pausar("Verificando usuario editado...");
        System.out.println(Miembro.encontrarMiembroPorUsuario(userEstudiante2));

        // Test 6: Eliminar Unidades de Libro
        System.out.println("\n--- Test Admin: Eliminar Unidades de Libro ---");
        System.out.println(adminTest.eliminarLibro(codLibro2, 1));
        pausar("Verificando unidades restantes...");
        adminTest.verLibro(codLibro2);

        // Test 7: Eliminar Libro Completo (sin prestamos)
        Libro libroTemp = new Libro("Libro Temporal", "Autor Temp",
                Libro.generarSiguienteCodigoLibro(testBiblioteca.getId(), testBiblioteca.getSede()), 1,
                testBiblioteca.getId(), testBiblioteca.getSede());
        libroTemp.guardar();
        pausar("Libro temporal creado para eliminacion: " + libroTemp.getCodigo());
        System.out.println("\n--- Test Admin: Eliminar Libro (completo si no esta prestado) ---");
        System.out.println(adminTest.eliminarLibro(libroTemp.getCodigo(), 1));
        pausar("Verificando que el libro temporal ya no exista.");
        if (Libro.encontrarLibroPorCodigo(libroTemp.getCodigo()) == null) {
            System.out.println("Exito: El libro temporal fue eliminado.");
        } else {
            System.err.println("Error: El libro temporal no fue eliminado.");
        }

        // Test 8: Intento de eliminar usuario con libros prestados (debe fallar)
        System.out.println("\n--- Test Admin: Eliminar Usuario con libros prestados (esperado que falle) ---");
        // Primero, el usuario pide un libro
        System.out.println(userEstudianteTest.solicitarPrestamo(codLibro1, testBiblioteca));
        pausar("Intentando eliminar usuario con libro prestado...");
        System.out.println(adminTest.eliminarUsuario(userEstudianteTest.getUsuario()));
        if (Miembro.encontrarMiembroPorUsuario(userEstudianteTest.getUsuario()) != null) {
            System.out.println("Exito: Usuario no eliminado porque tiene libros prestados.");
        } else {
            System.err.println("Error: Usuario eliminado a pesar de tener libros prestados.");
        }

        // Test 9: Intento de eliminar usuario con deuda (debe fallar)
        System.out.println("\n--- Test Admin: Eliminar Usuario con deuda (esperado que falle) ---");
        // Simulamos una deuda
        userEstudianteTest.setDeuda(100.0);
        userEstudianteTest.guardar();
        pausar("Simulando deuda y intentando eliminar usuario...");
        System.out.println(adminTest.eliminarUsuario(userEstudianteTest.getUsuario()));
        if (Miembro.encontrarMiembroPorUsuario(userEstudianteTest.getUsuario()) != null) {
            System.out.println("Exito: Usuario no eliminado porque tiene deuda.");
        } else {
            System.err.println("Error: Usuario eliminado a pesar de tener deuda.");
        }

        // Eliminar deuda y devolver libro para poder completar el test de eliminacion
        pausar("Preparando usuario para eliminacion: saldando deuda y devolviendo libro...");
        userEstudianteTest.setDeuda(0.0);
        userEstudianteTest.guardar();
        System.out.println(userEstudianteTest.realizarDevolucion(codLibro1, testBiblioteca));

        // Test 10: Eliminar Usuario exitosamente
        System.out.println("\n--- Test Admin: Eliminar Usuario exitosamente ---");
        System.out.println(adminTest.eliminarUsuario(userEstudiante2));
        pausar("Verificando que el usuario haya sido eliminado...");
        if (Miembro.encontrarMiembroPorUsuario(userEstudiante2) == null) {
            System.out.println("Exito: Usuario '" + userEstudiante2 + "' eliminado correctamente.");
        } else {
            System.err.println("Error: Usuario '" + userEstudiante2 + "' no fue eliminado.");
        }

        pausar("Listando todos los usuarios despues de manipulaciones...");
        adminTest.verTodosUsuarios();

        // --- Tests de Usuario ---
        System.out.println("\n--- TESTS DE USUARIO ---");

        // Test 11: Usuario busca libro
        System.out.println("\n--- Test Usuario: Buscar Libro ---");
        pausar("Buscando 'anillos' por nombre...");
        List<Libro> busqueda = testBiblioteca.buscarLibro("anillos", "nombre");
        if (!busqueda.isEmpty()) {
            System.out.println("Exito: Se encontraron libros con 'anillos'.");
            busqueda.forEach(System.out::println);
        } else {
            System.err.println("Error: No se encontraron libros con 'anillos'.");
        }

        // Test 12: Usuario pide prestado un libro (ya lo hizo en Test 8)
        System.out.println("\n--- Test Usuario: Ver mis libros prestados ---");
        userEstudianteTest.verMisLibrosPrestados();

        // Test 13: Prestar multiples libros (limite 5 para estudiante)
        System.out.println("\n--- Test Usuario: Prestar multiples libros (Estudiante) ---");
        System.out.println(userEstudianteTest.solicitarPrestamo(codLibro2, testBiblioteca)); // Ya tiene 1, este seria
                                                                                             // el 2do
        adminTest.agregarLibro("Nuevo Libro", "Autor Anonimo", "3-M-100", 1, testBiblioteca.getId(),
                testBiblioteca.getSede());
        String codLibro3 = "3-M-100"; // Test con un codigo manual
        System.out.println(userEstudianteTest.solicitarPrestamo(codLibro3, testBiblioteca)); // 3er libro
        adminTest.agregarLibro("Cuarto Libro", "Autor Cuatro",
                Libro.generarSiguienteCodigoLibro(testBiblioteca.getId(), testBiblioteca.getSede()), 1,
                testBiblioteca.getId(), testBiblioteca.getSede());
        String codLibro4 = Libro.generarSiguienteCodigoLibro(testBiblioteca.getId(), testBiblioteca.getSede());
        System.out.println(userEstudianteTest.solicitarPrestamo(codLibro4, testBiblioteca)); // 4to libro
        adminTest.agregarLibro("Quinto Libro", "Autor Cinco",
                Libro.generarSiguienteCodigoLibro(testBiblioteca.getId(), testBiblioteca.getSede()), 1,
                testBiblioteca.getId(), testBiblioteca.getSede());
        String codLibro5 = Libro.generarSiguienteCodigoLibro(testBiblioteca.getId(), testBiblioteca.getSede());
        System.out.println(userEstudianteTest.solicitarPrestamo(codLibro5, testBiblioteca)); // 5to libro
        pausar("Intentando prestar un 6to libro (debe fallar para estudiante)...");
        adminTest.agregarLibro("Sexto Libro", "Autor Seis",
                Libro.generarSiguienteCodigoLibro(testBiblioteca.getId(), testBiblioteca.getSede()), 1,
                testBiblioteca.getId(), testBiblioteca.getSede());
        String codLibro6 = Libro.generarSiguienteCodigoLibro(testBiblioteca.getId(), testBiblioteca.getSede());
        System.out.println(userEstudianteTest.solicitarPrestamo(codLibro6, testBiblioteca)); // Debe fallar

        pausar("Verificando libros prestados del estudiante...");
        userEstudianteTest.verMisLibrosPrestados();

        // Test 14: Usuario devuelve libro
        System.out.println("\n--- Test Usuario: Devolver Libro ---");
        System.out.println(userEstudianteTest.realizarDevolucion(codLibro2, testBiblioteca));
        pausar("Verificando libros prestados despues de la devolucion...");
        userEstudianteTest.verMisLibrosPrestados();

        // Test 15: Usuario cambia informacion personal
        System.out.println("\n--- Test Usuario: Cambiar informacion personal ---");
        userEstudianteTest.cambiarInfoPersonal((byte) 1, "Carlos", "Perez", "Bogota", "Historia");
        pausar("Verificando informacion personal actualizada (se reimprime el usuario)...");
        System.out.println(Miembro.encontrarMiembroPorUsuario(userEstudianteTest.getUsuario()));

        // Test 16: Usuario cambia contrasena
        System.out.println("\n--- Test Usuario: Cambiar contrasena ---");
        userEstudianteTest.cambiarContrasena("nuevaContrasenaUser");
        pausar("Contrasena cambiada (no visible por seguridad). Intentando login con la nueva contrasena para verificar.");
        // Re-loguear el usuario para verificar el cambio de contrasena
        Miembro loginVerification = new Miembro();
        if (!loginVerification.login(userEstudianteTest.getUsuario(), "nuevaContrasenaUser").isEmpty()) {
            System.out.println("Exito: Login con la nueva contrasena fue exitoso.");
        } else {
            System.err.println("Error: Login con la nueva contrasena fallo.");
        }
    }

    private static void pausar(String mensaje) {
        System.out.println("\n" + mensaje + " (Presione Enter para continuar...)");
        scanner.nextLine();
    }
}
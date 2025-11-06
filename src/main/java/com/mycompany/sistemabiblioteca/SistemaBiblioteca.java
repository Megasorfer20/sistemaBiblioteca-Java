/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.sistemabiblioteca; // Carpeta donde está organizado nuestro código.

import java.io.IOException; // Para manejar errores al trabajar con archivos.
import java.nio.charset.StandardCharsets; // Para especificar la codificación de texto (UTF-8).
import java.nio.file.Files; // Para operar con archivos.
import java.nio.file.Path; // Para rutas de archivos.
import java.nio.file.StandardOpenOption; // Opciones para guardar archivos.
import java.security.SecureRandom; // Para generar números aleatorios seguros.
import java.util.ArrayList; // Para listas dinámicas.
import java.util.InputMismatchException; // Para manejar errores si el usuario no escribe un número.
import java.util.List; // Para usar listas.
import java.util.Scanner; // Para leer la entrada del usuario.
import java.util.stream.Collectors; // Para operar con listas de forma avanzada.

// Esta es la clase principal que inicia y controla todo el sistema de la biblioteca.
// Maneja el login, los menús y ejecuta pruebas automáticas.
public class SistemaBiblioteca {

    private static Miembro miembroActual = null; // Guarda el usuario que ha iniciado sesión.
    private static Biblioteca bibliotecaSeleccionada = null; // Guarda la biblioteca con la que se está trabajando.
    private static Scanner scanner = new Scanner(System.in); // Herramienta para leer lo que escribe el usuario.

    // Caracteres permitidos para generar nombres de usuario y contraseñas
    // aleatorias.
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom(); // Generador de números aleatorios seguro.

    // Genera una cadena de texto aleatoria de una longitud específica.
    private static String stringAleatorio(int length) {
        StringBuilder sb = new StringBuilder(length); // Constructor de cadenas.
        for (int i = 0; i < length; i++) { // Repite 'length' veces.
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length()))); // Añade un caracter aleatorio.
        }
        return sb.toString(); // Devuelve la cadena final.
    }

    // Este es el método que se ejecuta cuando el programa inicia.
    public static void main(String[] args) {
        System.out.println("¡Bienvenido al Sistema de Gestion de Bibliotecas!"); // Mensaje de bienvenida.

        System.out.println("\n--- INICIANDO CONFIGURACION Y PRUEBAS AUTOMATICAS ---");
        limpiarArchivosDeDatos(); // Borra los datos antiguos para empezar de cero en cada ejecución.

        setupInitialData(); // Configura datos iniciales: bibliotecas, un admin y usuarios de prueba.
        ejecutarTestsAutomaticos(); // Ejecuta una serie de pruebas para verificar el sistema.
        System.out.println("--- PRUEBAS AUTOMATICAS COMPLETADAS ---\n");

        // Carga las bibliotecas disponibles.
        List<Biblioteca> bibliotecasDisponibles = Biblioteca.cargarTodasLasBibliotecas();
        if (bibliotecasDisponibles.isEmpty()) { // Si no hay bibliotecas configuradas.
            System.err.println(
                    "Error: No se encontraron bibliotecas configuradas. Por favor, asegurese de tener 'Bibliotecas.txt' con datos.");
            return; // El programa termina.
        }

        seleccionarBibliotecaInicial(bibliotecasDisponibles); // Pide al usuario que elija una biblioteca.

        if (bibliotecaSeleccionada == null) { // Si no se pudo seleccionar una biblioteca.
            System.out.println("No se pudo seleccionar una biblioteca. Saliendo del sistema.");
            scanner.close(); // Cierra el lector de teclado.
            return; // El programa termina.
        }

        // --- Información de login manual para pruebas ---
        // Busca el admin y el usuario de prueba para mostrar sus credenciales.
        Admin adminParaLogin = (Admin) Miembro.encontrarMiembroPorUsuario(
                Miembro.cargarTodosLosMiembros().stream().filter(m -> m.getRol() == 0).findFirst().get().getUsuario());
        Usuario userParaLogin = (Usuario) Miembro.encontrarMiembroPorUsuario("user3975");

        System.out.println("\n--- DATOS PARA LOGIN MANUAL DESPUES DE LOS TESTS ---");
        System.out.println("Admin -> Usuario: " + adminParaLogin.getUsuario()
                + ", Contrasena: <la que se mostro al inicio o cualquiera si ya la cambio>");
        System.out.println("Usuario Estudiante Original (user3975) -> Usuario: " + userParaLogin.getUsuario()
                + ", **Contrasena: nuevaContrasenaUser** (cambio durante los tests)");
        System.out.println("----------------------------------------------");

        login(); // Inicia el proceso de login.

        if (miembroActual != null) { // Si el login fue exitoso.
            if (miembroActual.getRol() == 0) { // Si es Administrador.
                mostrarMenuAdmin(); // Muestra el menú de Admin.
            } else { // Si es Usuario.
                mostrarMenuUsuario(); // Muestra el menú de Usuario.
            }
        }

        System.out.println("Gracias por usar el Sistema de Bibliotecas. ¡Hasta pronto!"); // Mensaje de despedida.
        scanner.close(); // Cierra el lector de teclado.
    }

    // Borra todos los archivos de datos para que cada ejecución del programa
    // empiece limpia.
    private static void limpiarArchivosDeDatos() {
        System.out.println("Limpiando archivos de datos para un inicio limpio...");
        // Obtiene las rutas de todos los archivos de datos.
        Path miembrosPath = PathManager.resolverRutaArchivo("Miembros.txt");
        Path librosPath = PathManager.resolverRutaArchivo("Libros.txt");
        Path prestamosPath = PathManager.resolverRutaArchivo("LibroPrestado.txt");
        Path bibliotecasPath = PathManager.resolverRutaArchivo("Bibliotecas.txt");

        try {
            Files.deleteIfExists(miembrosPath); // Borra el archivo de miembros si existe.
            Files.deleteIfExists(librosPath); // Borra el archivo de libros si existe.
            Files.deleteIfExists(prestamosPath); // Borra el archivo de préstamos si existe.
            Files.deleteIfExists(bibliotecasPath); // Borra el archivo de bibliotecas si existe.
            System.out.println("Archivos de datos eliminados correctamente.");
        } catch (IOException e) { // Si ocurre un error al borrar.
            System.err.println("Error al limpiar archivos de datos: " + e.getMessage());
        }
    }

    // Configura los datos iniciales del sistema (bibliotecas, admin, usuarios de
    // ejemplo).
    private static void setupInitialData() {
        System.out.println("\n--- CONFIGURANDO DATOS INICIALES ---");

        // 1. Crea "Bibliotecas.txt" con datos de ejemplo si no existe.
        Path bibliotecasPath = Biblioteca.resolverRutaBibliotecas();
        if (!Files.exists(bibliotecasPath) || Biblioteca.cargarTodasLasBibliotecas().isEmpty()) {
            System.out.println("Creando 'Bibliotecas.txt' con datos de ejemplo...");
            try {
                List<String> defaultBibliotecas = new ArrayList<>();
                defaultBibliotecas.add("1\\Medellin\\Biblioteca Central");
                defaultBibliotecas.add("2\\Bogota\\Biblioteca Norte");
                Files.write(bibliotecasPath, defaultBibliotecas, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING); // Escribe las bibliotecas en el archivo.
                System.out.println("Bibliotecas de ejemplo creadas.");
            } catch (IOException e) {
                System.err.println("Error al crear Bibliotecas.txt: " + e.getMessage());
            }
        } else {
            System.out.println("Bibliotecas existentes cargadas.");
        }

        // 2. Crea un administrador si no existe ninguno.
        Admin adminExistente = null;
        for (Miembro m : Miembro.cargarTodosLosMiembros()) {
            if (m instanceof Admin) {
                adminExistente = (Admin) m;
                break;
            }
        }

        if (adminExistente == null) {
            System.out.println("Creando un nuevo administrador...");
            String adminUser = "admin_" + stringAleatorio(4); // Genera usuario aleatorio.
            String adminPass = stringAleatorio(10); // Genera contraseña aleatoria.
            Admin nuevoAdmin = new Admin((byte) 1, 1000000000L + RANDOM.nextInt(100000000), (byte) 0, "Sys", "Admin",
                    adminUser, adminPass); // Crea el objeto Admin.
            nuevoAdmin.guardar(); // Guarda el admin.
            System.out.println("Admin creado (¡Use estas credenciales para iniciar sesion!):");
            System.out.println("  Usuario: " + adminUser);
            System.out.println("  Contrasena: " + adminPass); // Muestra credenciales.
        } else {
            System.out.println("Administrador existente encontrado: " + adminExistente.getUsuario());
        }

        // 3. Crea 3 usuarios aleatorios si hay menos de 3 (sin contar al admin).
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

                // Asegura que "user3975" sea el estudiante de prueba si es el primero en
                // crearse.
                if (i == 0 && userEstudianteTestUserName == null) {
                    user = "user3975";
                    pass = "pHuBTzFH";
                    rol = 1; // Estudiante.
                    name = "TuZSg";
                    lastName = "M5qZpR2";
                    sedeUni = "Bogota";
                    carreraUser = "Ingenieria de Sistemas";
                }
                Usuario nuevoUsuario = new Usuario(docType, docNum, rol, name, lastName, user, pass, 0.0, sedeUni,
                        carreraUser); // Crea el objeto Usuario.
                nuevoUsuario.guardar(); // Guarda el usuario.
                System.out.println("  Usuario creado: " + user + " (Rol: " + roles[rol - 1] + ", Pass: " + pass + ")");
            }
        } else {
            System.out.println("Suficientes usuarios existentes.");
        }
        System.out.println("--- CONFIGURACION INICIAL COMPLETA ---\n");
    }

    // Permite al usuario elegir una biblioteca para la sesión actual.
    private static void seleccionarBibliotecaInicial(List<Biblioteca> bibliotecasDisponibles) {
        System.out.println("\n--- SELECCION DE BIBLIOTECA ---");
        System.out.println("Seleccione una biblioteca para iniciar:");
        for (int i = 0; i < bibliotecasDisponibles.size(); i++) { // Muestra las bibliotecas con un número.
            System.out.println((i + 1) + ". " + bibliotecasDisponibles.get(i).getNombreBiblioteca() + " ("
                    + bibliotecasDisponibles.get(i).getSede() + ")");
        }
        int opcionBib;
        while (true) { // Bucle hasta que elija una opción válida.
            System.out.print("Ingrese el numero de la biblioteca: ");
            try {
                opcionBib = scanner.nextInt(); // Lee la opción.
                scanner.nextLine(); // Consume el Enter.
                if (opcionBib > 0 && opcionBib <= bibliotecasDisponibles.size()) { // Si la opción es válida.
                    bibliotecaSeleccionada = bibliotecasDisponibles.get(opcionBib - 1); // Guarda la biblioteca elegida.
                    System.out
                            .println("Ha seleccionado la biblioteca: " + bibliotecaSeleccionada.getNombreBiblioteca());
                    break; // Sale del bucle.
                } else {
                    System.out.println("Opcion no valida. Intente de nuevo.");
                }
            } catch (InputMismatchException e) { // Si no ingresa un número.
                System.out.println("Entrada invalida. Por favor, ingrese un numero.");
                scanner.nextLine(); // Limpia la entrada.
            }
        }
    }

    // Maneja el proceso de inicio de sesión.
    private static void login() {
        System.out.println("\n--- INICIO DE SESION ---");
        System.out.print("Usuario: ");
        String usuario = scanner.nextLine();
        System.out.print("Contrasena: ");
        String contrasena = scanner.nextLine();

        Miembro tempMiembro = new Miembro(); // Crea un Miembro temporal para llamar al método de login.
        String lineaMiembro = tempMiembro.login(usuario, contrasena); // Intenta hacer login.

        if (!lineaMiembro.isEmpty()) { // Si el login fue exitoso.
            // Carga el objeto Miembro completo (Admin o Usuario) para tener todos sus datos
            // específicos.
            Miembro foundMiembro = Miembro.encontrarMiembroPorUsuario(usuario);
            if (foundMiembro != null) {
                miembroActual = foundMiembro; // Establece el miembro actual.
            } else {
                System.err.println(
                        "Error interno: Miembro encontrado por login pero no por findMemberByUsername. Fallo la carga del objeto completo.");
                miembroActual = null;
                return;
            }
            System.out.println("Inicio de sesion exitoso. ¡Bienvenido, " + miembroActual.getNombre() + "!");
        } else {
            System.out.println("Usuario o contrasena incorrectos."); // Si el login falló.
            miembroActual = null;
        }
    }

    // --- Implementacion de Menus (Admin y Usuario) ---
    // Muestra el menú principal para los administradores.
    private static void mostrarMenuAdmin() {
        Admin admin = (Admin) miembroActual; // Convierte el miembro actual a Admin.
        int opcion;
        do { // Bucle del menú.
            System.out.println("\n--- MENU ADMINISTRADOR ---");
            System.out.println("1. Administrar Libros");
            System.out.println("2. Administrar Usuarios");
            System.out.println("3. Administrar Bibliotecas");
            System.out.println("4. Ver Libros Prestados (Todos)");
            System.out.println("5. Ver Libros Prestados por un Usuario especifico");
            System.out.println("6. Ver Todos los Libros");
            System.out.println("7. Ver un Libro Especifico");
            System.out.println("8. Ver Todos los Usuarios");
            System.out.println("9. Cambiar informacion personal de Admin");
            System.out.println("10. Cambiar contrasena de Admin");
            System.out.println("0. Cerrar Sesion");
            System.out.print("Seleccione una opcion: ");

            try {
                opcion = scanner.nextInt(); // Lee la opción.
                scanner.nextLine(); // Consume el Enter.

                switch (opcion) { // Ejecuta la acción según la opción.
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
            } catch (InputMismatchException e) { // Si no ingresa un número.
                System.out.println("Entrada invalida. Por favor, ingrese un numero.");
                scanner.nextLine();
                opcion = -1;
            }
        } while (opcion != 0); // Continúa hasta que elija 0.
    }

    // Muestra el submenú para que el admin gestione libros.
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
                    case 1: // Agregar libro.
                        System.out.print("Nombre: ");
                        String nombre = scanner.nextLine();
                        System.out.print("Autor: ");
                        String autor = scanner.nextLine();
                        System.out.print("Codigo (ID unico, teclear 'auto' para generar): ");
                        String codigo = scanner.nextLine();
                        System.out.print("Unidades Totales: ");
                        int unidades = scanner.nextInt();
                        scanner.nextLine();
                        System.out.println(admin.agregarLibro(nombre, autor, codigo, unidades,
                                bibliotecaSeleccionada.getId(), bibliotecaSeleccionada.getSede()));
                        break;
                    case 2: // Editar libro.
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
                                libroAEditar.getIdBiblioteca(), libroAEditar.getSedeBiblioteca()));
                        break;
                    case 3: // Eliminar unidades o libro completo.
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
            } catch (InputMismatchException | NumberFormatException e) { // Si hay error de tipo de dato.
                System.out.println("Entrada invalida. Asegurese de ingresar el tipo de dato correcto.");
                scanner.nextLine();
                opcion = -1;
            }
        } while (opcion != 0);
    }

    // Muestra el submenú para que el admin gestione usuarios.
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
                    case 1: // Agregar usuario.
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
                                contrasena, deuda, sedeUni, carrera));
                        break;
                    case 2: // Editar usuario.
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
                        if (newRol != 0) { // Si el rol no es Admin, pide datos de Usuario.
                            if (miembroAEditar instanceof Usuario) { // Si ya era un Usuario.
                                Usuario currentUsuario = (Usuario) miembroAEditar;
                                System.out.print(
                                        "Nueva Deuda (actual: " + String.format("%.2f", currentUsuario.getDeuda())
                                                + ", dejar vacio para no cambiar): ");
                                String newDeudaStr = scanner.nextLine();
                                nuevaDeuda = (newDeudaStr.isEmpty() ? currentUsuario.getDeuda()
                                        : Double.parseDouble(newDeudaStr.replace(',', '.')));
                                System.out.print("Nueva Sede Universidad (actual: "
                                        + currentUsuario.getSedeUniversidad() + ", dejar vacio para no cambiar): ");
                                String newSedeUniStr = scanner.nextLine();
                                nuevaSedeUni = (newSedeUniStr.isEmpty() ? currentUsuario.getSedeUniversidad()
                                        : newSedeUniStr);
                                System.out.print("Nueva Carrera (actual: " + currentUsuario.getCarrera()
                                        + ", dejar vacio para no cambiar): ");
                                String newCarreraStr = scanner.nextLine();
                                nuevaCarrera = (newCarreraStr.isEmpty() ? currentUsuario.getCarrera() : newCarreraStr);
                            } else { // Si era Admin y cambia a Usuario.
                                System.out.println(
                                        "Este miembro pasara a ser un Usuario. Por favor, ingrese sus datos especificos:");
                                System.out.print("Nueva Deuda (0.0 por defecto): ");
                                String newDeudaStr = scanner.nextLine();
                                nuevaDeuda = (newDeudaStr.isEmpty() ? 0.0
                                        : Double.parseDouble(newDeudaStr.replace(',', '.')));
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
                                newApellido, newUsuario, newContrasena, nuevaDeuda, nuevaSedeUni, nuevaCarrera));
                        break;
                    case 3: // Eliminar usuario.
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

    // Muestra el submenú para que el admin gestione bibliotecas.
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
                scanner.nextLine();

                switch (opcion) {
                    case 1: // Agregar biblioteca.
                        System.out.print("ID de la nueva biblioteca: ");
                        int newId = scanner.nextInt();
                        scanner.nextLine();
                        System.out.print("Sede de la nueva biblioteca: ");
                        String newSede = scanner.nextLine();
                        System.out.print("Nombre de la nueva biblioteca: ");
                        String newName = scanner.nextLine();
                        System.out.println(admin.agregarBiblioteca(newId, newSede, newName));
                        break;
                    case 2: // Editar biblioteca.
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
                    case 3: // Eliminar biblioteca.
                        System.out.print("ID de la biblioteca a eliminar: ");
                        int delId = scanner.nextInt();
                        scanner.nextLine();
                        System.out.println(admin.eliminarBiblioteca(delId));
                        break;
                    case 4: // Ver todas las bibliotecas.
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

    // Permite al admin cambiar su información personal.
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

        admin.cambiarInfoPersonal(newTipoDoc, newNombre, newApellido); // Llama al método de Admin.
        System.out.println("Informacion personal actualizada con exito.");
    }

    // Permite al admin cambiar su contraseña.
    private static void cambiarContrasenaAdmin(Admin admin) {
        System.out.println("\n--- CAMBIAR CONTRASENA DE ADMIN ---");
        System.out.print("Ingrese la nueva contrasena: ");
        String newPass = scanner.nextLine();
        admin.cambiarContrasena(newPass); // Llama al método de Admin para cambiar la contraseña.
        System.out.println("Contrasena actualizada con exito.");
    }

    // Muestra todos los libros que tienen unidades disponibles en cualquier
    // biblioteca.
    private static void verTodosLibrosDisponibles() {
        System.out.println("\n--- TODOS LOS LIBROS DISPONIBLES (todas las sedes) ---");
        List<Libro> todosDisponibles = Libro.cargarTodosLosLibros().stream()
                .filter(l -> l.getUnidadesLibres() > 0) // Filtra solo los libros con unidades libres.
                .collect(Collectors.toList());
        if (todosDisponibles.isEmpty()) {
            System.out.println("No hay ningun libro disponible en ninguna biblioteca.");
        } else {
            todosDisponibles.forEach(System.out::println);
        }
        System.out.println("-------------------------------------------------------------------\n");
    }

    // Permite a un usuario pagar parte o toda su deuda.
    private static void pagarDeuda(Usuario usuario) {
        System.out.println("\n--- PAGAR DEUDA ---");
        if (usuario.getDeuda() <= 0) {
            System.out.println("No tienes deuda pendiente.");
            return;
        } // Si no tiene deuda.
        System.out.println("Tu deuda actual es: " + String.format("%.2f", usuario.getDeuda()) + " pesos.");
        System.out.print("¿Cuanto deseas pagar? ");
        try {
            double montoPago = scanner.nextDouble();
            scanner.nextLine(); // Lee el monto.
            if (montoPago <= 0) {
                System.out.println("El monto a pagar debe ser mayor que cero.");
                return;
            }
            if (montoPago > usuario.getDeuda()) { // Si paga de más.
                System.out.println("Advertencia: Estas pagando mas de lo debido. Tu deuda se ajustara a 0.");
                usuario.setDeuda(0.0); // La deuda queda en 0.
            } else {
                usuario.setDeuda(usuario.getDeuda() - montoPago); // Resta el pago a la deuda.
            }
            usuario.guardar(); // Guarda el usuario con la deuda actualizada.
            System.out.println(
                    "Pago realizado. Tu nueva deuda es: " + String.format("%.2f", usuario.getDeuda()) + " pesos.");
        } catch (InputMismatchException e) { // Si no ingresa un número.
            System.out.println("Entrada invalida. Por favor, ingresa un numero valido para el monto.");
            scanner.nextLine();
        }
    }

    // Muestra el menú principal para los usuarios normales.
    private static void mostrarMenuUsuario() {
        Usuario usuario = (Usuario) miembroActual; // Convierte el miembro actual a Usuario.
        int opcion;
        do {
            System.out.println("\n--- MENU USUARIO (" + usuario.getNombre() + ") ---");
            System.out.println("Deuda Actual: " + String.format("%.2f", usuario.getDeuda()) + " pesos");
            System.out.println("1. Buscar Libro (por cualquier criterio)");
            System.out.println("2. Ver Libros Disponibles en MI Sede (" + usuario.getSedeUniversidad() + ")");
            System.out.println("3. Ver TODOS los Libros Disponibles (sin filtro de sede)");
            System.out.println("4. Prestar Libro");
            System.out.println("5. Devolver Libro");
            System.out.println("6. Ver Mis Libros Prestados");
            System.out.println("7. Pagar Deuda");
            System.out.println("8. Cambiar informacion personal");
            System.out.println("9. Cambiar Contrasena");
            System.out.println("0. Cerrar Sesion");
            System.out.print("Seleccione una opcion: ");

            try {
                opcion = scanner.nextInt();
                scanner.nextLine();

                switch (opcion) {
                    case 1: // Buscar Libro.
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
                    case 2: // Ver Libros Disponibles en su Sede.
                        List<Libro> disponiblesSede = bibliotecaSeleccionada
                                .librosDisponibles(usuario.getSedeUniversidad());
                        if (disponiblesSede.isEmpty()) {
                            System.out.println(
                                    "No hay libros disponibles en tu sede (" + usuario.getSedeUniversidad() + ").");
                        } else {
                            System.out.println(
                                    "\n--- LIBROS DISPONIBLES EN TU SEDE (" + usuario.getSedeUniversidad() + ") ---");
                            disponiblesSede.forEach(System.out::println);
                            System.out.println("-------------------------------------------------------------------\n");
                        }
                        break;
                    case 3:
                        verTodosLibrosDisponibles();
                        break; // Ver todos los libros disponibles.
                    case 4: // Prestar Libro.
                        System.out.print("Ingrese el codigo del libro a prestar: ");
                        String codPrestamo = scanner.nextLine();
                        Usuario userReloadedForLoan = (Usuario) Miembro
                                .encontrarMiembroPorUsuario(usuario.getUsuario()); // Recarga el usuario para la deuda
                                                                                   // actualizada.
                        if (userReloadedForLoan != null) {
                            System.out.println(
                                    userReloadedForLoan.solicitarPrestamo(codPrestamo, bibliotecaSeleccionada));
                            miembroActual = userReloadedForLoan;
                        } else {
                            System.err.println(
                                    "Error: No se pudo recargar la informacion del usuario para prestar el libro.");
                        }
                        break;
                    case 5: // Devolver Libro.
                        System.out.print("Ingrese el codigo del libro a devolver: ");
                        String codDevolucion = scanner.nextLine();
                        Usuario userReloadedForReturn = (Usuario) Miembro
                                .encontrarMiembroPorUsuario(usuario.getUsuario()); // Recarga el usuario.
                        if (userReloadedForReturn != null) {
                            System.out.println(
                                    userReloadedForReturn.realizarDevolucion(codDevolucion, bibliotecaSeleccionada));
                            miembroActual = userReloadedForReturn;
                        } else {
                            System.err.println(
                                    "Error: No se pudo recargar la informacion del usuario para devolver el libro.");
                        }
                        break;
                    case 6:
                        usuario.verMisLibrosPrestados();
                        break; // Ver sus libros prestados.
                    case 7:
                        pagarDeuda(usuario);
                        miembroActual = Miembro.encontrarMiembroPorUsuario(usuario.getUsuario());
                        break; // Pagar deuda.
                    case 8:
                        cambiarInfoPersonalUsuario(usuario);
                        miembroActual = Miembro.encontrarMiembroPorUsuario(usuario.getUsuario());
                        break; // Cambiar información personal.
                    case 9:
                        cambiarContrasenaUsuario(usuario);
                        miembroActual = Miembro.encontrarMiembroPorUsuario(usuario.getUsuario());
                        break; // Cambiar contraseña.
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

    // Permite a un usuario cambiar su información personal (nombre, apellido, sede,
    // carrera).
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

        usuario.cambiarInfoPersonal(newTipoDoc, newNombre, newApellido, newSedeUni, newCarrera); // Llama al método de
                                                                                                 // Usuario.
        System.out.println("Informacion personal actualizada con exito.");
    }

    // Permite a un usuario cambiar su contraseña.
    private static void cambiarContrasenaUsuario(Usuario usuario) {
        System.out.println("\n--- CAMBIAR CONTRASENA DE USUARIO ---");
        System.out.print("Ingrese la nueva contrasena: ");
        String newPass = scanner.nextLine();
        usuario.cambiarContrasena(newPass); // Llama al método de Usuario.
        System.out.println("Contrasena actualizada con exito.");
    }

    // Nombre de usuario del estudiante de prueba, usado en los tests.
    private static String userEstudianteTestUserName = null;

    // --- SECCION DE TESTS AUTOMATICOS ---
    // Ejecuta una serie de pruebas para verificar las funcionalidades del sistema.
    // Incluye pausas para revisión manual de los resultados.
    private static void ejecutarTestsAutomaticos() {
        pausar("Iniciando tests automaticos...");

        List<Biblioteca> bibliotecas = Biblioteca.cargarTodasLasBibliotecas();
        if (bibliotecas.isEmpty()) {
            System.err.println("No hay bibliotecas para ejecutar tests. Asegurese que Bibliotecas.txt tiene datos.");
            return;
        }
        Biblioteca testBiblioteca = bibliotecas.get(0); // Usa la primera biblioteca para los tests.
        System.out.println("Usando Biblioteca para tests: " + testBiblioteca.getNombreBiblioteca() + " (ID: "
                + testBiblioteca.getId() + ")");

        Admin adminTest = null;
        Usuario userEstudianteTest = null;
        for (Miembro m : Miembro.cargarTodosLosMiembros()) { // Busca el admin y el usuario de prueba.
            if (m instanceof Admin) {
                adminTest = (Admin) m;
            } else if (m instanceof Usuario && m.getRol() == 1 && m.getUsuario().equals("user3975")) {
                userEstudianteTest = (Usuario) m;
                userEstudianteTestUserName = m.getUsuario();
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

        // Test 1: Agregar una nueva biblioteca.
        System.out.println("\n--- Test Admin: Agregar Biblioteca ---");
        System.out.println(adminTest.agregarBiblioteca(3, "Cali", "Biblioteca Sede Oeste"));
        pausar("Verificando...");
        System.out.println("Bibliotecas actuales:");
        Biblioteca.cargarTodasLasBibliotecas().forEach(System.out::println);

        // Test 2: Agregar Libro.
        System.out.println("\n--- Test Admin: Agregar Libro ---");
        String codLibro1 = procesarResultadoAgregarLibro(adminTest.agregarLibro("El senor de los anillos",
                "J.R.R. Tolkien", "auto", 3, testBiblioteca.getId(), testBiblioteca.getSede()));
        String codLibro2 = procesarResultadoAgregarLibro(adminTest.agregarLibro("Cien Anos de Soledad",
                "Gabriel Garcia Marquez", "auto", 2, testBiblioteca.getId(), testBiblioteca.getSede()));
        pausar("Verificando libros agregados...");
        adminTest.verLibro(codLibro1);
        adminTest.verLibro(codLibro2);

        // Test 3: Editar Libro.
        System.out.println("\n--- Test Admin: Editar Libro ---");
        System.out.println(adminTest.editarLibro(codLibro1, "El senor de los anillos (Edicion Especial)",
                "J.R.R. Tolkien", 4, testBiblioteca.getId(), testBiblioteca.getSede()));
        pausar("Verificando libro editado...");
        adminTest.verLibro(codLibro1);

        // Test 4: Crear Usuario (otro estudiante).
        System.out.println("\n--- Test Admin: Crear Usuario ---");
        String userEstudiante2 = "estudTest" + stringAleatorio(3);
        String passEstudiante2 = stringAleatorio(6);
        System.out.println(adminTest.crearUsuario((byte) 1, 1122334455L, (byte) 1, "Ana", "Gomez", userEstudiante2,
                passEstudiante2, 0.0, "Medellin", "Ingenieria"));
        pausar("Verificando usuario creado...");
        adminTest.verTodosUsuarios();

        // Test 5: Editar Usuario.
        System.out.println("\n--- Test Admin: Editar Usuario ---");
        adminTest = (Admin) Miembro.encontrarMiembroPorUsuario(adminTest.getUsuario()); // Recarga el admin.
        System.out.println(adminTest.editarUsuario(userEstudiante2, (byte) 1, 1122334455L, (byte) 1, "Maria", "Gomez",
                userEstudiante2, "nuevaPass123", 0.0, "Medellin", "Arquitectura"));
        pausar("Verificando usuario editado...");
        System.out.println(Miembro.encontrarMiembroPorUsuario(userEstudiante2));
        userEstudianteTest = (Usuario) Miembro.encontrarMiembroPorUsuario(userEstudianteTest.getUsuario()); // Recarga
                                                                                                            // el
                                                                                                            // usuario
                                                                                                            // de
                                                                                                            // prueba.

        // Test 6: Eliminar Unidades de Libro.
        System.out.println("\n--- Test Admin: Eliminar Unidades de Libro ---");
        System.out.println(adminTest.eliminarLibro(codLibro2, 1));
        pausar("Verificando unidades restantes...");
        adminTest.verLibro(codLibro2);

        // Test 7: Eliminar Libro Completo (sin prestamos).
        String codLibroTemp = procesarResultadoAgregarLibro(adminTest.agregarLibro("Libro Temporal", "Autor Temp",
                "auto", 1, testBiblioteca.getId(), testBiblioteca.getSede()));
        pausar("Libro temporal creado para eliminacion: " + codLibroTemp);
        System.out.println("\n--- Test Admin: Eliminar Libro (completo si no esta prestado) ---");
        System.out.println(adminTest.eliminarLibro(codLibroTemp, 1));
        pausar("Verificando que el libro temporal ya no exista.");
        if (Libro.encontrarLibroPorCodigo(codLibroTemp) == null) {
            System.out.println("Exito: El libro temporal fue eliminado.");
        } else {
            System.err.println("Error: El libro temporal no fue eliminado.");
        }

        // Test 8: Intento de eliminar usuario con libros prestados (debe fallar).
        System.out.println("\n--- Test Admin: Eliminar Usuario con libros prestados (esperado que falle) ---");
        System.out.println(userEstudianteTest.solicitarPrestamo(codLibro1, testBiblioteca));
        pausar("Intentando eliminar usuario con libro prestado...");
        System.out.println(adminTest.eliminarUsuario(userEstudianteTest.getUsuario()));
        if (Miembro.encontrarMiembroPorUsuario(userEstudianteTest.getUsuario()) != null) {
            System.out.println("Exito: Usuario no eliminado porque tiene libros prestados.");
        } else {
            System.err.println("Error: Usuario eliminado a pesar de tener libros prestados.");
        }

        // Test 9: Intento de eliminar usuario con deuda (debe fallar).
        System.out.println("\n--- Test Admin: Eliminar Usuario con deuda (esperado que falle) ---");
        userEstudianteTest = (Usuario) Miembro.encontrarMiembroPorUsuario(userEstudianteTest.getUsuario()); // Recarga
                                                                                                            // el
                                                                                                            // usuario.
        userEstudianteTest.setDeuda(100.0);
        userEstudianteTest.guardar();
        pausar("Simulando deuda y intentando eliminar usuario...");
        System.out.println(adminTest.eliminarUsuario(userEstudianteTest.getUsuario()));
        if (Miembro.encontrarMiembroPorUsuario(userEstudianteTest.getUsuario()) != null) {
            System.out.println("Exito: Usuario no eliminado porque tiene deuda.");
        } else {
            System.err.println("Error: Usuario eliminado a pesar de tener deuda.");
        }

        // Eliminar deuda y devolver libro para poder completar el test de eliminacion.
        pausar("Preparando usuario para eliminacion: saldando deuda y devolviendo libro...");
        userEstudianteTest = (Usuario) Miembro.encontrarMiembroPorUsuario(userEstudianteTest.getUsuario());
        userEstudianteTest.setDeuda(0.0);
        userEstudianteTest.guardar();
        userEstudianteTest = (Usuario) Miembro.encontrarMiembroPorUsuario(userEstudianteTest.getUsuario());
        System.out.println(userEstudianteTest.realizarDevolucion(codLibro1, testBiblioteca));

        // Test 10: Eliminar Usuario exitosamente.
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

        // Test 11: Usuario busca libro.
        System.out.println("\n--- Test Usuario: Buscar Libro ---");
        pausar("Buscando 'anillos' por nombre...");
        List<Libro> busqueda = testBiblioteca.buscarLibro("anillos", "nombre");
        if (!busqueda.isEmpty()) {
            System.out.println("Exito: Se encontraron libros con 'anillos'.");
            busqueda.forEach(System.out::println);
        } else {
            System.err.println("Error: No se encontraron libros con 'anillos'.");
        }

        // Test 12: Usuario pide prestado un libro (ya lo hizo en Test 8).
        System.out.println("\n--- Test Usuario: Ver mis libros prestados ---");
        userEstudianteTest.verMisLibrosPrestados();

        // Test 13: Prestar multiples libros (limite 5 para estudiante).
        System.out.println("\n--- Test Usuario: Prestar multiples libros (Estudiante) ---");
        userEstudianteTest = (Usuario) Miembro.encontrarMiembroPorUsuario(userEstudianteTest.getUsuario());
        String prestamoResult2 = userEstudianteTest.solicitarPrestamo(codLibro2, testBiblioteca);
        System.out.println(prestamoResult2);

        String codLibro3 = procesarResultadoAgregarLibro(adminTest.agregarLibro("Nuevo Libro", "Autor Anonimo", "auto",
                1, testBiblioteca.getId(), testBiblioteca.getSede()));
        userEstudianteTest = (Usuario) Miembro.encontrarMiembroPorUsuario(userEstudianteTest.getUsuario());
        String prestamoResult3 = userEstudianteTest.solicitarPrestamo(codLibro3, testBiblioteca);
        System.out.println(prestamoResult3);

        String codLibro4 = procesarResultadoAgregarLibro(adminTest.agregarLibro("Cuarto Libro", "Autor Cuatro", "auto",
                1, testBiblioteca.getId(), testBiblioteca.getSede()));
        userEstudianteTest = (Usuario) Miembro.encontrarMiembroPorUsuario(userEstudianteTest.getUsuario());
        String prestamoResult4 = userEstudianteTest.solicitarPrestamo(codLibro4, testBiblioteca);
        System.out.println(prestamoResult4);

        String codLibro5 = procesarResultadoAgregarLibro(adminTest.agregarLibro("Quinto Libro", "Autor Cinco", "auto",
                1, testBiblioteca.getId(), testBiblioteca.getSede()));
        userEstudianteTest = (Usuario) Miembro.encontrarMiembroPorUsuario(userEstudianteTest.getUsuario());
        String prestamoResult5 = userEstudianteTest.solicitarPrestamo(codLibro5, testBiblioteca);
        System.out.println(prestamoResult5);

        pausar("Intentando prestar un 5to libro (debe ser exitoso para estudiante)...");
        String codLibro6 = procesarResultadoAgregarLibro(adminTest.agregarLibro("Sexto Libro (para limite)",
                "Autor Seis", "auto", 1, testBiblioteca.getId(), testBiblioteca.getSede()));
        userEstudianteTest = (Usuario) Miembro.encontrarMiembroPorUsuario(userEstudianteTest.getUsuario());
        String prestamoResult6 = userEstudianteTest.solicitarPrestamo(codLibro6, testBiblioteca);
        System.out.println(prestamoResult6);

        pausar("Intentando prestar un 6to libro (debe fallar para estudiante por limite)...");
        String codLibro7 = procesarResultadoAgregarLibro(adminTest.agregarLibro("Septimo Libro (para falla)",
                "Autor Siete", "auto", 1, testBiblioteca.getId(), testBiblioteca.getSede()));
        userEstudianteTest = (Usuario) Miembro.encontrarMiembroPorUsuario(userEstudianteTest.getUsuario());
        String prestamoResult7 = userEstudianteTest.solicitarPrestamo(codLibro7, testBiblioteca);
        System.out.println(prestamoResult7);

        pausar("Verificando libros prestados del estudiante...");
        userEstudianteTest.verMisLibrosPrestados();

        // Test 14: Usuario devuelve libro.
        System.out.println("\n--- Test Usuario: Devolver Libro ---");
        userEstudianteTest = (Usuario) Miembro.encontrarMiembroPorUsuario(userEstudianteTest.getUsuario());
        System.out.println(userEstudianteTest.realizarDevolucion(codLibro2, testBiblioteca));
        pausar("Verificando libros prestados despues de la devolucion...");
        userEstudianteTest.verMisLibrosPrestados();

        // Test 15: Usuario cambia informacion personal.
        System.out.println("\n--- Test Usuario: Cambiar informacion personal ---");
        userEstudianteTest = (Usuario) Miembro.encontrarMiembroPorUsuario(userEstudianteTest.getUsuario());
        if (userEstudianteTest != null) {
            userEstudianteTest.cambiarInfoPersonal((byte) 1, "Carlos", "Perez", "Bogota", "Historia");
            pausar("Verificando informacion personal actualizada (se reimprime el usuario)...");
            System.out.println(Miembro.encontrarMiembroPorUsuario(userEstudianteTest.getUsuario()));
        } else {
            System.err.println("Error: No se pudo recargar el usuario para el test de cambio de informacion personal.");
        }

        // Test 16: Usuario cambia contrasena.
        System.out.println("\n--- Test Usuario: Cambiar contrasena ---");
        userEstudianteTest = (Usuario) Miembro.encontrarMiembroPorUsuario(userEstudianteTest.getUsuario());
        if (userEstudianteTest != null) {
            userEstudianteTest.cambiarContrasena("nuevaContrasenaUser");
            pausar("Contrasena cambiada (no visible por seguridad). Intentando login con la nueva contrasena para verificar.");
            Miembro loginVerification = new Miembro();
            if (!loginVerification.login(userEstudianteTest.getUsuario(), "nuevaContrasenaUser").isEmpty()) {
                System.out.println("Exito: Login con la nueva contrasena fue exitoso.");
            } else {
                System.err.println("Error: Login con la nueva contrasena fallo.");
            }
        } else {
            System.err.println("Error: No se pudo recargar el usuario para el test de cambio de contrasena.");
        }

        // Test 17: Pagar Deuda (se crea una deuda de prueba).
        System.out.println("\n--- Test Usuario: Pagar Deuda ---");
        userEstudianteTest = (Usuario) Miembro.encontrarMiembroPorUsuario(userEstudianteTest.getUsuario());
        userEstudianteTest.setDeuda(50.00);
        userEstudianteTest.guardar();
        System.out.println("Deuda antes de pagar: " + String.format("%.2f", userEstudianteTest.getDeuda()));

        pausar("Simulando pago de deuda. Escriba 50.00 cuando se le pida el monto.");
        System.out.println("Iniciando metodo pagarDeuda manualmente...");
        simularPagoDeudaAutomatico(userEstudianteTest, 50.00); // Llama a la versión simulada del pago para el test.

        userEstudianteTest = (Usuario) Miembro.encontrarMiembroPorUsuario(userEstudianteTest.getUsuario());
        if (userEstudianteTest.getDeuda() == 0.0) {
            System.out.println("Exito: La deuda fue pagada y se establecio en 0.00.");
        } else {
            System.err.println("Error: La deuda no se pago correctamente. Deuda actual: "
                    + String.format("%.2f", userEstudianteTest.getDeuda()));
        }
    }

    // Muestra un mensaje y espera que el usuario presione Enter.
    private static void pausar(String mensaje) {
        System.out.println("\n" + mensaje + " (Presione Enter para continuar...)");
        scanner.nextLine();
    }

    // Ayuda a extraer el código de un libro de un mensaje de éxito.
    private static String procesarResultadoAgregarLibro(String resultado) {
        if (resultado.startsWith("Exito: Libro '")) { // Si el mensaje empieza con "Exito: Libro '".
            return resultado.substring(resultado.indexOf("Codigo: ") + "Codigo: ".length()).replace(".", ""); // Extrae
                                                                                                              // el
                                                                                                              // código
                                                                                                              // después
                                                                                                              // de
                                                                                                              // "Codigo:
                                                                                                              // ".
        }
        return null;
    }

    // Versión especial de 'pagarDeuda' para los tests, donde el monto se pasa
    // directamente.
    private static void simularPagoDeudaAutomatico(Usuario usuario, double montoPago) {
        System.out.println("\n--- PAGAR DEUDA (simulado para test) ---");
        if (usuario.getDeuda() <= 0) {
            System.out.println("No tienes deuda pendiente.");
            return;
        }
        System.out.println("Tu deuda actual es: " + String.format("%.2f", usuario.getDeuda()) + " pesos.");
        System.out.println("Simulando pago de: " + String.format("%.2f", montoPago) + " pesos.");

        if (montoPago <= 0) {
            System.out.println("El monto a pagar debe ser mayor que cero.");
            return;
        }
        if (montoPago > usuario.getDeuda()) {
            System.out.println("Advertencia: Estas pagando mas de lo debido. Tu deuda se ajustara a 0.");
            usuario.setDeuda(0.0);
        } else {
            usuario.setDeuda(usuario.getDeuda() - montoPago);
        }
        usuario.guardar();
        System.out.println("Pago realizado (simulado). Tu nueva deuda es: " + String.format("%.2f", usuario.getDeuda())
                + " pesos.");
    }
}
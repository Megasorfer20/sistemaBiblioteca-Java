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
import java.util.List;
import java.util.Scanner;
/**
 *
 * @author edrui
 */
public class SistemaBiblioteca {

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
        try {
            // Guardar el archivo en una ruta conocida (Carpeta Documentos del usuario)
            Path miembrosFile = Paths.get(System.getProperty("user.home"), "Documents", "Miembros.txt");
            // Asegurarse de que el directorio exista
            if (miembrosFile.getParent() != null) {
                Files.createDirectories(miembrosFile.getParent());
            }

            // Crear 5 miembros aleatorios y guardarlos en Miembros.txt (formato usuario,contraseña)
            List<String> miembros = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                String usuario = "user" + (1000 + RANDOM.nextInt(9000));
                String contrasenia = randomString(8);
                miembros.add(usuario + "," + contrasenia);
            }
            // Escribir con opciones explícitas para crear/reescribir el archivo
            Files.write(miembrosFile, miembros, StandardCharsets.UTF_8,
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.TRUNCATE_EXISTING);

            System.out.println("Miembros guardados en: " + miembrosFile.toAbsolutePath());
            System.out.println();

            // Mostrar en consola el usuario y contraseña de un miembro elegido aleatoriamente
            int elegido = RANDOM.nextInt(miembros.size());
            String muestra = miembros.get(elegido);
            String[] partes = muestra.split(",", 2);
            System.out.println("Miembro elegido (credenciales generadas automáticamente):");
            System.out.println("Usuario: " + partes[0]);
            System.out.println("Contraseña: " + partes[1]);
            System.out.println();

            // Permitir al usuario ingresar credenciales para probar el "login"
            try (Scanner sc = new Scanner(System.in)) {
                System.out.print("Ingrese usuario para probar login: ");
                String inputUser = sc.nextLine().trim();
                System.out.print("Ingrese contraseña para probar login: ");
                String inputPass = sc.nextLine().trim();

                // Verificar contra el archivo Miembros.txt
                List<String> lines = Files.readAllLines(miembrosFile, StandardCharsets.UTF_8);
                boolean encontrado = false;
                for (String line : lines) {
                    String[] p = line.split(",", 2);
                    if (p.length == 2 && p[0].equals(inputUser) && p[1].equals(inputPass)) {
                        System.out.println("Login correcto. Línea encontrada en Miembros.txt:");
                        System.out.println(line);
                        encontrado = true;
                        break;
                    }
                }
                if (!encontrado) {
                    System.out.println("Login fallido. No se encontró una coincidencia en Miembros.txt.");
                }
            }
        } catch (IOException e) {
            System.err.println("Error de E/S: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.sistemabiblioteca;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 *
 * @author edrui
 */
public class Biblioteca {

    public Biblioteca seleccionarSede(){
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("Bibliotecas.txt"); BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            String linea;
            while ((linea = reader.readLine()) != null) {
                System.out.println(linea);
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
        return this;
    }

    public void buscarLibro() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("Libros.txt"); BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            String linea;
            while ((linea = reader.readLine()) != null) {
                System.out.println(linea);
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }
}

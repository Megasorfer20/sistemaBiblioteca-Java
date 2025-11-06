/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.sistemabiblioteca;

/**
 * @author edrui
 */

// Esta interfaz define un "contrato" para objetos que pueden guardarse y
// eliminarse.
// Cualquier clase que use esta interfaz debe implementar estos métodos.
public interface IPersistible {
    // Guarda o actualiza el objeto en su archivo.
    void guardar();

    // Elimina el objeto de su archivo.
    void eliminar();

    // Convierte el objeto a una línea de texto para guardarlo en un archivo.
    String construirLinea();
}
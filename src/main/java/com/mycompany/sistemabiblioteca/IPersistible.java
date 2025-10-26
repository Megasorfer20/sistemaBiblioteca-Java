/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.sistemabiblioteca;

/**
 *
 * @author edrui
 */

/**
 * Interfaz para definir el comportamiento basico de persistencia
 * que deben tener los objetos del sistema (guardar, eliminar, construir linea).
 *
 * @author edrui
 */
public interface IPersistible {
    /**
     * Guarda o actualiza el estado del objeto en su archivo de persistencia.
     */
    void guardar();

    /**
     * Elimina el objeto de su archivo de persistencia.
     */
    void eliminar();

    /**
     * Construye una linea de texto que representa el estado actual del objeto,
     * para ser almacenada en un archivo.
     *
     * @return Una cadena formateada para persistencia.
     */
    String construirLinea();
}
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.sistemabiblioteca;

/**
 *
 * @author edrui
 */
public class Admin extends Miembro {
    public Admin() {}

    public Admin(byte tipoDoc, long noDoc, byte rol, String nombre, String apellido, String usuario, String contrasena) {
        super(tipoDoc, noDoc, rol, nombre, apellido, usuario, contrasena);
    }

    public void verTodosUsuarios (){
        
    }
    
    public void verTodosLibros () {
        
    }
    
    public void verLibrosPrestados() {
        
    }
    
    public void agregarLibros(){
        
    }
    
    public void editarLibros(){
        
    }
    
    public void eliminarLibros(){
        
    }
    
    public void agregarUsuarios(){
        
    }
    
    public void editarUsuarios(){
        
    }
    
    public void eliminarUsuarios(){
        
    }

    @Override
    public String toString() {
        return super.toString();
    }
}

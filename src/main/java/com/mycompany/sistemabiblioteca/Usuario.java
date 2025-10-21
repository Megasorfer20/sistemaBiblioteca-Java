/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.sistemabiblioteca;

/**
 *
 * @author edrui
 */
public class Usuario extends Miembro {
    
    // private int noLibroPrestados;
    public int deuda;
    
    public Usuario(){}
    
    public Usuario(byte tipoDoc, long noDoc, byte rol, String nombre, String apellido, String usuario, String contrasena, int deuda, int noLibroPrestados ) {
        super(tipoDoc, noDoc, rol, nombre, apellido, usuario, contrasena);
        // this.noLibroPrestados = noLibroPrestados;
        this.deuda = deuda;
    }
    
    public void buscarLibro(){
        
    }
    
    public void prestarLibro(){
        
    }
    
    public void devolverLibro() {
        
    }
    
    public void pagarDeuda(){
        
    }
    
    public void verLibrosPrestados (){
        
    }
    
    @Override
    public String toString() {
        return super.toString();
    }
}

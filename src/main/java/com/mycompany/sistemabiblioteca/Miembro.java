/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.sistemabiblioteca;

/**
 *
 * @author edrui
 */
public class Miembro {
    protected long noDoc;
    protected byte tipoDoc;
    protected byte rol;
    protected String nombre;
    protected String apellido;
    protected String usuario;
    protected String contrasena;

    public Miembro() {}

    public Miembro(byte tipoDoc, long noDoc, byte rol, String nombre, String apellido, String usuario, String contrasena) {
        this.tipoDoc = tipoDoc;
        this.noDoc = noDoc;
        this.rol = rol;
        this.nombre = nombre;
        this.apellido = apellido;
        this.usuario = usuario;
        this.contrasena = contrasena;
    }

    public long getNoDoc() {
        return noDoc;
    }

    public void setNoDoc(long noDoc) {
        this.noDoc = noDoc;
    }

    public byte getTipoDoc() {
        return tipoDoc;
    }

    public void setTipoDoc(byte tipoDoc) {
        this.tipoDoc = tipoDoc;
    }

    public byte getRol() {
        return rol;
    }

    public void setRol(byte rol) {
        this.rol = rol;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
    
    public void cambiarContrasena(){
        
    }
    
    public void cambiarInfoPersonal(){
        
    }

    @Override
    public String toString() {
        return "Miembro {" +
               "\n  Tipo de Documento: " + tipoDoc +
               "\n  Número de Documento: " + noDoc +
               "\n  Rol: " + rol +
               "\n  Nombre: " + nombre +
               "\n  Apellido: " + apellido +
               "\n  Usuario: " + usuario +
               "\n  Contraseña: " + contrasena +
               "\n}";
    }
}

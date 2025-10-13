/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.sistemabiblioteca;

/**
 *
 * @author Estudiante
 */
public class Fecha {
    private int dd;
    private int mm;
    private int aa;
    
    private static final String[] MONTH_NAMES = {
        "Mes Inválido", "enero", "febrero", "marzo", "abril", "mayo", "junio",
        "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
    };
    
    public Fecha(){
        this.dd=1;
        this.mm=1;
        this.aa=2000; 
    }
    public Fecha(int dd, int mm, int aa){
        this.dd=dd;
        this.mm=mm;
        this.aa=aa;
    }
    
    public int getDia(){return dd;} 
    public int getMes(){return mm;}
    public int getA(){return aa;}  
    
    public void setDia(int dd){this.dd=dd;} 
    public void setMes(int mm){this.mm=mm;} 
    public void setA(int aa){this.aa=aa;} 
    
    @Override
    public String toString() {
        String monthName;
        if (mm >= 1 && mm <= 12) {
            monthName = MONTH_NAMES[mm];
        } else {
            monthName = "Mes Inválido (" + mm + ")";
        }
        return String.format("%02d-%s-%d", dd, monthName, aa);
    }
}
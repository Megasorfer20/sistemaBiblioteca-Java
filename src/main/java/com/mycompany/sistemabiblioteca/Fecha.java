/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.sistemabiblioteca; // Carpeta donde está organizado nuestro código.

import java.text.ParseException; // Para manejar errores al convertir texto a fecha.
import java.text.SimpleDateFormat; // Para dar formato a las fechas (ej. "yyyy-MM-dd").
import java.util.Calendar; // Para operaciones avanzadas con fechas (sumar días, etc.).
import java.util.Date; // Clase principal para representar una fecha y hora.

// Esta clase es una herramienta útil para manejar fechas en el sistema.
// Facilita formatear, leer y hacer cálculos con fechas.
public class Fecha {
    // Define el formato estándar para todas las fechas en el sistema:
    // "año-mes-día".
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    // Obtiene la fecha actual (hoy), pero sin incluir la hora, minutos, segundos ni
    // milisegundos.
    // Esto es útil para comparar solo el día.
    public static Date getToday() {
        Calendar cal = Calendar.getInstance(); // Obtiene la fecha y hora actual.
        cal.set(Calendar.HOUR_OF_DAY, 0); // Pone la hora a las 00:00.
        cal.set(Calendar.MINUTE, 0); // Pone los minutos a 0.
        cal.set(Calendar.SECOND, 0); // Pone los segundos a 0.
        cal.set(Calendar.MILLISECOND, 0); // Pone los milisegundos a 0.
        return cal.getTime(); // Devuelve la fecha resultante (el inicio del día actual).
    }

    // Añade o resta un número de días a una fecha dada.
    public static Date addDays(Date date, int days) {
        Calendar cal = Calendar.getInstance(); // Obtiene una instancia de Calendar.
        cal.setTime(date); // Establece la fecha de inicio.
        cal.add(Calendar.DAY_OF_MONTH, days); // Suma o resta los días.
        return cal.getTime(); // Devuelve la nueva fecha.
    }

    // Calcula la diferencia en días entre dos fechas.
    public static long getDaysBetween(Date date1, Date date2) {
        // Normaliza ambas fechas al inicio del día para que la hora no afecte el
        // cálculo.
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        cal1.set(Calendar.HOUR_OF_DAY, 0);
        cal1.set(Calendar.MINUTE, 0);
        cal1.set(Calendar.SECOND, 0);
        cal1.set(Calendar.MILLISECOND, 0);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        cal2.set(Calendar.HOUR_OF_DAY, 0);
        cal2.set(Calendar.MINUTE, 0);
        cal2.set(Calendar.SECOND, 0);
        cal2.set(Calendar.MILLISECOND, 0);

        long diffMillis = cal1.getTimeInMillis() - cal2.getTimeInMillis(); // Calcula la diferencia en milisegundos.
        long diffDays = diffMillis / (1000 * 60 * 60 * 24); // Convierte milisegundos a días.
        return diffDays; // Devuelve la cantidad de días.
    }

    // Convierte un objeto 'Date' a una cadena de texto con el formato "yyyy-MM-dd".
    // Si la fecha es nula, devuelve la palabra "null".
    public static String formatDate(Date date) {
        if (date == null) { // Si la fecha es nula.
            return "null"; // Devuelve la cadena "null".
        }
        return DATE_FORMAT.format(date); // Formatea la fecha y la devuelve como texto.
    }

    // Convierte una cadena de texto con formato "yyyy-MM-dd" a un objeto 'Date'.
    // Si la cadena es "null" o está vacía, devuelve 'null'.
    public static Date parseDate(String dateString) throws ParseException { // Puede lanzar un error si el formato es
                                                                            // incorrecto.
        if (dateString == null || dateString.trim().isEmpty() || dateString.equalsIgnoreCase("null")) { // Si la cadena
                                                                                                        // es inválida o
                                                                                                        // "null".
            return null; // Devuelve 'null'.
        }
        return DATE_FORMAT.parse(dateString); // Intenta convertir la cadena a una fecha.
    }
}
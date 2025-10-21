/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.sistemabiblioteca;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Clase de utilidad para el manejo de fechas, adaptada para usar
 * java.util.Date.
 * Proporciona métodos para formatear, parsear y realizar operaciones básicas
 * con fechas.
 *
 * @author edrui 
 */
public class Fecha {
    // Definimos el SimpleDateFormat aquí para su uso consistente en todo el
    // sistema.
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    // Los atributos dd, mm, aa del usuario original se eliminan ya que esta clase
    // ahora actúa como un utilitario para java.util.Date.

    // El constructor por defecto o con parámetros para dd,mm,aa no son necesarios
    // si esta clase es solo un utilitario con métodos estáticos.

    /**
     * Obtiene la fecha actual (hoy) sin información de hora, minutos, segundos o
     * milisegundos.
     * 
     * @return Una instancia de Date representando el inicio del día actual.
     */
    public static Date getToday() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * Añade o resta un número de días a una fecha dada.
     * 
     * @param date La fecha base.
     * @param days El número de días a añadir (positivo) o restar (negativo).
     * @return La nueva fecha resultante.
     */
    public static Date addDays(Date date, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_MONTH, days);
        return cal.getTime();
    }

    /**
     * Calcula la diferencia en días entre dos fechas.
     * 
     * @param date1 La primera fecha (normalmente la más reciente).
     * @param date2 La segunda fecha (normalmente la más antigua).
     * @return El número de días entre date1 y date2. Positivo si date1 es posterior
     *         a date2.
     *         Retorna 0 si las fechas son iguales o date1 es anterior a date2 (si
     *         se quiere solo días de retraso).
     */
    public static long getDaysBetween(Date date1, Date date2) {
        // Asegurarse de comparar solo el día, ignorando la hora
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

        long diffMillis = cal1.getTimeInMillis() - cal2.getTimeInMillis();
        long diffDays = diffMillis / (1000 * 60 * 60 * 24);
        return diffDays;
    }

    /**
     * Formatea una instancia de {@code Date} a una cadena de texto en el formato
     * "yyyy-MM-dd".
     * Si la fecha es {@code null}, retorna "null" (para persistencia).
     * 
     * @param date La fecha a formatear.
     * @return La fecha formateada como {@code String} o "null".
     */
    public static String formatDate(Date date) {
        if (date == null) {
            return "null"; // Consistent with how we save null dates in Prestamo
        }
        return DATE_FORMAT.format(date);
    }

    /**
     * Parsea una cadena de texto en formato "yyyy-MM-dd" a una instancia de
     * {@code Date}.
     * Si la cadena es "null" o vacía, retorna {@code null}.
     * 
     * @param dateString La cadena de texto de la fecha.
     * @return La instancia de {@code Date} o {@code null} si la cadena es "null" o
     *         vacía.
     * @throws ParseException Si la cadena no puede ser parseada al formato
     *                        esperado.
     */
    public static Date parseDate(String dateString) throws ParseException {
        if (dateString == null || dateString.trim().isEmpty() || dateString.equalsIgnoreCase("null")) {
            return null;
        }
        return DATE_FORMAT.parse(dateString);
    }
}
package com.luis.tfg.performance;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación para monitorear la actividad de hilos durante la ejecución de un método.
 * 
 * <p>Al aplicar esta anotación, se registra el uso de hilos para detectar
 * posibles problemas de concurrencia y rendimiento. Es útil en métodos que
 * crean o gestionan hilos para optimizar el uso de recursos.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MonitorearHilos {
}

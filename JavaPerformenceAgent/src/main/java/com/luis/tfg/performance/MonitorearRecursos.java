package com.luis.tfg.performance;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación para monitorear el uso de recursos del sistema durante la ejecución de un método.
 * 
 * <p>Aplicando esta anotación a un método, el sistema activa la monitorización
 * de recursos tales como uso de CPU y memoria mientras el método está en ejecución.
 * Esta información es útil para evaluar el rendimiento y optimización.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MonitorearRecursos {
}

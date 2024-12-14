package com.luis.tfg.performance;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación para medir el tiempo de ejecución de un método.
 * 
 * <p>Aplicando esta anotación a un método, se registra el tiempo de inicio
 * y finalización para calcular la duración de su ejecución. Esto es útil
 * para evaluar y optimizar el rendimiento de métodos específicos.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MedirTiempo {
}

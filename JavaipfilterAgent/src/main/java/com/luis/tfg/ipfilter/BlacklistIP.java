package com.luis.tfg.ipfilter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación para marcar métodos que deben ser bloqueados para IPs en la blacklist.
 * 
 * <p>Al aplicar esta anotación a un método, se verifica si la IP solicitante
 * está en la lista negra antes de permitir su ejecución. Esto es útil para 
 * restringir el acceso a usuarios no autorizados.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface BlacklistIP {
}

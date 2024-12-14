package com.luis.tfg.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * La anotación @SecureClass se utiliza para marcar clases que requieren medidas adicionales
 * de seguridad o una validación específica en el contexto de la aplicación.
 * 
 * Al aplicar esta anotación a una clase, se indica que la clase está destinada a ser 
 * monitoreada o revisada con fines de seguridad. Esta anotación puede ser utilizada por 
 * agentes de seguridad o componentes de análisis para aplicar políticas adicionales o 
 * auditorías en tiempo de ejecución.
 * 
 * @Retention(RetentionPolicy.RUNTIME) - La anotación se conserva en tiempo de ejecución, lo que permite 
 * que los agentes de seguridad o reflexiones la accedan durante la ejecución del programa.
 * 
 * @Target(ElementType.TYPE) - Esta anotación solo se puede aplicar a tipos de clase.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SecureClass {
}

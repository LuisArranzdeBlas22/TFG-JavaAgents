package com.luis.tfg.ipfilter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación para marcar métodos que deben ser permitidos solo para IPs en la whitelist.
 * 
 * <p>Cuando un método se anota con `@WhitelistIP`, solo las IPs incluidas en la lista blanca 
 * podrán acceder a él. Esta anotación permite una capa adicional de seguridad para métodos 
 * específicos, restringiendo su acceso según la configuración del agente.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface WhitelistIP {
}

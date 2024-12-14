package com.luis.tfg.ipfilter;

import net.bytebuddy.asm.Advice;
import java.lang.reflect.Method;
import java.util.function.Supplier;

/**
 * Clase de asesoramiento para el filtrado de IPs en métodos anotados.
 * <p>
 * Esta clase utiliza un proveedor de IP para verificar si la dirección IP de
 * la solicitud cumple con las reglas de whitelist y blacklist antes de ejecutar
 * el método.
 */
public class IPFilterAdvice {
    private static final AgentConfig config = new AgentConfig("ip_filter.config");
    private static Supplier<String> ipProvider = IPFilterAdvice::getRequestIP;

    /**
     * Establece un proveedor personalizado para obtener la IP solicitante.
     *
     * @param provider Proveedor de IP.
     */
    public static void setIpProvider(Supplier<String> provider) {
        ipProvider = provider;
    }

    /**
     * Método de entrada para verificar si la IP tiene acceso al método anotado.
     * <p>
     * Si el método tiene anotaciones de whitelist o blacklist, se valida si la IP
     * está en la lista correspondiente. Lanza una excepción si el acceso es denegado.
     *
     * @param method Método de destino para la verificación de IP.
     */
    @Advice.OnMethodEnter
    public static void checkIP(@Advice.Origin Method method) {
        if (method == null) return;
        String ip = ipProvider.get();
        boolean whitelistEnabled = method.isAnnotationPresent(WhitelistIP.class);
        boolean blacklistEnabled = method.isAnnotationPresent(BlacklistIP.class);

        if (whitelistEnabled && !config.getWhitelist().contains(ip)) {
            System.out.println("Access denied for IP (not in whitelist): " + ip);
            throw new SecurityException("Access denied for IP: " + ip);
        }

        if (blacklistEnabled && config.getBlacklist().contains(ip)) {
            System.out.println("Access denied for blacklisted IP: " + ip);
            throw new SecurityException("Access denied for IP: " + ip);
        }

        System.out.println("Access granted for IP: " + ip);
    }

    /**
     * Método por defecto para obtener la IP de la solicitud.
     *
     * @return IP por defecto.
     */
    private static String getRequestIP() {
        return "192.168.1.1";  // IP por defecto
    }
}

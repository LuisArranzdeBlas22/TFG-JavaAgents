package com.luis.tfg.performance;

import net.bytebuddy.asm.Advice;

/**
 * Clase que realiza la instrumentación de métodos anotados para monitorear el rendimiento.
 * Implementa consejos para medir la duración de métodos y el uso de recursos.
 */
public class MonitoringAdvice {

    /**
     * Inicia el monitoreo de tiempo cuando un método anotado comienza su ejecución.
     *
     * @param method el nombre del método que se está monitoreando.
     * @return el tiempo de inicio en milisegundos.
     */
    @Advice.OnMethodEnter
    public static long onEnter(@Advice.Origin String method) {
        System.out.println("[INFO] Iniciando monitoreo del método: " + method);
        return System.currentTimeMillis();
    }

    /**
     * Finaliza el monitoreo de tiempo y calcula la duración del método.
     *
     * @param method el nombre del método que se monitorea.
     * @param startTime el tiempo de inicio recibido de onEnter.
     */
    @Advice.OnMethodExit
    public static void onExit(@Advice.Origin String method, @Advice.Enter long startTime) {
        long endTime = System.currentTimeMillis();
        System.out.println("[INFO] El método " + method + " tomó " + (endTime - startTime) + " ms");
    }

    /**
     * Inicia el monitoreo de recursos al entrar en un método específico.
     *
     * @param method el nombre del método monitoreado.
     * @return true si el monitoreo se inicia con éxito.
     */
    @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
    public static boolean onResourceMonitoringEnter(@Advice.Origin String method) {
        System.out.println("[INFO] Monitoreo de recursos iniciado para: " + method);
        return true;
    }

    /**
     * Finaliza el monitoreo de recursos al salir del método monitoreado.
     *
     * @param method el nombre del método monitoreado.
     */
    @Advice.OnMethodExit
    public static void onResourceMonitoringExit(@Advice.Origin String method) {
        System.out.println("[INFO] Monitoreo de recursos finalizado para: " + method);
    }

    /**
     * Inicia el monitoreo de hilos cuando comienza la ejecución de un método anotado.
     *
     * @param method el nombre del método monitoreado.
     * @return true si el monitoreo de hilos se inicia correctamente.
     */
    @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
    public static boolean onThreadMonitoringEnter(@Advice.Origin String method) {
        System.out.println("[INFO] Monitoreo de hilos iniciado para: " + method);
        return true;
    }

    /**
     * Finaliza el monitoreo de hilos al salir del método monitoreado.
     *
     * @param method el nombre del método monitoreado.
     */
    @Advice.OnMethodExit
    public static void onThreadMonitoringExit(@Advice.Origin String method) {
        System.out.println("[INFO] Monitoreo de hilos finalizado para: " + method);
    }
    
    /**
     * Registra un mensaje de error para informar fallos durante la ejecución del método.
     *
     * @param errorMessage el mensaje de error a registrar.
     */
    public static void logError(String errorMessage) {
        System.out.println("Error: " + errorMessage);
    }
}

package com.luis.tfg.performance;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

/**
 * Clase principal del agente que realiza la instrumentación para medir rendimiento.
 * Utiliza `byte-buddy` para interceptar clases con anotaciones específicas.
 */
public class PerformanceAgent {

    /**
     * Método de entrada para el agente que se ejecuta antes de la carga de clases.
     * Configura el `AgentBuilder` para interceptar y transformar clases que contengan
     * las anotaciones `@MedirTiempo`, `@MonitorearRecursos` o `@MonitorearHilos`.
     *
     * @param agentArgs argumentos pasados al agente.
     * @param inst instancia de Instrumentation para instrumentar las clases.
     */
    public static void premain(String agentArgs, Instrumentation inst) {
        new AgentBuilder.Default()
            .type(ElementMatchers.isAnnotatedWith(MedirTiempo.class)
                .or(ElementMatchers.isAnnotatedWith(MonitorearRecursos.class))
                .or(ElementMatchers.isAnnotatedWith(MonitorearHilos.class)))
            .transform(new AgentBuilder.Transformer() {
                /**
                 * Transforma las clases interceptadas añadiendo el consejo de monitoreo.
                 *
                 * @param builder el generador dinámico de clases.
                 * @param typeDescription descripción del tipo de clase.
                 * @param classLoader el cargador de clases de la aplicación.
                 * @param module el módulo Java asociado.
                 * @param protectionDomain el dominio de protección de la clase.
                 * @return el generador de clases modificado.
                 */
                @Override
                public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder,
                                                        TypeDescription typeDescription,
                                                        ClassLoader classLoader,
                                                        JavaModule module,
                                                        ProtectionDomain protectionDomain) {
                    return builder.visit(Advice.to(MonitoringAdvice.class).on(ElementMatchers.any()));
                }
            })
            .with(AgentBuilder.Listener.StreamWriting.toSystemOut())
            .installOn(inst);
    }
}

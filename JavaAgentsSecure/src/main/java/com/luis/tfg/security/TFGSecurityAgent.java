package com.luis.tfg.security;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.utility.JavaModule;

import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

/**
 * La clase TFGSecurityAgent es un agente de seguridad diseñado para interceptar 
 * y aplicar medidas de seguridad a las clases que estén marcadas con la anotación 
 * @SecureClass. Utiliza ByteBuddy para instrumentar y añadir lógica de seguridad 
 * en tiempo de ejecución.
 * 
 * El método `premain` actúa como punto de entrada para el agente, el cual se ejecuta 
 * antes de que la aplicación principal comience. Configura un agente ByteBuddy que:
 * 
 * - Intercepta clases que están anotadas con @SecureClass.
 * - Aplica el consejo (`Advice`) de la clase TFGSecurityAdvice para añadir lógica 
 *   de seguridad adicional a todos los métodos de las clases interceptadas.
 * - Utiliza un Listener para registrar los eventos de instrumentación en la consola.
 * 
 * @param agentArgs Argumentos pasados al agente (opcional).
 * @param inst Instancia de Instrumentation que permite la transformación de clases.
 */
public class TFGSecurityAgent {
    public static void premain(String agentArgs, Instrumentation inst) {
        new AgentBuilder.Default()
            // Interceptar solo las clases que tienen la anotación @SecureClass
            .type(ElementMatchers.isAnnotatedWith(SecureClass.class)) 
            .transform(new AgentBuilder.Transformer() {
                @Override
                public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, 
                                                        TypeDescription typeDescription, 
                                                        ClassLoader classLoader, 
                                                        JavaModule module, 
                                                        ProtectionDomain protectionDomain) {
                    return builder.visit(Advice.to(TFGSecurityAdvice.class).on(ElementMatchers.any()));
                }
            })
            // Loguea los eventos de instrumentación a la salida estándar
            .with(AgentBuilder.Listener.StreamWriting.toSystemOut())
            .installOn(inst);
    }
}

package com.luis.tfg.ExecutionOrder;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.instrument.Instrumentation;

public class MethodOrderAgent {
    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("Agente iniciado. Argumentos: " + agentArgs);
        try {
            System.out.println("El agente se está cargando correctamente.");

            // Configurar el agente para interceptar todas las clases anotadas con @ExecutionOrder
            new AgentBuilder.Default()
                .type(ElementMatchers.any()) // Seleccionar cualquier clase
                .transform((builder, typeDescription, classLoader, module, protectionDomain) -> {
                    // Solo aplicar si la clase tiene la anotación @ExecutionOrder
                    if (typeDescription.getDeclaredAnnotations()
                            .isAnnotationPresent(ExecutionOrder.class)) {
                        return builder
                            .constructor(ElementMatchers.any()) // Interceptar constructores
                            .intercept(net.bytebuddy.asm.Advice.to(ConstructorAdvice.class))
                            .method(ElementMatchers.any()
                                .and(ElementMatchers.not(ElementMatchers.named("toString"))) // Excluir toString
                                .and(ElementMatchers.not(ElementMatchers.named("equals")))   // Excluir equals
                                .and(ElementMatchers.not(ElementMatchers.named("hashCode"))) // Excluir hashCode
                                .and(ElementMatchers.not(ElementMatchers.named("clone")))    // Excluir clone
                                .and(ElementMatchers.not(ElementMatchers.isDeclaredBy(Object.class)))) // Excluir métodos de Object
                            .intercept(net.bytebuddy.asm.Advice.to(MethodOrderAdvice.class));
                    }
                    return builder; // Si no tiene la anotación, no modificar
                })
                .installOn(inst);

            System.out.println("El agente se ha instalado correctamente.");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al inicializar el agente", e);
        }
    }
}

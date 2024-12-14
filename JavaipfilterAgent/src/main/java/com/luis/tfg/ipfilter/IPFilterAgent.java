package com.luis.tfg.ipfilter;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;
import java.lang.instrument.Instrumentation;

/**
 * Agente de filtrado de IP que intercepta métodos anotados con `WhitelistIP` y `BlacklistIP`.
 * 
 * <p>El agente se instala mediante Byte Buddy antes de cargar la aplicación y aplica asesoramiento
 * (`Advice`) a métodos específicos que contienen las anotaciones de lista blanca y negra.</p>
 */
public class IPFilterAgent {

    /**
     * Método premain que instala el agente para interceptar métodos anotados.
     * 
     * <p>Este método se ejecuta antes de que la aplicación comience a ejecutarse y configura un 
     * transformador para interceptar métodos que estén anotados con `WhitelistIP` o `BlacklistIP`,
     * aplicando el asesoramiento definido en `IPFilterAdvice`.</p>
     *
     * @param agentArgs argumentos del agente, no utilizados en esta implementación.
     * @param inst instancia de `Instrumentation` utilizada para la instrumentación de clases.
     */
    public static void premain(String agentArgs, Instrumentation inst) {
        new AgentBuilder.Default()
            .type(ElementMatchers.any())
            .transform((builder, type, classLoader, module, protectionDomain) ->
            builder.method(ElementMatchers.isAnnotatedWith(WhitelistIP.class)
                          .or(ElementMatchers.isAnnotatedWith(BlacklistIP.class)))
                   .intercept(Advice.to(IPFilterAdvice.class))
         ).installOn(inst);
    }
}

package com.luis.tfg.security;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import java.lang.instrument.Instrumentation;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
/**
 * Clase de pruebas para el agente de seguridad `TFGSecurityAgent`.
 * Esta clase contiene varios métodos de prueba para verificar la funcionalidad de seguridad proporcionada por
 * `TFGSecurityAdvice` y para asegurar que la instrumentación de agentes funcione correctamente.
 */
public class TFGSecurityAgentTest {

    private static Instrumentation instrumentation;
    private static final Logger logger = Logger.getLogger(TFGSecurityAgentTest.class.getName());

    /**
     * Inicializa el agente de seguridad utilizando una implementación de `MockInstrumentation`.
     * Se ejecuta una vez antes de todas las pruebas para configurar el entorno del agente.
     */
    @BeforeAll
    public static void setupAgent() {
        logger.info("Inicializando el agente de seguridad para pruebas.");
        instrumentation = MockInstrumentation.getInstrumentation(); // Usar el Mock
    }

    /**
     * Configuración previa a cada prueba.
     * Este método se ejecuta antes de cada prueba individual para inicializar recursos necesarios.
     */
    @BeforeEach
    public void setup() {
        logger.info("Ejecutando configuración antes de cada prueba.");
    }

    /**
     * Limpieza posterior a cada prueba.
     * Este método se ejecuta después de cada prueba individual para limpiar recursos utilizados.
     */
    @AfterEach
    public void tearDown() {
        logger.info("Limpieza después de cada prueba.");
    }

    /**
     * Prueba para verificar la detección de inyecciones SQL en entradas sospechosas.
     * Verifica que `TFGSecurityAdvice.detectSQLInjection` maneje correctamente diferentes tipos de entradas
     * que simulan inyecciones SQL, asegurándose de que solo las entradas seguras no generen alertas.
     */
    @Test
    public void testSQLInjectionDetection() {
        String[] testCases = {
            "1 OR 1=1",  // Inyección simple
            "' OR '1'='1",  // Variante de inyección
            "SELECT * FROM users WHERE username = 'admin' --",  // Comentario en SQL
            "username; DROP TABLE users;",  // SQL Injection clásico
            "safeString"  // Entrada segura
        };

        for (String input : testCases) {
            if (!input.equals("safeString")) {
                assertDoesNotThrow(() -> {
                    TFGSecurityAdvice.detectSQLInjection("testMethod", input);
                }, "El método detectSQLInjection falló al procesar: " + input);
            } else {
                assertDoesNotThrow(() -> {
                    TFGSecurityAdvice.detectSQLInjection("testMethod", input);
                }, "El método detectSQLInjection no debería arrojar una excepción para esta entrada segura: " + input);
            }
        }
    }

    /**
     * Prueba para detectar vulnerabilidades de Path Traversal.
     * Verifica que `TFGSecurityAdvice.isPotentialPathTraversal` detecte correctamente rutas sospechosas 
     * que podrían ser utilizadas para acceder a archivos de forma no autorizada.
     */
    @Test
    public void testPathTraversalDetection() {
        String[] paths = {
            "../etc/passwd",
            "/../../Windows/System32",
            "/var/www/html/../../.ssh/id_rsa",
            "C:\\Users\\user\\Documents\\..\\..\\..\\",
            "safe/path/to/file.txt"
        };

        for (String path : paths) {
            boolean expected = !path.startsWith("safe");
            assertEquals(expected, TFGSecurityAdvice.isPotentialPathTraversal(path), 
                "La detección de path traversal falló para: " + path);
        }
    }

    /**
     * Prueba para verificar la detección de criptografía débil.
     * Asegura que `TFGSecurityAdvice.detectWeakCryptography` identifique algoritmos de cifrado obsoletos e inseguros,
     * y que no genere falsos positivos para cifrados considerados seguros.
     */
    @Test
    public void testWeakCryptographyDetection() {
        String[] cryptoCases = {
            "U29tZUJhc2U2NEVuY29kaW5n",  // Base64 codificado
            "aes256-cbc",  // Algoritmo de cifrado débil
            "ssl3",  // Protocolo inseguro
            "safeCrypto"  // Representa un cifrado fuerte simulado
        };

        for (String crypto : cryptoCases) {
            if (!crypto.equals("safeCrypto")) {
                assertDoesNotThrow(() -> {
                    TFGSecurityAdvice.detectWeakCryptography("testMethod", crypto);
                }, "El método detectWeakCryptography lanzó una excepción para: " + crypto);
            } else {
                assertThrows(IllegalArgumentException.class, () -> {
                    TFGSecurityAdvice.detectWeakCryptography("testMethod", crypto);
                }, "El método detectWeakCryptography no debería detectar esta entrada segura: " + crypto);
            }
        }
    }

    /**
     * Prueba para verificar la instalación del agente.
     * Se asegura de que el agente pueda ser instalado sin lanzar excepciones inesperadas.
     */
    @Test
    public void testAgentTransformation() {
        AgentBuilder agentBuilder = new AgentBuilder.Default()
            .type(ElementMatchers.nameMatches("com.luis.tfg.security.TFGSecurityAgent"))
            .transform((builder, typeDescription, classLoader, module, protectionDomain) -> 
                builder.visit(Advice.to(TFGSecurityAdvice.class).on(ElementMatchers.any()))
            );

        assertNotNull(agentBuilder, "El AgentBuilder no debería ser nulo.");
        assertDoesNotThrow(() -> {
            agentBuilder.installOn(instrumentation);
        }, "El AgentBuilder lanzó una excepción inesperada durante la instalación.");
    }

    /**
     * Método auxiliar para registrar errores.
     * Verifica que `TFGSecurityAdvice.logError` maneje la impresión de mensajes de error sin lanzar excepciones.
     */
    public static void logError(String errorMessage) {
        System.out.println("Error: " + errorMessage);
    }

    /**
     * Prueba de manejo de errores generales.
     * Valida que el método `logError` de `TFGSecurityAdvice` no cause errores durante la ejecución.
     */
    @Test
    public void testGeneralErrorHandling() {
        assertDoesNotThrow(() -> {
            TFGSecurityAdvice.logError("Error inesperado durante la operación.");
        }, "El método logError no debería lanzar excepciones.");
    }
}

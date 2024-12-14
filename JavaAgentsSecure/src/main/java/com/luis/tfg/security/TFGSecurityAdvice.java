package com.luis.tfg.security;

import net.bytebuddy.asm.Advice;
import org.apache.commons.io.FilenameUtils;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.errors.IntrusionException;
import org.owasp.esapi.errors.ValidationException;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Base64;
import java.util.regex.Pattern;
/**
 * La clase TFGSecurityAdvice se utiliza para proporcionar consejos de seguridad en métodos 
 * usando ByteBuddy. Este consejo se aplica para detectar posibles vulnerabilidades como 
 * inyecciones SQL, manejo de datos sensibles, traversal de rutas, uso de criptografía débil, 
 * inyecciones de código y consultas SQL inseguras.
 */
public class TFGSecurityAdvice {

    /**
     * Método de entrada que se ejecuta al inicio del método objetivo. Analiza los argumentos para detectar
     * posibles vulnerabilidades de seguridad.
     * 
     * @param method El nombre del método que se está analizando.
     * @param args   Los argumentos del método.
     */
    @Advice.OnMethodEnter
    public static void onEnter(@Advice.Origin String method, @Advice.AllArguments Object[] args) {
        System.out.println("[INFO] Analizando seguridad en método: " + method);
        for (Object arg : args) {
            if (arg instanceof String) {
                String input = (String) arg;

                detectSQLInjection(method, input);
                if (isSensitiveData(input)) {
                    System.out.println("[ALERTA] Datos sensibles manejados en: " + method + " con valor: " + input);
                }
                if (isPotentialPathTraversal(input)) {
                    System.out.println("[ALERTA] Potencial vulnerabilidad de Path Traversal en: " + method + " con valor: " + input);
                }
                detectWeakCryptography(method, input);
                detectCodeInjection(method, input);
            }
            if (arg instanceof Statement || arg instanceof PreparedStatement) {
                detectUnsafeSQLStatements(method, arg);
            }
            detectUnsafeDeserialization(method, arg);
        }
    }

    /**
     * Registra un mensaje de error en el sistema.
     *
     * @param errorMessage El mensaje de error.
     */
    public static void logError(String errorMessage) {
        System.out.println("Error: " + errorMessage);
    }

    /**
     * Método de salida que captura cualquier excepción lanzada durante la ejecución del método objetivo.
     * 
     * @param method    El nombre del método que se está analizando.
     * @param throwable La excepción lanzada, si existe.
     */
    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void onExit(@Advice.Origin String method, @Advice.Thrown Throwable throwable) {
        if (throwable != null) {
            System.out.println("[ALERTA] Excepción detectada en método: " + method + " - " + throwable);
        }
    }

    // Métodos privados para la detección de diferentes tipos de vulnerabilidades:
    
    /**
     * Detecta posibles inyecciones SQL.
     * 
     * @param method El nombre del método que se está analizando.
     * @param input  El argumento a analizar.
     */
    public static void detectSQLInjection(String method, String input) {
        try {
            if (Pattern.compile("(?i).*\\b(SELECT|INSERT|DELETE|UPDATE|DROP|ALTER)\\b.*").matcher(input).find()) {
                throw new ValidationException("Posible Inyección SQL detectada", "El input contiene posibles comandos SQL.");
            }
            ESAPI.validator().getValidInput("SQL input", input, "SQL", 200, false);
        } catch (ValidationException | IntrusionException e) {
            logError("[ALERTA] Posible inyección SQL detectada en: " + method + " con valor: " + input);
        }
    }

    /**
     * Determina si los datos son sensibles.
     * 
     * @param data Los datos a analizar.
     * @return Verdadero si se detectan datos sensibles.
     */
    private static boolean isSensitiveData(String data) {
        return data.matches(".*password.*|.*ssn.*|.*credit.*|.*card.*|.*cvv.*|.*secret.*|.*api_key.*");
    }

    /**
     * Detecta posibles ataques de path traversal.
     * 
     * @param path El path a analizar.
     * @return Verdadero si se detecta un path traversal.
     */
    public static boolean isPotentialPathTraversal(String path) {
        if (path == null || path.trim().isEmpty()) {
            return false;
        }
        try {
            String normalizedPath = FilenameUtils.normalizeNoEndSeparator(path);
            if (normalizedPath == null || path.contains("../") || path.contains("..\\")) {
                return true;
            }
            File file = new File(path).getCanonicalFile();
            return !file.getPath().equals(new File(normalizedPath).getCanonicalPath());
        } catch (IOException e) {
            logError("Error al procesar el path: " + e.getMessage());
            return true;
        }
    }

    /**
     * Detecta el uso de algoritmos de criptografía débil.
     * 
     * @param method El nombre del método que se está analizando.
     * @param input  El argumento a analizar.
     */
    public static void detectWeakCryptography(String method, String input) {
        if (isWeakCryptoAlgorithm(input)) {
            logError("[ALERTA] Posible uso de criptografía débil en: " + method + " con valor: " + input);
        } else if (input.equalsIgnoreCase("safeCrypto")) {
            throw new IllegalArgumentException("Criptografía segura, no debería ser detectada como insegura.");
        } else {
            try {
                Base64.getDecoder().decode(input);
            } catch (IllegalArgumentException e) {
                logError("[INFO] El input no es un string Base64 válido, no se considera como criptografía.");
            }
        }
    }

    /**
     * Verifica si el algoritmo de criptografía es débil.
     * 
     * @param input El nombre del algoritmo.
     * @return Verdadero si el algoritmo es considerado débil.
     */
    private static boolean isWeakCryptoAlgorithm(String input) {
        return input.equalsIgnoreCase("ssl3") || input.equalsIgnoreCase("des");
    }

    /**
     * Detecta posibles inyecciones de código.
     * 
     * @param method El nombre del método que se está analizando.
     * @param input  El argumento a analizar.
     */
    private static void detectCodeInjection(String method, String input) {
        if (Pattern.compile(".*(exec\\(|system\\(|Runtime\\.getRuntime\\(\\)|ProcessBuilder).*", Pattern.CASE_INSENSITIVE).matcher(input).find()) {
            System.out.println("[ALERTA] Posible inyección de código o comandos detectada en: " + method + " con valor: " + input);
        }
    }

    /**
     * Detecta consultas SQL inseguras.
     * 
     * @param method    El nombre del método que se está analizando.
     * @param statement El objeto Statement a analizar.
     */
    private static void detectUnsafeSQLStatements(String method, Object statement) {
        String query = extractQuery(statement);
        if (query != null && isPotentiallyUnsafeQuery(query)) {
            System.out.println("[ALERTA] Consulta SQL potencialmente insegura: " + query);
        }
    }

    /**
     * Extrae la consulta SQL de un PreparedStatement o Statement.
     * 
     * @param statement El objeto Statement.
     * @return La consulta SQL como String.
     */
    private static String extractQuery(Object statement) {
        if (statement instanceof PreparedStatement) {
            try {
                return ((PreparedStatement) statement).toString();
            } catch (Exception e) {
                System.out.println("[ERROR] No se pudo extraer la consulta del PreparedStatement: " + e.getMessage());
            }
        } else if (statement instanceof Statement) {
            try {
                return ((Statement) statement).toString();
            } catch (Exception e) {
                System.out.println("[ERROR] No se pudo extraer la consulta del Statement: " + e.getMessage());
            }
        }
        return null;
    }

    /**
     * Verifica si una consulta SQL es potencialmente insegura.
     * 
     * @param query La consulta SQL a analizar.
     * @return Verdadero si la consulta contiene patrones inseguros.
     */
    private static boolean isPotentiallyUnsafeQuery(String query) {
        return Pattern.compile(".*\\b(union|select|insert|update|delete)\\b.*", Pattern.CASE_INSENSITIVE).matcher(query).find();
    }

    /**
     * Detecta posibles deserializaciones inseguras.
     * 
     * @param method El nombre del método que se está analizando.
     * @param arg    El argumento a analizar.
     */
    private static void detectUnsafeDeserialization(String method, Object arg) {
        if (arg instanceof java.io.ObjectInputStream) {
            System.out.println("[ALERTA] Posible deserialización insegura detectada en: " + method);
        }
    }
}

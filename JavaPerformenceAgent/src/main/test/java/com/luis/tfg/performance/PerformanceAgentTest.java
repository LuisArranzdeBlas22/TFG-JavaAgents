package com.luis.tfg.performance;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import com.luis.tfg.performance.MockInstrumentation;

import java.lang.instrument.Instrumentation;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class PerformanceAgentTest {

    private static Instrumentation instrumentation;
    private static final Logger logger = Logger.getLogger(PerformanceAgentTest.class.getName());

    /**
     * Configura el agente de rendimiento utilizando `MockInstrumentation`.
     * Este método se ejecuta una vez antes de todas las pruebas.
     */
    @BeforeAll
    public static void setupAgent() {
        logger.info("Inicializando el agente de rendimiento para pruebas.");
        instrumentation = MockInstrumentation.getInstrumentation();
    }

    /**
     * Configuración previa a cada prueba individual.
     * Este método se ejecuta antes de cada prueba y permite inicializar
     * recursos específicos.
     */
    @BeforeEach
    public void setup() {
        logger.info("Configuración antes de cada prueba.");
    }

    /**
     * Limpieza de recursos después de cada prueba individual.
     * Se utiliza para liberar o resetear cualquier recurso utilizado.
     */
    @AfterEach
    public void tearDown() {
        logger.info("Limpieza después de cada prueba.");
    }

    /**
     * Prueba de rendimiento que verifica que el método anotado con `@MedirTiempo`
     * mide el tiempo de ejecución de `metodoLento`. La prueba pasa si la duración
     * es de al menos 200 ms.
     */
    @Test
    public void testMedirTiempo() {
        class TestClass {
            @MedirTiempo
            public void metodoLento() {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        TestClass testClass = new TestClass();
        long start = System.nanoTime();
        testClass.metodoLento();
        long end = System.nanoTime();

        long duration = (end - start) / 1_000_000;
        assertTrue(duration >= 200, "El tiempo de ejecución fue menor al esperado.");
    }

    /**
     * Prueba el monitoreo de recursos usando la anotación `@MonitorearRecursos`.
     * Simula el consumo de memoria mediante un array de 50 MB y asegura que no se lancen excepciones.
     */
    @Test
    public void testMonitorearRecursos() {
        class TestClass {
            @MonitorearRecursos
            public void ejecutarOperacion() {
                byte[] data = new byte[1024 * 1024 * 50];
                data[0] = 1;
            }
        }

        TestClass testClass = new TestClass();
        assertDoesNotThrow(() -> testClass.ejecutarOperacion(), "El monitoreo de recursos debería funcionar sin lanzar excepciones.");
    }

    /**
     * Verifica la creación y monitoreo de hilos con `@MonitorearHilos`.
     * Se crean dos hilos que simulan una operación de 100 ms cada uno.
     */
    @Test
    public void testMonitorearHilos() {
        class TestClass {
            @MonitorearHilos
            public void crearHilos() {
                Thread t1 = new Thread(() -> {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
                Thread t2 = new Thread(() -> {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });

                t1.start();
                t2.start();

                try {
                    t1.join();
                    t2.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        TestClass testClass = new TestClass();
        assertDoesNotThrow(() -> testClass.crearHilos(), "La creación de hilos debería monitorearse sin lanzar excepciones.");
    }

    /**
     * Configura y verifica la instalación del `AgentBuilder` con las anotaciones
     * `@MedirTiempo`, `@MonitorearRecursos` y `@MonitorearHilos`. 
     * La prueba pasa si el agente se instala sin lanzar excepciones.
     */
    @Test
    public void testAgentInstallation() {
        AgentBuilder agentBuilder = new AgentBuilder.Default()
                .type(ElementMatchers.isAnnotatedWith(MedirTiempo.class)
                    .or(ElementMatchers.isAnnotatedWith(MonitorearRecursos.class))
                    .or(ElementMatchers.isAnnotatedWith(MonitorearHilos.class)))
                .transform((builder, typeDescription, classLoader, module, protectionDomain) -> 
                    builder.visit(Advice.to(MonitoringAdvice.class).on(ElementMatchers.any()))
                );

        assertNotNull(agentBuilder, "El AgentBuilder no debería ser nulo.");
        assertDoesNotThrow(() -> {
            agentBuilder.installOn(instrumentation);
        }, "El AgentBuilder lanzó una excepción inesperada durante la instalación.");
    }

    /**
     * Verifica que el método `logError` de `MonitoringAdvice` maneje errores
     * sin lanzar excepciones.
     */
    @Test
    public void testErrorHandling() {
        assertDoesNotThrow(() -> {
            MonitoringAdvice.logError("Error inesperado durante la operación.");
        }, "El método logError no debería lanzar excepciones.");
    }
}

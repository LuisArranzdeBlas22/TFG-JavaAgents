package com.luis.tfg.ipfilter;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.Method;

/**
 * Pruebas para el agente de filtrado de IP, verificando la funcionalidad de listas
 * blancas y negras.
 */
public class IPFilterAgentTest {

    private AgentConfig agentConfig;

    /**
     * Configura las listas de IP antes de cada prueba.
     */
    @BeforeEach
    public void setUp() {
        agentConfig = new AgentConfig("ip_filter.config");
        agentConfig.addToWhitelist("192.168.1.1");
        agentConfig.addToBlacklist("192.168.1.1");
    }

    /**
     * Limpia las listas de IP después de cada prueba.
     */
    @AfterEach
    public void tearDown() {
        agentConfig.removeFromWhitelist("192.168.1.1");
        agentConfig.removeFromBlacklist("192.168.1.1");
    }

    /**
     * Verifica el acceso permitido para una IP en la lista blanca.
     */
    @Test
    public void testAllowAccessForWhitelistedIP() {
        IPFilterAdvice.setIpProvider(() -> "192.168.1.1");
        try {
            Method method = TestMethodWithWhitelist.class.getMethod("exampleMethod");
            IPFilterAdvice.checkIP(method);
        } catch (Exception e) {
            fail("El acceso debería estar permitido para una IP en la whitelist.");
        }
    }

    /**
     * Verifica que se deniegue el acceso para una IP que no está en la lista blanca.
     */
    @Test
    public void testDenyAccessForNonWhitelistedIP() throws NoSuchMethodException {
        IPFilterAdvice.setIpProvider(() -> "192.168.1.2"); 
        Method method = TestMethodWithWhitelist.class.getMethod("exampleMethod");

        assertThrows(SecurityException.class, () ->
            IPFilterAdvice.checkIP(method),
            "El acceso debería estar denegado para una IP que no está en la whitelist."
        );
    }

    /**
     * Verifica que se deniegue el acceso para una IP que está en la lista negra.
     */
    @Test
    public void testDenyAccessForBlacklistedIP() throws NoSuchMethodException {
        IPFilterAdvice.setIpProvider(() -> "192.168.1.1");
        Method method = TestMethodWithBlacklist.class.getMethod("exampleMethod");

        assertThrows(SecurityException.class, () ->
            IPFilterAdvice.checkIP(method),
            "El acceso debería estar denegado para una IP en la blacklist."
        );
    }

    /**
     * Verifica el acceso permitido para una IP que no está en la lista negra.
     */
    @Test
    public void testAllowAccessForNonBlacklistedIP() throws NoSuchMethodException {
        IPFilterAdvice.setIpProvider(() -> "192.168.1.2"); 
        Method method = TestMethodWithBlacklist.class.getMethod("exampleMethod");

        try {
            IPFilterAdvice.checkIP(method);
        } catch (SecurityException e) {
            fail("El acceso debería estar permitido para una IP que no está en la blacklist.");
        }
    }

    /**
     * Prueba la eliminación de una IP de la lista blanca.
     */
    @Test
    public void testRemoveIPFromWhitelist() {
        agentConfig.addToWhitelist("192.168.1.3");
        assertTrue(agentConfig.getWhitelist().contains("192.168.1.3"), "La IP debería estar en la whitelist.");

        agentConfig.removeFromWhitelist("192.168.1.3");
        assertFalse(agentConfig.getWhitelist().contains("192.168.1.3"), "La IP no debería estar en la whitelist después de eliminarla.");
    }

    /**
     * Prueba la eliminación de una IP de la lista negra.
     */
    @Test
    public void testRemoveIPFromBlacklist() {
        agentConfig.addToBlacklist("192.168.1.4");
        assertTrue(agentConfig.getBlacklist().contains("192.168.1.4"), "La IP debería estar en la blacklist.");

        agentConfig.removeFromBlacklist("192.168.1.4");
        assertFalse(agentConfig.getBlacklist().contains("192.168.1.4"), "La IP no debería estar en la blacklist después de eliminarla.");
    }

    /**
     * Prueba la adición y persistencia de una IP en la lista blanca.
     */
    @Test
    public void testAddAndPersistWhitelistIP() {
        agentConfig.addToWhitelist("192.168.1.5");
        assertTrue(agentConfig.getWhitelist().contains("192.168.1.5"), "La IP debería haberse añadido a la whitelist.");
    }

    /**
     * Prueba la adición y persistencia de una IP en la lista negra.
     */
    @Test
    public void testAddAndPersistBlacklistIP() {
        agentConfig.addToBlacklist("192.168.1.6");
        assertTrue(agentConfig.getBlacklist().contains("192.168.1.6"), "La IP debería haberse añadido a la blacklist.");
    }

    /**
     * Clase interna con método anotado para pruebas de lista blanca.
     */
    public static class TestMethodWithWhitelist {
        @WhitelistIP
        public void exampleMethod() {} 
    }

    /**
     * Clase interna con método anotado para pruebas de lista negra.
     */
    public static class TestMethodWithBlacklist {
        @BlacklistIP
        public void exampleMethod() {} 
    }
}

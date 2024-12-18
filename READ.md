
# TFG-JavaAgents: Agentes Java para Validación, Seguridad y Monitoreo

Este proyecto, desarrollado como parte de un Trabajo de Fin de Grado, implementa cuatro agentes Java diseñados para instrumentación en tiempo de ejecución. Los agentes tienen el propósito de mejorar la seguridad, el rendimiento y la gestión lógica de aplicaciones Java sin necesidad de modificar el código fuente original.

## Agentes Implementados

- **MethodOrderAgent**  
  Garantiza que los métodos de una clase se ejecuten en el orden correcto especificado mediante la anotación `@ExecutionOrder`. Este agente es ideal para flujos de trabajo estrictos y pipelines de datos.

- **IPFilterAgent**  
  Proporciona control de acceso basado en direcciones IP, utilizando listas de control dinámicas (whitelist y blacklist). Es útil para proteger APIs y restringir accesos no autorizados.

- **PerformanceAgent**  
  Monitorea en tiempo real el rendimiento de las aplicaciones Java, midiendo tiempos de ejecución, uso de recursos (CPU, memoria) y gestión de hilos, todo mediante anotaciones como `@MedirTiempo`.

- **TFGSecurityAgent**  
  Detecta y mitiga vulnerabilidades de seguridad como inyecciones SQL, exposición de datos sensibles y uso de criptografía débil. Se apoya en OWASP ESAPI para validaciones avanzadas.

## Características Principales

- **Instrumentación Dinámica:** Los agentes utilizan ByteBuddy para interceptar métodos y aplicar transformaciones en tiempo de ejecución.
- **No Invasivo:** Se integran en aplicaciones Java sin modificar su código fuente.
- **Anotaciones Personalizadas:** Cada agente utiliza anotaciones específicas (`@ExecutionOrder`, `@WhitelistIP`, `@MedirTiempo`, etc.) para facilitar la configuración.
- **Flexibilidad:** Configuración dinámica mediante archivos (`ip_filter.config`) y reglas personalizadas.

## Requisitos del Sistema

- **Java:** JDK 11 o superior.
- **Herramientas:**
  - Maven para la gestión de dependencias y construcción del proyecto.
  - ByteBuddy para la instrumentación dinámica.
  - JUnit para pruebas automatizadas.

## Instalación

1. **Clonar el Repositorio:**  
   ```bash
   git clone https://github.com/LuisArranzdeBlas22/TFG-JavaAgents.git
   cd TFG-JavaAgents
   ```

2. **Construir el Proyecto con Maven:**  
   ```bash
   mvn clean package
   ```

3. **Configurar el Agente (opcional):**  
   Algunos agentes, como `IPFilterAgent`, requieren configurar listas de control en `ip_filter.config`, que se generará automáticamente la primera vez que se ejecute el agente.

## Ejecución

Cada agente puede ejecutarse de forma independiente. A continuación, se explican los pasos generales para ejecutar un agente con una aplicación Java:

1. **Agregar el Agente:**  
   Utiliza el siguiente comando para ejecutar tu aplicación con el agente correspondiente:  
   ```bash
   java -javaagent:target/[AGENTE].jar -jar [APLICACION].jar
   ```
   Por ejemplo, para ejecutar el `MethodOrderAgent`:  
   ```bash
   java -javaagent:target/MethodExecutionAgent-1.0-SNAPSHOT.jar -jar myApp.jar
   ```

2. **Pruebas Automatizadas:**  
   Ejecuta las pruebas con Maven:  
   ```bash
   mvn test
   ```

## Documentación de los Agentes

### 1. MethodOrderAgent
Permite garantizar la correcta secuencialidad de los métodos definidos mediante la anotación `@ExecutionOrder`.  
Por ejemplo:  
```java
@ExecutionOrder("(start process end)")
public class Workflow {
    public void start() { /* ... */ }
    public void process() { /* ... */ }
    public void end() { /* ... */ }
}
```

### 2. IPFilterAgent
Controla el acceso a métodos según la IP de origen mediante anotaciones como `@WhitelistIP` y `@BlacklistIP`.  
Ejemplo:  
```java
@WhitelistIP
public void metodoProtegido() {
    System.out.println("Acceso permitido");
}
```

### 3. PerformanceAgent
Mide tiempos de ejecución y supervisa recursos (CPU, memoria). Ideal para sistemas críticos:  
```java
@MedirTiempo
public void procesarDatos() {
    // Código intensivo en cálculos
}
```

### 4. TFGSecurityAgent
Detecta vulnerabilidades como inyecciones SQL y criptografía débil:  
```java
@SecureClass
public class UserController {
    public void handleUserInput(String input) {
        System.out.println("Procesando entrada: " + input);
    }
}
```

## Posibles Aplicaciones

- Validación de flujos de trabajo complejos: `MethodOrderAgent`.
- Protección de APIs y sistemas críticos: `IPFilterAgent`.
- Optimización en tiempo real: `PerformanceAgent`.
- Detección y mitigación de vulnerabilidades: `TFGSecurityAgent`.

## Contribuciones

Este proyecto está en continuo desarrollo. Se aceptan contribuciones para mejorar la funcionalidad y extender las capacidades de los agentes.
package com.luis.tfg.ExecutionOrder;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;

/**
 * MockInstrumentation es una implementación simulada de la interfaz `Instrumentation` de Java.
 * Esta clase se utiliza para pruebas y simulaciones sin tener que aplicar cambios reales 
 * a las clases o módulos del sistema.
 * 
 * Métodos importantes:
 * 
 * - `addTransformer(ClassFileTransformer transformer)`: Añade un transformador de clase a la lista.
 * - `removeTransformer(ClassFileTransformer transformer)`: Elimina un transformador de la lista.
 * - `redefineClasses`, `retransformClasses`: Métodos simulados que no realizan ninguna acción en esta implementación.
 * - `isModifiableClass`, `isModifiableModule`: Devuelven `true`, indicando que las clases y módulos pueden ser modificados.
 * 
 * Otros métodos como `appendToSystemClassLoaderSearch` y `setNativeMethodPrefix` están presentes 
 * para cumplir con la interfaz, pero no tienen implementación real en esta clase simulada.
 * 
 * Esta clase permite probar la funcionalidad de agentes Java y transformadores de clases 
 * sin necesidad de cambios reales, simulando el comportamiento del Instrumentation real.
 */
public class MockInstrumentation implements Instrumentation {
    private final List<ClassFileTransformer> transformers = new ArrayList<>();

    @Override
    public void addTransformer(ClassFileTransformer transformer, boolean canRetransform) {
        transformers.add(transformer);
    }

    @Override
    public void addTransformer(ClassFileTransformer transformer) {
        transformers.add(transformer);
    }

    @Override
    public boolean removeTransformer(ClassFileTransformer transformer) {
        return transformers.remove(transformer);
    }

    @Override
    public boolean isRetransformClassesSupported() {
        return true;
    }

    @Override
    public void retransformClasses(Class<?>... classes) throws UnmodifiableClassException {
        // No-op mock implementation
    }

    @Override
    public boolean isRedefineClassesSupported() {
        return true;
    }

    @Override
    public void redefineClasses(ClassDefinition... definitions) throws ClassNotFoundException, UnmodifiableClassException {
        // No-op mock implementation
    }

    @Override
    public boolean isModifiableClass(Class<?> theClass) {
        return true;
    }

    @Override
    public Class[] getAllLoadedClasses() {
        return new Class<?>[0];
    }

    @Override
    public Class[] getInitiatedClasses(ClassLoader loader) {
        return new Class<?>[0];
    }

    @Override
    public long getObjectSize(Object objectToSize) {
        return 0;
    }

    @Override
    public void appendToSystemClassLoaderSearch(JarFile jarfile) {
        // No-op mock implementation
    }

    @Override
    public void appendToBootstrapClassLoaderSearch(JarFile jarfile) {
        // No-op mock implementation
    }

    @Override
    public boolean isNativeMethodPrefixSupported() {
        return true;
    }

    @Override
    public void setNativeMethodPrefix(ClassFileTransformer transformer, String prefix) {
        // No-op mock implementation
    }

    @Override
    public boolean isModifiableModule(Module module) {
        return true; // Mock implementation
    }

	@Override
	public void redefineModule(Module arg0, Set<Module> arg1, Map<String, Set<Module>> arg2,
			Map<String, Set<Module>> arg3, Set<Class<?>> arg4, Map<Class<?>, List<Class<?>>> arg5) {
		// TODO Auto-generated method stub
		
	}

	public static Instrumentation getInstrumentation() {
	    return new MockInstrumentation(); // Retornar una instancia de MockInstrumentation
	}

}

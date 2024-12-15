package com.luis.tfg.ExecutionOrder;

import org.junit.jupiter.api.Test;

import com.luis.tfg.ExecutionOrder.ConstructorAdvice;
import com.luis.tfg.ExecutionOrder.ExecutionOrder;
import com.luis.tfg.ExecutionOrder.MethodOrderAdvice;

import net.bytebuddy.matcher.ElementMatchers;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.instrument.Instrumentation;

public class MethodExecutionAgentTest {
	
	public static void main(String[] args) {
	    System.out.println("Iniciando pruebas manuales...");
	    MethodExecutionAgentTest test = new MethodExecutionAgentTest();
	    test.testCorrectOrderExecution(); // Llama a tu método de prueba
	    test.testIncorrectOrderExecution(); // Llama a tu otro método de prueba
	    System.out.println("Pruebas completadas.");
	}

	@ExecutionOrder("(addToCart checkout payment confirmation)")
    public static class TestClass {
		String id;
		public TestClass(String id) {
			this.id=id;
		}
		
        public void addToCart() {
            System.out.println("Executing addToCart");
        }

        public void checkout() {
            System.out.println("Executing checkout");
        }

        public void payment() {
            System.out.println("Executing payment");
        }

        public void confirmation() {
            System.out.println("Executing confirmation");
        }
        
        @Override
        public String toString() {
            return "TestClass{id='" + id + "'}";
        }
    }

    @Test
    public void testCorrectOrderExecution() {
        System.out.println("Iniciando testCorrectOrderExecution...");
        TestClass testInstance = new TestClass("id1");
        testInstance.addToCart();
        testInstance.checkout();
        testInstance.payment();
        testInstance.confirmation();
    }

    @Test
    public void testIncorrectOrderExecution() {
        System.out.println("Iniciando testIncorrectOrderExecution...");
        TestClass testInstance2 = new TestClass("id2");

        // Aquí esperamos que falle en el orden incorrecto
        assertThrows(SecurityException.class, () -> {
            testInstance2.addToCart();
            testInstance2.confirmation(); // Esto debería lanzar una excepción
        }, "No se lanzó SecurityException cuando se llamó a los métodos en un orden incorrecto");
    }

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("Registrando agente para los tests...");
        new net.bytebuddy.agent.builder.AgentBuilder.Default()
                .type(net.bytebuddy.matcher.ElementMatchers.isAnnotatedWith(ExecutionOrder.class))
                .transform((builder, typeDescription, classLoader, module, protectionDomain) ->
                builder
                    .constructor(ElementMatchers.any()) // Interceptar constructores
                    .intercept(net.bytebuddy.asm.Advice.to(ConstructorAdvice.class))
                    .method(ElementMatchers.not(ElementMatchers.named("toString"))) // Excluir toString()
                    .intercept(net.bytebuddy.asm.Advice.to(MethodOrderAdvice.class))
            )
                .installOn(inst);
    }


}

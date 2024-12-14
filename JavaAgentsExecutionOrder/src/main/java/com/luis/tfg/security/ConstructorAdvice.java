package com.luis.tfg.security;

import net.bytebuddy.asm.Advice;

public class ConstructorAdvice {

    @Advice.OnMethodExit
    public static void onConstructorExit(@Advice.This Object instance,
                                         @Advice.Origin("#t") String className) {
        System.out.println("[INFO] Constructor intercepted for instance: " + instance + " of class: " + className);

        // Obtener la anotación ExecutionOrder
        ExecutionOrder executionOrder = instance.getClass().getAnnotation(ExecutionOrder.class);
        if (executionOrder == null) {
            System.out.println("[WARNING] No @ExecutionOrder annotation found for class: " + className);
            return;
        }

        // Registrar en el ExecutionOrderRegistry
        String executionOrderValue = executionOrder.value();
        System.out.println("[INFO] @ExecutionOrder value: " + executionOrderValue);

        ExecutionOrderRegistry.register(instance, executionOrderValue);

        // Verificar el registro
        StateMachine stateMachine = ExecutionOrderRegistry.getStateMachine(instance);
        if (stateMachine == null) {
            System.out.println("[ERROR] StateMachine registration failed for instance: " + instance);
            throw new IllegalStateException("Failed to register StateMachine for instance: " + instance);
        } else {
            System.out.println("[SUCCESS] StateMachine registered successfully for instance: " + instance);
        }
    }
}
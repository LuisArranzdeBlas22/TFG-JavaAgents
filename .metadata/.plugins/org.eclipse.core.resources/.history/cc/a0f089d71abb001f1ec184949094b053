package com.luis.tfg.security;

import net.bytebuddy.asm.Advice;

public class MethodOrderAdvice {

    @Advice.OnMethodEnter
    public static void onMethodEnter(@Advice.This Object instance,
                                     @Advice.Origin("#m") String methodName) {
        System.out.println("[INFO] Intercepted method: " + methodName + " in instance: " + instance);

        StateMachine stateMachine = ExecutionOrderRegistry.getStateMachine(instance);
        if (stateMachine == null) {
            System.out.println("[ERROR] StateMachine NOT FOUND for instance: " + instance);
            throw new IllegalStateException("StateMachine not found for instance: " + instance);
        }

        try {
            stateMachine.validateTransition(methodName);
        } catch (IllegalStateException e) {
            System.out.println("[ERROR] Invalid method order: " + methodName + " for instance: " + instance);
            throw new SecurityException("Invalid transition from current state in StateMachine", e);
        }
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void onMethodExit(@Advice.This Object instance) {
        System.out.println("[INFO] Exiting method in instance: " + instance);

        StateMachine stateMachine = ExecutionOrderRegistry.getStateMachine(instance);
        if (stateMachine == null) {
            System.out.println("[ERROR] StateMachine NOT FOUND during method exit for instance: " + instance);
            return;
        }

        if (stateMachine.isInFinalState()) {
            System.out.println("[SUCCESS] Instance finished in a valid final state: " + instance);
        } else {
            System.out.println("[ERROR] Instance did not finish in a valid final state. Current state: "
                    + stateMachine.getCurrentState());
        }
    }
}
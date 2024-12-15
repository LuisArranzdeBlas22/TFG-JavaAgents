package com.luis.tfg.ExecutionOrder;

import java.util.concurrent.ConcurrentHashMap;

public class ExecutionOrderRegistry {

    private static final ConcurrentHashMap<Object, StateMachine> stateMachineMap = new ConcurrentHashMap<>();

    public static void register(Object instance, String executionOrderRegex) {
        System.out.println("[INFO] Registering StateMachine for instance: " + instance);

        // Evitar duplicados
        if (stateMachineMap.containsKey(instance)) {
            System.out.println("[WARNING] StateMachine already exists for instance: " + instance);
            return;
        }

        // Crear y registrar StateMachine
        StateMachine stateMachine = new StateMachine(executionOrderRegex);
        stateMachineMap.put(instance, stateMachine);

        System.out.println("[INFO] StateMachine registered for instance: " + instance + " with transitions: "
                + stateMachine.getTransitions());
        printStateMachineMap();
    }

    public static StateMachine getStateMachine(Object instance) {
        System.out.println("[INFO] Fetching StateMachine for instance: " + instance);
        StateMachine stateMachine = stateMachineMap.get(instance);

        if (stateMachine == null) {
            System.out.println("[ERROR] StateMachine NOT FOUND for instance: " + instance);
        } else {
            System.out.println("[INFO] StateMachine fetched for instance: " + instance);
        }

        return stateMachine;
    }
    
    public static void validateInstance(Object instance) {
        if (!stateMachineMap.containsKey(instance)) {
            System.out.println("[ERROR] Instance not registered: " + instance);
            System.out.println("[INFO] Available instances in registry: ");
            stateMachineMap.keySet().forEach(key -> System.out.println("    " + key));
        }
    }


    public static void printStateMachineMap() {
        System.out.println("[INFO] Current StateMachineMap content:");
        stateMachineMap.forEach((key, value) -> {
            System.out.println("    Instance: " + key + " -> StateMachine: " + value);
        });
    }
}
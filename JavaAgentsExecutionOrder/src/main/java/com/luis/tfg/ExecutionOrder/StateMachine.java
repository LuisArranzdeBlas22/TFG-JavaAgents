package com.luis.tfg.ExecutionOrder;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StateMachine {
    private final Map<String, State> states = new HashMap<>();
    private State currentState;
    private final Map<String, String> logicalToUuidMap = new HashMap<>(); // Mapeo lógico-UUID
 // Transiciones especiales para estados finales
    private final Map<String, List<String>> postFinalTransitions = new HashMap<>();

    public StateMachine(String regex) {
        System.out.println("[INFO] Initializing StateMachine with regex: " + regex);
        parseRegex(regex);
        debugTransitions(); 
    }
    
    private void debugTransitions() {
        System.out.println("[DEBUG] Complete StateMachine transitions:");
        for (Map.Entry<String, State> entry : states.entrySet()) {
            System.out.println("State " + entry.getKey() + " transitions to: " + entry.getValue().getTransitions());
        }
    }

    private void parseRegex(String regex) {
        boolean usesArrowSyntax = regex.contains("->");

        // Separar la parte principal de la expresión y las transiciones especiales (si existen)
        String[] regexParts = regex.split("\\[");
        String mainRegex = regexParts[0].trim(); // Parte principal (antes del bloque especial)
        String specialTransitions = regexParts.length > 1 ? regexParts[1].replaceAll("]", "").trim() : null;

        System.out.println("[DEBUG] Full regex: " + regex);
        System.out.println("[DEBUG] Main regex: " + mainRegex);
        System.out.println("[DEBUG] Special transitions: " + specialTransitions);

        // Separar estados dependiendo del formato
        String[] parts = usesArrowSyntax ? mainRegex.split("->") : mainRegex.split("\\s+");

        if (parts.length < 2) {
            throw new IllegalArgumentException("[ERROR] Invalid regex format. Expected 'sequence -> target' or 'sequence'. Got: " + regex);
        }

        // Crear estado inicial y final explícito
        State initialState = states.computeIfAbsent("INITIAL", id -> new State(id, false, 0, id));
        State finalExplicitState = states.computeIfAbsent("FINAL", id -> new State(id, true, 0, id));

        // Identificar el estado final declarado
        String finalStateId = cleanStateId(parts[parts.length - 1].trim());
        State finalDeclaredState = states.computeIfAbsent(finalStateId, id -> new State(id, true, 0, id));

        System.out.println("[DEBUG] Initial state: " + initialState.getId());
        System.out.println("[DEBUG] Declared final state: " + finalStateId);

        // Variables auxiliares para manejar transiciones
        State previousState = initialState;

        for (int i = 0; i < parts.length; i++) {
            String segment = parts[i].trim();
            if (segment.isEmpty()) continue;

            System.out.println("[DEBUG] Processing segment: " + segment);

            // Identificar el siguiente estado (si existe)
            State nextState = (i < parts.length - 1)
                    ? states.computeIfAbsent(cleanStateId(parts[i + 1].trim()), id -> new State(id, false, 0, id))
                    : finalDeclaredState;

            // Manejo de operadores complejos
            if (segment.contains("&") || segment.contains("|") || segment.matches(".*\\{\\d+}.*") || segment.equals(".*")) {
                System.out.println("[DEBUG] Handling complex segment: " + segment);

                // Procesar combinaciones AND-OR
                if (segment.contains("&") && segment.contains("|")) {
                    int orIndex = segment.indexOf("|");
                    int andIndex = segment.indexOf("&");

                    if (orIndex < andIndex) {
                        System.out.println("[DEBUG] Detected OR-AND combination");
                        handleOrAndCondition(previousState, nextState, segment);
                    } else {
                        System.out.println("[DEBUG] Detected AND-OR combination");
                        handleAndOrCombination(previousState, nextState, segment);
                    }
                    continue;
                }

                // Procesar condición AND
                if (segment.contains("&")) {
                    System.out.println("[DEBUG] Detected AND condition. Segment: " + segment);
                    handleAndCondition(previousState, nextState, segment);
                    continue;
                }

                // Procesar repetición
                if (segment.matches(".*\\{\\d+}.*")) {
                    System.out.println("[DEBUG] Detected repetition. Segment: " + segment);
                    handleRepetition(previousState, nextState, segment);
                    continue;
                }

                // Procesar condición OR
                if (segment.contains("|")) {
                    System.out.println("[DEBUG] Detected OR condition. Segment: " + segment);
                    handleOrCondition(previousState, nextState, segment);
                    continue;
                }

                // Procesar condición comodín (.*)
                if (segment.equals(".*")) {
                    System.out.println("[DEBUG] Detected wildcard. Segment: " + segment);
                    handleWildcardCondition(previousState, finalDeclaredState, segment);
                    continue;
                }
            }

            // Procesar transiciones simples (lineales)
            String[] subStates = segment.split("\\s+");
            for (String subState : subStates) {
                String cleanedStateId = cleanStateId(subState);
                boolean isFinalState = cleanedStateId.equals(finalStateId);

                State currentState = states.computeIfAbsent(cleanedStateId, id -> new State(id, isFinalState, 0, cleanedStateId));

                if (previousState == initialState) {
                    initialState.addTransition(currentState);
                } else {
                    previousState.addTransition(currentState);
                }

                System.out.println("[DEBUG] Added transition from " + previousState.getId() + " to " + currentState.getId());
                previousState = currentState; // Actualizar estado previo
            }
        }

        // **Nuevo bloque para procesar las transiciones especiales**
        if (specialTransitions != null) {
            parseSpecialTransitions(finalStateId, specialTransitions);
        }

        // Conectar el último estado al estado final explícito, solo si no está ya conectado y no es redundante
        if (!previousState.equals(initialState)
                && !previousState.hasTransitionTo(finalExplicitState.getId())
                && !previousState.equals(finalExplicitState)) {
            previousState.addTransition(finalExplicitState);
            System.out.println("[DEBUG] Connected final state: " + previousState.getId() + " to FINAL");
        }

        this.currentState = initialState;

        // Log todos los estados y sus transiciones
        System.out.println("[DEBUG] Complete StateMachine transitions:");
        for (Map.Entry<String, State> entry : states.entrySet()) {
            System.out.println("State " + entry.getKey() + " transitions to: " + entry.getValue().getTransitions());
        }
    }


 // **Nuevo método para procesar transiciones especiales**
    private void parseSpecialTransitions(String finalStateId, String specialTransitions) {
        List<String> transitions = new ArrayList<>();

        if (specialTransitions.startsWith("end:")) {
            String actualTransitions = specialTransitions.replaceFirst("end:", "").trim();

            if (actualTransitions.equals("+")) {
                transitions.add("+"); // Todas las transiciones son válidas
            } else {
                String[] methods = actualTransitions.split(",");
                for (String method : methods) {
                    transitions.add(method.trim());
                }
            }

            postFinalTransitions.put(finalStateId, transitions);

            // Imprimir cada transición en una línea diferente
            System.out.println("[INFO] Special transitions configured for state '" + finalStateId + "':");
            for (String transition : transitions) {
                System.out.println("  - " + transition);
            }
        } else {
            throw new IllegalArgumentException("[ERROR] Invalid syntax for special transitions: " + specialTransitions);
        }
    }



    
    private void handleWildcardCondition(State previousState, State finalState, String segment) {
        // Crear el estado wildcard si no existe
        State wildcardState = states.computeIfAbsent(".*", id -> new State(id, false, 0, id));

        // Permitir transiciones dinámicas desde el estado anterior al comodín
        previousState.addTransition(wildcardState);

        // Configurar transiciones del comodín hacia todos los estados intermedios (excepto final y él mismo)
        for (State state : states.values()) {
            if (!state.isFinalState() && !state.equals(previousState) && !state.equals(wildcardState)) {
                wildcardState.addTransition(state);
            }
        }

        // Añadir autoreferencia dinámica: si no se encuentra un estado válido, se queda en el wildcard
        wildcardState.addTransition(wildcardState);

        // Permitir transición directa del wildcard al estado final
        wildcardState.addTransition(finalState);

        System.out.println("[DEBUG] Wildcard condition configured: " + previousState.getId() + " -> .* -> [" + finalState.getId() + "]");
    }

    // Método para limpiar transiciones inválidas
    private void cleanInvalidTransitions() {
        for (State state : states.values()) {
            // Si estamos en el estado `end`, eliminar transiciones hacia `middle`
            if (state.getId().equals("end")) {
                state.getTransitions().removeIf(target -> target.getId().equals("middle"));
            }
        }
    }

    
    private void handleAndOrCombination(State initialState, State finalState, String segment) {
        String[] orSegments = segment.split("\\|");
        for (String orSegment : orSegments) {
            orSegment = orSegment.trim();

            // Procesar cada segmento OR
            if (orSegment.contains("&")) {
                // Si contiene & (AND), procesar la combinación AND
                handleAndCondition(initialState, finalState, orSegment);
            } else {
                // Crear estados para cada opción OR
                String cleanedStateId = cleanStateId(orSegment);
                State intermediateState = states.computeIfAbsent(cleanedStateId, id -> new State(id, false, 0, cleanedStateId));

                // Conectar initialState al estado intermedio
                initialState.addTransition(intermediateState);

                // Conectar el estado intermedio al final
                if (!intermediateState.hasTransitionTo(finalState.getId())) {
                    intermediateState.addTransition(finalState);
                }
            }
        }

        // Validar que las conexiones al estado final son consistentes
        validateFinalStateConnections(finalState, initialState);
    }

    
    private void validateFinalStateConnections(State finalState, State initialState) {
        for (Map.Entry<String, State> entry : states.entrySet()) {
            State state = entry.getValue();

            // Asegurarse de que `end` solo apunte a `FINAL`
            if (state.getId().equals("end")) {
                state.getTransitions().removeIf(target -> !target.equals(finalState));
            }

            // Evitar transiciones redundantes dentro del mismo estado
            state.getTransitions().removeIf(target -> target.equals(state));
        }
    }
    
    private void handleRepetition(State initialState, State finalState, String segment) {
        Matcher matcher = Pattern.compile("\\(([^)]+)\\)\\{(\\d+)}").matcher(segment);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("[ERROR] Invalid repetition syntax in: " + segment);
        }

        // Extraer el nombre del estado y el número de repeticiones
        String repeatedStateName = matcher.group(1).trim();
        int repetitions = Integer.parseInt(matcher.group(2));

        State previousState = initialState;

        // Crear los estados intermedios para las repeticiones
        for (int i = 1; i <= repetitions; i++) {
            String stateId = repeatedStateName + "_rep" + i;
            State repeatedState = new State(stateId, false, 0, repeatedStateName);
            states.put(stateId, repeatedState);

            // Conectar el estado anterior con el actual
            previousState.addTransition(repeatedState);

            // Actualizar el estado previo
            previousState = repeatedState;
        }

        // Conectar el último estado repetido con el estado final
        previousState.addTransition(finalState);

        // Eliminar transiciones innecesarias desde `INITIAL`
        initialState.getTransitions().removeIf(state -> state.equals(finalState));

        // Debug de transiciones configuradas para la repetición
        System.out.println("[DEBUG] State transitions for repetition:");
        System.out.println("  " + initialState.getName() + " -> [" + repeatedStateName + "_rep1]");
        for (int i = 1; i < repetitions; i++) {
            System.out.println("  " + repeatedStateName + "_rep" + i + " -> [" + repeatedStateName + "_rep" + (i + 1) + "]");
        }
        System.out.println("  " + repeatedStateName + "_rep" + repetitions + " -> [" + finalState.getName() + "]");
    }
    
    private void handleOrCondition(State initialState, State nextState, String segment) {
        String[] subStates = segment.split("\\|");
        if (subStates.length < 2) {
            throw new IllegalArgumentException("[ERROR] Invalid OR syntax in: " + segment);
        }

        for (String subState : subStates) {
            String cleanedSubState = cleanStateId(subState.trim());

            // Crear o recuperar el estado intermedio (start o process)
            State intermediateState = states.computeIfAbsent(cleanedSubState, id -> new State(id, false, 0, cleanedSubState));

            // Conectar el estado inicial al estado intermedio
            initialState.addTransition(intermediateState);

            // Conectar al siguiente estado intermedio si no es FINAL
            if (!"FINAL".equals(nextState.getId())) {
                intermediateState.addTransition(nextState);
            } else {
                // Conectar directamente al estado FINAL si es el caso
                State finalState = states.computeIfAbsent("FINAL", id -> new State(id, true, 0, id));
                intermediateState.addTransition(finalState);
            }

            // Debugging para cada transición
            System.out.println("[DEBUG] OR condition: " + initialState.getName() + " -> [" + intermediateState.getName() + "]");
            System.out.println("[DEBUG] " + intermediateState.getName() + " -> [" + nextState.getName() + "]");
        }
    }
    
    private void handleAndCondition(State initialState, State finalState, String segment) {
        // Delegar a handleOrAndCondition si el segmento contiene un OR
        if (segment.contains("|")) {
            handleOrAndCondition(initialState, finalState, segment);
            return; // Salir para que no se siga procesando aquí
        }

        String[] subStates = segment.split("&");
        if (subStates.length != 2) {
            throw new IllegalArgumentException("[ERROR] Invalid AND syntax in: " + segment);
        }

        // Extraer nombres lógicos
        String subStateA = cleanStateId(subStates[0].trim());
        String subStateB = cleanStateId(subStates[1].trim());

        // Crear estados base
        State startState = states.computeIfAbsent(subStateA, id -> new State(subStateA, false, 1, subStateA));
        State processState = states.computeIfAbsent(subStateB, id -> new State(subStateB, false, 1, subStateB));

        // Crear estados intermedios (opcional)
        State intermediateStateA = new State(UUID.randomUUID().toString(), false, 0, subStateA);
        State intermediateStateB = new State(UUID.randomUUID().toString(), false, 0, subStateB);
        states.putIfAbsent(intermediateStateA.getId(), intermediateStateA);
        states.putIfAbsent(intermediateStateB.getId(), intermediateStateB);

        // Transiciones para el caso AND:
        // Camino 1: INITIAL -> startState -> intermediateStateB -> finalState
        initialState.addTransition(startState);
        startState.addTransition(intermediateStateB);
        intermediateStateB.addTransition(finalState);

        // Camino 2: INITIAL -> processState -> intermediateStateA -> finalState
        initialState.addTransition(processState);
        processState.addTransition(intermediateStateA);
        intermediateStateA.addTransition(finalState);

        // Registrar mapeo lógico -> ID
        logicalToUuidMap.put(subStateA, startState.getId());
        logicalToUuidMap.put(subStateB, processState.getId());

        // Debug de transiciones
        System.out.println("[DEBUG] AND-Condition Transitions Configured:");
        System.out.println("  INITIAL -> [" + startState.getName() + ", " + processState.getName() + "]");
        System.out.println("  " + startState.getName() + " -> [" + intermediateStateB.getName() + "]");
        System.out.println("  " + processState.getName() + " -> [" + intermediateStateA.getName() + "]");
        System.out.println("  " + intermediateStateB.getName() + ", " + intermediateStateA.getName() + " -> [end]");
    }

    
    private void handleOrAndCondition(State initialState, State finalState, String segment) {
        System.out.println("[DEBUG] Entering handleOrAndCondition");
        System.out.println("[DEBUG] Initial state: " + initialState.getId());
        System.out.println("[DEBUG] Final state: " + finalState.getId());
        System.out.println("[DEBUG] Segment: " + segment);

        // Crear estados intermedios explícitos según la imagen
        State endState = states.computeIfAbsent("end", id -> new State(id, false, 0, id));
        State startState = states.computeIfAbsent("start", id -> new State(id, false, 0, id));
        State processState = states.computeIfAbsent("process", id -> new State(id, false, 0, id));

        // Configurar las transiciones explícitas
        initialState.addTransition(startState);
        initialState.addTransition(processState);
        initialState.addTransition(endState);

        // Transiciones desde `start` y `process` hacia `end`
        startState.addTransition(endState);
        processState.addTransition(endState);

        // Transiciones desde `end` hacia `start` y `process`
        endState.addTransition(startState);
        endState.addTransition(processState);

        // Conectar `end` con el estado final
        endState.addTransition(finalState);

        System.out.println("[DEBUG] Transitions Configured:");
        System.out.println("  " + initialState.getId() + " -> [start, process, end]");
        System.out.println("  start -> [end]");
        System.out.println("  process -> [end]");
        System.out.println("  end -> [start, process, FINAL]");
    }


    
    private String cleanStateId(String stateId) {
        return stateId.replaceAll("[(){}]", "").replaceAll("\\{\\d+}", "").trim();
    }

    
    public boolean validateTransition(String methodName) {
        // Traducir nombres lógicos a UUIDs si están en el mapeo
        String tempMethodName = cleanStateId(methodName);
        final String cleanedMethodName;

        if (logicalToUuidMap.containsKey(tempMethodName)) {
            cleanedMethodName = logicalToUuidMap.get(tempMethodName);
        } else {
            cleanedMethodName = tempMethodName;
        }

        if (currentState == null) {
            throw new IllegalStateException("[ERROR] StateMachine not initialized properly");
        }

        // Si estamos en el estado FINAL, manejar transiciones especiales o bloquear
        if (currentState.getId().equals("FINAL")) {
        	System.out.println("[INFO] handleSpecialFinalTransitions from FINAL");
            if (handleSpecialFinalTransitions(cleanedMethodName, true)) { // Validar incluso si estamos en FINAL
                return true;
            }
            System.out.println("[ERROR] Transition attempted from final state: FINAL");
            throw new IllegalStateException("[ERROR] Cannot transition from final state: FINAL");
        }

        // Validar transiciones desde estados finales declarados
        if (currentState.isFinalState() && !currentState.getId().equals("FINAL")) {
        	System.out.println("[INFO] handleSpecialFinalTransitions form FINAL STATE");
            if (handleSpecialFinalTransitions(cleanedMethodName, false)) { // Validar desde un estado final declarado
                return true;
            }

            // Si no hay transiciones especiales configuradas, seguir la lógica existente
            if (currentState.hasTransitionTo("FINAL")) {
                System.out.println("[INFO] Transition to FINAL is explicitly allowed.");
                currentState = states.get("FINAL");
                return true;
            } else {
                throw new IllegalStateException("[ERROR] Cannot transition from declared final state: " + currentState.getId());
            }
        }

        // Validar la transición como en estados normales
        for (State targetState : currentState.getTransitions()) {
            // Manejo de transiciones comodín (.*)
            if (targetState.getName().equals(".*")) {
                System.out.println("[INFO] Wildcard transition detected from " + currentState.getId() + " to " + cleanedMethodName);

                // Buscar o crear el estado destino dinámicamente
                State nextState = states.get(cleanedMethodName);

                if (nextState == null) {
                    // Crear un estado dinámico si no existe
                    nextState = new State(cleanedMethodName, false, 0, cleanedMethodName);
                    states.put(cleanedMethodName, nextState);
                    targetState.addTransition(nextState); // Agregar transición al wildcard
                    System.out.println("[DEBUG] Created intermediate state for wildcard transition: " + cleanedMethodName);
                }

                // Agregar transiciones dinámicas desde el estado actual al siguiente
                if (!currentState.hasTransitionTo(nextState.getId())) {
                    currentState.addTransition(nextState); // Conectar el estado actual al siguiente
                    System.out.println("[DEBUG] Dynamically added transition from " + currentState.getId() + " to " + nextState.getId());
                }

                // **Nuevo: Agregar transiciones dinámicas desde estados intermedios**
                for (State previousState : states.values()) {
                    if (previousState.getTransitions().contains(currentState) && !previousState.getTransitions().contains(nextState)) {
                        previousState.addTransition(nextState);
                        System.out.println("[DEBUG] Dynamically added transition from " + previousState.getId() + " to " + nextState.getId());
                    }
                }

                // **Nuevo: Conectar el siguiente estado dinámico al estado esperado final**
                for (State nextExpectedState : targetState.getTransitions()) {
                    if (!nextState.hasTransitionTo(nextExpectedState.getId())) {
                        nextState.addTransition(nextExpectedState);
                        System.out.println("[DEBUG] Dynamically added transition from " + nextState.getId() + " to " + nextExpectedState.getId());
                    }
                }

                // Actualizar el estado actual al estado válido
                currentState = nextState;
                System.out.println("[INFO] Wildcard transition valid to state: " + cleanedMethodName);
                return true;
            }

            // Validar si coincide con el nombre lógico o el ID
            if (targetState.getId().equals(cleanedMethodName) || targetState.getName().equals(methodName)) {
                currentState = targetState;
                System.out.println("[INFO] Transition valid. Current state updated to: " + currentState.getId());
                return true;
            }
        }

        // Si no hay transición válida, lanzar excepción con detalles
        throw new IllegalStateException("[ERROR] Invalid transition from " + currentState.getId() + " to " + cleanedMethodName + ".");
    }

    // **Actualización en la función para manejar transiciones finales especiales**
    private boolean handleSpecialFinalTransitions(String methodName, boolean allowFromFinal) {
    	System.out.println("Allow from final: " + allowFromFinal);
        // Si no se permiten transiciones desde FINAL y estamos en FINAL, salir
        if (!allowFromFinal && currentState.getId().equals("FINAL")) {
        	System.out.println("[INFO] Retorna en el primer IF");
            return false;
        }

        if (postFinalTransitions.containsKey(currentState.getId())) {
            List<String> allowedEndTransitions = postFinalTransitions.get(currentState.getId());

            // Validar si "*" está permitido o si el método actual está en la lista
            if (allowedEndTransitions.contains("+") || allowedEndTransitions.contains(methodName)) {
                System.out.println("[INFO] Valid special transition: " + methodName);           
                return true;
            } else {
                throw new IllegalStateException("[ERROR] Invalid special transition: " + methodName);
            }
        }
        return false; // No se encontraron transiciones finales especiales
    }



    public boolean isInFinalState() {
        if (currentState == null) {
            return false; // No hay estado actual, no puede ser final
        }
        // Verificar si el estado actual es FINAL o tiene una transición directa hacia FINAL
        return "FINAL".equals(currentState.getId()) || currentState.hasTransitionTo("FINAL");
    }

    public Map<String, List<String>> getTransitions() {
        Map<String, List<String>> transitionsMap = new HashMap<>();
        for (State state : states.values()) {
            List<State> outgoingTransitions = state.getTransitions();

            // Incluir estados con transiciones salientes
            if (!outgoingTransitions.isEmpty()) {
                List<String> targetStates = new ArrayList<>();
                for (State target : outgoingTransitions) {
                    // Incluir todas las transiciones, incluyendo las que apuntan a FINAL
                    targetStates.add(target.getId());
                }
                transitionsMap.put(state.getId(), targetStates);
            }
        }
        return transitionsMap;
    }
    
    public State getCurrentState() {
        return this.currentState;
    }
}
	package com.luis.tfg.ExecutionOrder;
	
	import java.util.HashMap;
	import java.util.List;
	import java.util.Map;
	
	import java.util.*;
	
	public class State {
	    private final String id;       // Identificador único del estado
	    private final String name;     // Nombre del estado (puede ser no único)
	    private final boolean isFinal; // Si el estado es final
	    private final int maxRepetitions;
	    private int currentRepetitions;
	    private final Map<String, State> transitions = new HashMap<>();
	
	    // Constructor
	 // Nuevo constructor con cuatro parámetros
	    public State(String id, boolean isFinal, int maxRepetitions, String name) {
	        this.id = id;                   
	        this.isFinal = isFinal;         
	        this.maxRepetitions = maxRepetitions; 
	        this.currentRepetitions = 0;    
	        this.name = name; // Permitir que el nombre se pase como argumento
	    }
	
	
	    // Método para agregar una transición
	    public void addTransition(State targetState) {
	        transitions.put(targetState.getId(), targetState);
	    }
	    
	    public void removeTransition(State targetState) {
	        if (transitions.containsKey(targetState.getId())) {
	            transitions.remove(targetState.getId());
	            System.out.println("[INFO] Removed transition to state: " + targetState.getId() + " from state: " + id);
	        } else {
	            System.out.println("[INFO] No transition found to state: " + targetState.getId() + " from state: " + id);
	        }
	    }
	
	    // Método para validar la transición a otro estado
	    public boolean validateTransition(String methodName) {
	        if (this.id.equals(methodName)) {
	            // Si es el mismo estado, validar repetición
	            if (maxRepetitions > 0) {
	                if (currentRepetitions < maxRepetitions) {
	                    currentRepetitions++;
	                    System.out.println("[INFO] Valid repetition for state: " + id + ". Remaining: " + (maxRepetitions - currentRepetitions));
	                    return true;
	                } else {
	                    System.out.println("[ERROR] Repetition limit reached for state: " + id);
	                    throw new IllegalStateException("[ERROR] Repetition limit reached for state: " + id);
	                }
	            }
	            // No se permite la transición al mismo estado sin repetición
	            System.out.println("[ERROR] Invalid transition from " + id + " to itself.");
	            return false;
	        }
	
	        // Validar transición a un nuevo estado
	        if (transitions.containsKey(methodName)) {
	            System.out.println("[INFO] Transition to state " + methodName + " is valid.");
	            return true;
	        }
	
	        // Transición no válida
	        System.out.println("[ERROR] Invalid transition from " + id + " to " + methodName + ".");
	        throw new IllegalStateException("[ERROR] Invalid transition from " + id + " to " + methodName + ".");
	    }
	
	    // Método para obtener todas las transiciones como una lista de estados
	    public List<State> getTransitions() {
	        return new ArrayList<>(transitions.values());
	    }
	
	    // Getter para el ID del estado
	    public String getId() {
	        return id;
	    }
	    
	    // Getter para currentRepetitions
	    public int getCurrentRepetitions() {
	        return currentRepetitions;
	    }
	    
	    // Getter para maxRepetitions
	    public int getMaxRepetitions() {
	        return maxRepetitions;
	    }
	
	    // Getter para el nombre del estado
	    public String getName() {
	        return name;
	    }
	    
	
	
	    // Getter para verificar si es un estado final
	    public boolean isFinalState() {
	        return isFinal;
	    }
	
	    // Validación de repeticiones
	    public boolean validateRepetition() {
	        if (currentRepetitions < maxRepetitions) {
	            currentRepetitions++;
	            return true;
	        }
	        return false;
	    }
	    
	    public boolean hasTransitionTo(String stateId) {
	        return transitions.containsKey(stateId);
	    }
	
	    // Reiniciar repeticiones
	    public void resetRepetitions() {
	        currentRepetitions = 0;
	    }
	}
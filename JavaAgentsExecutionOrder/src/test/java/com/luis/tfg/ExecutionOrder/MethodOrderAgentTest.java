package com.luis.tfg.ExecutionOrder;

import org.junit.jupiter.api.Test;

import com.luis.tfg.ExecutionOrder.ExecutionOrderRegistry;
import com.luis.tfg.ExecutionOrder.StateMachine;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

public class MethodOrderAgentTest {

    @Test
    public void testValidOrder() {
        TestClass instance = new TestClass("id1");
        ExecutionOrderRegistry.register(instance, "(start process end)");

        StateMachine sm = ExecutionOrderRegistry.getStateMachine(instance);
        assertNotNull(sm, "StateMachine should not be null");

        assertTrue(sm.validateTransition("start"), "Transition to 'start' should be valid");
        assertTrue(sm.validateTransition("process"), "Transition to 'process' should be valid");
        assertTrue(sm.validateTransition("end"), "Transition to 'end' should be valid");

        assertTrue(sm.isInFinalState(), "StateMachine should be in a final state");
    }

    @Test
    public void testNotEndingInFinalState() {
        System.out.println("[TEST] Starting testNotEndingInFinalState");

        TestClass instance = new TestClass("id2");
        ExecutionOrderRegistry.register(instance, "(start process end)");

        StateMachine sm = ExecutionOrderRegistry.getStateMachine(instance);
        assertNotNull(sm, "StateMachine should not be null");

        try {
            assertTrue(sm.validateTransition("start"));
            assertTrue(sm.validateTransition("process"));
            assertFalse(sm.isInFinalState(), "StateMachine should not be in a final state");
        } catch (IllegalStateException e) {
            fail("Unexpected IllegalStateException: " + e.getMessage());
        }

        System.out.println("[TEST] Finished testNotEndingInFinalState");
    }

    @Test
    public void testInvalidOrder() {
        System.out.println("[TEST] Starting testInvalidOrder");

        TestClass instance = new TestClass("id3");
        ExecutionOrderRegistry.register(instance, "(start process end)");

        StateMachine sm = ExecutionOrderRegistry.getStateMachine(instance);
        assertNotNull(sm, "StateMachine should not be null");

        try {
            sm.validateTransition("start");
            sm.validateTransition("end"); // Invalid transition
            fail("Expected an IllegalStateException due to invalid transition");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("Invalid transition from start to end"),
                    "Expected detailed invalid transition exception");
        }

        System.out.println("[TEST] Finished testInvalidOrder");
    }

    @Test
    public void testStateMachineNotFound() {
        System.out.println("[TEST] Starting testStateMachineNotFound");

        TestClass instance = new TestClass("id4");
        StateMachine sm = ExecutionOrderRegistry.getStateMachine(instance);
        assertNull(sm, "StateMachine should not be found for unregistered instance");

        System.out.println("[TEST] Finished testStateMachineNotFound");
    }

    @Test
    public void testFlexibleSequence() {
        System.out.println("[TEST] Starting testFlexibleSequence");

        TestClass instance = new TestClass("id5");
        ExecutionOrderRegistry.register(instance, "(start .* end)");

        StateMachine sm = ExecutionOrderRegistry.getStateMachine(instance);
        assertNotNull(sm, "StateMachine should not be null");

        try {
            assertTrue(sm.validateTransition("start"));
            assertTrue(sm.validateTransition("middle"));
            assertTrue(sm.validateTransition("end"));

            assertTrue(sm.isInFinalState(), "StateMachine should end in a final state");
        } catch (IllegalStateException e) {
            fail("Unexpected IllegalStateException: " + e.getMessage());
        }

        System.out.println("[TEST] Finished testFlexibleSequence");
    }

    @Test
    public void testAndCondition() {
        TestClass instance = new TestClass("id6");
        ExecutionOrderRegistry.register(instance, "(start & process) -> end");

        StateMachine sm = ExecutionOrderRegistry.getStateMachine(instance);
        assertNotNull(sm);

        assertTrue(sm.validateTransition("start"), "Transition to 'start' should be valid");
        assertTrue(sm.validateTransition("process"), "Transition to 'process' should be valid");
        assertTrue(sm.validateTransition("end"), "Transition to 'end' should be valid");

        assertTrue(sm.isInFinalState(), "StateMachine should be in a final state");
    }
    
    @Test
    public void testAndCondition2() {
        TestClass instance = new TestClass("id12");
        ExecutionOrderRegistry.register(instance, "(process & start) -> end");

        StateMachine sm = ExecutionOrderRegistry.getStateMachine(instance);
        assertNotNull(sm);

        assertTrue(sm.validateTransition("start"), "Transition to 'start' should be valid");
        assertTrue(sm.validateTransition("process"), "Transition to 'process' should be valid");
        assertTrue(sm.validateTransition("end"), "Transition to 'end' should be valid");

        assertTrue(sm.isInFinalState(), "StateMachine should be in a final state");
    }


    @Test
    public void testOrCondition() {
        System.out.println("[TEST] Starting testOrCondition");

        TestClass instance = new TestClass("id7");
        ExecutionOrderRegistry.register(instance, "(start | process) -> end");

        StateMachine sm = ExecutionOrderRegistry.getStateMachine(instance);
        assertNotNull(sm, "StateMachine should not be null");

        try {
            assertTrue(sm.validateTransition("process"));
            assertTrue(sm.validateTransition("end"));

            assertTrue(sm.isInFinalState(), "StateMachine should end in a final state");
        } catch (IllegalStateException e) {
            fail("Unexpected IllegalStateException: " + e.getMessage());
        }

        System.out.println("[TEST] Finished testOrCondition");
    }
    
    @Test
    public void testOrCondition2() {
        System.out.println("[TEST] Starting testOrCondition");

        TestClass instance = new TestClass("id72");
        ExecutionOrderRegistry.register(instance, "(process | start) -> end");

        StateMachine sm = ExecutionOrderRegistry.getStateMachine(instance);
        assertNotNull(sm, "StateMachine should not be null");

        try {
            assertTrue(sm.validateTransition("process"));
            assertTrue(sm.validateTransition("end"));

            assertTrue(sm.isInFinalState(), "StateMachine should end in a final state");
        } catch (IllegalStateException e) {
            fail("Unexpected IllegalStateException: " + e.getMessage());
        }

        System.out.println("[TEST] Finished testOrCondition");
    }

    @Test
    public void testRepetition() {
        TestClass instance = new TestClass("id8");
        ExecutionOrderRegistry.register(instance, "(repeat){2} -> end");

        StateMachine sm = ExecutionOrderRegistry.getStateMachine(instance);
        assertNotNull(sm);

        assertTrue(sm.validateTransition("repeat"), "First repeat should be valid");
        assertTrue(sm.validateTransition("repeat"), "Second repeat should be valid");
        assertTrue(sm.validateTransition("end"), "Transition to end should be valid");

        assertTrue(sm.isInFinalState(), "StateMachine should be in a final state");
    }

    

    @Test
    public void testInvalidRepetition() {
        System.out.println("[TEST] Starting testInvalidRepetition");

        TestClass instance = new TestClass("id10");
        ExecutionOrderRegistry.register(instance, "(repeat){2} -> end");

        StateMachine sm = ExecutionOrderRegistry.getStateMachine(instance);
        assertNotNull(sm, "StateMachine should not be null");

        try {
            sm.validateTransition("repeat");
            sm.validateTransition("end");
            fail("Expected an IllegalStateException due to invalid transition");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("Invalid transition"),
                    "Expected detailed invalid transition exception");
        }

        System.out.println("[TEST] Finished testInvalidRepetition");
    }
    
    @Test
    public void testGetTransitions() {
        System.out.println("[TEST] Starting testGetTransitions");

        TestClass instance = new TestClass("id21");
        ExecutionOrderRegistry.register(instance, "(start process end -> FINAL)");

        StateMachine sm = ExecutionOrderRegistry.getStateMachine(instance);
        assertNotNull(sm, "StateMachine should not be null");

        Map<String, List<String>> transitions = sm.getTransitions();
        
        // Verificar que hay 4 estados con transiciones ahora (INITIAL incluido)
        assertEquals(4, transitions.size(), "StateMachine should have 4 states with transitions (including INITIAL)");

        // Validar que INITIAL tiene transición a "start"
        assertTrue(transitions.containsKey("INITIAL"), "StateMachine should contain INITIAL state");
        assertTrue(transitions.get("INITIAL").contains("start"), "INITIAL state should transition to start");

        // Validar las transiciones del flujo principal
        assertTrue(transitions.containsKey("start"), "StateMachine should contain start state");
        assertTrue(transitions.get("start").contains("process"), "start state should transition to process");

        assertTrue(transitions.containsKey("process"), "StateMachine should contain process state");
        assertTrue(transitions.get("process").contains("end"), "process state should transition to end");

        assertTrue(transitions.containsKey("end"), "StateMachine should contain end state");
        assertTrue(transitions.get("end").contains("FINAL"), "end state should transition to FINAL");

        System.out.println("[TEST] Finished testGetTransitions");
    }
    
    @Test
    public void testNoRedundantTransitionsFromInitial() {
        TestClass instance = new TestClass("id22");
        ExecutionOrderRegistry.register(instance, "(start process end)");

        StateMachine sm = ExecutionOrderRegistry.getStateMachine(instance);
        assertNotNull(sm, "StateMachine should not be null");

        List<String> initialTransitions = sm.getTransitions().get("INITIAL");
        assertNotNull(initialTransitions, "Transitions from INITIAL should not be null");
        assertEquals(1, initialTransitions.size(), "INITIAL should only transition to 'start'");
        assertTrue(initialTransitions.contains("start"), "INITIAL should transition only to 'start'");
    }
    
    @Test
    public void testInvalidRepeatedTransition() {
        TestClass instance = new TestClass("id23");
        ExecutionOrderRegistry.register(instance, "(start process end)");

        StateMachine sm = ExecutionOrderRegistry.getStateMachine(instance);
        assertNotNull(sm, "StateMachine should not be null");

        try {
            sm.validateTransition("start");
            sm.validateTransition("start"); // Repeated transition
            fail("Expected an IllegalStateException due to repeated transition");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("Invalid transition"), 
                "Expected an invalid transition exception for repeated state");
        }
    }
    
    @Test
    public void testMultipleOrTransitions() {
        TestClass instance = new TestClass("id24");
        ExecutionOrderRegistry.register(instance, "(start | process) -> end -> FINAL");

        StateMachine sm = ExecutionOrderRegistry.getStateMachine(instance);
        assertNotNull(sm, "StateMachine should not be null");

        // Realizar las transiciones esperadas
        sm.validateTransition("start"); // Transición válida a "start"
        sm.validateTransition("end");   // Transición válida a "end"

        // Verificar que estamos en un estado final declarado (antes de FINAL)
        assertTrue(sm.isInFinalState(), "StateMachine should consider 'end' as a final state with transition to 'FINAL'");

        // Realizar una transición explícita al estado FINAL
        sm.validateTransition("FINAL"); // Transición válida a "FINAL"

        // Verificar que el estado actual es FINAL
        assertEquals("FINAL", sm.getCurrentState().getId(), "Current state should be 'FINAL'");

        // Intentar otra transición desde FINAL debe fallar
        try {
            sm.validateTransition("start");
            fail("Expected IllegalStateException for transition from final state");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("Cannot transition from final state"),
                    "Expected exception for transition from final state");
        }
    }

    
    @Test
    public void testAndOrCombination() {
        // Prueba la rama del operador AND
        TestClass instanceAnd = new TestClass("id55-and"); // Identificador único
        ExecutionOrderRegistry.register(instanceAnd, "((start & process) | middle) -> end -> FINAL");

        StateMachine smAnd = ExecutionOrderRegistry.getStateMachine(instanceAnd);
        assertNotNull(smAnd, "StateMachine for AND branch should not be null");

        // Validar transiciones en la rama AND
        assertTrue(smAnd.validateTransition("start"), "Transition to 'start' should be valid");
        assertTrue(smAnd.validateTransition("process"), "Transition to 'process' should be valid");
        assertTrue(smAnd.validateTransition("end"), "Transition to 'end' should be valid");

        // Prueba la rama del operador OR
        TestClass instanceOr = new TestClass("id55-or"); // Identificador único
        ExecutionOrderRegistry.register(instanceOr, "((start & process) | middle) -> end -> FINAL");

        StateMachine smOr = ExecutionOrderRegistry.getStateMachine(instanceOr);
        assertNotNull(smOr, "StateMachine for OR branch should not be null");

        // Validar transiciones en la rama OR
        assertTrue(smOr.validateTransition("middle"), "Transition to 'middle' should be valid");
        assertTrue(smOr.validateTransition("end"), "Transition to 'end' should be valid after 'middle'");
    }

    
    @Test
    public void testNoTransitionsFromFinalState() {
        TestClass instance = new TestClass("id26");
        ExecutionOrderRegistry.register(instance, "(start process end) -> FINAL");

        StateMachine sm = ExecutionOrderRegistry.getStateMachine(instance);
        assertNotNull(sm, "StateMachine should not be null");

        sm.validateTransition("start");
        sm.validateTransition("process");
        sm.validateTransition("end");

        // Validar que al llegar a 'end' estamos en un estado final declarado
        assertTrue(sm.isInFinalState(), "StateMachine should be in a final state");
        assertEquals("end", sm.getCurrentState().getId(), "Current state should be the declared final state ('end')");

        // Probar transición explícita a FINAL
        sm.validateTransition("FINAL");

        // Validar que el estado actual es FINAL
        assertEquals("FINAL", sm.getCurrentState().getId(), "Current state should be FINAL");

        try {
            sm.validateTransition("process");
            fail("Expected an IllegalStateException for transition from final state");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("Cannot transition from final state"),
                "Expected exception for transition from final state");
        }
    }

    
    @Test
    public void testWildcardTransitions() {
        TestClass instance = new TestClass("id337");
        ExecutionOrderRegistry.register(instance, "(start .* end)");

        StateMachine sm = ExecutionOrderRegistry.getStateMachine(instance);
        assertNotNull(sm, "StateMachine should not be null");

        assertTrue(sm.validateTransition("start"), "Transition to 'start' should be valid");
        assertTrue(sm.validateTransition("intermediate1"), "Wildcard transition should be valid");
        assertTrue(sm.validateTransition("intermediate2"), "Wildcard transition should be valid");
        assertTrue(sm.validateTransition("end"), "Transition to 'end' should be valid");

        assertTrue(sm.isInFinalState(), "StateMachine should end in a final state");
    }

    
    @Test
    public void testMultipleStateMachines() {
        TestClass instance1 = new TestClass("id28");
        TestClass instance2 = new TestClass("id29");

        ExecutionOrderRegistry.register(instance1, "(start process end)");
        ExecutionOrderRegistry.register(instance2, "(init action done)");

        StateMachine sm1 = ExecutionOrderRegistry.getStateMachine(instance1);
        StateMachine sm2 = ExecutionOrderRegistry.getStateMachine(instance2);

        assertNotNull(sm1, "StateMachine for instance1 should not be null");
        assertNotNull(sm2, "StateMachine for instance2 should not be null");

        // Verificar transiciones en sm1
        assertTrue(sm1.validateTransition("start"));
        assertTrue(sm1.validateTransition("process"));
        assertTrue(sm1.validateTransition("end"));
        assertTrue(sm1.isInFinalState(), "StateMachine for instance1 should end in a final state");

        // Verificar transiciones en sm2
        assertTrue(sm2.validateTransition("init"));
        assertTrue(sm2.validateTransition("action"));
        assertTrue(sm2.validateTransition("done"));
        assertTrue(sm2.isInFinalState(), "StateMachine for instance2 should end in a final state");
    }
    
    @Test
    public void testWildcardTransitions2() {
        System.out.println("[TEST] Starting testWildcardTransitions");

        TestClass instance = new TestClass("id31");
        ExecutionOrderRegistry.register(instance, "(start .* middle .* end)");

        StateMachine sm = ExecutionOrderRegistry.getStateMachine(instance);
        assertNotNull(sm, "StateMachine should not be null");

        try {
            assertTrue(sm.validateTransition("start"), "Start transition should be valid");
            assertTrue(sm.validateTransition("step1"), "Wildcard step1 should be valid");
            assertTrue(sm.validateTransition("middle"), "Middle transition should be valid");
            assertTrue(sm.validateTransition("step2"), "Wildcard step2 should be valid");
            assertTrue(sm.validateTransition("end"), "End transition should be valid");

            assertTrue(sm.isInFinalState(), "StateMachine should be in a final state");
        } catch (IllegalStateException e) {
            fail("Unexpected IllegalStateException: " + e.getMessage());
        }

        System.out.println("[TEST] Finished testWildcardTransitions");
    }
    
    @Test
    public void testFlexibleSequence2() {
        System.out.println("[TEST] Starting testFlexibleSequence");

        TestClass instance = new TestClass("id27");
        ExecutionOrderRegistry.register(instance, "(start .* end)");

        StateMachine sm = ExecutionOrderRegistry.getStateMachine(instance);
        assertNotNull(sm, "StateMachine should not be null");

        try {
            assertTrue(sm.validateTransition("start"), "Start transition should be valid");
            assertTrue(sm.validateTransition("step1"), "Wildcard step1 should be valid");
            assertTrue(sm.validateTransition("step2"), "Wildcard step2 should be valid");
            assertTrue(sm.validateTransition("end"), "End transition should be valid");

            assertTrue(sm.isInFinalState(), "StateMachine should be in a final state");
        } catch (IllegalStateException e) {
            fail("Unexpected IllegalStateException: " + e.getMessage());
        }

        System.out.println("[TEST] Finished testFlexibleSequence");
    }
    
    @Test
    public void testOrAndCombination() {
        System.out.println("[TEST] Starting testOrAndCombination");

        // Configuración de la máquina de estados
        TestClass instance = new TestClass("id-or-and");
        ExecutionOrderRegistry.register(instance, "((start | process) & end) -> FINAL");

        StateMachine sm = ExecutionOrderRegistry.getStateMachine(instance);
        assertNotNull(sm, "StateMachine should not be null");

        try {
            // Validar transición OR (start)
            assertTrue(sm.validateTransition("start"), "Transition to 'start' should be valid");

            // Validar transición AND (end después de start)
            assertTrue(sm.validateTransition("end"), "Transition to 'end' should be valid after 'start'");

            // Transición explícita a FINAL
            assertTrue(sm.validateTransition("FINAL"), "Transition to 'FINAL' should be valid");

            // Validar estado final
            assertTrue(sm.isInFinalState(), "StateMachine should be in a final state");

            // Intentar una transición adicional desde FINAL
            try {
                sm.validateTransition("start");
                fail("Expected IllegalStateException for transition from final state");
            } catch (IllegalStateException e) {
                assertTrue(e.getMessage().contains("Cannot transition from final state"),
                        "Expected exception for transition from final state");
            }
        } catch (IllegalStateException e) {
            fail("Unexpected IllegalStateException: " + e.getMessage());
        }

        System.out.println("[TEST] Finished testOrAndCombination");
    }
    
    @Test
    public void testOrAndCombination_AllPaths() {
        System.out.println("[TEST] Starting testOrAndCombination_AllPaths");

        // Camino 1: INITIAL -> start -> end -> FINAL
        {
            TestClass instancePath1 = new TestClass("id-or-and-path1");
            ExecutionOrderRegistry.register(instancePath1, "((start | process) & end) -> FINAL");

            StateMachine smPath1 = ExecutionOrderRegistry.getStateMachine(instancePath1);
            assertNotNull(smPath1, "StateMachine for path 1 should not be null");

            try {
                assertTrue(smPath1.validateTransition("start"), "Transition to 'start' should be valid");
                assertTrue(smPath1.validateTransition("end"), "Transition to 'end' after 'start' should be valid");
                assertTrue(smPath1.isInFinalState(), "StateMachine for path 1 should end in a final state");
            } catch (IllegalStateException e) {
                fail("Unexpected IllegalStateException in path 1: " + e.getMessage());
            }
        }

        // Camino 2: INITIAL -> process -> end -> FINAL
        {
            TestClass instancePath2 = new TestClass("id-or-and-path2");
            ExecutionOrderRegistry.register(instancePath2, "((start | process) & end) -> FINAL");

            StateMachine smPath2 = ExecutionOrderRegistry.getStateMachine(instancePath2);
            assertNotNull(smPath2, "StateMachine for path 2 should not be null");

            try {
                assertTrue(smPath2.validateTransition("process"), "Transition to 'process' should be valid");
                assertTrue(smPath2.validateTransition("end"), "Transition to 'end' after 'process' should be valid");
                assertTrue(smPath2.isInFinalState(), "StateMachine for path 2 should end in a final state");
            } catch (IllegalStateException e) {
                fail("Unexpected IllegalStateException in path 2: " + e.getMessage());
            }
        }

        // Camino 3: INITIAL -> end -> start -> end -> FINAL
        {
            TestClass instancePath3 = new TestClass("id-or-and-path3");
            ExecutionOrderRegistry.register(instancePath3, "((start | process) & end) -> FINAL");

            StateMachine smPath3 = ExecutionOrderRegistry.getStateMachine(instancePath3);
            assertNotNull(smPath3, "StateMachine for path 3 should not be null");

            try {
                assertTrue(smPath3.validateTransition("end"), "Transition to 'end' should be valid");
                assertTrue(smPath3.validateTransition("start"), "Transition to 'start' from 'end' should be valid");
                assertTrue(smPath3.validateTransition("end"), "Transition back to 'end' should be valid");
                assertTrue(smPath3.isInFinalState(), "StateMachine for path 3 should end in a final state");
            } catch (IllegalStateException e) {
                fail("Unexpected IllegalStateException in path 3: " + e.getMessage());
            }
        }

        // Camino 4: INITIAL -> end -> process -> end -> FINAL
        {
            TestClass instancePath4 = new TestClass("id-or-and-path4");
            ExecutionOrderRegistry.register(instancePath4, "((start | process) & end) -> FINAL");

            StateMachine smPath4 = ExecutionOrderRegistry.getStateMachine(instancePath4);
            assertNotNull(smPath4, "StateMachine for path 4 should not be null");

            try {
                assertTrue(smPath4.validateTransition("end"), "Transition to 'end' should be valid");
                assertTrue(smPath4.validateTransition("process"), "Transition to 'process' from 'end' should be valid");
                assertTrue(smPath4.validateTransition("end"), "Transition back to 'end' should be valid");
                assertTrue(smPath4.isInFinalState(), "StateMachine for path 4 should end in a final state");
            } catch (IllegalStateException e) {
                fail("Unexpected IllegalStateException in path 4: " + e.getMessage());
            }
        }

        System.out.println("[TEST] Finished testOrAndCombination_AllPaths");
    }
    
    @Test
    public void testIsInFinalStateWithDeclaredFinalState() {
        System.out.println("[TEST] Starting testIsInFinalStateWithDeclaredFinalState");

        // Crear una instancia del StateMachine con un estado final declarado explícitamente
        TestClass instance = new TestClass("id-declared-final-state-test");
        ExecutionOrderRegistry.register(instance, "start -> declaredFinal");

        StateMachine sm = ExecutionOrderRegistry.getStateMachine(instance);
        assertNotNull(sm, "StateMachine should not be null");

        // Verificar que no estamos en el estado final inicialmente
        assertFalse(sm.isInFinalState(), "StateMachine should not be in a final state initially");

        // Validar transición a 'start'
        assertTrue(sm.validateTransition("start"), "Transition to 'start' should be valid");
        assertFalse(sm.isInFinalState(), "StateMachine should not be in a final state after 'start'");

        // Validar transición a 'declaredFinal' (estado final declarado explícitamente)
        assertTrue(sm.validateTransition("declaredFinal"), "Transition to 'declaredFinal' should be valid");
        assertTrue(sm.isInFinalState(), "StateMachine should be in a final state after 'declaredFinal'");

        // Validar transición desde el estado final declarado hacia 'FINAL'
        assertTrue(sm.validateTransition("FINAL"), "Transition from 'declaredFinal' to 'FINAL' should be valid");
        assertTrue(sm.isInFinalState(), "StateMachine should still be in a final state after transitioning to 'FINAL'");

        System.out.println("[TEST] Finished testIsInFinalStateWithDeclaredFinalState");
    }
    
    @Test
    public void testSpecialFinalTransitions() {
        System.out.println("[TEST] Starting testSpecialFinalTransitions");

        // Crear una instancia de la máquina de estados
        TestClass instance = new TestClass("id-special-final");
        // Registrar la máquina de estados con transiciones especiales
        ExecutionOrderRegistry.register(instance, "start -> end [end:fun1,fun2]");

        // Obtener la máquina de estados asociada
        StateMachine sm = ExecutionOrderRegistry.getStateMachine(instance);
        assertNotNull(sm, "StateMachine should not be null");

        try {
            // Validar transición normal a "start"
            assertTrue(sm.validateTransition("start"), "Transition to 'start' should be valid");
            assertTrue(sm.validateTransition("end"), "Transition to 'end' should be valid");

            // Validar transiciones especiales desde "end"
            assertTrue(sm.validateTransition("fun1"), "Special transition 'fun1' should be valid");
            assertTrue(sm.validateTransition("fun2"), "Special transition 'fun2' should be valid");

            // Validar transición inválida (no incluida en las transiciones especiales)
            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> sm.validateTransition("invalidFun"));
            assertEquals("[ERROR] Invalid special transition: invalidFun", exception.getMessage());

            // Validar estado final después de las transiciones especiales
            assertTrue(sm.isInFinalState(), "StateMachine should be in a final state after valid special transitions");

        } catch (IllegalStateException e) {
            fail("Unexpected IllegalStateException: " + e.getMessage());
        }

        System.out.println("[TEST] Finished testSpecialFinalTransitions");
    }
    
    @Test
    public void testSpecialFinalTransitions2() {
        System.out.println("[TEST] Starting testSpecialFinalTransitions");

        // Crear una instancia de la máquina de estados
        TestClass instance = new TestClass("id-special-final2");
        // Registrar la máquina de estados con transiciones especiales
        ExecutionOrderRegistry.register(instance, "start -> end [end:+]");

        // Obtener la máquina de estados asociada
        StateMachine sm = ExecutionOrderRegistry.getStateMachine(instance);
        assertNotNull(sm, "StateMachine should not be null");

        try {
            // Validar transición normal a "start"
            assertTrue(sm.validateTransition("start"), "Transition to 'start' should be valid");
            assertTrue(sm.validateTransition("end"), "Transition to 'end' should be valid");

            // Validar transiciones especiales desde "end"
            assertTrue(sm.validateTransition("fun1"), "Special transition 'fun1' should be valid");
            assertTrue(sm.validateTransition("fun2"), "Special transition 'fun2' should be valid");


            // Validar estado final después de las transiciones especiales
            assertTrue(sm.isInFinalState(), "StateMachine should be in a final state after valid special transitions");

        } catch (IllegalStateException e) {
            fail("Unexpected IllegalStateException: " + e.getMessage());
        }

        System.out.println("[TEST] Finished testSpecialFinalTransitions");
    }


}

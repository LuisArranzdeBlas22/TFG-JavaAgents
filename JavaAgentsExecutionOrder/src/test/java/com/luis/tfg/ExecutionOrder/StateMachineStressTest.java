package com.luis.tfg.ExecutionOrder;

import org.junit.jupiter.api.Test;

import com.luis.tfg.ExecutionOrder.ExecutionOrderRegistry;
import com.luis.tfg.ExecutionOrder.StateMachine;

import static org.junit.jupiter.api.Assertions.*;

public class StateMachineStressTest {

    @Test
    public void testLongSequence() {
        System.out.println("[TEST] Starting testLongSequence");

        StringBuilder regex = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            regex.append("state").append(i).append(" -> ");
        }
        regex.append("FINAL");

        TestClass instance = new TestClass("id-long-sequence");
        ExecutionOrderRegistry.register(instance, regex.toString());

        StateMachine sm = ExecutionOrderRegistry.getStateMachine(instance);
        assertNotNull(sm, "StateMachine should not be null");

        try {
            for (int i = 0; i < 1000; i++) {
                assertTrue(sm.validateTransition("state" + i), "Transition to state" + i + " should be valid");
            }
            assertTrue(sm.validateTransition("FINAL"), "Transition to FINAL should be valid");
        } catch (IllegalStateException e) {
            fail("Unexpected IllegalStateException: " + e.getMessage());
        }

        System.out.println("[TEST] Finished testLongSequence");
    }

    @Test
    public void testComplexCombinations() {
        System.out.println("[TEST] Starting testComplexCombinations");

        // Camino 1: a -> c -> x -> middle1 -> middle2 -> y -> z -> FINAL
        TestClass instancePath1 = new TestClass("complex-combinations-path1");
        ExecutionOrderRegistry.register(instancePath1, "((a | b) & (c | d)) -> (x .* y) -> z -> FINAL");
        StateMachine smPath1 = ExecutionOrderRegistry.getStateMachine(instancePath1);
        assertNotNull(smPath1, "StateMachine for path 1 should not be null");

        try {
            assertTrue(smPath1.validateTransition("a"), "Transition to 'a' should be valid for path 1");
            assertTrue(smPath1.validateTransition("c"), "Transition to 'c' should be valid for path 1");
            assertTrue(smPath1.validateTransition("x"), "Transition to 'x' should be valid for path 1");
            assertTrue(smPath1.validateTransition("middle1"), "Wildcard transition 'middle1' should be valid for path 1");
            assertTrue(smPath1.validateTransition("middle2"), "Wildcard transition 'middle2' should be valid for path 1");
            assertTrue(smPath1.validateTransition("y"), "Transition to 'y' should be valid for path 1");
            assertTrue(smPath1.validateTransition("z"), "Transition to 'z' should be valid for path 1");
            assertTrue(smPath1.validateTransition("FINAL"), "Transition to 'FINAL' should be valid for path 1");
            assertTrue(smPath1.isInFinalState(), "StateMachine for path 1 should be in a final state");
        } catch (IllegalStateException e) {
            fail("Unexpected IllegalStateException for path 1: " + e.getMessage());
        }

        // Camino 2: b -> d -> x -> y -> z -> FINAL
        TestClass instancePath2 = new TestClass("complex-combinations-path2");
        ExecutionOrderRegistry.register(instancePath2, "((a | b) & (c | d)) -> (x .* y) -> z -> FINAL");
        StateMachine smPath2 = ExecutionOrderRegistry.getStateMachine(instancePath2);
        assertNotNull(smPath2, "StateMachine for path 2 should not be null");

        try {
            assertTrue(smPath2.validateTransition("b"), "Transition to 'b' should be valid for path 2");
            assertTrue(smPath2.validateTransition("d"), "Transition to 'd' should be valid for path 2");
            assertTrue(smPath2.validateTransition("x"), "Transition to 'x' should be valid for path 2");
            assertTrue(smPath2.validateTransition("y"), "Transition to 'y' should be valid for path 2");
            assertTrue(smPath2.validateTransition("z"), "Transition to 'z' should be valid for path 2");
            assertTrue(smPath2.validateTransition("FINAL"), "Transition to 'FINAL' should be valid for path 2");
            assertTrue(smPath2.isInFinalState(), "StateMachine for path 2 should be in a final state");
        } catch (IllegalStateException e) {
            fail("Unexpected IllegalStateException for path 2: " + e.getMessage());
        }

        System.out.println("[TEST] Finished testComplexCombinations");
    }

    @Test
    public void testWildcardExplosion() {
        System.out.println("[TEST] Starting testWildcardExplosion");

        TestClass instance = new TestClass("id-wildcard-explosion");
        ExecutionOrderRegistry.register(instance, "(start .* middle .* end)");

        StateMachine sm = ExecutionOrderRegistry.getStateMachine(instance);
        assertNotNull(sm, "StateMachine should not be null");

        try {
            assertTrue(sm.validateTransition("start"), "Transition to 'start' should be valid");
            for (int i = 0; i < 100; i++) {
                assertTrue(sm.validateTransition("wildcard" + i), "Wildcard transition 'wildcard" + i + "' should be valid");
            }
            assertTrue(sm.validateTransition("middle"), "Transition to 'middle' should be valid");
            for (int i = 100; i < 200; i++) {
                assertTrue(sm.validateTransition("wildcard" + i), "Wildcard transition 'wildcard" + i + "' should be valid");
            }
            assertTrue(sm.validateTransition("end"), "Transition to 'end' should be valid");
            assertTrue(sm.isInFinalState(), "StateMachine should be in a final state");
        } catch (IllegalStateException e) {
            fail("Unexpected IllegalStateException: " + e.getMessage());
        }

        System.out.println("[TEST] Finished testWildcardExplosion");
    }

    @Test
    public void testInvalidRegexHandling() {
        System.out.println("[TEST] Starting testInvalidRegexHandling");

        try {
            TestClass instance = new TestClass("id-invalid-regex");
            ExecutionOrderRegistry.register(instance, "(start & (a | )) -> end");
            fail("Expected an IllegalArgumentException for invalid regex");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Invalid"), "Expected exception for invalid regex");
        }

        System.out.println("[TEST] Finished testInvalidRegexHandling");
    }

    @Test
    public void testSpecialFinalTransitionLimit() {
        System.out.println("[TEST] Starting testSpecialFinalTransitionLimit");

        TestClass instance = new TestClass("id-special-final-limit");
        ExecutionOrderRegistry.register(instance, "start -> end [end:+]");

        StateMachine sm = ExecutionOrderRegistry.getStateMachine(instance);
        assertNotNull(sm, "StateMachine should not be null");

        try {
            assertTrue(sm.validateTransition("start"), "Transition to 'start' should be valid");
            assertTrue(sm.validateTransition("end"), "Transition to 'end' should be valid");

            // Validar un número alto de transiciones especiales
            for (int i = 0; i < 1000; i++) {
                assertTrue(sm.validateTransition("fun" + i), "Special transition 'fun" + i + "' should be valid");
            }

            assertTrue(sm.isInFinalState(), "StateMachine should be in a final state");
        } catch (IllegalStateException e) {
            fail("Unexpected IllegalStateException: " + e.getMessage());
        }

        System.out.println("[TEST] Finished testSpecialFinalTransitionLimit");
    }
    
    @Test
    public void testDeepAndOrRepetitionCombination() {
        System.out.println("[TEST] Starting testDeepAndOrRepetitionCombination");

        // Camino 1: start -> mid1 -> end
        TestClass instancePath1 = new TestClass("complex-and-or-rep-path1");
        ExecutionOrderRegistry.register(instancePath1, "((start & (mid1 | mid2)) | (alt & rep{2})) -> end");
        StateMachine smPath1 = ExecutionOrderRegistry.getStateMachine(instancePath1);
        assertNotNull(smPath1, "StateMachine for path 1 should not be null");

        try {
            assertTrue(smPath1.validateTransition("start"), "Transition to 'start' should be valid for path 1");
            assertTrue(smPath1.validateTransition("mid1"), "Transition to 'mid1' should be valid for path 1");
            assertTrue(smPath1.validateTransition("end"), "Transition to 'end' should be valid for path 1");
            assertTrue(smPath1.isInFinalState(), "StateMachine for path 1 should be in a final state");
        } catch (IllegalStateException e) {
            fail("Unexpected IllegalStateException for path 1: " + e.getMessage());
        }

        // Camino 2: alt -> rep -> rep -> end
        TestClass instancePath2 = new TestClass("complex-and-or-rep-path2");
        ExecutionOrderRegistry.register(instancePath2, "((start & (mid1 | mid2)) | (alt & rep{2})) -> end");
        StateMachine smPath2 = ExecutionOrderRegistry.getStateMachine(instancePath2);
        assertNotNull(smPath2, "StateMachine for path 2 should not be null");

        try {
            assertTrue(smPath2.validateTransition("alt"), "Transition to 'alt' should be valid for path 2");
            assertTrue(smPath2.validateTransition("rep"), "First 'rep' should be valid for path 2");
            assertTrue(smPath2.validateTransition("rep"), "Second 'rep' should be valid for path 2");
            assertTrue(smPath2.validateTransition("end"), "Transition to 'end' should be valid for path 2");
            assertTrue(smPath2.isInFinalState(), "StateMachine for path 2 should be in a final state");
        } catch (IllegalStateException e) {
            fail("Unexpected IllegalStateException for path 2: " + e.getMessage());
        }

        System.out.println("[TEST] Finished testDeepAndOrRepetitionCombination");
    }

    @Test
    public void testDeepWildcardAndRepetition() {
        TestClass instance = new TestClass("deep-wildcard-rep");
        ExecutionOrderRegistry.register(instance, "(start .* (mid{3} | end))");
        StateMachine sm = ExecutionOrderRegistry.getStateMachine(instance);
        assertNotNull(sm, "StateMachine should not be null for deep wildcard and repetition");

        assertTrue(sm.validateTransition("start"), "Transition to 'start' should be valid");
        assertTrue(sm.validateTransition("wild1"), "Wildcard transition should be valid");
        assertTrue(sm.validateTransition("mid"), "First 'mid' should be valid");
        assertTrue(sm.validateTransition("mid"), "Second 'mid' should be valid");
        assertTrue(sm.validateTransition("mid"), "Third 'mid' should be valid");
        assertTrue(sm.validateTransition("end"), "Transition to 'end' should be valid");
    }

    @Test
    public void testRedundantPaths() {
        System.out.println("[TEST] Starting testRedundantPaths");

        // Crear dos instancias separadas para explorar caminos redundantes
        TestClass instancePathA = new TestClass("redundant-paths-a");
        TestClass instancePathB = new TestClass("redundant-paths-b");

        // Registrar el mismo patrón en ambas instancias
        ExecutionOrderRegistry.register(instancePathA, "((a | b) & c) -> d -> FINAL");
        ExecutionOrderRegistry.register(instancePathB, "((a | b) & c) -> d -> FINAL");

        // Obtener las máquinas de estados
        StateMachine smPathA = ExecutionOrderRegistry.getStateMachine(instancePathA);
        StateMachine smPathB = ExecutionOrderRegistry.getStateMachine(instancePathB);

        assertNotNull(smPathA, "StateMachine for path A should not be null");
        assertNotNull(smPathB, "StateMachine for path B should not be null");

        // Explorar camino redundante 1: "a -> c -> d -> FINAL"
        try {
            assertTrue(smPathA.validateTransition("a"), "Transition to 'a' should be valid for path A");
            assertTrue(smPathA.validateTransition("c"), "Transition to 'c' should be valid for path A");
            assertTrue(smPathA.validateTransition("d"), "Transition to 'd' should be valid for path A");
            assertTrue(smPathA.validateTransition("FINAL"), "Transition to 'FINAL' should be valid for path A");

            // Verificar que está en estado final
            assertTrue(smPathA.isInFinalState(), "StateMachine for path A should be in a final state");
        } catch (IllegalStateException e) {
            fail("Unexpected IllegalStateException for path A: " + e.getMessage());
        }

        // Explorar camino redundante 2: "b -> c -> d -> FINAL"
        try {
            assertTrue(smPathB.validateTransition("b"), "Transition to 'b' should be valid for path B");
            assertTrue(smPathB.validateTransition("c"), "Transition to 'c' should be valid for path B");
            assertTrue(smPathB.validateTransition("d"), "Transition to 'd' should be valid for path B");
            assertTrue(smPathB.validateTransition("FINAL"), "Transition to 'FINAL' should be valid for path B");

            // Verificar que está en estado final
            assertTrue(smPathB.isInFinalState(), "StateMachine for path B should be in a final state");
        } catch (IllegalStateException e) {
            fail("Unexpected IllegalStateException for path B: " + e.getMessage());
        }

        System.out.println("[TEST] Finished testRedundantPaths");
    }

    @Test
    public void testComplexStateMachineWithCycles() {
        TestClass instance = new TestClass("complex-cycles");
        ExecutionOrderRegistry.register(instance, "(start -> a -> b -> c -> d -> a -> FINAL)");
        StateMachine sm = ExecutionOrderRegistry.getStateMachine(instance);
        assertNotNull(sm, "StateMachine should not be null for complex cycles");

        assertTrue(sm.validateTransition("start"), "Transition to 'start' should be valid");
        assertTrue(sm.validateTransition("a"), "Transition to 'a' should be valid");
        assertTrue(sm.validateTransition("b"), "Transition to 'b' should be valid");
        assertTrue(sm.validateTransition("c"), "Transition to 'c' should be valid");
        assertTrue(sm.validateTransition("d"), "Transition to 'd' should be valid");
        // Loop back to 'a'
        assertTrue(sm.validateTransition("a"), "Transition back to 'a' should be valid");
        // Finally, to 'FINAL'
        assertTrue(sm.validateTransition("FINAL"), "Transition to 'FINAL' should be valid");
    }

    @Test
    public void testInvalidStateMachineCombination() {
        TestClass instance = new TestClass("invalid-combination");
        assertThrows(IllegalArgumentException.class, () -> {
            ExecutionOrderRegistry.register(instance, "(start & | end)");
        }, "Expected an IllegalArgumentException for invalid configuration");
    }
    
    @Test
    public void testOrPaths() {
        System.out.println("[TEST] Starting testOrPaths");

        // Registro de la máquina de estados con caminos alternativos
        TestClass instanceA = new TestClass("id-or-paths-a");
        TestClass instanceB = new TestClass("id-or-paths-b");

        ExecutionOrderRegistry.register(instanceA, "start -> (a | b) -> end");
        ExecutionOrderRegistry.register(instanceB, "start -> (a | b) -> end");

        // Obtener las máquinas de estados asociadas
        StateMachine smA = ExecutionOrderRegistry.getStateMachine(instanceA);
        StateMachine smB = ExecutionOrderRegistry.getStateMachine(instanceB);

        assertNotNull(smA, "StateMachine A should not be null");
        assertNotNull(smB, "StateMachine B should not be null");

        try {
            // Validar transición por el camino "a" en la máquina A
            assertTrue(smA.validateTransition("start"), "Transition to 'start' in machine A should be valid");
            assertTrue(smA.validateTransition("a"), "Transition to 'a' in machine A should be valid");
            assertTrue(smA.validateTransition("end"), "Transition to 'end' in machine A should be valid");

            // Validar transición por el camino "b" en la máquina B
            assertTrue(smB.validateTransition("start"), "Transition to 'start' in machine B should be valid");
            assertTrue(smB.validateTransition("b"), "Transition to 'b' in machine B should be valid");
            assertTrue(smB.validateTransition("end"), "Transition to 'end' in machine B should be valid");

            // Verificar que ambas máquinas están en un estado final
            assertTrue(smA.isInFinalState(), "StateMachine A should be in a final state");
            assertTrue(smB.isInFinalState(), "StateMachine B should be in a final state");

        } catch (IllegalStateException e) {
            fail("Unexpected IllegalStateException: " + e.getMessage());
        }

        System.out.println("[TEST] Finished testOrPaths");
    }
}

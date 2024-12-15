package com.luis.tfg.ExecutionOrder;

import org.junit.jupiter.api.Test;

public class AgentExecutionTest {
	
    public static void main(String[] args) {
        System.out.println("[INFO] Starting AgentExecutionTest");

        // Instanciar la clase anotada
        EcommerceOrder order = new EcommerceOrder("1");
        EcommerceOrder order2 = new EcommerceOrder("2");

        System.out.println("[INFO] Simulating invalid execution...");
        try {
            // Ejecución inválida: se omite "checkout"
            order.addToCart();
            order.payment();
            order.confirmation();
        } catch (IllegalStateException e) {
            System.out.println("[ERROR] Invalid method execution: " + e.getMessage());
        }

        System.out.println("\n[INFO] Simulating valid execution...");
        try {
            // Ejecución válida: sigue el flujo correcto
            order2.addToCart();
            order2.checkout();
            order2.payment();
            order2.confirmation();
        } catch (IllegalStateException e) {
            System.out.println("[ERROR] Unexpected error in valid execution: " + e.getMessage());
        }
    }
}
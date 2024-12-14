package com.luis.tfg.security;

import java.util.Objects;

@ExecutionOrder("(addToCart checkout payment confirmation)")
public class EcommerceOrder {
	
	String id;

	public EcommerceOrder(String id) {
		this.id = id;
	}
	
    public void addToCart() {
        System.out.println("[ACTION] Adding items to cart...");
    }

    public void checkout() {
        System.out.println("[ACTION] Checking out...");
    }

    public void payment() {
        System.out.println("[ACTION] Processing payment...");
    }

    public void confirmation() {
        System.out.println("[ACTION] Sending order confirmation...");
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
}

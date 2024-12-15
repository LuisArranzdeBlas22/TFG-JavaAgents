package com.luis.tfg.security;

import java.util.Objects;


public class TestClass {
    private final String uniqueId;

    public TestClass(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestClass that = (TestClass) o;
        return Objects.equals(uniqueId, that.uniqueId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uniqueId);
    }

    @Override
    public String toString() {
        // Devuelve una representación personalizada de la clase, evita llamar a 'this' o 'String.valueOf(this)'
        return "TestClass{id='" + uniqueId + "'}";
    }
}
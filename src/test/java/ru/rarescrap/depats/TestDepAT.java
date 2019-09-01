package ru.rarescrap.depats;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;
import ru.rarescrap.depats.data.DepAT;

public class TestDepAT {

    @Test
    public void testEquals() {
        EqualsVerifier.forClass(DepAT.class).verify(); // TODO: И для потомков тоже
    }
}

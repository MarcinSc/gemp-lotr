package com.gempukku.context;

import com.gempukku.context.initializer.inject.AnnotationSystemInitializer;
import com.gempukku.context.resolver.expose.AnnotationSystemResolver;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InjectionAndResolvingTest {
    @Test
    public void javaSideTest() {
        ExampleOtherSystem otherSystem = new ExampleOtherSystem();
        ExampleSystem system = new ExampleSystem();

        DefaultGempukkuContext context = new DefaultGempukkuContext(
                null,
                new AnnotationSystemResolver(
                        Arrays.stream(
                                new Object[]{system, otherSystem}
                        ).toList()
                ), new AnnotationSystemInitializer(),
                null
        );

        context.initialize();

        assertEquals(otherSystem, system.getOtherSystem());
        assertEquals(1, system.getOtherSystems().size());
        assertEquals(otherSystem, system.getOtherSystems().getFirst());
    }
}

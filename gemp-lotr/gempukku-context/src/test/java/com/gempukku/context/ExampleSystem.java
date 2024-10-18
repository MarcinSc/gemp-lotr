package com.gempukku.context;

import com.gempukku.context.processor.inject.Inject;
import com.gempukku.context.processor.inject.InjectList;

import java.util.List;

public class ExampleSystem {
    @Inject
    private OtherSystem otherSystem;

    @InjectList
    private List<OtherSystem> otherSystems;

    public OtherSystem getOtherSystem() {
        return otherSystem;
    }

    public List<OtherSystem> getOtherSystems() {
        return otherSystems;
    }
}

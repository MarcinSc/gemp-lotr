package com.gempukku.lotro.async;

import com.gempukku.lotro.builder.DaoBuilder;
import com.gempukku.lotro.builder.ServerBuilder;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class GempukkuServer {
    private final Map<Type, Object> context;

    public GempukkuServer() {
        Map<Type, Object> objects = new HashMap<>();


        //Libraries and other important prereq managers that are used by lots of other managers
        ServerBuilder.CreatePrerequisites(objects);
        //Now bulk initialize various managers
        DaoBuilder.CreateDatabaseAccessObjects(objects);
        ServerBuilder.CreateServices(objects);
        ServerBuilder.StartServers(objects);

        context = objects;
    }

    public Map<Type, Object> getContext() {
        return context;
    }
}

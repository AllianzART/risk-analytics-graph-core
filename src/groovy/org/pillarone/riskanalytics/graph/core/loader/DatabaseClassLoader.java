package org.pillarone.riskanalytics.graph.core.loader;

import java.util.List;

public class DatabaseClassLoader extends ClassLoader {

    private List<String> availableClasses;

    public DatabaseClassLoader(ClassLoader parent) {
        super(parent);
        availableClasses = GroovyHelper.getAllClassNames();
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (!availableClasses.contains(name)) {
            throw new ClassNotFoundException(name);
        }
        byte[] data = GroovyHelper.getClassDefinition(name);
        return defineClass(name, data, 0, data.length);

    }


}

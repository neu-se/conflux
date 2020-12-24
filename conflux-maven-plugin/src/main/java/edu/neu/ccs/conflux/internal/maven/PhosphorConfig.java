package edu.neu.ccs.conflux.internal.maven;

import java.io.File;
import java.util.Properties;

/**
 * Record type.
 */
public class PhosphorConfig {
    public String name = null;
    public Properties options = new Properties();
    public File instrumentedJVM = null;

    public String getName() {
        return name;
    }

    public Properties getOptions() {
        return options;
    }

    public File getInstrumentedJVM() {
        return instrumentedJVM;
    }

    @Override
    public String toString() {
        return "PhosphorConfig{" +
                "name='" + name + '\'' +
                ", options=" + options +
                ", instrumentedJVM=" + instrumentedJVM +
                '}';
    }
}

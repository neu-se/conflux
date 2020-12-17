package edu.gmu.swe.phosphor.ignored.maven;

import java.io.File;
import java.util.Properties;

/**
 * Record type.
 */
public class PhosphorConfig {
    public String name = null;
    public Properties options = new Properties();
    public File instrumentedJVM = null;

    @Override
    public String toString() {
        return "PhosphorConfig{" +
                "name='" + name + '\'' +
                ", options=" + options +
                ", instrumentedJVM=" + instrumentedJVM +
                '}';
    }
}

package edu.gmu.swe.phosphor;

import org.apache.maven.surefire.api.util.ScannerFilter;

public class IdentityFilter implements ScannerFilter {
    @Override
    public boolean accept(Class aClass) {
        return true;
    }
}

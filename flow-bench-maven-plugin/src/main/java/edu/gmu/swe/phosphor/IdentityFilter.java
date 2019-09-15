package edu.gmu.swe.phosphor;

import org.apache.maven.surefire.util.ScannerFilter;

public class IdentityFilter implements ScannerFilter {

    @Override
    public boolean accept(Class testClass) {
        return true;
    }
}

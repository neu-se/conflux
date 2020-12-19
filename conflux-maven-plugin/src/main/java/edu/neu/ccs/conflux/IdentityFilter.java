package edu.neu.ccs.conflux;

import org.apache.maven.surefire.api.util.ScannerFilter;

public class IdentityFilter implements ScannerFilter {
    @Override
    public boolean accept(Class aClass) {
        return true;
    }
}

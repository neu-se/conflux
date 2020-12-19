package edu.neu.ccs.conflux;

import edu.columbia.cs.psl.phosphor.PreMain;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

public class BaseConfluxTest {
    @Before
    public void clearErrorFlag() {
        PreMain.INSTRUMENTATION_EXCEPTION_OCCURRED = false;
    }

    @After
    public void checkForError() {
		if(PreMain.INSTRUMENTATION_EXCEPTION_OCCURRED) {
			Assert.fail("Instrumentation error occurred");
		}
    }
}

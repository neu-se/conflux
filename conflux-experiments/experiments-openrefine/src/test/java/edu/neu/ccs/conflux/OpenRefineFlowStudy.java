package edu.neu.ccs.conflux;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.refine.ProjectMetadata;
import com.google.refine.RefineServletStub;
import com.google.refine.importers.ImportingParserBase;
import com.google.refine.importers.SeparatorBasedImporter;
import com.google.refine.importing.ImportingJob;
import com.google.refine.importing.ImportingManager;
import com.google.refine.model.Project;
import com.google.refine.util.ParsingUtilities;
import edu.columbia.cs.psl.phosphor.runtime.MultiTainter;
import edu.neu.ccs.conflux.internal.FlowStudy;
import edu.neu.ccs.conflux.internal.runtime.StudyTaintTagChecker;
import org.mockito.Mockito;

import java.io.StringReader;
import java.util.ArrayList;

public class OpenRefineFlowStudy {

    /**
     * https://github.com/OpenRefine/OpenRefine/issues/2584
     */
    @FlowStudy(project = "openrefine", issue = "2583")
    public void issue652(StudyTaintTagChecker checker) {
        String csv = FlowEvalUtil.readResource(getClass(), "/openrefine-2583.csv");
        String json = FlowEvalUtil.readResource(getClass(), "/openrefine-2583.json");
        char[] c = csv.toCharArray();
        int i;
        for (i = 0; i < c.length; i++) {
            c[i] = MultiTainter.taintedChar(c[i], i);
        }
        csv = new String(c);
        c = json.toCharArray();
        for (int j = 0; j < c.length; j++) {
            c[j] = MultiTainter.taintedChar(c[j], i++);
        }
        json = new String(c);
        checker.recordInput(csv + json);
        RefineServletStub servlet = new RefineServletStub();
        ImportingManager.initialize(servlet);
        ObjectNode options = ParsingUtilities.evaluateJsonStringToObjectNode(json);
        ImportingJob job = Mockito.spy(ImportingManager.createJob());
        Mockito.when(job.getRetrievalRecord()).thenReturn(ParsingUtilities.mapper.createObjectNode());
        Project project = new Project();
        ProjectMetadata metadata = new ProjectMetadata();
        ImportingParserBase parser = new SeparatorBasedImporter();
        try {
            parser.parseOneFile(project, metadata, job, "file-source", new StringReader(csv), -1, options,
                    new ArrayList<>());
            project.update();
        } catch (Throwable t) {
            checker.check(t);
            return;
        }
        throw new AssertionError("Expected exception to be thrown");
    }
}

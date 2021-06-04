package edu.neu.ccs.conflux;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.refine.ProjectMetadata;
import com.google.refine.RefineServletStub;
import com.google.refine.importers.SeparatorBasedImporter;
import com.google.refine.importing.ImportingJob;
import com.google.refine.importing.ImportingManager;
import com.google.refine.model.Project;
import com.google.refine.util.ParsingUtilities;

import java.io.StringReader;
import java.util.ArrayList;

public class OpenRefineRunner extends StudyRunner {
    public OpenRefineRunner() {
        super(ClassCastException.class, new StackTraceElement(
                "com.google.refine.importers.TabularImportingParserBase",
                "readTable",
                "TabularImportingParserBase.java",
                173
        ), "/openrefine-2583.json");
    }

    @Override
    protected void run(String input) {
        // Note: ParsingUtilities will call Throwable.printStackTrace in the case of certain errors
        String csv = FlowEvalUtil.readResource(getClass(), "/openrefine-2583-min.csv");
        RefineServletStub servlet = new RefineServletStub();
        ImportingManager.initialize(servlet);
        ObjectNode options = ParsingUtilities.evaluateJsonStringToObjectNode(input);
        ImportingJob job = ImportingManager.createJob();
        try {
            new SeparatorBasedImporter().parseOneFile(new Project(), new ProjectMetadata(), job,
                    "file-source", new StringReader(csv),
                    -1, options, new ArrayList<>());
        } finally {
            ImportingManager.disposeJob(job.id);
        }
    }
}

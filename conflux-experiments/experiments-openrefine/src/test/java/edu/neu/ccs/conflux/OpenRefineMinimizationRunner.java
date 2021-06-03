package edu.neu.ccs.conflux;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.refine.ProjectMetadata;
import com.google.refine.RefineServletStub;
import com.google.refine.importers.SeparatorBasedImporter;
import com.google.refine.importing.ImportingJob;
import com.google.refine.importing.ImportingManager;
import com.google.refine.model.Project;
import com.google.refine.util.ParsingUtilities;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

public class OpenRefineMinimizationRunner extends MinimizationRunner {
    public OpenRefineMinimizationRunner() {
        super(ClassCastException.class, new StackTraceElement(
                "com.google.refine.importers.TabularImportingParserBase",
                "readTable",
                "TabularImportingParserBase.java",
                173
        ), 0);
    }

    @Override
    protected void test(String json) {
        String csv = FlowEvalUtil.readResource(getClass(), "/openrefine-2583-min.csv");
        RefineServletStub servlet = new RefineServletStub();
        ImportingManager.initialize(servlet);
        ObjectNode options = ParsingUtilities.evaluateJsonStringToObjectNode(json);
        ImportingJob job = ImportingManager.createJob();
        try {
            new SeparatorBasedImporter().parseOneFile(new Project(), new ProjectMetadata(), job,
                    "file-source", new StringReader(csv),
                    -1, options, new ArrayList<>());
        } finally {
            ImportingManager.disposeJob(job.id);
        }
    }

    public static void main(String[] arguments) throws IOException {
        new OpenRefineMinimizationRunner().run(new File(arguments[0]));
        System.exit(0);
    }
}

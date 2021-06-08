package edu.neu.ccs.conflux;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.refine.ProjectMetadata;
import com.google.refine.RefineServletStub;
import com.google.refine.importers.SeparatorBasedImporter;
import com.google.refine.importing.ImportingJob;
import com.google.refine.importing.ImportingManager;
import com.google.refine.model.Project;
import com.google.refine.util.ParsingUtilities;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Pattern;

public class OpenRefineRunner extends StudyRunner {

    private static final String SEPARATOR = "$$$$";
    private static final PrintStream DISCARD = new PrintStream(new OutputStream() {
        @Override
        public void write(int b) {
        }
    });

    public OpenRefineRunner() {
        super(ClassCastException.class, new StackTraceElement(
                "com.google.refine.importers.TabularImportingParserBase",
                "readTable",
                "TabularImportingParserBase.java",
                173
        ));
    }

    @Override
    public void run(String input) {
        if (input.contains(SEPARATOR)) {
            String[] elements = input.split(Pattern.quote(SEPARATOR));
            run(elements[0], elements[1]);
        }
    }

    private void run(String csv, String json) {
        PrintStream err = System.err;
        // ParsingUtilities will call Throwable.printStackTrace in the case of certain errors
        // Temporarily discard output to standard err discard these errors
        System.setErr(DISCARD);
        RefineServletStub servlet = new RefineServletStub();
        ImportingManager.initialize(servlet);
        try {
            ObjectNode options = ParsingUtilities.evaluateJsonStringToObjectNode(json);
            ImportingJob job = ImportingManager.createJob();
            try {
                new SeparatorBasedImporter().parseOneFile(new Project(), new ProjectMetadata(), job,
                        "file-source", new StringReader(csv),
                        -1, options, new ArrayList<>());
            } finally {
                job.canceled = true;
                ImportingManager.disposeJob(job.id);
            }
        } finally {
            System.setErr(err);
            try {
                Field f = ImportingManager.class.getDeclaredField("service");
                f.setAccessible(true);
                ScheduledExecutorService service = (ScheduledExecutorService) f.get(null);
                service.shutdownNow();
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getInitial() {
        String csv = FlowEvalUtil.readResource(getClass(), "/openrefine-2583.csv");
        String json = FlowEvalUtil.readResource(getClass(), "/openrefine-2583.json");
        return csv + SEPARATOR + json;
    }
}
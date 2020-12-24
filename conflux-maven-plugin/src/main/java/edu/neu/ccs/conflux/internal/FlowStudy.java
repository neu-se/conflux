package edu.neu.ccs.conflux.internal;

import edu.neu.ccs.conflux.internal.runtime.TaintTagChecker;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The <code>FlowStudy</code> annotation indicates that a method can be run as a taint tracking study.
 * The annotated method must be <code>public</code> and <code>void</code>.
 * It must have only one parameter of type {@link TaintTagChecker}.
 * This <code>TaintTagChecker</code> is used by the study to check whether the set of taint tags
 * that propagated to a value matches an expected set of taint tags.
 * <p>
 * To run a study, a new instance of the class that owns the study method is created.
 * Then, this instance is used to invoke the study method.
 * Any uncaught exceptions thrown during the execution of the study will cause the study to result in an error.
 * <p>
 * The <code>FlowStudy</code> annotation requires two parameters: <code>project</code> and <code>issue</code>.
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface FlowStudy {

    String project();

    String issue();
}

package edu.neu.ccs.conflux.internal;

import edu.neu.ccs.conflux.internal.runtime.BenchTaintTagChecker;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The <code>FlowBench</code> annotation indicates that a method can be run as a taint tracking benchmark.
 * The annotated method must be <code>public</code> and <code>void</code>.
 * It must have two parameters: the first of type {@link BenchTaintTagChecker} and the second of type <code>int</code>.
 * The parameter of type <code>BenchTaintTagChecker</code> is used by the benchmark to check whether the set of taint tags
 * that propagated to a value matches an expected set of taint tags.
 * The parameter of type <code>int</code> specifies the length of the input (measured in abstract entities,
 * i.e., an atomic unit of information with respect to the transformation featured in the benchmark) used by the
 * benchmark.
 * <p>
 * To run a benchmark, a new instance of the class that owns the benchmark method is created.
 * Then, this instance is used to invoke the benchmark method.
 * Any uncaught exceptions thrown during the execution of the benchmark will cause the benchmark to result in an error.
 * <p>
 * The <code>FlowBench</code> annotation requires three parameters: <code>group</code>, <code>project</code>, and
 * <code>implementation</code>.
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface FlowBench {

    String group();

    String project();

    String implementation();
}


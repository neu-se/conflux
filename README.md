# Conflux: A Practical Approach for Dynamic Taint Tracking with Control-Flow Relationships

Conflux is a system that uses alternative semantics for propagating taint tags along control flows. 
These semantics aim to reduce control-flow-related over-tainting by decreasing the scopes of control flows and by providing a heuristic for reducing loop-related over-tainting.
This repository contains the source code for Conflux.

Conflux is an extension to [Phosphor](https://github.com/gmu-swe/phosphor), a Java dynamic taint tracking framework.
Currently, Phosphor only fully supports Java versions 8 or lower.

## Requirements

* Java Development Kit 8
* [Apache Maven](https://maven.apache.org/) 3.3.0+

## Building Conflux

1. Make sure that you have some version of OpenJDK 8 installed. Set the JAVA_HOME environmental variable to this path.
   For example, on Mac, run `export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_222-openjdk/Contents/Home/`.
2. Clone this repository.
3. In the root project directory, run `mvn -DskipTests install` to build Conflux.

## Experiments

The following instructions assume that you have already built Conflux following the above instructions. 
In the "conflux-experiments" directory execute the command `mvn install` to run all the benchmarks and case study experiments.

### Interpreting the Results

The results of the experiments will be written to the file "conflux-experiments/experiments-report/target/flow-report.json".
This file will contain a JSON (JavaScript Object Notation) object with two properties: "studies" and "benchmarks".

The value of "studies" reports the results of our case study experiment examining the efficacy of different control flow propagation policies at determining failure-inducing inputs.
Each element of the "studies" array has four properties: "project", "issue", "input", and "policies".
The "project" and "issue" properties identify which case study subject (i.e., failure) was examined.
The "input" property specifies the failing-inducing input used to reproduce the failure being examined.
The "policies" property reports which indices are of the failing-inducing input were reported are failure-relevant by the different control flow propagation policies.

The value of "benchmarks" reports the results of different control flow propagation policies on our taint tracking benchmark suite.
Each element of the "benchmarks" array has four properties: "group", "implementation", "project", and "policies".
The "group", "implementation", and "project" properties identify which benchmark was executed.
The "policies" property reports the number of true positives, false positives, and false negatives produced by the different control flow propagation policies for different input size (measured in number of entities).

### Reducing Failure-Inducing Inputs

In the "conflux-experiments" directory, run ```mvn -Preduce -DskipTests verify``` to reduce the failure-inducing inputs for all the case study subjects.
The command will print to the console the length and reduced failure-inducing input for each of case study subjects.

### Notes:

When any of Conflux's tests or experiments are first run, the build process will create four Phosphor-instrumented JVMs:
jvm-inst-data-only, jvm-inst-basic-control, jvm-inst-strict-control, and jvm-inst-conflux. 
Each of these JVMs is instrumented to support a different control flow propagation policy.

## License

This software release is licensed under the BSD 3-Clause License.

Copyright (c) 2021, Katherine Hough and Jonathan Bell 
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following
   disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
   disclaimer in the documentation and/or other materials provided with the distribution.

3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products
   derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

## Acknowledgements

Conflux makes use of the following libraries:

* [ASM](http://asm.ow2.org/license.html), (c) 2000-2011 INRIA, France
  Telecom, [license](http://asm.ow2.org/license.html)
* [Apache Harmony](https://harmony.apache.org), (c) The Apache Software
  Foundation, [license](http://www.apache.org/licenses/LICENSE-2.0)

Conflux's performance tuning is made possible by
the [JProfiler](https://www.ej-technologies.com/products/jprofiler/overview.html), a Java profiler.

Katherine Hough and Jonathan Bell are funded in part by NSF CCF-2100037, NSF CNS-2100015, and the NSA under contract number H98230-18-D-008.
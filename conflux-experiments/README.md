To build the benchmark and study experiments run:

```mvn -DskipTests install```

To reduce the failure-inducing inputs for the study subjects run:

```mvn -Preduce -DskipTests verify [-Ddd.log.test]```

To reduce the failure-inducing input for a particular study subject run:

```mvn -f experiments-<subject>/pom.xml -Preduce -DskipTests verify [-Ddd.log.test]```
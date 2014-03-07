JPMML-Cascading
===============

Cascading application framework (http://www.cascading.org) library for scoring PMML models on Apache Hadoop.

# Installation #

Enter the project root directory and build using [Apache Maven] (http://maven.apache.org/):
```
mvn clean install
```

The build produces two JAR files:
* `pmml-cascading/target/pmml-cascading-${version}.jar` - Library JAR file.
* `pmml-cascading-example/target/example-${version}-job.jar` - Hadoop job JAR file.

# Usage #

## Library ##

The JPMML library provides interface `org.jpmml.evaluator.Evaluator` for performing model input variable preparation and model evaluation. It is best to be obtained via the factory method:
```java
PMML pmml = IOUtil.unmarshal(...);

// Transform default SAX Locator information to java.io.Serializable form
pmml.accept(new SourceLocationTransformer());

PMMLManager pmmlManager = new PMMLManager(pmml);

Evaluator evaluator = (Evaluator)pmmlManager.getModelManager(null, ModelEvaluatorFactory.getInstance());
```

The JPMML-Cascading library provides Cascading assembly planner class `org.jpmml.cascading.PMMLPlanner`, which integrates the specified `org.jpmml.evaluator.Evaluator` instance into the specified flow instance. Internally, the heavy-lifting is handled by Cascading function class `org.jpmml.cascading.PMMLFunction`. The argument fields of the function match the active fields in the [MiningSchema element] (http://www.dmg.org/v4-1/MiningSchema.html). The output fields of the function match the predicted fields in the [MiningSchema element] (http://www.dmg.org/v4-1/MiningSchema.html), plus all the output fields in the [Output element] (http://www.dmg.org/v4-1/Output.html).
```java
Evaluator evaluator = ...

FlowDef flowDef = ...

PMMLPlanner pmmlPlanner = new PMMLPlanner(evaluator);
pmmlPlanner.setRetainOnlyActiveFields();

flowDef = flowDef.addAssemblyPlanner(pmmlPlanner);
```

## Hadoop job ##

The Hadoop job JAR file contains a single executable class `org.jpmml.cascading.Main`. It expects three arguments: 1) the name of the PMML file in local filesystem, 2) the Cascading Hfs specification of the source resource and 3) the Cascading Hfs specification of the sink resource:

For example, the following command scores the PMML file `P:/cascading/model.pmml` by reading arguments from the input file `P:/cascading/input.tsv` (TSV data format) and writing results to the output directory `P:/cascading/output`:
```
hadoop jar example-1.0-SNAPSHOT-job.jar P:/cascading/model.pmml file:///P:/cascading/input.tsv file:///P:/cascading/output
```

# Additional information #

Please contact: [info@openscoring.io] (mailto:info@openscoring.io)
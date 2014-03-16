JPMML-Cascading [![Build Status](https://travis-ci.org/jpmml/jpmml-cascading.png?branch=master)](https://travis-ci.org/jpmml/jpmml-cascading)
===============

[Cascading application framework] (http://www.cascading.org) library for scoring PMML models on Apache Hadoop.

# Features #

JPMML-Cascading is a thin wrapper around [JPMML-Model] (https://github.com/jpmml/jpmml-model) and [JPMML-Evaluator] (https://github.com/jpmml/jpmml-evaluator) libraries.

# Installation #

## Library ##

JPMML-Cascading library JAR file is released via [Maven Central Repository] (http://repo1.maven.org/maven2/org/jpmml/). Please join the [JPMML mailing list] (https://groups.google.com/forum/#!forum/jpmml) for release announcements.

The current version is **1.1.1** (16 March, 2014).

```xml
<dependency>
	<groupId>org.jpmml</groupId>
	<artifactId>pmml-cascading</artifactId>
	<version>1.1.1</version>
</dependency>
```

## Hadoop job ##

Enter the project root directory and build using [Apache Maven] (http://maven.apache.org/):
```
mvn clean install
```

The build produces two JAR files:
* `pmml-cascading/target/pmml-cascading-1.1-SNAPSHOT.jar` - Library JAR file.
* `pmml-cascading-example/target/example-1.1-SNAPSHOT-job.jar` - Hadoop job JAR file.

# Usage #

## Library ##

The [JPMML-Model] (https://github.com/jpmml/jpmml-model) library provides facilities for loading PMML schema version 3.X and 4.X documents into an instance of `org.dmg.pmml.PMML`:
```java
// Use SAX filtering to transform PMML schema version 3.X and 4.X documents to PMML schema version 4.2 document
Source source = ImportFilter.apply(...);

PMML pmml = JAXBUtil.unmarshalPMML(source);

// Transform default SAX Locator information to java.io.Serializable form
pmml.accept(new SourceLocationTransformer());
```

The [JPMML-Evaluator] (https://github.com/jpmml/jpmml-evaluator) library provides facilities for obtaining a proper instance of `org.jpmml.evaluator.ModelEvaluator`:
```java
PMML pmml = ...;

PMMLManager pmmlManager = new PMMLManager(pmml);

ModelEvaluator<?> modelEvaluator = (ModelEvaluator<?>)pmmlManager.getModelManager(null, ModelEvaluatorFactory.getInstance());
```

The JPMML-Cascading library itself provides Cascading assembly planner class `org.jpmml.cascading.PMMLPlanner`, which integrates the specified `org.jpmml.evaluator.ModelEvaluator` instance into the specified Cascading flow instance. Internally, the heavy-lifting is handled by Cascading function class `org.jpmml.cascading.PMMLFunction`. The argument fields of the function match the active fields in the [MiningSchema element] (http://www.dmg.org/v4-2/MiningSchema.html). The output fields of the function match the target fields in the [MiningSchema element] (http://www.dmg.org/v4-2/MiningSchema.html), plus all the output fields in the [Output element] (http://www.dmg.org/v4-2/Output.html).
```java
ModelEvaluator<?> modelEvaluator = ...;

FlowDef flowDef = ...;

PMMLPlanner pmmlPlanner = new PMMLPlanner(modelEvaluator);
pmmlPlanner.setRetainOnlyActiveFields();

flowDef = flowDef.addAssemblyPlanner(pmmlPlanner);
```

Please see [the example application] (https://github.com/jpmml/jpmml-cascading/blob/master/pmml-cascading-example/src/main/java/org/jpmml/cascading/Main.java) for full picture.

## Hadoop job ##

The Hadoop job JAR file contains a single executable class `org.jpmml.cascading.Main`. It expects three arguments: 1) the name of the PMML file in local filesystem, 2) the Cascading Hfs specification of the source resource and 3) the Cascading Hfs specification of the sink resource:

For example, the following command scores the PMML file `P:/cascading/model.pmml` by reading arguments from the input file `P:/cascading/input.tsv` (TSV data format) and writing results to the output directory `P:/cascading/output`:
```
hadoop jar example-1.1-SNAPSHOT-job.jar P:/cascading/model.pmml file:///P:/cascading/input.tsv file:///P:/cascading/output
```

# License #

JPMML-Cascading is dual-licensed under the [GNU Affero General Public License (AGPL) version 3.0] (http://www.gnu.org/licenses/agpl-3.0.html) and a commercial license.

# Additional information #

Please contact [info@openscoring.io] (mailto:info@openscoring.io)
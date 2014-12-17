JPMML-Cascading [![Build Status](https://travis-ci.org/jpmml/jpmml-cascading.png?branch=master)](https://travis-ci.org/jpmml/jpmml-cascading)
===============

PMML evaluator library for the [Cascading application framework] (http://www.cascading.org).

# Features #

* Full support for PMML specification versions 3.0 through 4.2. The evaluation is handled by the [JPMML-Evaluator] (https://github.com/jpmml/jpmml-evaluator) library.

# Prerequisites #

* Cascading application framework version 2.2.0 or greater.

# Installation #

## Library ##

JPMML-Cascading library JAR file is released via [Maven Central Repository] (http://repo1.maven.org/maven2/org/jpmml/). Please join the [JPMML mailing list] (https://groups.google.com/forum/#!forum/jpmml) for release announcements.

The current version is **1.1.5** (17 December, 2014).

```xml
<dependency>
	<groupId>org.jpmml</groupId>
	<artifactId>pmml-cascading</artifactId>
	<version>1.1.5</version>
</dependency>
```

## Example Hadoop job ##

Enter the project root directory and build using [Apache Maven] (http://maven.apache.org/):
```
mvn clean install
```

The build produces two JAR files:
* `pmml-cascading/target/pmml-cascading-1.1-SNAPSHOT.jar` - Library JAR file.
* `pmml-cascading-example/target/example-1.1-SNAPSHOT-job.jar` - Example Hadoop job JAR file.

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

ModelEvaluator<?> modelEvaluator = (ModelEvaluator<?>)pmmlManager.getModelManager(ModelEvaluatorFactory.getInstance());
```

The JPMML-Cascading library itself provides Cascading assembly planner class `org.jpmml.cascading.PMMLPlanner`, which integrates the specified `org.jpmml.evaluator.ModelEvaluator` instance into the specified Cascading flow instance. Internally, the heavy-lifting is handled by Cascading function class `org.jpmml.cascading.PMMLFunction`. The argument fields of the function match the active fields in the [MiningSchema element] (http://www.dmg.org/v4-2-1/MiningSchema.html). The output fields of the function match the target fields in the [MiningSchema element] (http://www.dmg.org/v4-2-1/MiningSchema.html), plus all the output fields in the [Output element] (http://www.dmg.org/v4-2-1/Output.html).
```java
ModelEvaluator<?> modelEvaluator = ...;

FlowDef flowDef = ...;

PMMLPlanner pmmlPlanner = new PMMLPlanner(modelEvaluator);
pmmlPlanner.setRetainOnlyActiveFields();

flowDef = flowDef.addAssemblyPlanner(pmmlPlanner);
```

Please see [the example application] (https://github.com/jpmml/jpmml-cascading/blob/master/pmml-cascading-example/src/main/java/org/jpmml/cascading/Main.java) for full picture.

## Example Hadoop job ##

The example Hadoop job JAR file contains a single executable class `org.jpmml.cascading.Main`.

This class expects three command-line arguments:

1. The path of the **model** PMML file in local filesystem.
2. The path of the Cascading **source** CSV resource in Hadoop filesystem.
3. The path of the Cascading **sink** CSV resource in Hadoop filesystem.

For example:
```
hadoop jar example-1.1-SNAPSHOT-job.jar /tmp/cascading/model.pmml file:///tmp/cascading/input.csv file:///tmp/cascading/output
```

# License #

JPMML-Cascading is dual-licensed under the [GNU Affero General Public License (AGPL) version 3.0] (http://www.gnu.org/licenses/agpl-3.0.html) and a commercial license.

# Additional information #

Please contact [info@openscoring.io] (mailto:info@openscoring.io)
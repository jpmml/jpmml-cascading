JPMML-Cascading [![Build Status](https://travis-ci.org/jpmml/jpmml-cascading.png?branch=master)](https://travis-ci.org/jpmml/jpmml-cascading)
===============

PMML evaluator library for the [Cascading application framework] (http://www.cascading.org).

# Features #

* Full support for PMML specification versions 3.0 through 4.2. The evaluation is handled by the [JPMML-Evaluator] (https://github.com/jpmml/jpmml-evaluator) library.

# Prerequisites #

* Cascading application framework version 2.2.0 or greater.

# Installation #

## Library ##

JPMML-Cascading library JAR file is released via [Maven Central Repository] (http://repo1.maven.org/maven2/org/jpmml/).

The current version is **1.2.1** (15 December, 2015).

```xml
<dependency>
	<groupId>org.jpmml</groupId>
	<artifactId>pmml-cascading</artifactId>
	<version>1.2.1</version>
</dependency>
```

## Example Hadoop job ##

Enter the project root directory and build using [Apache Maven] (http://maven.apache.org/):
```
mvn clean install
```

The build produces two JAR files:
* `pmml-cascading/target/pmml-cascading-1.2-SNAPSHOT.jar` - Library JAR file.
* `pmml-cascading-example/target/example-1.2-SNAPSHOT-job.jar` - Example Hadoop job JAR file.

# Usage #

## Library ##

Constructing an instance of Cascading planner class `org.jpmml.cascading.PMMLPlanner` based on a PMML document in local filesystem:
```java
File pmmlFile = ...;
Evaluator evaluator = PMMLPlannerUtil.createEvaluator(pmmlFile);
PMMLPlanner pmmlPlanner = new PMMLPlanner(evaluator);
```

Building a simple flow for scoring data:
```java
FlowDef flowDef = ...;

flowDef = flowDef.addSource("input", ...);
flowDef = flowDef.addSink("output", ...);

pmmlPlanner.setHeadName("input");
pmmlPlanner.setTailName("output");

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
hadoop jar example-1.2-SNAPSHOT-job.jar /tmp/cascading/model.pmml file:///tmp/cascading/input.csv file:///tmp/cascading/output
```

# License #

JPMML-Cascading is dual-licensed under the [GNU Affero General Public License (AGPL) version 3.0] (http://www.gnu.org/licenses/agpl-3.0.html) and a commercial license.

# Additional information #

Please contact [info@openscoring.io] (mailto:info@openscoring.io)
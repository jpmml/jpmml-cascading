# Usage #

Enter the project root directory and build using [Apache Maven] (http://maven.apache.org/):
```
mvn clean install
```

The build produces the following two JAR files (located in the `target` directory):
* `cascading-${version}.jar` - Library JAR file.
* `jpmml-cascading-${version}-job.jar` - Hadoop job JAR file.


## Library ##

JPMML provides high-level interface `org.jpmml.evaluator.Evaluator` for performing model input variable preparation and model evaluation.

The Cascading function class `org.jpmml.cascading.PMMLFunction` is parameterized with an instance of `org.jpmml.evaluator.Evaluator`. The argument fields of the function match the active fields in the [MiningSchema element] (http://www.dmg.org/v4-1/MiningSchema.html). The output fields of the function match the predicted fields in the [MiningSchema element] (http://www.dmg.org/v4-1/MiningSchema.html), plus the output fields in the [Output element] (http://www.dmg.org/v4-1/Output.html).
```
Evaluator evaluator = ...

Fields argumentFields = FieldsUtil.getActiveFields(evaluator);
Fields outputFields = (FieldsUtil.getPredictedFields(evaluator)).append(FieldsUtil.getOutputFields(evaluator));

PMMLFunction pmmlFunction = new PMMLFunction(outputFields, evaluator);

Each each = new Each(..., argumentFields, pmmlFunction, outputFields);
```

The Cascading assembly planner class `org.jpmml.cascading.PMMLPlanner` relieves application developers from writing the above boiler plate code:
```
Evaluator evaluator = ...

FlowDef flowDef = ...

PMMLPlanner pmmlPlanner = new PMMLPlanner(evaluator);
pmmlPlanner.setRetainOnlyActiveFields();

flowDef = flowDef.addAssemblyPlanner(pmmlPlanner);
```

## Hadoop job ##

The Hadoop job JAR file contains a single executable class `org.jpmml.cascading.Main`. It expects three arguments: 1) the name of the PMML file in local filesystem, 2) the Cascading Hfs specification of the source resource and 3) the Cascading Hfs specification of the sink resource:

For example, the following command scores the PMML file `P:/cascading/model.pmml` by reading arguments from the TSV data format file `P:/cascading/input.tsv` and writing results to the directory `P:/cascading/output`:
```
hadoop jar jpmml-cascading-1.0-SNAPSHOT-job.jar P:/cascading/model.pmml file:///P:/cascading/input.tsv file:///P:/cascading/output
```


# Contact and Support #

Please use the e-mail displayed at [GitHub profile page] (https://github.com/jpmml)
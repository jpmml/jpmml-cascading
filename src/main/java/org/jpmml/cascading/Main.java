/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.cascading;

import java.io.*;
import java.util.*;

import cascading.flow.*;
import cascading.flow.hadoop.*;
import cascading.property.*;
import cascading.scheme.hadoop.*;
import cascading.tap.*;
import cascading.tap.hadoop.*;

import org.jpmml.evaluator.*;
import org.jpmml.manager.*;

import org.dmg.pmml.*;

public class Main {

	static
	public void main(String... args) throws Exception {

		if(args.length != 3){
			System.err.println("Usage: hadoop jar job.jar <PMML file> <HFS source> <HFS sink>");

			System.exit(-1);
		}

		Properties properties = new Properties();

		AppProps.setApplicationJarClass(properties, Main.class);

		FlowConnector connector = new HadoopFlowConnector(properties);

		PMML pmml = IOUtil.unmarshal(new File(args[0]));
		pmml.accept(new SourceLocationTransformer());

		PMMLManager pmmlManager = new PMMLManager(pmml);

		ModelManager<?> modelManager = pmmlManager.getModelManager(null, ModelEvaluatorFactory.getInstance());

		FlowDef flowDef = FlowDef.flowDef();

		Tap source = new Hfs(new TextDelimited(true, "\t"), args[1]);
		flowDef = flowDef.addSource("source", source);

		Tap sink = new Hfs(new TextDelimited(true, "\t"), args[2]);
		flowDef = flowDef.addSink("sink", sink);

		PMMLPlanner pmmlPlanner = new PMMLPlanner((Evaluator)modelManager);
		pmmlPlanner.setRetainOnlyActiveFields();

		flowDef = flowDef.addAssemblyPlanner(pmmlPlanner);

		Flow<?> flow = connector.connect(flowDef);

		flow.complete();
	}
}
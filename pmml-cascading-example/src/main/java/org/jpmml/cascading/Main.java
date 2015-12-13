/*
 * Copyright (c) 2013 Villu Ruusmann
 *
 * This file is part of JPMML-Cascading
 *
 * JPMML-Cascading is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JPMML-Cascading is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with JPMML-Cascading.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jpmml.cascading;

import java.io.File;
import java.util.Properties;

import cascading.flow.Flow;
import cascading.flow.FlowConnector;
import cascading.flow.FlowDef;
import cascading.flow.hadoop.HadoopFlowConnector;
import cascading.property.AppProps;
import cascading.scheme.hadoop.TextDelimited;
import cascading.tap.Tap;
import cascading.tap.hadoop.Hfs;
import org.jpmml.evaluator.Evaluator;

public class Main {

	static
	public void main(String... args) throws Exception {

		if(args.length != 3){
			System.err.println("Usage: hadoop jar job.jar <PMML file> <HFS source> <HFS sink>");

			System.exit(-1);
		}

		Evaluator evaluator = PMMLPlannerUtil.createEvaluator(new File(args[0]));

		Properties properties = new Properties();

		AppProps.setApplicationJarClass(properties, Main.class);

		FlowConnector connector = new HadoopFlowConnector(properties);

		FlowDef flowDef = FlowDef.flowDef();

		Tap source = new Hfs(new TextDelimited(true, ","), args[1]);
		flowDef = flowDef.addSource("input", source);

		Tap sink = new Hfs(new TextDelimited(true, ","), args[2]);
		flowDef = flowDef.addSink("output", sink);

		PMMLPlanner pmmlPlanner = new PMMLPlanner(evaluator);
		pmmlPlanner.setRetainOnlyActiveFields();
		pmmlPlanner.setHeadName("input");
		pmmlPlanner.setTailName("output");

		flowDef = flowDef.addAssemblyPlanner(pmmlPlanner);

		Flow<?> flow = connector.connect(flowDef);

		flow.complete();
	}
}
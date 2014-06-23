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

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import javax.xml.transform.Source;

import cascading.flow.Flow;
import cascading.flow.FlowConnector;
import cascading.flow.FlowDef;
import cascading.flow.hadoop.HadoopFlowConnector;
import cascading.property.AppProps;
import cascading.scheme.hadoop.TextDelimited;
import cascading.tap.Tap;
import cascading.tap.hadoop.Hfs;
import org.dmg.pmml.PMML;
import org.jpmml.evaluator.ModelEvaluator;
import org.jpmml.evaluator.ModelEvaluatorFactory;
import org.jpmml.manager.PMMLManager;
import org.jpmml.model.ImportFilter;
import org.jpmml.model.JAXBUtil;
import org.jpmml.model.SourceLocationTransformer;
import org.xml.sax.InputSource;

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

		PMML pmml;

		InputStream is = new FileInputStream(args[0]);

		try {
			Source source = ImportFilter.apply(new InputSource(is));

			pmml = JAXBUtil.unmarshalPMML(source);
		} finally {
			is.close();
		}

		pmml.accept(new SourceLocationTransformer());

		PMMLManager pmmlManager = new PMMLManager(pmml);

		ModelEvaluator<?> modelEvaluator = (ModelEvaluator<?>)pmmlManager.getModelManager(null, ModelEvaluatorFactory.getInstance());

		FlowDef flowDef = FlowDef.flowDef();

		Tap source = new Hfs(new TextDelimited(true, "\t"), args[1]);
		flowDef = flowDef.addSource("source", source);

		Tap sink = new Hfs(new TextDelimited(true, "\t"), args[2]);
		flowDef = flowDef.addSink("sink", sink);

		PMMLPlanner pmmlPlanner = new PMMLPlanner(modelEvaluator);
		pmmlPlanner.setRetainOnlyActiveFields();

		flowDef = flowDef.addAssemblyPlanner(pmmlPlanner);

		Flow<?> flow = connector.connect(flowDef);

		flow.complete();
	}
}
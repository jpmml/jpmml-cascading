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

import java.util.ArrayList;
import java.util.List;

import cascading.flow.FlowProcess;
import cascading.operation.Aggregator;
import cascading.operation.AggregatorCall;
import cascading.operation.BaseOperation;
import cascading.operation.OperationCall;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntry;

public class CollectionAggregator extends BaseOperation<List<Object>> implements Aggregator<List<Object>> {

	public CollectionAggregator(Fields fields){
		super(1, fields);

		if(fields.size() != 1){
			throw new IllegalArgumentException();
		}
	}

	@Override
	public void prepare(FlowProcess flowProcess, OperationCall<List<Object>> operationCall){
		List<Object> context = new ArrayList<>();

		operationCall.setContext(context);
	}

	@Override
	public void start(FlowProcess flowProcess, AggregatorCall<List<Object>> aggregatorCall){
		List<Object> context = aggregatorCall.getContext();

		context.clear();
	}

	@Override
	public void aggregate(FlowProcess flowProcess, AggregatorCall<List<Object>> aggregatorCall){
		List<Object> context = aggregatorCall.getContext();

		TupleEntry arguments = aggregatorCall.getArguments();
		context.add(arguments.getObject(0));
	}

	@Override
	public void complete(FlowProcess flowProcess, AggregatorCall<List<Object>> aggregatorCall){
		List<Object> context = aggregatorCall.getContext();

		Tuple result = Tuple.size(1);
		result.set(0, new ArrayList<>(context));

		aggregatorCall.getOutputCollector().add(result);
	}
}
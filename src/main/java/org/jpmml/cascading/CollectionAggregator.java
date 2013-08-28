/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.cascading;

import java.util.*;

import cascading.flow.*;
import cascading.operation.*;
import cascading.tuple.*;

public class CollectionAggregator extends BaseOperation<List<Object>> implements Aggregator<List<Object>> {

	public CollectionAggregator(Fields fields){
		super(1, fields);

		if(fields.size() != 1){
			throw new IllegalArgumentException();
		}
	}

	@Override
	public void prepare(FlowProcess flowProcess, OperationCall<List<Object>> operationCall){
		List<Object> context = new ArrayList<Object>();

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
		result.set(0, new ArrayList<Object>(context));

		aggregatorCall.getOutputCollector().add(result);
	}
}
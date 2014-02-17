/*
 * Copyright (c) 2013 Villu Ruusmann
 */
package org.jpmml.cascading;

import java.util.*;

import cascading.flow.*;
import cascading.operation.*;
import cascading.tuple.*;

import org.jpmml.evaluator.*;
import org.jpmml.evaluator.FieldValue;

import org.dmg.pmml.*;

public class PMMLFunction extends BaseOperation<Object> implements Function<Object> {

	private Evaluator evaluator = null;


	public PMMLFunction(Fields fields, Evaluator evaluator){
		super(fields);

		setEvaluator(evaluator);
	}

	@Override
	public void operate(FlowProcess flowProcess, FunctionCall<Object> functionCall){
		Evaluator evaluator = getEvaluator();

		TupleEntry input = functionCall.getArguments();

		Map<FieldName, FieldValue> arguments = decodeArguments(evaluator, input);

		Map<FieldName, ?> result = evaluator.evaluate(arguments);

		TupleEntry output = encodeResult(evaluator, getFieldDeclaration(), result);

		TupleEntryCollector outputCollector = functionCall.getOutputCollector();

		outputCollector.add(output);
	}

	public Evaluator getEvaluator(){
		return this.evaluator;
	}

	private void setEvaluator(Evaluator evaluator){

		if(evaluator == null){
			throw new NullPointerException();
		}

		this.evaluator = evaluator;
	}

	static
	private Map<FieldName, FieldValue> decodeArguments(Evaluator evaluator, TupleEntry tuple){
		Map<FieldName, FieldValue> result = new LinkedHashMap<FieldName, FieldValue>();

		Fields fields = tuple.getFields();

		for(int i = 0; i < fields.size(); i++){
			FieldName key = FieldName.create((String)fields.get(i));
			Object value = tuple.getObject(key.getValue());

			result.put(key, EvaluatorUtil.prepare(evaluator, key, value));
		}

		return result;
	}

	static
	private TupleEntry encodeResult(Evaluator evaluator, Fields fields, Map<FieldName, ?> map){
		TupleEntry result = new TupleEntry(fields, Tuple.size(fields.size()));

		for(int i = 0; i < fields.size(); i++){
			FieldName key = new FieldName((String)fields.get(i));
			Object value = map.get(key);

			result.setRaw(key.getValue(), EvaluatorUtil.decode(value));
		}

		return result;
	}
}
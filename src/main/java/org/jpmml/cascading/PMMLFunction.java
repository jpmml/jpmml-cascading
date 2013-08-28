/*
 * Copyright (c) 2013 University of Tartu
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

		TupleEntry arguments = functionCall.getArguments();

		Map<FieldName, Object> parameters = decodeArguments(arguments);

		// Convert raw input values to (J)PMML input values
		Collection<? extends Map.Entry<FieldName, Object>> entries = parameters.entrySet();
		for(Map.Entry<FieldName, Object> entry : entries){
			FieldName name = entry.getKey();
			Object value = entry.getValue();

			FieldValue preparedValue;

			if(value instanceof Collection){
				List<Object> values = new ArrayList<Object>();

				DataType dataType = null;

				OpType opType = null;

				Collection<?> rawValues = (Collection<?>)value;
				for(Object rawValue : rawValues){
					preparedValue = evaluator.prepare(name, rawValue);

					if(preparedValue != null){

						if(dataType == null){
							dataType = preparedValue.getDataType();
						} // End if

						if(opType == null){
							opType = preparedValue.getOpType();
						}
					}

					values.add(FieldValueUtil.getValue(preparedValue));
				}

				preparedValue = FieldValueUtil.create(dataType, opType, values);
			} else

			{
				preparedValue = evaluator.prepare(name, value);
			}

			entry.setValue(preparedValue);
		}

		Map<FieldName, ?> result = evaluator.evaluate(parameters);

		Set<FieldName> resultFields = new LinkedHashSet<FieldName>();
		resultFields.addAll(evaluator.getPredictedFields());
		resultFields.addAll(evaluator.getOutputFields());

		// Deliver promised fields only
		(result.keySet()).retainAll(resultFields);

		// Convert (J)PMML output values to raw output values
		result = EvaluatorUtil.decodeValues(result);

		TupleEntry output = encodeOutput(getFieldDeclaration(), (Map)result);

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
	private Map<FieldName, Object> decodeArguments(TupleEntry tuple){
		Map<FieldName, Object> result = new LinkedHashMap<FieldName, Object>();

		Fields fields = tuple.getFields();

		for(int i = 0; i < fields.size(); i++){
			FieldName key = FieldName.create((String)fields.get(i));
			Object value = tuple.getObject(key.getValue());

			result.put(key, value);
		}

		return result;
	}

	static
	private TupleEntry encodeOutput(Fields fields, Map<FieldName, Object> map){
		TupleEntry result = new TupleEntry(fields, Tuple.size(fields.size()));

		for(int i = 0; i < fields.size(); i++){
			FieldName key = new FieldName((String)fields.get(i));
			Object value = map.get(key);

			result.setRaw(key.getValue(), value);
		}

		return result;
	}
}
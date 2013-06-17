/*
 * Copyright (c) 2013 University of Tartu
 */
package org.jpmml.cascading;

import java.lang.reflect.*;
import java.util.*;

import cascading.tuple.*;

import org.jpmml.evaluator.*;

import org.dmg.pmml.*;

public class FieldsUtil {

	private FieldsUtil(){
	}

	static
	public Fields getActiveFields(Evaluator evaluator){
		Fields result = new Fields();

		List<FieldName> activeFields = evaluator.getActiveFields();
		for(FieldName activeField : activeFields){
			DictionaryField dictionaryField = evaluator.getDataField(activeField);

			result = result.append(createFields(activeField.getValue(), dictionaryField));
		}

		return result;
	}

	static
	public Fields getPredictedFields(Evaluator evaluator){
		Fields result = new Fields();

		List<FieldName> predictedFields = evaluator.getPredictedFields();
		for(FieldName predictedField : predictedFields){
			DictionaryField dictionaryField = evaluator.getDataField(predictedField);

			result = result.append(createFields(predictedField.getValue(), dictionaryField));
		}

		return result;
	}

	static
	public Fields getOutputFields(Evaluator evaluator){
		Fields result = new Fields();

		List<FieldName> outputFields = evaluator.getOutputFields();
		for(FieldName outputField : outputFields){
			DictionaryField dictionaryField = evaluator.getOutputField(outputField);

			result = result.append(createFields(outputField.getValue(), dictionaryField));
		}

		return result;
	}

	static
	private Fields createFields(String name, DictionaryField dictionaryField){
		DataType dataType = dictionaryField.getDataType();
		if(dataType == null){
			dataType = DataType.STRING;
		}

		return new Fields(name, getType(dataType));
	}

	static
	private Type getType(DataType dataType){

		switch(dataType){
			case STRING:
				return String.class;
			case INTEGER:
				return Integer.class;
			case FLOAT:
			case DOUBLE:
				return Double.class;
			default:
				throw new IllegalArgumentException();
		}
	}
}
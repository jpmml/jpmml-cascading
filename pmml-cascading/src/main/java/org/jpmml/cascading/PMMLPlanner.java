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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import cascading.flow.AssemblyPlanner;
import cascading.flow.Flow;
import cascading.flow.planner.PlannerException;
import cascading.pipe.Each;
import cascading.pipe.Every;
import cascading.pipe.GroupBy;
import cascading.pipe.Pipe;
import cascading.pipe.assembly.Retain;
import cascading.tuple.Fields;
import org.jpmml.evaluator.Evaluator;

public class PMMLPlanner implements AssemblyPlanner {

	private Evaluator evaluator = null;

	private String branchName = "pmml";

	private String headName = null;

	private String tailName = null;

	private Fields retainedFields = null;


	public PMMLPlanner(Evaluator evaluator){
		setEvaluator(evaluator);
	}

	/**
	 * @since Cascading 2.6.0
	 */
	/*@Override*/
	public Map<String, String> getFlowDescriptor(){
		return Collections.emptyMap();
	}

	@Override
	public List<Pipe> resolveTails(Context context){
		List<Pipe> tails = context.getTails();

		Pipe tail;

		String headName = getHeadName();

		if(tails.size() == 0){

			if(headName == null){
				headName = findHeadName(context);
			}

			tail = new Pipe(headName);
		} else

		if(tails.size() == 1){

			if(headName != null){
				throw new PlannerException("Cannot specify a head name when there is an incoming branch");
			}

			tail = tails.get(0);
		} else

		{
			throw new PlannerException("Too many incoming branches to choose from");
		}

		String tailName = getTailName();
		if(tailName == null){
			tailName = findTailName(context);
		}

		tail = new Pipe(tailName, resolveAssembly(tail));

		return Collections.singletonList(tail);
	}

	private Pipe resolveAssembly(Pipe pipe){
		Pipe tail;

		if(pipe == null){
			tail = new Pipe(getBranchName());
		} else

		{
			tail = new Pipe(getBranchName(), pipe);
		}

		Evaluator evaluator = getEvaluator();

		Fields retainedFields = getRetainedFields();
		if(retainedFields != null){
			tail = new Retain(tail, retainedFields);
		}

		Fields activeFields = FieldsUtil.getActiveFields(evaluator);
		Fields groupFields = FieldsUtil.getGroupFields(evaluator);
		Fields targetFields = FieldsUtil.getTargetFields(evaluator);
		Fields outputFields = FieldsUtil.getOutputFields(evaluator);

		if(groupFields.size() > 0){

			if(groupFields.size() > 1){
				throw new PlannerException("Too many group by fields");
			}

			tail = new GroupBy(tail, groupFields);

			tail = new Every(tail, activeFields, new CollectionAggregator(activeFields));
		}

		Fields argumentFields = new Fields()
			.append(activeFields)
			.append(groupFields);

		Fields resultFields = new Fields()
			.append(targetFields)
			.append(outputFields);

		PMMLFunction function = new PMMLFunction(resultFields, evaluator);

		tail = new Each(tail, argumentFields, function, Fields.ALL);

		return tail;
	}

	private String findHeadName(Context context){
		Flow<?> flow = context.getFlow();

		List<String> sourceNames = flow.getSourceNames();
		if(sourceNames.size() != 1){
			throw new PlannerException("Too many sources to choose from: " + sourceNames);
		}

		return sourceNames.get(0);
	}

	private String findTailName(Context context){
		Flow<?> flow = context.getFlow();

		List<String> sinkNames = flow.getSinkNames();
		if(sinkNames.size() != 1){
			throw new PlannerException("Too many sinks to choose from: " + sinkNames);
		}

		return sinkNames.get(0);
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

	public String getBranchName(){
		return this.branchName;
	}

	public PMMLPlanner setBranchName(String branchName){

		if(branchName == null){
			throw new NullPointerException();
		}

		this.branchName = branchName;

		return this;
	}

	public String getHeadName(){
		return this.headName;
	}

	/**
	 * Sets the name of the incoming source.
	 *
	 * This attribute is mandatory when more than one sources have been declared.
	 */
	public PMMLPlanner setHeadName(String headName){
		this.headName = headName;

		return this;
	}

	public String getTailName(){
		return this.tailName;
	}

	/**
	 * Sets the name of the outgoing sink.
	 *
	 * This attribute is mandatory when more than one sinks have been declared.
	 */
	public PMMLPlanner setTailName(String tailName){
		this.tailName = tailName;

		return this;
	}

	/**
	 * Orders the retention of only those incoming fields that represent PMML function argument fields.
	 *
	 * @see #setRetainedFields(Fields)
	 */
	public PMMLPlanner setRetainOnlyActiveFields(){
		Evaluator evaluator = getEvaluator();

		Fields incomingFields = new Fields()
			.append(FieldsUtil.getActiveFields(evaluator))
			.append(FieldsUtil.getGroupFields(evaluator));

		return setRetainedFields(incomingFields);
	}

	public Fields getRetainedFields(){
		return this.retainedFields;
	}

	/**
	 * Sets the incoming fields that must be retained.
	 *
	 * By default, all incoming fields are retained. This will cause problems if there is a collison with PMML function output fields.
	 *
	 * @see FieldsUtil#getActiveFields(Evaluator)
	 * @see FieldsUtil#getGroupFields(Evaluator)
	 * @see FieldsUtil#getTargetFields(Evaluator)
	 * @see FieldsUtil#getOutputFields(Evaluator)
	 */
	public PMMLPlanner setRetainedFields(Fields retainedFields){
		this.retainedFields = retainedFields;

		return this;
	}
}
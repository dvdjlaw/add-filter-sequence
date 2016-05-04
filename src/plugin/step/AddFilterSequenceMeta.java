/*
 * !
 * *****************************************************************************
 * *
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 ******************************************************************************/

package plugin.step;

import java.util.ArrayList;
import java.util.List;
import org.pentaho.di.core.Condition;
import org.pentaho.di.core.Const;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaAndData;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

import plugin.step.AddFilterSequence;
import plugin.step.AddFilterSequenceData;
import plugin.step.AddFilterSequenceDialog;

/**
 * 
 * @author David Law
 *
 */
@Step(id = "AddFilterSequence", image = "plugin/step/resources/FCS.svg", i18nPackageName = "plugin.step", name = "AddFilterSequence.Name", description = "AddFilterSequence.TooltipDesc", categoryDescription = "AddFilterSequence.Category")
public class AddFilterSequenceMeta extends BaseStepMeta implements StepMetaInterface {
	/**
	 * The PKG member is used when looking up internationalized strings. The
	 * properties file with localized keys is expected to reside in {the package
	 * of the class specified}/messages/messages_{locale}.properties
	 */
	private static Class<?> PKG = AddFilterSequenceMeta.class; // for i18n purposes

	/**
	 * Name of the new field
	 */
	private String fieldName;

	/**
	 * Starting number for the sequence
	 */
	private String startAt;

	/**
	 * The number to increment by
	 */
	private String incrementBy;

	/**
	 * This is the main condition for the complete filter.
	 */
	private Condition condition;

	/**
	 * Returns the fieldName
	 * 
	 * @return
	 */
	public String getFieldName() {
		return fieldName;
	}

	/**
	 * @param fieldName
	 *            The fieldName to set.
	 */
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	/**
	 * @return Returns the start of the sequence.
	 */
	public String getStartAt() {
		return startAt;
	}

	/**
	 * @param startAt
	 *            The starting point of the sequence to set.
	 */
	public void setStartAt(long startAt) {
		setStartAt(Long.toString(startAt));
	}

	/**
	 * @param startAt
	 *            The starting point of the sequence to set.
	 */
	public void setStartAt(String startAt) {
		this.startAt = startAt;
	}

	/**
	 * @return Returns the incrementBy.
	 */
	public String getIncrementBy() {
		return incrementBy;
	}

	/**
	 * @param incrementBy
	 *            The incrementBy to set.
	 */
	public void setIncrementBy(String incrementBy) {
		this.incrementBy = incrementBy;
	}

	/**
	 * @return Returns the condition.
	 */
	public Condition getCondition() {
		return condition;
	}

	/**
	 * @param condition
	 *            The condition to set.
	 */
	public void setCondition(Condition condition) {
		this.condition = condition;
	}

	/**
	 * Constructor should call super() to make sure the base class has a chance
	 * to initialize properly.
	 */
	public AddFilterSequenceMeta() {
		super();
		setDefault();
	}

	/**
	 * Called by Spoon to get a new instance of the SWT dialog for the step. A
	 * standard implementation passing the arguments to the constructor of the
	 * step dialog is recommended.
	 * 
	 * @param shell
	 *            an SWT Shell
	 * @param meta
	 *            description of the step
	 * @param transMeta
	 *            description of the the transformation
	 * @param name
	 *            the name of the step
	 * @return new instance of a dialog for this step
	 */
	public StepDialogInterface getDialog(Shell shell, StepMetaInterface meta, TransMeta transMeta, String name) {
		return new AddFilterSequenceDialog(shell, meta, transMeta, name);
	}

	/**
	 * Called by PDI to get a new instance of the step implementation. A
	 * standard implementation passing the arguments to the constructor of the
	 * step class is recommended.
	 * 
	 * @param stepMeta
	 *            description of the step
	 * @param stepDataInterface
	 *            instance of a step data class
	 * @param cnr
	 *            copy number
	 * @param transMeta
	 *            description of the transformation
	 * @param disp
	 *            runtime implementation of the transformation
	 * @return the new instance of a step implementation
	 */
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta,
			Trans disp) {
		return new AddFilterSequence(stepMeta, stepDataInterface, cnr, transMeta, disp);
	}

	/**
	 * Called by PDI to get a new instance of the step data class.
	 */
	public StepDataInterface getStepData() {
		return new AddFilterSequenceData();
	}

	/**
	 * This method is called every time a new step is created and should
	 * allocate/set the step configuration to sensible defaults. The values set
	 * here will be used by Spoon when a new step is created.
	 */
	public void setDefault() {
		fieldName = "fieldName";
		condition = new Condition();
		startAt = "1";
		incrementBy = "1";
	}

	/**
	 * Deep clone
	 */
	public Object clone() {
		AddFilterSequenceMeta retval = (AddFilterSequenceMeta) super.clone();

		if (condition != null) {
			retval.condition = (Condition) condition.clone();
		} else {
			retval.condition = null;
		}

		return retval;
	}

	/**
	 * De-serialize from XML
	 */
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore) throws KettleXMLException {
		readData(stepnode);
	}

	/**
	 * Read XML into class properties
	 * @param stepnode
	 * @throws KettleXMLException
	 */
	private void readData(Node stepnode) throws KettleXMLException {
		try {
			fieldName = XMLHandler.getTagValue(stepnode, "fieldName");
			startAt = XMLHandler.getTagValue(stepnode, "start_at");
			incrementBy = XMLHandler.getTagValue(stepnode, "increment_by");

			Node compare = XMLHandler.getSubNode(stepnode, "compare");
			Node condnode = XMLHandler.getSubNode(compare, "condition");

			// The new situation...
			if (condnode != null) {
				condition = new Condition(condnode);
			} else {
				// Old style condition: Line1 OR Line2 OR Line3: @deprecated!
				condition = new Condition();

				int nrkeys = XMLHandler.countNodes(compare, "key");
				if (nrkeys == 1) {
					Node knode = XMLHandler.getSubNodeByNr(compare, "key", 0);

					String key = XMLHandler.getTagValue(knode, "name");
					String value = XMLHandler.getTagValue(knode, "value");
					String field = XMLHandler.getTagValue(knode, "field");
					String comparator = XMLHandler.getTagValue(knode, "condition");

					condition.setOperator(Condition.OPERATOR_NONE);
					condition.setLeftValuename(key);
					condition.setFunction(Condition.getFunction(comparator));
					condition.setRightValuename(field);
					condition.setRightExact(new ValueMetaAndData("value", value));
				} else {
					for (int i = 0; i < nrkeys; i++) {
						Node knode = XMLHandler.getSubNodeByNr(compare, "key", i);

						String key = XMLHandler.getTagValue(knode, "name");
						String value = XMLHandler.getTagValue(knode, "value");
						String field = XMLHandler.getTagValue(knode, "field");
						String comparator = XMLHandler.getTagValue(knode, "condition");

						Condition subc = new Condition();
						if (i > 0) {
							subc.setOperator(Condition.OPERATOR_OR);
						} else {
							subc.setOperator(Condition.OPERATOR_NONE);
						}
						subc.setLeftValuename(key);
						subc.setFunction(Condition.getFunction(comparator));
						subc.setRightValuename(field);
						subc.setRightExact(new ValueMetaAndData("value", value));

						condition.addCondition(subc);
					}
				}
			}
		} catch (Exception e) {
			throw new KettleXMLException(
					BaseMessages.getString(PKG, "AddFilterSequenceMeta.Exception..UnableToLoadStepInfoFromXML"), e);
		}
	}

	/**
	 * Serialize to XML
	 */
	public String getXML() throws KettleException {
		StringBuilder retval = new StringBuilder(200);

		retval.append("      ").append(XMLHandler.addTagValue("fieldName", fieldName));
		retval.append("      ").append(XMLHandler.addTagValue("start_at", startAt));
		retval.append("      ").append(XMLHandler.addTagValue("increment_by", incrementBy));
		retval.append("    <compare>").append(Const.CR);

		if (condition != null) {
			retval.append(condition.getXML());
		}

		retval.append("    </compare>").append(Const.CR);

		return retval.toString();
	}

	/**
	 * Read from repository
	 */
	public void readRep(Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases)
			throws KettleException {
		try {
			fieldName = rep.getStepAttributeString(id_step, "fieldName");
			startAt = rep.getStepAttributeString(id_step, "start_at");
			incrementBy = rep.getStepAttributeString(id_step, "increment_by");

			// Fix for backwards compatibility, only to be used from previous
			// versions (TO DO Sven Boden: remove in later
			// versions)
			if (startAt == null) {
				long start = rep.getStepAttributeInteger(id_step, "start_at");
				startAt = Long.toString(start);
			}

			if (incrementBy == null) {
				long increment = rep.getStepAttributeInteger(id_step, "increment_by");
				incrementBy = Long.toString(increment);
			}
			
			condition = rep.loadConditionFromStepAttribute( id_step, "id_condition" );
		} catch (Exception e) {
			throw new KettleException(
					BaseMessages.getString(PKG, "AddSequenceMeta.Exception.UnableToReadStepInfo") + id_step, e);
		}
	}

	/**
	 * Save to repository
	 */
	public void saveRep(Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step)
			throws KettleException {
		try {
			rep.saveStepAttribute(id_transformation, id_step, "fieldName", fieldName);
			rep.saveStepAttribute(id_transformation, id_step, "start_at", startAt);
			rep.saveStepAttribute(id_transformation, id_step, "increment_by", incrementBy);
			rep.saveConditionStepAttribute( id_transformation, id_step, "id_condition", condition );

		} catch (Exception e) {
			throw new KettleException(
					BaseMessages.getString(PKG, "AddSequenceMeta.Exception.UnableToSaveStepInfo") + id_step, e);
		}
	}

	/**
	 * Load the new field
	 */
	@Override
	public void getFields(RowMetaInterface rowMeta, String origin, RowMetaInterface[] info, StepMeta nextStep,
			VariableSpace space, Repository repository, IMetaStore metaStore) throws KettleStepException {
		// Clear the sortedDescending flag on fields used within the condition -
		// otherwise the comparisons will be
		// inverted!!
		String[] conditionField = condition.getUsedFields();
		for (int i = 0; i < conditionField.length; i++) {
			int idx = rowMeta.indexOfValue(conditionField[i]);
			if (idx >= 0) {
				ValueMetaInterface valueMeta = rowMeta.getValueMeta(idx);
				valueMeta.setSortedDescending(false);
			}
		}

		ValueMetaInterface v;
		try {
			v = ValueMetaFactory.createValueMeta(fieldName, ValueMetaInterface.TYPE_INTEGER);
			v.setName(fieldName);
			v.setOrigin(origin);
			rowMeta.addValueMeta(v);
		} catch (KettlePluginException e) {
			logBasic("Could not create new field: " + e.getMessage());
		}
	}

	/**
	 * Validate data
	 */
	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev,
			String[] input, String[] output, RowMetaInterface info, VariableSpace space, Repository repository,
			IMetaStore metaStore) {
		CheckResult cr;
		String error_message = "";

		if (condition.isEmpty()) {
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR,
					BaseMessages.getString(PKG, "AddFilterSequenceMeta.CheckResult.NoConditionSpecified"), stepMeta);
		} else {
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK,
					BaseMessages.getString(PKG, "AddFilterSequenceMeta.CheckResult.ConditionSpecified"), stepMeta);
		}
		remarks.add(cr);

		// Look up fields in the input stream <prev>
		if (prev != null && prev.size() > 0) {
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG,
					"AddFilterSequenceMeta.CheckResult.StepReceivingFields", prev.size() + ""), stepMeta);
			remarks.add(cr);

			List<String> orphanFields = getOrphanFields(condition, prev);
			if (orphanFields.size() > 0) {
				error_message = BaseMessages.getString(PKG,
						"AddFilterSequenceMeta.CheckResult.FieldsNotFoundFromPreviousStep") + Const.CR;
				for (String field : orphanFields) {
					error_message += "\t\t" + field + Const.CR;
				}
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta);
			} else {
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK,
						BaseMessages.getString(PKG, "AddFilterSequenceMeta.CheckResult.AllFieldsFoundInInputStream"),
						stepMeta);
			}
			remarks.add(cr);
		} else {
			error_message = BaseMessages.getString(PKG,
					"AddFilterSequenceMeta.CheckResult.CouldNotReadFieldsFromPreviousStep") + Const.CR;
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta);
			remarks.add(cr);
		}

		// See if we have input streams leading to this step!
		if (input.length > 0) {
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK,
					BaseMessages.getString(PKG, "AddFilterSequenceMeta.CheckResult.StepReceivingInfoFromOtherSteps"),
					stepMeta);
			remarks.add(cr);
		} else {
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR,
					BaseMessages.getString(PKG, "AddFilterSequenceMeta.CheckResult.NoInputReceivedFromOtherSteps"),
					stepMeta);
			remarks.add(cr);
		}
	}

	/**
	 * Get non-existing referenced input fields
	 * 
	 * @param condition
	 * @param prev
	 * @return
	 */
	public List<String> getOrphanFields(Condition condition, RowMetaInterface prev) {
		List<String> orphans = new ArrayList<String>();
		if (condition == null || prev == null) {
			return orphans;
		}
		String[] key = condition.getUsedFields();
		for (int i = 0; i < key.length; i++) {
			if (Const.isEmpty(key[i])) {
				continue;
			}
			ValueMetaInterface v = prev.searchValueMeta(key[i]);
			if (v == null) {
				orphans.add(key[i]);
			}
		}
		return orphans;
	}

}

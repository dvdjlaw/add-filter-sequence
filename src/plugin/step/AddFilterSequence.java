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

import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * @author David Law
 * @category Transform
 * 
 *           Adds a sequence field that increments only when certain filter
 *           conditions are met on a row
 */

public class AddFilterSequence extends BaseStep implements StepInterface {
	private static Class<?> PKG = AddFilterSequence.class; // for i18n needed by Translator2!!

	private AddFilterSequenceMeta meta;

	private AddFilterSequenceData data;

	/**
	 * The constructor should simply pass on its arguments to the parent class.
	 * 
	 * @param s
	 *            step description
	 * @param stepDataInterface
	 *            step data class
	 * @param c
	 *            step copy
	 * @param t
	 *            transformation description
	 * @param dis
	 *            transformation executing
	 */
	public AddFilterSequence(StepMeta s, StepDataInterface stepDataInterface, int c, TransMeta t, Trans dis) {
		super(s, stepDataInterface, c, t, dis);
	}

	/**
	 * This method is called by PDI during transformation startup.
	 * 
	 * It should initialize required for step execution.
	 * 
	 * The meta and data implementations passed in can safely be cast to the
	 * step's respective implementations.
	 * 
	 * It is mandatory that super.init() is called to ensure correct behavior.
	 * 
	 * Typical tasks executed here are establishing the connection to a
	 * database, as wall as obtaining resources, like file handles.
	 * 
	 * @param smi
	 *            step meta interface implementation, containing the step
	 *            settings
	 * @param sdi
	 *            step data interface implementation, used to store runtime
	 *            information
	 * 
	 * @return true if initialization completed successfully, false if there was
	 *         an error preventing the step from working.
	 * 
	 */
	public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
		meta = (AddFilterSequenceMeta) smi;
		data = (AddFilterSequenceData) sdi;

		if (super.init(smi, sdi)) {
			meta.getCondition().clearFieldPositions();

			// Insert Kettle variables
			data.setLookup("@@sequence:" + meta.getFieldName());

			try {
				data.start = Long.parseLong(environmentSubstitute(meta.getStartAt()));
			} catch (NumberFormatException ex) {
				logError(BaseMessages.getString(PKG, "AddSequence.Log.CouldNotParseCounterValue", "start",
						meta.getStartAt(), environmentSubstitute(meta.getStartAt()), ex.getMessage()));
			}

			try {
				data.increment = Long.parseLong(environmentSubstitute(meta.getIncrementBy()));
			} catch (NumberFormatException ex) {
				logError(BaseMessages.getString(PKG, "AddSequence.Log.CouldNotParseCounterValue", "increment",
						meta.getIncrementBy(), environmentSubstitute(meta.getIncrementBy()), ex.getMessage()));
			}

			// Necessary? Having multiple counters does not make sense
			if (getTrans().getCounters() != null) {
				// check if counter exists
				synchronized (getTrans().getCounters()) {
					data.counter = getTrans().getCounters().get(data.getLookup());
					if (data.counter == null) {
						// create a new one
						data.counter = new Counter(data.start, data.increment);
						getTrans().getCounters().put(data.getLookup(), data.counter);
					}
				}
				return true;
			} else {
				logError(BaseMessages.getString(PKG, "AddSequence.Log.TransformationCountersHashtableNotAllocated"));
			}

			return true;
		}
		return false;
	}

	/**
	 * Create the row with a new field for the sequence
	 * 
	 * @param inputRowMeta
	 * @param inputRowData
	 * @param increment
	 *            Increments the sequence counter if true
	 * @return outputRowData The row data with the sequence counter
	 * @throws KettleException
	 */
	public Object[] addSequence(RowMetaInterface inputRowMeta, Object[] inputRowData, boolean doIncrement)
			throws KettleException {
		Object next = null;

		synchronized (data.counter) {
			long prev = data.counter.getCounter();

			if (doIncrement) {
				long nval = prev + data.counter.getIncrement();
				data.counter.setCounter(nval);
			}

			next = data.counter.getCounter();
		}

		if (next != null) {
			Object[] outputRowData = RowDataUtil.addValueData(inputRowData, data.outputRowMeta.size() - 1, next);
			return outputRowData;
		} else { // Never happens
			throw new KettleStepException(
					BaseMessages.getString(PKG, "AddSequence.Exception.CouldNotFindNextValueForSequence")
							+ meta.getFieldName());
		}
	}

	/**
	 * Evaluates the conditions against this row and returns true when met
	 * 
	 * @param rowMeta
	 * @param row
	 * @return
	 * @throws KettleException
	 */
	private synchronized boolean evaluateRow(RowMetaInterface rowMeta, Object[] row) throws KettleException {
		try {
			return meta.getCondition().evaluate(rowMeta, row);
		} catch (Exception e) {
			String message = BaseMessages.getString(PKG,
					"AddFilterSequence.Exception.UnexpectedErrorFoundInEvaluationFunction");
			logError(message);
			logError(BaseMessages.getString(PKG, "AddFilterSequence.Log.ErrorOccurredForRow") + rowMeta.getString(row));
			logError(Const.getStackTracker(e));
			throw new KettleException(message, e);
		}
	}

	/**
	 * Checks the fields coming from the input stream
	 * 
	 * @throws KettleException
	 */
	protected void checkNonExistingFields() throws KettleException {
		List<String> orphanFields = meta.getOrphanFields(meta.getCondition(), getInputRowMeta());
		if (orphanFields != null && orphanFields.size() > 0) {
			String fields = "";
			boolean first = true;
			for (String field : orphanFields) {
				if (!first) {
					fields += ", ";
				}
				fields += "'" + field + "'";
				first = false;
			}
			String errorMsg = BaseMessages.getString(PKG,
					"AddFilterSequence.CheckResult.FieldsNotFoundFromPreviousStep", fields);
			throw new KettleException(errorMsg);
		}

	}

	/**
	 * @param smi
	 *            the step meta interface containing the step settings
	 * @param sdi
	 *            the step data interface that should be used to store
	 * 
	 * @return true to indicate that the function should be called again, false
	 *         if the step is done
	 */
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
		meta = (AddFilterSequenceMeta) smi;
		data = (AddFilterSequenceData) sdi;

		boolean doIncrement;

		Object[] r = getRow(); // Get next usable row from input rowset(s)!
		if (r == null) { // no more input to be expected...
			setOutputDone();
			return false;
		}

		if (first) {
			first = false;

			data.outputRowMeta = (RowMetaInterface) getInputRowMeta().clone();
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this, null, null);//repository, metaStore

			// if filter refers to non-existing fields, throw exception
			checkNonExistingFields();

			// First row will never increment
			Object[] outputRow = addSequence(getInputRowMeta(), r, false);
			putRow(data.outputRowMeta, outputRow);
		} else {
			try {
				doIncrement = evaluateRow(getInputRowMeta(), r);
				Object[] outputRow = addSequence(getInputRowMeta(), r, doIncrement);
				putRow(data.outputRowMeta, outputRow);
			} catch (KettleException e) {
				logError(BaseMessages.getString(PKG, "AddSequenceCriteria.Log.ErrorInStep") + e.getMessage());
				setErrors(1);
				stopAll();
				setOutputDone(); // signal end to receiver(s)
				return false;
			}
		}

		// log progress if it is time to to so
		if (checkFeedback(getLinesRead())) {
			logBasic("Linenr " + getLinesRead()); // Some basic logging
		}

		return true;
	}

	/**
	 * 
	 */
	public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
		meta = (AddFilterSequenceMeta) smi;
		data = (AddFilterSequenceData) sdi;

		if (data.getLookup() != null) {
			getTrans().getCounters().remove(data.getLookup());
		}
		data.counter = null;

		super.dispose(smi, sdi);
	}
}

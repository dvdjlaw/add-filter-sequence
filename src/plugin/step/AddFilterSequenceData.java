/*! ******************************************************************************
*
* Pentaho Data Integration
*
* Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
*
*******************************************************************************
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with
* the License. You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
******************************************************************************/

package plugin.step;

import org.pentaho.di.core.Counter;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * 
 * @author David Law
 *
 */
public class AddFilterSequenceData extends BaseStepData implements StepDataInterface {
	private String lookup;
	public RowMetaInterface outputRowMeta;
	public Counter counter;
	
	public long start;
	public long increment;

	public AddFilterSequenceData() {
		super();
	}

	/**
	 * @return Returns the lookup string usually "@@"+the name of the sequence.
	 */
	public String getLookup() {
		return lookup;
	}

	/**
	 * @param lookup
	 *            the lookup string usually "@@"+the name of the sequence.
	 */
	public void setLookup(String lookup) {
		this.lookup = lookup;
	}
}

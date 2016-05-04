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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Condition;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.ui.core.widget.ConditionEditor;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

import plugin.step.AddFilterSequenceMeta;

/**
 * This class is part of the demo step plug-in implementation. It demonstrates
 * the basics of developing a plug-in step for PDI.
 * 
 * The demo step adds a new string field to the row stream and sets its value to
 * "Hello World!". The user may select the name of the new field.
 * 
 * This class is the implementation of StepDialogInterface. Classes implementing
 * this interface need to:
 * 
 * - build and open a SWT dialog displaying the step's settings (stored in the
 * step's meta object) - write back any changes the user makes to the step's
 * meta object - report whether the user changed any settings when confirming
 * the dialog
 * 
 */
public class AddFilterSequenceDialog extends BaseStepDialog implements StepDialogInterface {
	private static Class<?> PKG = AddFilterSequenceMeta.class; // for i18n purposes, needed by Translator2!!

	// Name of new field
	private Label wlFieldName;
	private Text wFieldName;

	// Group for counter
	private Group gOption;
	private FormData fdOption;

	private Label wlStartAt;
	private TextVar wStartAt;

	private Label wlIncrBy;
	private TextVar wIncrBy;

	private Label wlCondition;
	private ConditionEditor wCondition;
	private FormData fdlCondition, fdCondition;

	private Condition condition;
	private Condition backupCondition;

	private AddFilterSequenceMeta input;

	/**
	 * 
	 * @param parent
	 * @param in
	 * @param transMeta
	 * @param sname
	 */
	public AddFilterSequenceDialog(Shell parent, Object in, TransMeta transMeta, String sname) {
		super(parent, (BaseStepMeta) in, transMeta, sname);
		input = (AddFilterSequenceMeta) in;

		condition = (Condition) input.getCondition().clone();
	}

	/**
	 * 
	 */
	public String open() {
		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
		props.setLook(shell);
		setShellImage(shell, input);

		ModifyListener lsMod = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				input.setChanged();
			}
		};

		changed = input.hasChanged();
		backupCondition = (Condition) condition.clone();

		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "AddFilterSequenceDialog.Shell.Title"));

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		// Stepname line
		wlStepname = new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "AddFilterSequenceDialog.StepName.Label"));
		props.setLook(wlStepname);
		fdlStepname = new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right = new FormAttachment(middle, -margin);
		fdlStepname.top = new FormAttachment(0, margin);
		wlStepname.setLayoutData(fdlStepname);
		wStepname = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStepname.setText(stepname);
		props.setLook(wStepname);
		wStepname.addModifyListener(lsMod);
		fdStepname = new FormData();
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top = new FormAttachment(0, margin);
		fdStepname.right = new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);

		// fieldname line
		wlFieldName = new Label(shell, SWT.RIGHT);
		wlFieldName.setText(BaseMessages.getString(PKG, "AddFilterSequenceDialog.FieldName.Label"));
		props.setLook(wlFieldName);
		FormData fdlfieldname = new FormData();
		fdlfieldname.left = new FormAttachment(0, 0);
		fdlfieldname.top = new FormAttachment(wStepname, margin);
		fdlfieldname.right = new FormAttachment(middle, -margin);
		wlFieldName.setLayoutData(fdlfieldname);
		wFieldName = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wFieldName.setText("");
		props.setLook(wFieldName);
		wFieldName.addModifyListener(lsMod);
		FormData fdfieldname = new FormData();
		fdfieldname.left = new FormAttachment(middle, 0);
		fdfieldname.top = new FormAttachment(wStepname, margin);
		fdfieldname.right = new FormAttachment(100, 0);
		wFieldName.setLayoutData(fdfieldname);

		// Group for counter options
		gOption = new Group(shell, SWT.NONE);
		gOption.setText(BaseMessages.getString(PKG, "AddFilterSequenceDialog.CounterGroup.Label"));
		FormLayout counterLayout = new FormLayout();
		counterLayout.marginHeight = margin;
		counterLayout.marginWidth = margin;
		gOption.setLayout(counterLayout);
		props.setLook(gOption);
		fdOption = new FormData();
		fdOption.left = new FormAttachment(0, 0);
		fdOption.right = new FormAttachment(100, 0);
		fdOption.top = new FormAttachment(wFieldName, 2 * margin);
		gOption.setLayoutData(fdOption);

		// StartAt line
		wlStartAt = new Label(gOption, SWT.RIGHT);
		wlStartAt.setText(BaseMessages.getString(PKG, "AddFilterSequenceDialog.StartAt.Label"));
		props.setLook(wlStartAt);
		FormData fdlStartAt = new FormData();
		fdlStartAt.left = new FormAttachment(0, 0);
		fdlStartAt.right = new FormAttachment(middle, -margin);
		fdlStartAt.top = new FormAttachment(wFieldName, margin);
		wlStartAt.setLayoutData(fdlStartAt);
		wStartAt = new TextVar(transMeta, gOption, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStartAt.setText("");
		props.setLook(wStartAt);
		wStartAt.addModifyListener(lsMod);
		FormData fdStartAt = new FormData();
		fdStartAt.left = new FormAttachment(middle, 0);
		fdStartAt.top = new FormAttachment(wFieldName, margin);
		fdStartAt.right = new FormAttachment(100, 0);
		wStartAt.setLayoutData(fdStartAt);

		// IncrBy line
		wlIncrBy = new Label(gOption, SWT.RIGHT);
		wlIncrBy.setText(BaseMessages.getString(PKG, "AddFilterSequenceDialog.IncrBy.Label"));
		props.setLook(wlIncrBy);
		FormData fdlIncrBy = new FormData();
		fdlIncrBy.left = new FormAttachment(0, 0);
		fdlIncrBy.right = new FormAttachment(middle, -margin);
		fdlIncrBy.top = new FormAttachment(wStartAt, margin);
		wlIncrBy.setLayoutData(fdlIncrBy);
		wIncrBy = new TextVar(transMeta, gOption, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wIncrBy.setText("");
		props.setLook(wIncrBy);
		wIncrBy.addModifyListener(lsMod);
		FormData fdIncrBy = new FormData();
		fdIncrBy.left = new FormAttachment(middle, 0);
		fdIncrBy.top = new FormAttachment(wStartAt, margin);
		fdIncrBy.right = new FormAttachment(100, 0);
		wIncrBy.setLayoutData(fdIncrBy);

		// Condition editor
		wlCondition = new Label(shell, SWT.NONE);
		wlCondition.setText(BaseMessages.getString(PKG, "AddFilterSequenceDialog.Condition.Label"));
		props.setLook(wlCondition);
		fdlCondition = new FormData();
		fdlCondition.left = new FormAttachment(0, 0);
		fdlCondition.top = new FormAttachment(gOption, margin);
		wlCondition.setLayoutData(fdlCondition);

		RowMetaInterface inputfields = null;
		try {
			inputfields = transMeta.getPrevStepFields(stepname);
		} catch (KettleException ke) {
			inputfields = new RowMeta();
			new ErrorDialog(shell, BaseMessages.getString(PKG, "AddFilterSequenceDialog.FailedToGetFields.DialogTitle"),
					BaseMessages.getString(PKG, "AddFilterSequenceDialog.FailedToGetFields.DialogMessage"), ke);
		}

		// Some buttons
		wOK = new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
		wCancel = new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

		setButtonPositions(new Button[] { wOK, wCancel }, margin, null);

		wCondition = new ConditionEditor(shell, SWT.BORDER, condition, inputfields);

		fdCondition = new FormData();
		fdCondition.left = new FormAttachment(0, 0);
		fdCondition.top = new FormAttachment(wlCondition, margin);
		fdCondition.right = new FormAttachment(100, 0);
		fdCondition.bottom = new FormAttachment(wOK, -2 * margin);
		wCondition.setLayoutData(fdCondition);
		wCondition.addModifyListener(lsMod);

		// Add listeners
		lsOK = new Listener() {
			public void handleEvent(Event e) {
				ok();
			}
		};
		lsCancel = new Listener() {
			public void handleEvent(Event e) {
				cancel();
			}
		};

		wOK.addListener(SWT.Selection, lsOK);
		wCancel.addListener(SWT.Selection, lsCancel);

		lsDef = new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				ok();
			}
		};

		wStepname.addSelectionListener(lsDef);
		wFieldName.addSelectionListener(lsDef);
		wStartAt.addSelectionListener(lsDef);
		wIncrBy.addSelectionListener(lsDef);

		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(new ShellAdapter() {
			public void shellClosed(ShellEvent e) {
				cancel();
			}
		});

		// Set the shell size, based upon previous time...
		setSize();

		getData();
		// input.setChanged(changed);

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return stepname;
	}

	public void enableFields() {
		wlStartAt.setEnabled(true);
		wStartAt.setEnabled(true);
		wlIncrBy.setEnabled(true);
		wIncrBy.setEnabled(true);
	}

	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */
	public void getData() {
		logDebug(BaseMessages.getString(PKG, "AddFilterSequenceDialog.Log.GettingKeyInfo"));

		if (input.getFieldName() != null) {
			wFieldName.setText(input.getFieldName());
		}

		wStartAt.setText(Const.NVL(input.getStartAt(), "1"));
		wIncrBy.setText(Const.NVL(input.getIncrementBy(), "1"));

		enableFields();

		wStepname.selectAll();
		wStepname.setFocus();
	}

	private void cancel() {
		stepname = null;
		input.setChanged(changed);
		// Also change the condition back to what it was...
		input.setCondition(backupCondition);
		dispose();
	}

	private void ok() {
		if (wStepname.getText() == "") {// Const.isEmpty -> NoSuchMethodError
			return;
		}

		if (wCondition.getLevel() > 0) {
			wCondition.goUp();
		} else {
			stepname = wStepname.getText();

			if (!Const.isEmpty(wFieldName.getText())) {
				input.setFieldName(wFieldName.getText());
			}
			if (!Const.isEmpty(wStartAt.getText())) {
				input.setStartAt(wStartAt.getText());
			}
			if (!Const.isEmpty(wIncrBy.getText())) {
				input.setIncrementBy(wIncrBy.getText());
			}

			input.setCondition(condition);

			dispose();
		}

	}
}

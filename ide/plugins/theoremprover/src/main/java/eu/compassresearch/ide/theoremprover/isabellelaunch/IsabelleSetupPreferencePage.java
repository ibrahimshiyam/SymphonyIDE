package eu.compassresearch.ide.theoremprover.isabellelaunch;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import eu.compassresearch.ide.theoremprover.CmlTPPlugin;

public class IsabelleSetupPreferencePage extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage
{

	public IsabelleSetupPreferencePage()
	{
		super();
	    setDescription("Setup values for using the Symphony theorem proving support");
	    setTitle("Theorem Prover Setup");
	}
	
	@Override
	public void init(IWorkbench workbench)
	{
	    setDescription("Setup values for using the Symphony theorem proving support");
	    setTitle("Theorem Prover Setup");
	}

	@Override
	protected void createFieldEditors()
	{
		addField(new DirectoryFieldEditor(IIsabelleConstants.ATTR_LOCATION, "Isabelle location", getFieldEditorParent()));
		addField(new DirectoryFieldEditor(IIsabelleConstants.ATTR_SESSION_DIRS, "CML Theory location", getFieldEditorParent()));
		addField(new BooleanFieldEditor(IIsabelleConstants.Z3_NON_COMMERCIAL, "Check box if for non commercial use", getFieldEditorParent()));
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore()
	{
		return CmlTPPlugin.getDefault().getPreferenceStore();
	}

	@Override
	protected void performDefaults()
	{
		IPreferenceStore store = getPreferenceStore();
		store.setDefault(IIsabelleConstants.ATTR_LOCATION, "");
		store.setDefault(IIsabelleConstants.ATTR_SESSION_DIRS, "");
		store.setDefault(IIsabelleConstants.Z3_NON_COMMERCIAL, false);
		super.performDefaults();		
	}

}

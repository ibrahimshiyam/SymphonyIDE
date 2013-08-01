package eu.compassresearch.ide.cml.pogplugin;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.overture.ast.analysis.AnalysisException;
import org.overture.ide.core.resources.IVdmProject;
import org.overture.ide.plugins.poviewer.view.PoOverviewTableView;
import org.overture.ide.ui.utility.VdmTypeCheckerUi;
import org.overture.pog.obligation.ProofObligationList;
import org.overture.pog.pub.IProofObligation;
import org.overture.pog.pub.IProofObligationList;

import eu.compassresearch.core.analysis.pog.obligations.CmlProofObligationList;
import eu.compassresearch.core.analysis.pog.visitors.ProofObligationGenerator;
import eu.compassresearch.core.common.Registry;
import eu.compassresearch.core.common.RegistryFactory;
import eu.compassresearch.ide.core.resources.ICmlModel;
import eu.compassresearch.ide.core.resources.ICmlProject;

public class PogPluginDoStuff
{
	private IWorkbenchWindow window;
	private IWorkbenchSite site;

	/**
	 * The action has been activated. The argument of the method represents the 'real' action sitting in the workbench
	 * UI.
	 * 
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run()
	{

		try
		{

			IProject proj = PogPluginUtility.getCurrentlySelectedProject();
			if (proj == null)
			{
				popErrorMessage("No project selected.");
				return;
			}

			// Check project is built
			IVdmProject vdmProject = (IVdmProject) proj.getAdapter(IVdmProject.class);

			if (vdmProject == null)
			{
				return;
			}

			final ICmlModel model = (ICmlModel) vdmProject.getModel().getAdapter(ICmlModel.class);
			if (model.isParseCorrect())
			{

				if (!model.isParseCorrect())
				{
					return;
					// return new Status(Status.ERROR, IPoviewerConstants.PLUGIN_ID,
					// "Project contains parse errors");
				}

				if (model == null || !model.isTypeCorrect())
				{
					VdmTypeCheckerUi.typeCheck(this.window.getShell(), vdmProject);
				}

				if (model.isTypeCorrect())
				{

					// ArrayList<IResource> cmlfiles = PogPluginUtility.getAllCFilesInProject(proj);
					//
					// for (IResource cmlfile : cmlfiles)
					// {
					// ICmlSourceUnit source = (ICmlSourceUnit) cmlfile.getAdapter(ICmlSourceUnit.class);
					// // CmlSourceUnit source = CmlSourceUnit
					// // .getFromFileResource((IFile) cmlfile);
					// if (!CmlTypeChecker.Utils.isWellType(source.getSourceAst()))
					// {
					// popErrorMessage("There were type errors in "
					// + source.getFile().getName());
					// return;
					// }
					// }

					addPOsToRegistry(model);
					showPOs(vdmProject, model);
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			popErrorMessage(e.getMessage());
		}

	}

	private void popErrorMessage(String message)
	{
		MessageDialog.openInformation(window.getShell(), "COMPASS", "Could not generate POs.\n\n"
				+ message);
	}

	private void addPOsToRegistry(ICmlModel model)
	{
		

		// for (IResource cmlfile : cmlfiles)
		// {
		// ICmlSourceUnit cmlSource = (ICmlSourceUnit) cmlfile.getAdapter(ICmlSourceUnit.class);
		CmlProofObligationList poList = new CmlProofObligationList();
		ProofObligationGenerator pog = new ProofObligationGenerator(model.getAstSource());
		try
		{
			poList = pog.generatePOs();
		} catch (AnalysisException e)
		{
			popErrorMessage(e.getMessage());
			e.printStackTrace();
		}

	//	poList.rem
		// registry.store(cmlSource.getSourceAst(), poList);
		model.setAttribute(POConstants.PO_REGISTRY_ID, poList);
		// }

	}
	
	public static void redrawPos(ICmlProject proj, IProofObligationList polist)
	{
		//FIXME Check if the data in the viewer table is still not being updated!
		final IProofObligationList pol = polist;
		final ICmlProject project = proj;
		Display.getDefault().asyncExec(new Runnable()
		{

			public void run()
			{
				IViewPart v;

				try
				{
					v = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(POConstants.PO_OVERVIEW_TABLE);
					if (v instanceof PoOverviewTableView)
					{
						((PoOverviewTableView) v).setDataList((IVdmProject) project.getAdapter(IVdmProject.class), pol);

					}

					// PogPluginUtility ppu = new PogPluginUtility(site);
					// ppu.openPoviewPerspective();
				} catch (Exception e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		});
	}
	

	private void showPOs(final IVdmProject project, ICmlModel model)
	{
		final ProofObligationList pol = new ProofObligationList();
		// Registry registry = RegistryFactory.getInstance(POConstants.PO_REGISTRY_ID).getRegistry();
		// for (IResource cmlfile : cmlFiles)
		// {
		// ICmlSourceUnit cmlSource = (ICmlSourceUnit) cmlfile.getAdapter(ICmlSourceUnit.class);
		// pol.addAll(registry.lookup(cmlSource.getSourceAst(), CMLProofObligationList.class));
		//
		// }
		pol.addAll(model.getAttribute(POConstants.PO_REGISTRY_ID, CmlProofObligationList.class));

		site.getPage().getWorkbenchWindow().getShell().getDisplay().asyncExec(new Runnable()
		{

			public void run()
			{
				IViewPart v;

				try
				{
					v = site.getPage().showView(POConstants.PO_OVERVIEW_TABLE);
					if (v instanceof PoOverviewTableView)
					{
						((PoOverviewTableView) v).setDataList(project, pol);

					}

					PogPluginUtility ppu = new PogPluginUtility(site);
					ppu.openPoviewPerspective();
				} catch (PartInitException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		});
	}

	/**
	 * Selection in the workbench has been changed. We can change the state of the 'real' action here if we want, but
	 * this can only happen after the delegate has been created.
	 * 
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection)
	{
	}

	/**
	 * We will cache window object in order to be able to provide parent shell for the message dialog.
	 * 
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public PogPluginDoStuff(IWorkbenchWindow window, IWorkbenchSite site)
	{
		this.window = window;
		this.site = window.getActivePage().getActivePart().getSite();
	}

}
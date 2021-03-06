package eu.compassresearch.ide.refinementtool.handlers;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.FileEditorInput;
import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.definitions.PDefinition;
import org.overture.ast.node.INode;

import eu.compassresearch.ast.actions.AStmAction;
import eu.compassresearch.ast.statements.AActionStm;
import eu.compassresearch.core.analysis.pog.obligations.CmlProofObligationList;
import eu.compassresearch.ide.core.resources.ICmlProject;
import eu.compassresearch.ide.core.resources.ICmlSourceUnit;
import eu.compassresearch.ide.refinementtool.CmlRefinePlugin;
import eu.compassresearch.ide.refinementtool.INodeNearCaret;
import eu.compassresearch.ide.refinementtool.IRefineLaw;
import eu.compassresearch.ide.refinementtool.RefConstants;
import eu.compassresearch.ide.refinementtool.laws.ChoiceStopLeft;
import eu.compassresearch.ide.refinementtool.laws.ChoiceStopRight;
import eu.compassresearch.ide.refinementtool.laws.CopyRuleLR;
import eu.compassresearch.ide.refinementtool.laws.CopyRuleRL;
import eu.compassresearch.ide.refinementtool.laws.ImplicitOperationRefineLaw;
import eu.compassresearch.ide.refinementtool.laws.LetIntroRefineLaw;
import eu.compassresearch.ide.refinementtool.laws.LetPreRefineLaw;
import eu.compassresearch.ide.refinementtool.laws.MaudeRefineLaw;
import eu.compassresearch.ide.refinementtool.laws.NullRefineLaw;
import eu.compassresearch.ide.refinementtool.laws.SpecIterRefineLaw;
import eu.compassresearch.ide.refinementtool.laws.SpecPostRefineLaw;
import eu.compassresearch.ide.refinementtool.laws.SpecPreRefineLaw;
import eu.compassresearch.ide.refinementtool.laws.SpecSeqRefineLaw;
import eu.compassresearch.ide.refinementtool.laws.SpecSkipRefineLaw;
import eu.compassresearch.ide.refinementtool.maude.MaudePrettyPrinter;
import eu.compassresearch.ide.refinementtool.maude.MaudeRefineInfo;
import eu.compassresearch.ide.refinementtool.maude.MaudeRefiner;
import eu.compassresearch.ide.refinementtool.view.RefineLawView;
import eu.compassresearch.ide.ui.editor.core.CmlEditor;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class RefineHandler extends AbstractHandler {
	/**
	 * The constructor.
	 */
	public RefineHandler() {
		
	}

	static IResource extractSelection(ISelection sel) {
		if (!(sel instanceof IStructuredSelection))
			return null;
		IStructuredSelection ss = (IStructuredSelection) sel;
		Object element = ss.getFirstElement();
		if (element instanceof IResource)
			return (IResource) element;
		if (!(element instanceof IAdaptable))
			return null;
		IAdaptable adaptable = (IAdaptable) element;
		Object adapter = adaptable.getAdapter(IResource.class);
		return (IResource) adapter;
	}
	
	public INode leastCommonAncestor(INode a, INode b) {
		if (ancestorOf(a, b)) {
			return a;
		} else if (ancestorOf(b, a)) {
			return b;
		} else {
			return leastCommonAncestor(a, b.parent());
		}
	}
	
	public boolean ancestorOf(INode a, INode b) {
		if (b.parent() == null) {
			return false;
		} else if (a == b) {
			return true;
		} else return ancestorOf(a, b.parent());
	}
	
	
	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);

		IStructuredSelection sel = (IStructuredSelection) window
				.getSelectionService().getSelection(
						"eu.compassresearch.ide.ui.CmlNavigator");
		IResource res = extractSelection(sel);

		IProject project = null;
		if (res != null ) {
			project = res.getProject();
		}
		
		CmlEditor editor = (CmlEditor) window.getActivePage().getActiveEditor();
		
		ICmlProject cmlProj = (ICmlProject) project
				.getAdapter(ICmlProject.class);

		MaudeRefiner mref = cmlProj.getModel().getAttribute(RefConstants.REF_MAUDE, MaudeRefiner.class);
		List<IRefineLaw> laws = cmlProj.getModel().getAttribute(RefConstants.REF_LAWS_ID, List.class);		
		CmlProofObligationList pol = cmlProj.getModel().getAttribute(RefConstants.RPOL_ID, CmlProofObligationList.class);
		
		if (mref == null) {
			String maudeLoc = CmlRefinePlugin.getDefault().getPreferenceStore().getString(RefConstants.MAUDE_LOC);
			String maudeThy = CmlRefinePlugin.getDefault().getPreferenceStore().getString(RefConstants.MAUDE_THY);
			
			if (maudeLoc != "" && maudeThy != "") {
				mref = new MaudeRefiner(maudeLoc, maudeThy);
				cmlProj.getModel().setAttribute(RefConstants.REF_MAUDE, mref);
			}
		}
		
		if (laws == null) {
			laws = new LinkedList<IRefineLaw>();
					
			laws.add(new NullRefineLaw());
			laws.add(new ChoiceStopLeft());
			laws.add(new ChoiceStopRight());
			laws.add(new ImplicitOperationRefineLaw());
			laws.add(new SpecSkipRefineLaw());
			laws.add(new SpecSeqRefineLaw());
			laws.add(new SpecIterRefineLaw());
			laws.add(new SpecPreRefineLaw());
			laws.add(new SpecPostRefineLaw());
			laws.add(new LetIntroRefineLaw());
			laws.add(new LetPreRefineLaw());
			laws.add(new CopyRuleLR());
			laws.add(new CopyRuleRL());
			
			cmlProj.getModel().setAttribute(RefConstants.REF_LAWS_ID, laws);  
		}
	
		if (pol == null) {
			pol = new CmlProofObligationList();
			cmlProj.getModel().setAttribute(RefConstants.RPOL_ID, pol);
		}
		
		ITextSelection selection = (ITextSelection) editor.getSelectionProvider().getSelection();
		
		INode node = null;
		INode node2 = null;

		FileEditorInput fei = (FileEditorInput) editor.getEditorInput();
		
		// FIXME get source unit, update implementation
		ICmlSourceUnit csu = (ICmlSourceUnit) fei.getFile().getAdapter(ICmlSourceUnit.class);
		if (csu == null || csu.hasParseErrors() || !csu.hasParseTree())
		{
			return null;
		}
		List<PDefinition> ast = csu.getParseListDefinitions();

		INodeNearCaret visitor = new INodeNearCaret(selection.getOffset(), ast.get(0));
		INodeNearCaret visitor2 = new INodeNearCaret(selection.getOffset()+selection.getLength()+1, ast.get(0));
		try
		{
			for (PDefinition def : ast)
			{
				def.apply(visitor);
				def.apply(visitor2);
			}
			// ast.apply(visitor);
			node = visitor.getBestCandidate();
			node2 = visitor2.getBestCandidate();
			
			node = leastCommonAncestor(node,node2);
			
		} catch (AnalysisException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		editor.selectAndReveal(node);
		
		selection = (ITextSelection) editor.getSelectionProvider().getSelection();
	
		RefineLawView rv = null;
		
		try {
			rv  = (RefineLawView) window.getActivePage().showView(RefConstants.REF_LAW_VIEW);
		} catch (PartInitException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 

		rv.clearLaws();
		rv.setSelection(selection);
		rv.setNode(node);
		
		// If Maude is available, search for appropriate laws
		if (mref != null) {
		
			MaudePrettyPrinter mpp = new MaudePrettyPrinter();
		
			try {
				for (MaudeRefineInfo l : mref.findApplLaws(node.apply(mpp, 0))) {
					rv.addRefineLaw(new MaudeRefineLaw(l, mref));
				}
			} catch (AnalysisException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		for (IRefineLaw l : laws) {
			while (node instanceof AActionStm || node instanceof AStmAction) {
				if (node instanceof AActionStm)
					node = ((AActionStm)node).getAction();
				if (node instanceof AStmAction)
					node = ((AStmAction)node).getStatement();

			}
			if (l.isApplicable(node)) {
				rv.addRefineLaw(l);				
			}
		}
				
		
		//IDocument doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
/*
		
		try {
			doc.replace(selection.getOffset(), selection.getLength(), "Hello!");
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
*/
		
		
	
/*		
		IRegion range = editor.getHighlightRange();
		
		int offset = range.getOffset();
		
		INode node = editor.getElementAt(offset);
		*/
		
		
		
		
		return null;
	}
}

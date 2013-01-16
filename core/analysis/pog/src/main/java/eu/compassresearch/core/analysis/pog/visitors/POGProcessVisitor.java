package eu.compassresearch.core.analysis.pog.visitors;


import java.util.LinkedList;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.definitions.PDefinition;
import org.overture.pog.obligation.POContextStack;
import org.overture.pog.obligation.ProofObligationList;

import eu.compassresearch.ast.analysis.QuestionAnswerCMLAdaptor;
import eu.compassresearch.ast.process.AActionProcess;
import eu.compassresearch.ast.process.PProcess;
import eu.compassresearch.core.analysis.pog.obligations.CMLProofObligationList;

@SuppressWarnings("serial")
public class POGProcessVisitor extends QuestionAnswerCMLAdaptor<POContextStack, ProofObligationList>
{
    private ProofObligationGenerator parentPOG;
    
    public POGProcessVisitor(ProofObligationGenerator parent)
    {
        this.parentPOG = parent;
    }
    
    @Override
    public ProofObligationList defaultPProcess(PProcess node,
	    POContextStack question) throws AnalysisException {
    	System.out.println("PProcess: " + node.toString());
		return new ProofObligationList();
    }
    
    @Override
    public CMLProofObligationList caseAActionProcess(AActionProcess node,POContextStack question) throws AnalysisException{
    	System.out.println("A StateProcess: " + node.toString());
    	CMLProofObligationList pol = new CMLProofObligationList();
    	
//Print the separate parts to screen
    	System.out.println("A StateProcess: " + node.toString());
    	System.out.println("A StateProcess process defintions: " + node.getProcessDefinition());
    	System.out.println("A StateProcess defintion paragraphs: " + node.getDefinitionParagraphs());
    	System.out.println("A StateProcess action: " + node.getAction());
    	

    	LinkedList<SParagraphDefinition> pdef = node.getDefinitionParagraphs();
    	for (SParagraphDefinition def : pdef) {
    		System.out.println(def.toString());
    		
    		pol.addAll(def.apply(parentPOG, question));
    	}
    	
    	// RWL Line below does not apply in the updated Ast.
    	// System.out.println(node.getProcessDefinition());
    	System.out.println(pdef);
    	System.out.println(node.getAction());
		return pol;
    }

}

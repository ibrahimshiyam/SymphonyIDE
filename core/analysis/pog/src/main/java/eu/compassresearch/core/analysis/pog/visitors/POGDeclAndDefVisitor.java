package eu.compassresearch.core.analysis.pog.visitors;

//POG-related imports
import java.util.LinkedList;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.analysis.QuestionAnswerAdaptor;
import org.overture.ast.definitions.PDefinition;
import org.overture.ast.expressions.PExp;
import org.overture.ast.lex.LexIdentifierToken;
import org.overture.ast.node.INode;
import org.overture.ast.types.PType;
import org.overture.pog.obligation.POContextStack;
import org.overture.pog.obligation.ProofObligationList;
import org.overture.pog.visitor.PogParamDefinitionVisitor;


import eu.compassresearch.ast.analysis.QuestionAnswerCMLAdaptor;
import eu.compassresearch.ast.declarations.PDeclaration;
import eu.compassresearch.ast.definitions.AActionDefinition;
import eu.compassresearch.ast.definitions.AActionParagraphDefinition;
import eu.compassresearch.ast.definitions.AChannelNameDefinition;
import eu.compassresearch.ast.definitions.AChannelParagraphDefinition;
import eu.compassresearch.ast.definitions.AChansetDefinition;
import eu.compassresearch.ast.definitions.AChansetParagraphDefinition;
import eu.compassresearch.ast.definitions.AClassParagraphDefinition;
import eu.compassresearch.ast.definitions.AFunctionParagraphDefinition;
import eu.compassresearch.ast.definitions.AOperationParagraphDefinition;
import eu.compassresearch.ast.definitions.AProcessDefinition;
import eu.compassresearch.ast.definitions.AProcessParagraphDefinition;
import eu.compassresearch.ast.definitions.AStateParagraphDefinition;
import eu.compassresearch.ast.definitions.ATypesParagraphDefinition;
import eu.compassresearch.ast.definitions.AValueParagraphDefinition;
import eu.compassresearch.ast.expressions.AUnresolvedPathExp;
import eu.compassresearch.ast.process.PProcess;
import eu.compassresearch.core.analysis.pog.obligations.CMLProofObligationList;

@SuppressWarnings("serial")
public class POGDeclAndDefVisitor extends
	QuestionAnswerCMLAdaptor<POContextStack, ProofObligationList> {

    // Errors and other things are recorded on this guy
    final private QuestionAnswerAdaptor<POContextStack, ProofObligationList> parentPOG;
    final private PogParamDefinitionVisitor<POContextStack, ProofObligationList> overtureVisitor;
    
    public POGDeclAndDefVisitor(QuestionAnswerAdaptor<POContextStack, 
    		ProofObligationList> parent) {
    	this.parentPOG = parent;
    	this.overtureVisitor = new PogParamDefinitionVisitor<POContextStack, ProofObligationList>
    		(this, this);
	
    }
    
    
    // handle CML Defns And Decls
    /**
     * 
     * CML ELEMENT - Channels
     * 
     */
    @Override
    public CMLProofObligationList caseAChannelsDefinition(
    	AChannelsDefinition node, POContextStack question)
    	throws AnalysisException{
    	
    	CMLProofObligationList pol = new CMLProofObligationList();
    
    	LinkedList<AChannelNameDefinition> cns = node.getChannelNameDeclarations();
    	for (AChannelNameDefinition c : cns){
    		pol.addAll(c.apply(this, question));
    	}
    
    	return pol;
     }
     
      
    /**
	  * CML channel definition
	  * CURRENTLY JUST PRINT TO SCREEN
     */    
    @Override
    public CMLProofObligationList caseAChannelNameDefinition(
    		AChannelNameDefinition node, POContextStack question)
    		    	throws AnalysisException{

    	System.out.println("----------***----------");
		System.out.println("AChannelNameDefinition");
		System.out.println(node.toString());
		System.out.println("----------***----------");
    	
    	CMLProofObligationList pol = new CMLProofObligationList();
    
    	/*
    	 * Not clear what POs these may generate? May be useful for generating CMLPOContext
    	 */
    	// Commented out by RWL, unused variables creates warnings
//		LinkedList ids = node.getSingleType().getIdentifiers();
//		PType type = node.getSingleType().getType();    	
    
    	return pol;
    }
    
    /**
     * 
     * CML ELEMENT - Chansets
     * 
     */
    @Override
    public ProofObligationList caseAChansetsDefinition(
	    AChansetsDefinition node, POContextStack question)
	    throws AnalysisException {
	
		LinkedList<AChansetDefinition> subNodes = node.getChansets();
		CMLProofObligationList pol = new CMLProofObligationList();
		
		for (AChansetDefinition d : subNodes) {
		    pol.addAll(d.apply(this, question));
		}
	
		return pol;
    }
    
    /**
	  * CML chanset definition
	  * CURRENTLY JUST PRINT TO SCREEN
     */
    @Override
    public ProofObligationList caseAChansetDefinition(
	    AChansetDefinition node, POContextStack question)
	    throws AnalysisException {
    	
    	System.out.println("----------***----------");
		System.out.println("AChansetDefinition");
		System.out.println(node.toString());
		System.out.println("----------***----------");
		
		CMLProofObligationList pol = new CMLProofObligationList();

    	/*
    	 * Not clear what POs these may generate? May be useful for generating CMLPOContext
    	 */
		// Commented out by RWL: Unused variables creates warnings.
		// LexIdentifierToken id = node.getIdentifier();
		// PVarsetExpression expr = node.getChansetExpression();
		
		return pol;
    }
    
    
    /**
     * 
     * CML ELEMENT - Classes
     * 
     */
    @Override
    public CMLProofObligationList caseAClassDefinition(
	    AClassDefinition node, POContextStack question)
	    throws AnalysisException {
		System.out.println("------");
		System.out
			.println("Reached POGDeclAndDefVisitor - caseAClassParagraphDefinition");
	
		CMLProofObligationList pol = new CMLProofObligationList();
	
		for (PDefinition def : node.getBody()) {
		    System.out.println("In defn Paragraph Loop: " + def.toString());
		    pol.addAll(def.apply(parentPOG, question));
		}
	
		return pol;
    }


      @Override
    public ProofObligationList caseAProcessDefinition(
	    AProcessDefinition node, POContextStack question)
	    throws AnalysisException {
    	
    	System.out.println("----------***----------");
		System.out.println("AProcessDefinition");
		System.out.println(node.toString());
		
		CMLProofObligationList pol = new CMLProofObligationList();
		
		LinkedList<PSingleDeclaration> lstate = node.getLocalState();
		
		PProcess pdef = node.getProcess();
		System.out.println("State :" + lstate.toString() + ", process :" + pdef.toString()); 

		System.out.println("----------***----------");
	
		// NEED TO WORK OUT CONTENTS OF PROCESS AND DISPATCH RELEVANT PARTS
		// LinkedList<> lstate = node.getLocalState();
		// for ( s : lstate)
		// {
		// System.out.println("----------***----------");
		// CMLProofObligationList td = s.apply(this, question);
		// System.out.println("----------***----------");
		// }

		// Dispatch local state?
		// for (PDeclaration s : node.getLocalState())
		// {
		// 		pol.addAll(s.apply(parentPOG, question));
		// }

//		return pdef.apply(this, question);
		pol.addAll(pdef.apply(parentPOG, question));
		return pol;
    }
    
    
    @Override
    public ProofObligationList caseAStateDefinition(
    		AStateDefinition node, POContextStack question)
    	    throws AnalysisException {
    	System.out.println("------");
		System.out
			.println("Reached POGDeclAndDefVisitor - caseAStateParagraphDefinition");
		
		System.out.println("State: " + node.toString() + ", Type: " + node.getType());
	
		CMLProofObligationList pol = new CMLProofObligationList();

		for (PDefinition def : node.getStateDefs()) {
		    System.out
			    .println("In State Paragraph Loop");
		    System.out.println("Def: " + def.toString() + ", Type: " + def.getType());
		    pol.addAll(def.apply(parentPOG, question));
		}
	
		return pol;
    }
    
    /**
     * 
     * CML ELEMENT - Actions
     * 
     */
    @Override
    public ProofObligationList caseAActionsDefinition(
	    AActionsDefinition node, POContextStack question)
	    throws AnalysisException {
		System.out.println("------");
		System.out
			.println("Reached POGDeclAndDefVisitor - caseAActionParagraphDefinition");
		
    	CMLProofObligationList pol = new CMLProofObligationList();
	
		LinkedList<AActionDefinition> actions = node.getActions();
		for (AActionDefinition action : actions) {
		    System.out.println("Action: " + action.toString() + ", Type: " + action.getType());
	}

		return pol;
    }
    
    
    
    // Typechecker will eventually solve resolve these. For now, we hack past it.
    @Override
    public ProofObligationList caseAUnresolvedPathExp(AUnresolvedPathExp node,
	    POContextStack question) throws AnalysisException {
	return new CMLProofObligationList();
    }

   
    
    
    // Call Overture for the other expressions    
    @Override
    public CMLProofObligationList defaultPDeclaration(PDeclaration node,
	    POContextStack question) throws AnalysisException {
    	CMLProofObligationList pol = new CMLProofObligationList();
    	pol.addAll(node.apply(overtureVisitor, question));
    	return pol;
    }

    @Override
    public CMLProofObligationList defaultPDefinition(PDefinition node,
	    POContextStack question) throws AnalysisException {
    	CMLProofObligationList pol = new CMLProofObligationList();
    	pol.addAll(node.apply(overtureVisitor, question));
    	return pol;
    }
    

    // Call the main pog when it's not a defintion/declaration
    @Override
    public ProofObligationList defaultINode(INode node, POContextStack question)
	    throws AnalysisException {
    	CMLProofObligationList pol = new CMLProofObligationList();
    	pol.addAll(node.apply(parentPOG, question));
    	return pol;
    }



   
    /**
     * 
     * VDM ELEMENT - Types
     * 
     */
    @Override
    public ProofObligationList caseATypesDefinition(
	    ATypesDefinition node, POContextStack question)
	    throws AnalysisException {
    	CMLProofObligationList pol = new CMLProofObligationList();
	
    	for (PDefinition def : node.getTypes()) {
    		pol.addAll(def.apply(parentPOG, question));
    	}

    	return pol;
    }
    
    /**
	  * Invariant definition
	  * CURRENTLY PRINT TO SCREEN
	  */

// DOES NOT EXIST ANYMORE
   @Override
   public ProofObligationList caseAInvariantDefinition(
		   AInvariantDefinition node, POContextStack question)
	    throws AnalysisException {
   	
   	CMLProofObligationList pol = new CMLProofObligationList();
   	
	System.out.println("----------***----------");
	System.out.println("AInvariantDefinition");
	System.out.println(node.toString());
	System.out.println(node.getPattern());
	System.out.println(node.getExpression());
	System.out.println("----------***----------");
	
   	return pol;
   }
    
	  /**
	  * 
	  * VDM ELEMENT - Values
	  * 
	  */
	 @Override
	 public ProofObligationList caseAValuesDefinition(
		    AValuesDefinition node, POContextStack question)
		    throws AnalysisException {
	 	
			CMLProofObligationList pol = new CMLProofObligationList();
			LinkedList<PDefinition> list = node.getValueDefinitions();
			
			for (PDefinition def : list) {
			   pol.addAll(def.apply(parentPOG, question));
			}
		
			return pol;
	 }
	 
	  /**
	  * 
	  * VDM ELEMENT - Functions
	  * 
	  */
	 @Override
	 public ProofObligationList caseAFunctionsDefinition(
		    AFunctionsDefinition node, POContextStack question)
		    throws AnalysisException {
			CMLProofObligationList obligations = new CMLProofObligationList();
		
			for (PDefinition def : node.getFunctionDefinitions()) {
				obligations.addAll(def.apply(parentPOG, question));
			}
		
			return obligations;
	 }
	 
	  /**
	  * 
	  * VDM ELEMENT - Operations
	  * 
	  */
	 @Override
	 public ProofObligationList caseAOperationsDefinition(
		    AOperationsDefinition node, POContextStack question)
		    throws AnalysisException {
	 	
		 CMLProofObligationList pol = new CMLProofObligationList();
		 
		 for (SCmlOperationDefinition def : node.getOperations()) {
			 pol.addAll(def.apply(parentPOG, question));
		 }
		 return pol;
	 }
	 
	/**
	 * Implicit operations - CML does not reuse Overture operations
	 */
	@Override
	public ProofObligationList caseAImplicitCmlOperationDefinition(
			 AImplicitOperationDefinition node, POContextStack question) 
			throws AnalysisException{

		System.out.println("----------***----------");
		System.out.println("AImplicitOperationDefinition");
		System.out.println(node.toString());
		System.out.println("----------***----------");
		
		CMLProofObligationList pol = new CMLProofObligationList();

		//Taken from Overture - Needed?
		LexNameList pids = new LexNameList();






//
//    @Override
	public ProofObligationList caseAExplicitOperationDefinition(
	    AExplicitOperationDefinition node, POContextStack question)
	    throws AnalysisException {
		
		CMLProofObligationList pol = new CMLProofObligationList();

		LexNameList pids = new LexNameList();

		// add all defined names from the function parameter list
		for (PPattern p : node.getParameterPatterns())
			for (PDefinition def : p.getDefinitions())
				pids.add(def.getName());

		if (pids.hasDuplicates()){
			pol.add(new CMLParameterPatternObligation(node, question));
		}

		// if operation has a precondition, dispatch for PO checking
		if (node.getPrecondition() != null){
			pol.addAll(node.getPrecondition().apply(parentPOG, question));
		}
		// if  operation has a precondition, dispatch for PO checking
		// and generate OperationPostConditionObligation
		if (node.getPostcondition() != null){
			pol.addAll(node.getPostcondition().apply(parentPOG, question));
			pol.add(new CMLOperationPostConditionObligation(node, question));
		}
		
		// dispatch operation body for PO checking
		pol.addAll(node.getBody().apply(parentPOG, question));

//		/*
//		 * Taken from Overture POG - not currently working
//		 */
//		
//		if (node.getIsConstructor() && node.getClassDefinition() != null
//				&& node.getClassDefinition().getInvariant() != null){
//			pol.add(new CMLStateInvariantObligation(node, question));
//		}
//		
//		if (!node.getIsConstructor()
//				&& !TypeComparator.isSubType(node.getActualResult(), node.getType().getResult())){
//			pol.add(new SubTypeObligation(node, node.getActualResult(), question));
//		}
   	
		return pol;
	}
//

//
//    @Override
//    public ProofObligationList caseAExplicitOperationDefinition(
//	    AExplicitOperationDefinition node, POContextStack question)
//	    throws AnalysisException {
//    	System.out.println("----------***----------");
//		System.out.println("AExplicitOperationDefinition");
//		System.out.println(node.toString());
//		System.out.println("----------***----------");
//		
//    	CMLProofObligationList pol = new CMLProofObligationList();
//
//		LexNameList pids = new LexNameList();
//
//		// add all defined names from the function parameter list
//		for (PPattern p : node.getParameterPatterns())
//			for (PDefinition def : p.getDefinitions())
//				pids.add(def.getName());
//
//		if (pids.hasDuplicates()){
//			pol.add(new ParameterPatternObligation(node, question));
//		}
//
//		if (node.getPrecondition() != null){
//			pol.addAll(node.getPrecondition().apply(parentPOG, question));
//		}
//
//		if (node.getPostcondition() != null){
//			pol.addAll(node.getPostcondition().apply(parentPOG, question));
//			pol.add(new OperationPostConditionObligation(node, question));
//		}
//
//		pol.addAll(node.getBody().apply(parentPOG, question));
//
//		if (node.getIsConstructor() && node.getClassDefinition() != null
//				&& node.getClassDefinition().getInvariant() != null){
//			pol.add(new StateInvariantObligation(node, question));
//		}
//		
//		/*
//		 * Subtype Obligation not working well - mainly the if statement, I believe.
//		 */
////		if (!node.getIsConstructor()
////				&& !TypeComparator.isSubType(node.getActualResult(), node.getType().getResult())){
////			pol.add(new SubTypeObligation(node, node.getActualResult(), question));
////		}
//    	
//		return pol;
//    }
//    
//    @Override
//	public CMLProofObligationList caseAImplicitOperationDefinition(
//			AImplicitOperationDefinition node, POContextStack question) throws AnalysisException
//	{
//
//		CMLProofObligationList pol = new CMLProofObligationList();
//		LexNameList pids = new LexNameList();
//
//		for (APatternListTypePair tp : node.getParameterPatterns())
//			for (PPattern p : tp.getPatterns())
//				for (PDefinition def : p.getDefinitions())
//					pids.add(def.getName());
//
//		if (pids.hasDuplicates()){
//			pol.add(new ParameterPatternObligation(node, question));
//		}
//
//		// if implicit operation has a precondition, dispatch for PO checking
//		if (node.getPrecondition() != null){
//			pol.addAll(node.getPrecondition().apply(parentPOG, question));
//		}
//		
//		// if implicit operation has a precondition, dispatch for PO checking
//		// and generate OperationPostConditionObligation
//		if (node.getPostcondition() != null){
//			pol.addAll(node.getPostcondition().apply(parentPOG, question));
//			pol.add(new OperationPostConditionObligation(node, question));
//		}
//
//		
//		if (node.getBody() != null){
//			pol.addAll(node.getBody().apply(parentPOG, question));
//
//			if (node.getIsConstructor() && node.getClassDefinition() != null
//					&& node.getClassDefinition().getInvariant() != null){
//				pol.add(new StateInvariantObligation(node, question));
//			}
//
//			/*
//			 * Subtype Obligation not working well - mainly the if statement, I believe.
//			 */
////			if (!node.getIsConstructor()
////					&& !TypeComparator.isSubType(node.getActualResult(), node.getType().getResult())){
////				pol.add(new SubTypeObligation(node, node.getActualResult(), question));
////			}
//		}else if (node.getPostcondition() != null){
//				question.push(new POOperationDefinitionContext(node, false, node.getStateDefinition()));
//				pol.add(new SatisfiabilityObligation(node, node.getStateDefinition(), question));
//				question.pop();
//			
//		}
//
//		return pol;
//	}
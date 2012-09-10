package eu.compassresearch.core.interpreter.eval;

import org.overture.interpreter.values.Value;

import eu.compassresearch.ast.actions.ACommunicationAction;
import eu.compassresearch.ast.actions.AIdentifierStateDesignator;
import eu.compassresearch.ast.actions.ASingleGeneralAssignmentStatementAction;
import eu.compassresearch.ast.actions.ASkipAction;
import eu.compassresearch.ast.analysis.AnalysisException;
import eu.compassresearch.ast.analysis.QuestionAnswerAdaptor;
import eu.compassresearch.ast.definitions.AAssignmentDefinition;
import eu.compassresearch.core.interpreter.runtime.Context;
import eu.compassresearch.core.interpreter.values.ProcessValue;

public class ActionEvaluator extends QuestionAnswerAdaptor<Context, Value> {

	private CmlEvaluator parentInterpreter; 
	
	public ActionEvaluator(CmlEvaluator parentInterpreter)
	{
		this.parentInterpreter = parentInterpreter;
	}
	
	@Override
	public Value caseASingleGeneralAssignmentStatementAction(
			ASingleGeneralAssignmentStatementAction node, Context question)
			throws AnalysisException {
				
		Value v = node.getExpression().apply(parentInterpreter,question);
		//TODO Remove this
		AIdentifierStateDesignator id = (AIdentifierStateDesignator)node.getStateDesignator();
		question.put(id.getName(), v);
		
		System.out.println( id.getName() + " := " + v);
		
		return new ProcessValue();
	}
	
	@Override
	public Value caseACommunicationAction(ACommunicationAction node,
			Context question) throws AnalysisException {
		
		//System.out.println("<" + node.getIdentifier() + ">");
		
//		if(question.getContinueOnEvent() != null && 
//				question.getContinueOnEvent().equals(node.getIdentifier().getName())  )
//			return node.getAction().apply(parentInterpreter,question);
//		else
		
		return new ProcessValue(node,question);
				
	}
	
//	@Override
//	public Value caseAAssignmentDefinition(AAssignmentDefinition node,
//			Context question) throws AnalysisException {
//		
//		System.out.println("assign");
//		
//		return new ProcessValue();
//	}
	
	@Override
	public Value caseASkipAction(ASkipAction node, Context question)
			throws AnalysisException {
		
		//question.getProcessThread().waitForSchedule();
		
		//System.out.print("<Skip>");
		
		return new ProcessValue();
	}
	
}

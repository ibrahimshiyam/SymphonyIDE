package eu.compassresearch.core.interpreter.cml;

import java.util.Iterator;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.lex.LexNameToken;
import org.overture.interpreter.runtime.Context;
import org.overture.interpreter.values.Value;

import eu.compassresearch.ast.actions.ABlockStatementAction;
import eu.compassresearch.ast.actions.ACommunicationAction;
import eu.compassresearch.ast.actions.AExternalChoiceAction;
import eu.compassresearch.ast.actions.AGeneralisedParallelismParallelAction;
import eu.compassresearch.ast.actions.AGuardedAction;
import eu.compassresearch.ast.actions.AInterleavingParallelAction;
import eu.compassresearch.ast.actions.AReferenceAction;
import eu.compassresearch.ast.actions.ASequentialCompositionAction;
import eu.compassresearch.ast.actions.ASingleGeneralAssignmentStatementAction;
import eu.compassresearch.ast.actions.ASkipAction;
import eu.compassresearch.ast.actions.PAction;
import eu.compassresearch.ast.actions.SParallelAction;
import eu.compassresearch.core.interpreter.api.InterpretationErrorMessages;
import eu.compassresearch.core.interpreter.api.InterpreterRuntimeException;
import eu.compassresearch.core.interpreter.cml.events.ObservableEvent;
import eu.compassresearch.core.interpreter.eval.CmlOpsToString;
import eu.compassresearch.core.interpreter.events.CmlProcessStateEvent;
import eu.compassresearch.core.interpreter.events.CmlProcessStateObserver;
import eu.compassresearch.core.interpreter.events.CmlProcessTraceObserver;
import eu.compassresearch.core.interpreter.events.TraceEvent;
import eu.compassresearch.core.interpreter.runtime.CmlContext;
import eu.compassresearch.core.interpreter.runtime.CmlRuntime;
import eu.compassresearch.core.interpreter.util.CmlActionAssistant;
import eu.compassresearch.core.interpreter.util.CmlBehaviourThreadUtility;
import eu.compassresearch.core.interpreter.util.Pair;

/**
 *  This class represents a running CML Action. It represents a specific node as specified in D23.2 section 7.4.2,
 *  where a node is specified as a tuple (w,s,a) where w is the set of variables, s is the state values and a is the 
 *  current action.
 *  w and s are stored in the current Context object and a is represented by storing the next action AST node to be executed.
 * 
 * 	The possible transitions are handled in the visitor case methods.
 * 
 *  Therefore this Class should be fully consistent with the operational semantics described in D23.2 chapter 7.
 * 
 * @author akm
 *
 */
public class CmlAction extends AbstractBehaviourThread<PAction> implements CmlProcessStateObserver, CmlProcessTraceObserver{

	/**
	 * 
	 */
	private static final long serialVersionUID = 993071972119803788L;
	private LexNameToken name;
	
	public CmlAction(PAction action,CmlContext context, LexNameToken name)
	{
		super(null);
		this.name = name;
		pushNext(action, context);
	}
	
	public CmlAction(PAction action,CmlContext context, LexNameToken name, CmlAction parent)
	{
		super(parent);
		this.name = name;
		pushNext(action, context);
	}
	
	@Override
	public void start(CmlSupervisorEnvironment env) {
		
		this.env = env; 
		
		//If it has no parent it is controlled by a CmlProcess object as the main process behaviour
		//Therefore it should not be added as a pupil
		if(parent() != null)
			supervisor().addPupil(this);
		
		setState(CmlProcessState.RUNNABLE);
	}

	@Override
	public CmlAlphabet inspect()
	{
		try
		{
			if(hasNext())
			{
				Pair<PAction,CmlContext> next = nextState();
				return next.first.apply(alphabetInspectionVisitor,next.second);
			}
			//if the process is done we return the empty alphabet
			else
			{
				return new CmlAlphabet();
			}
		}
		catch(AnalysisException ex)
		{
			CmlRuntime.logger().throwing(this.toString(),"inspect()", ex);
			throw new InterpreterRuntimeException(InterpretationErrorMessages.FATAL_ERROR.customizeMessage(),ex);
		}
	}

	@Override
	public LexNameToken name() {
		return this.name;
	}
	
	@Override
	public String nextStepToString() {
		
		if(hasNext())
		{
			if(hasChildren())
			{
				CmlBehaviourThread leftChild = children().get(0);
				CmlBehaviourThread rightChild = children().get(1);
				
				return "(" + leftChild.nextStepToString() + ")" + CmlOpsToString.toString(nextState().first) + "(" + rightChild.nextStepToString()+")";
			}
			else{
				return nextState().first.toString();
			}
		}
		else
			return "Finished";
	}
	
	@Override
	public String toString() {
	
		return name.toString();
	}
	
	/**
	 * Process state methods 
	 */
	@Override
	public CmlProcessState getState() {
		return state;
	}

	@Override
	protected void setState(CmlProcessState state) {
		
		if(getState() != state)
		{
			CmlProcessStateEvent ev = new CmlProcessStateEvent(this, this.state, state);
			this.state = state;
			notifyOnStateChange(ev);
			CmlRuntime.logger().finest(name() + ":" + state.toString());
		}
	}
	
	/**
	 * CmlProcessStateObserver interface 
	 */
	
	@Override
	public void onStateChange(CmlProcessStateEvent stateEvent) {

		switch(stateEvent.getTo())
		{
		case WAIT_CHILD:
		case RUNNING:
			setState(CmlProcessState.WAIT_CHILD);
			break;
		case WAIT_EVENT:
			//if at least one child are waiting for an event this process must invoke either Parallel Non-sync or sync
			if(CmlBehaviourThreadUtility.isAllChildrenFinishedOrWaitingForEvent(this))
				setState(CmlProcessState.RUNNABLE);
			break;
		case FINISHED:
			stateEvent.getSource().onStateChanged().unregisterObserver(this);
			
			//if all the children are finished this process can continue and evolve into skip
			if(CmlBehaviourThreadUtility.isAllChildrenFinishedOrWaitingForEvent(this))
				setState(CmlProcessState.RUNNABLE);
			
			break;
		default:
			break;
		}
	}

	/**
	 * CmlProcessTraceObserver interface 
	 */
	
	/**
	 * This will provide the traces from all the child actions
	 */
	@Override
	public void onTraceChange(TraceEvent traceEvent) {
		
		this.trace.addEvent(traceEvent.getEvent());
		notifyOnTraceChange(TraceEvent.createRedirectedEvent(this, traceEvent));
	}
	
	/**
	 * Private helper methods
	 */
	
	/*
	 * Child support -- we must help the children
	 */
	
	/**
	 * Executes the next state of the child process silently, meaning that the trace event
	 * is disabled since the patent processes (this process) already have the event in the trace
	 * since its supervising the child processes
	 * @param child
	 * @return
	 */
	private CmlBehaviourSignal executeChild(CmlBehaviourThread child)
	{
		child.onTraceChanged().unregisterObserver(this);
		CmlBehaviourSignal result = child.execute(supervisor());
		child.onTraceChanged().registerObserver(this);
		
		return result;
	}
	
	private void addChild(CmlAction child)
	{
		//Add the child to the process graph
		children().add(child);
		//Register for state change and trace change events
		child.onStateChanged().registerObserver(this);
		child.onTraceChanged().registerObserver(this);
		
		child.start(supervisor());
	}
	
	/**
	 * Transition methods
	 */
	
	@Override
	public CmlBehaviourSignal defaultPAction(PAction node, CmlContext question)
			throws AnalysisException {

		throw new InterpreterRuntimeException(node.getClass().getSimpleName() + " case is not yet implemented.");
	}
	
	/**
	 * The action inside a block is executed directly, since it has no semantic meaning.
	 */
	@Override
	public CmlBehaviourSignal caseABlockStatementAction(
			ABlockStatementAction node, CmlContext question)
			throws AnalysisException {
		
		//pushNext(node.getAction(), question); 
		//return CmlBehaviourSignal.EXEC_SUCCESS;
		return node.getAction().apply(this,question);
	}

	/**
	 * Synchronisation and Communication D23.2 7.5.2
	 * 
	 * This transition can either be
	 * Simple prefix   	: a -> A
	 * Synchronisation 	: a.1 -> A
	 * Output			: a!2 -> A
	 * Input			: a?x -> A
	 * As defined in 7.5.2 in D23.2
	 */
	@Override
	public CmlBehaviourSignal caseACommunicationAction(
			ACommunicationAction node, CmlContext question)
			throws AnalysisException {
		//At this point the supervisor has already given go to the event,
		//TODO: input is still missing
		pushNext(node.getAction(), question); 
		
		return CmlBehaviourSignal.EXEC_SUCCESS;
		
		 
//		//so we can execute it immediately. We just have figure out which kind of event it is
//		if(CmlActionAssistant.isPrefixEvent(node))
//			result = casePrefixEvent(node, question);
//		//supervisor().clearSelectedCommunication();
	}
	
	/**
	 * Helper methods for Synchronisation and Communication transition rules
	 */
	
	
	private CmlBehaviourSignal casePrefixEvent(ACommunicationAction node, CmlContext question) 
			throws AnalysisException 
	{
		pushNext(node.getAction(), question); 
		return CmlBehaviourSignal.EXEC_SUCCESS;
	}
	
	
	/**
	 * External Choice D23.2 7.5.4
	 * 
	 *  There four transition rules for external choice:
	 *  
	 *  * External Choice Begin
	 *  
	 *  * External Choice Silent
	 *  
	 *  * External Choice SKIP
	 *  
	 *  * External Choice End
	 *  
	 */
	@Override
	public CmlBehaviourSignal caseAExternalChoiceAction(
			AExternalChoiceAction node, CmlContext question)
			throws AnalysisException {
		
		CmlBehaviourSignal result = null;
		
		//if true this means that this is the first time here, so the Parallel Begin rule is invoked.
		if(!hasChildren()){
			result = caseExternalChoiceBegin(node,question);
		}
		//If this is true, the Skip rule is instantiated. This means that the entire choice evolves into Skip
		//with the state from the skip. After this all the children processes are terminated
		else if(CmlBehaviourThreadUtility.existsAFinishedChild(this))
		{
			result = caseExternalChoiceSkip();
		}
		//if this is true, then we can resolve the choice to the event
		//of one of the children that are waiting for events
		else if(CmlBehaviourThreadUtility.isAtLeastOneChildWaitingForEvent(this))
		{
			result = caseExternalChoiceEnd();
		}
		else
			result = CmlBehaviourSignal.FATAL_ERROR;
		
		return result;
	}
	
	/**
	 * External Choice helper methods
	 */

	/**
	 * handles the External Choice Begin Rule
	 * @param node
	 * @param question
	 * @return
	 */
	private CmlBehaviourSignal caseExternalChoiceBegin(AExternalChoiceAction node,CmlContext question)
	{
		PAction left = node.getLeft();
		PAction right = node.getRight();
		
		//TODO: create a local copy of the question state for each of the actions
		CmlAction leftInstance = 
				new CmlAction(left, question, 
						new LexNameToken(name.module,name.getIdentifier().getName() + "[]" ,left.getLocation()),this);
		
		CmlAction rightInstance = 
				new CmlAction(right, question, 
						new LexNameToken(name.module,"[]" + name.getIdentifier().getName(),right.getLocation()),this);
		
		//Add the children to the process graph
		addChild(leftInstance);
		addChild(rightInstance);
		
		//Now let this process wait for the children to get into a waitForEvent state
		setState(CmlProcessState.WAIT_CHILD);
		
		//We push the current state, since this process will control the child processes created by it
		pushNext(node, question);
		
		return CmlBehaviourSignal.EXEC_SUCCESS;
	}
	
	/**
	 * Handles the External Choice Skip rule
	 * @return
	 */
	private CmlBehaviourSignal caseExternalChoiceSkip()
	{
		//find the finished child
		CmlBehaviourThread skipChild = findFinishedChild();
		
		//FIXME: maybe the we should differentiate between actions and process instead of just having CmlProcess
		// 		Childerens. We clearly need it!
		//we know its an action
		CmlAction childAction = (CmlAction)skipChild; 
		
		//Extract the current CmlContext of finished child action and use it as the CmlContext
		//for the Skip action.
		pushNext(new ASkipAction(), childAction.prevState().second);
		
		//mmmmuhuhuhahaha kill all the children
		killAndRemoveAllTheEvidenceOfTheChildren();
		
		return CmlBehaviourSignal.EXEC_SUCCESS;
	}
	
	private CmlBehaviourSignal caseExternalChoiceEnd()
	{
		AbstractBehaviourThread<PAction> theChoosenOne = findTheChoosenChild(supervisor().selectedObservableEvent());
		
		//first we execute the child
		CmlBehaviourSignal result = executeChild(theChoosenOne);
		
		if(theChoosenOne.hasNext())
		{	//get the state replace the current state
			//FIXME: this is really really ugly
			for(Pair<PAction,CmlContext> state : theChoosenOne.getExecutionStack())
			{
				pushNext(state.first, 
						state.second);
			}
		}
		else
		{
			pushNext(theChoosenOne.prevState().first, 
					theChoosenOne.prevState().second);
		}
		setState(CmlProcessState.RUNNING);
		
		//mmmmuhuhuhahaha kill all the children
		killAndRemoveAllTheEvidenceOfTheChildren();
		
		return result;
	}
	
	/**
	 * Finds the first finished child if any
	 * @return The first finished child, if none then null is returned
	 */
	private CmlBehaviourThread findFinishedChild()
	{
		for(CmlBehaviourThread child : children())
		{
			if(child.finished())
				return child;
		}
		
		return null;
	}
	
	/**
	 * 
	 * @param event
	 * @return
	 */
	private AbstractBehaviourThread<PAction> findTheChoosenChild(ObservableEvent event)
	{
		for(AbstractBehaviourThread<PAction> child : children)
		{
			if(child.waiting() && child.inspect().containsObservableEvent(event))
				return child;
		}
		
		return null;
	}
	
	private void killAndRemoveAllTheEvidenceOfTheChildren()
	{
		//Abort all the children of this action
		for(CmlBehaviourThread child : children())
		{
			child.setAbort(null);
		}
		
		//Remove them from the supervisor
		removeTheChildren();
	}
	
	/**
	 * External Choice  
	 * End of region
	 * 
	*/
	
	/**
	 * This implements the 7.5.10 Action Reference transition rule in D23.2. 
	 */
	@Override
	public CmlBehaviourSignal caseAReferenceAction(AReferenceAction node,
			CmlContext question) throws AnalysisException {
		//FIXME: the scoping is not correct, this should be done as described in the transition rule
		
		//FIXME: Consider: Instead of this might create a child process, and behave as this child until it terminates
		//CMLActionInstance refchild = new CMLActionInstance(node.getActionDefinition().getAction(), question, node.getName()); 
		
		pushNext(node.getActionDefinition().getAction(), question); 
		return CmlBehaviourSignal.EXEC_SUCCESS;
	}
	
	/**
	 * This implements the 7.5.6 Sequential Composition transition rules in D23.2.
	 */
	@Override
	public CmlBehaviourSignal caseASequentialCompositionAction(
			ASequentialCompositionAction node, CmlContext question)
			throws AnalysisException {

		//First push right and then left, so that left get executed first
		pushNext(node.getRight(), question);
		pushNext(node.getLeft(), question);
		
		return CmlBehaviourSignal.EXEC_SUCCESS;
	}

	/**
	 * Parallel composition D23.2 7.5.7
	 *  
	 */
	
	/**
	 * Generalised Parallelism
	 * A [| cs |] B (no state) 
	 * 
	 * or
	 * 
	 * A [| ns1 | cs | ns2 |] B 
	 */
	@Override
	public CmlBehaviourSignal caseAGeneralisedParallelismParallelAction(
			AGeneralisedParallelismParallelAction node, CmlContext question)
			throws AnalysisException {
	
		//TODO: This only implements the "A [| cs |] B (no state)" and not "A [| ns1 | cs | ns2 |] B"
		CmlBehaviourSignal result = null;
		
		//if true this means that this is the first time here, so the Parallel Begin rule is invoked.
		if(!hasChildren()){
			result = caseParallelBegin(node,question);
			//We push the current state, since this process will control the child processes created by it
			pushNext(node, question);
		}
		//The process has children and they have all evolved into Skip so now the parallel end rule will be invoked 
		else if (CmlBehaviourThreadUtility.isAllChildrenFinished(this))
		{
			result = caseParallelEnd(question); 
		}
		//At least one child is not finished and waiting for event, this will either invoke the Parallel Non-sync or Sync rule
		else if(CmlBehaviourThreadUtility.isAllChildrenFinishedOrWaitingForEvent(this))
		{
			//convert the channelset of the current node to a alphabet
			CmlAlphabet cs = CmlBehaviourThreadUtility.convertChansetExpToAlphabet(this,
					node.getChansetExpression(),question);		
			
			//get the immediate alphabets of the left and right child
			CmlBehaviourThread leftChild = children().get(0);
			CmlAlphabet leftChildAlpha = leftChild.inspect().flattenSyncEvents(); 
			CmlBehaviourThread rightChild = children().get(1);
			CmlAlphabet rightChildAlpha = rightChild.inspect().flattenSyncEvents();

			//convert the selected event to a CmlAlphabet
			CmlAlphabet selectedEventAlpha = supervisor().selectedObservableEvent().getAsAlphabet();
			//now make the intersection between the selectedEventAlpha and the children's alpha
			CmlAlphabet leftOption = selectedEventAlpha.intersect(leftChildAlpha);
			CmlAlphabet rightOption = selectedEventAlpha.intersect(rightChildAlpha);
			
			//if both intersections are non empty it must be a sync event
			if(!leftOption.isEmpty() &&
					!rightOption.isEmpty())
			{
				//supervisor().setSelectedObservableEvent(leftOption.getObservableEvents().iterator().next());
				executeChild(leftChild);
				//supervisor().setSelectedObservableEvent(rightOption.getObservableEvents().iterator().next());
				executeChild(rightChild);
				result = CmlBehaviourSignal.EXEC_SUCCESS;
			}
			else if(!leftOption.isEmpty())
			{
				result = executeChild(leftChild);
			}
			else if(!rightOption.isEmpty())
			{
				result = executeChild(rightChild);
			}
			else
			{
				result = CmlBehaviourSignal.FATAL_ERROR;
			}
			
			//We push the current state, 
			pushNext(node, question);
		}
		
		
		return result;
	}
	
	/**
	 * Interleaving
	 * A ||| B (no state)
	 * 
	 * or 
	 * 
	 * A [|| ns1 | ns2 ||] B
	 * 
	 * This has three parts:
	 * 
	 * Parallel Begin:
	 * 	At this step the interleaving action are not yet created. So this will be a silent (tau) transition
	 * 	where the left and right actions will be created and started.
	 * 
	 * Parallel Non-sync:
	 * 	At this step the actions are each executed separately. Since no sync shall stake place this Action just wait
	 * 	for the child actions to be in the FINISHED state. 
	 * 
	 * Parallel End:
	 *  At this step both child actions are in the FINISHED state and they will be removed from the running process network
	 *  and this will make a silent transition into Skip. 
	 */
	@Override
	public CmlBehaviourSignal caseAInterleavingParallelAction(
			AInterleavingParallelAction node, CmlContext question)
			throws AnalysisException {

		//TODO: This only implements the "A ||| B (no state)" and not "A [|| ns1 | ns2 ||] B"
		CmlBehaviourSignal result = null;
		
		//if true this means that this is the first time here, so the Parallel Begin rule is invoked.
		if(!hasChildren()){
			result = caseParallelBegin(node,question);
			//We push the current state, since this process will control the child processes created by it
			pushNext(node, question);

		}
		//At least one child is not finished and waiting for event, this will invoke the Parallel Non-sync 
		else if(CmlBehaviourThreadUtility.isAtLeastOneChildWaitingForEvent(this))
		{
			CmlBehaviourThread leftChild = children().get(0);
			CmlAlphabet leftChildAlpha = leftChild.inspect(); 
			CmlBehaviourThread rightChild = children().get(1);
			CmlAlphabet rightChildAlpha = rightChild.inspect();
			
			if(leftChildAlpha.containsObservableEvent(supervisor().selectedObservableEvent()) )
			{
				result = executeChild(leftChild);
			}
			else if(rightChildAlpha.containsObservableEvent(supervisor().selectedObservableEvent()) )
			{
				result = executeChild(rightChild);
			}
			else
			{
				result = CmlBehaviourSignal.FATAL_ERROR;
			}
			
			//We push the current state, 
			pushNext(node, question);
			
		}
		//the process has children and must now handle either termination or event sync
		else if (CmlBehaviourThreadUtility.isAllChildrenFinished(this))
		{
			result = caseParallelEnd(question); 
		}
		//else if ()
		
		return result;
	}
	
	/**
	 * Parallel composition Helper methods
	 */
	
	/**
	 * This method introduces a local state for each parallel action which is the source state component
	 * restricted by the nameset expressions
	 * @param question
	 * @return
	 */
	private CmlBehaviourSignal caseParallelBegin(SParallelAction node, CmlContext question)
	{
		PAction left = node.getLeftAction();
		PAction right = node.getRightAction();
		
		//TODO: create a local copy of the question state for each of the actions
		CmlAction leftInstance = 
				new CmlAction(left, question, 
						new LexNameToken(name.module,name.getIdentifier().getName() + "|||" ,left.getLocation()),this);
		
		CmlAction rightInstance = 
				new CmlAction(right, question, 
						new LexNameToken(name.module,"|||" + name.getIdentifier().getName(),right.getLocation()),this);
		
		//add the children to the process graph
		addChild(leftInstance);
		addChild(rightInstance);

		//Now let this process wait for the children to get into a waitForEvent state
		setState(CmlProcessState.WAIT_CHILD);
		
		return CmlBehaviourSignal.EXEC_SUCCESS;
	}
	
	private void removeTheChildren()
	{
		for(Iterator<CmlBehaviourThread> iterator = children().iterator(); iterator.hasNext(); )
		{
			CmlBehaviourThread child = iterator.next();
			supervisor().removePupil(child);
			iterator.remove();
		}
	}
	
	private CmlBehaviourSignal caseParallelEnd(CmlContext question)
	{
		removeTheChildren();
		
		//now this process evolves into Skip
		pushNext(new ASkipAction(), question);
		
		return CmlBehaviourSignal.EXEC_SUCCESS;
	}
	
	@Override
	public CmlBehaviourSignal caseASkipAction(ASkipAction node, CmlContext question)
			throws AnalysisException {

		//if hasNext() is true then Skip is in sequential composition with next
		if(!hasNext())
			setState(CmlProcessState.FINISHED);
		return CmlBehaviourSignal.EXEC_SUCCESS;
	}
	
	/**
	 * Assignment - section 7.5.1 D23.2
	 * 
	 */
	@Override
	public CmlBehaviourSignal caseASingleGeneralAssignmentStatementAction(
			ASingleGeneralAssignmentStatementAction node, CmlContext question)
					throws AnalysisException {
//		question.putNew(new NameValuePair(new LexNameToken("", new LexIdentifierToken("a", false, new LexLocation())), new IntegerValue(2)));
		Value expValue = node.getExpression().apply(cmlEvaluator,question);
		
		//TODO Change this to deal with it in general
		LexNameToken stateDesignatorName = CmlActionAssistant.extractNameFromStateDesignator(node.getStateDesignator());
		CmlContext nameContext = (CmlContext)question.locate(stateDesignatorName);
		
		nameContext.put(stateDesignatorName, expValue);
		
		System.out.println(stateDesignatorName + " = " + expValue);
		
//		if(nameContext == null)
		//					nameContext = new CMLContext(node.getLocation(),"caseASi
		//
//									nameContext.put(id.getName(), expValue);
		//
		//					System.out.println( id.getName() + " := " + expValue);
		//
		//					return new ProcessValueOld(null);
		
		return CmlBehaviourSignal.EXEC_SUCCESS;
	}
	
	/**
	 * State-based Choice - section 7.5.5 D23.2
	 * Guard
	 * Guarded actions are stuck, unless the guard is true.
	 * So If we ever execute this transition, the guard expression would already
	 * have been checked for being true.
	 */
	@Override
	public CmlBehaviourSignal caseAGuardedAction(AGuardedAction node,
			CmlContext question) throws AnalysisException {

		pushNext(node.getAction(), question); 
		
		return CmlBehaviourSignal.EXEC_SUCCESS;
	}
}

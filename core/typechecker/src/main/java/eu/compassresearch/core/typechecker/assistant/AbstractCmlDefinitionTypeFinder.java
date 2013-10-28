package eu.compassresearch.core.typechecker.assistant;

import java.io.File;
import java.io.InputStream;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.statements.AActionStm;
import org.overture.ast.statements.AAltNonDeterministicStm;
import org.overture.ast.statements.ADoNonDeterministicStm;
import org.overture.ast.statements.AIfNonDeterministicStm;
import org.overture.ast.statements.ANewStm;
import org.overture.ast.statements.AUnresolvedStateDesignator;
import org.overture.ast.types.PType;
import org.overture.typechecker.assistant.ITypeCheckerAssistantFactory;
import org.overture.typechecker.utilities.DefinitionTypeFinder;

import eu.compassresearch.ast.actions.AAlphabetisedParallelismParallelAction;
import eu.compassresearch.ast.actions.AAlphabetisedParallelismReplicatedAction;
import eu.compassresearch.ast.actions.AChannelRenamingAction;
import eu.compassresearch.ast.actions.AChaosAction;
import eu.compassresearch.ast.actions.ACommonInterleavingReplicatedAction;
import eu.compassresearch.ast.actions.ACommunicationAction;
import eu.compassresearch.ast.actions.ADeclarationInstantiatedAction;
import eu.compassresearch.ast.actions.ADivAction;
import eu.compassresearch.ast.actions.AEndDeadlineAction;
import eu.compassresearch.ast.actions.AExternalChoiceAction;
import eu.compassresearch.ast.actions.AExternalChoiceReplicatedAction;
import eu.compassresearch.ast.actions.AGeneralisedParallelismParallelAction;
import eu.compassresearch.ast.actions.AGeneralisedParallelismReplicatedAction;
import eu.compassresearch.ast.actions.AGuardedAction;
import eu.compassresearch.ast.actions.AHidingAction;
import eu.compassresearch.ast.actions.AInterleavingParallelAction;
import eu.compassresearch.ast.actions.AInterleavingReplicatedAction;
import eu.compassresearch.ast.actions.AInternalChoiceAction;
import eu.compassresearch.ast.actions.AInternalChoiceReplicatedAction;
import eu.compassresearch.ast.actions.AInterruptAction;
import eu.compassresearch.ast.actions.AMuAction;
import eu.compassresearch.ast.actions.AParametrisedAction;
import eu.compassresearch.ast.actions.AParametrisedInstantiatedAction;
import eu.compassresearch.ast.actions.AReadCommunicationParameter;
import eu.compassresearch.ast.actions.AReferenceAction;
import eu.compassresearch.ast.actions.AResParametrisation;
import eu.compassresearch.ast.actions.ASequentialCompositionAction;
import eu.compassresearch.ast.actions.ASequentialCompositionReplicatedAction;
import eu.compassresearch.ast.actions.ASignalCommunicationParameter;
import eu.compassresearch.ast.actions.ASkipAction;
import eu.compassresearch.ast.actions.AStartDeadlineAction;
import eu.compassresearch.ast.actions.AStmAction;
import eu.compassresearch.ast.actions.AStopAction;
import eu.compassresearch.ast.actions.ASynchronousParallelismParallelAction;
import eu.compassresearch.ast.actions.ASynchronousParallelismReplicatedAction;
import eu.compassresearch.ast.actions.ATimedInterruptAction;
import eu.compassresearch.ast.actions.ATimeoutAction;
import eu.compassresearch.ast.actions.AUntimedTimeoutAction;
import eu.compassresearch.ast.actions.AValParametrisation;
import eu.compassresearch.ast.actions.AVresParametrisation;
import eu.compassresearch.ast.actions.AWaitAction;
import eu.compassresearch.ast.actions.AWriteCommunicationParameter;
import eu.compassresearch.ast.analysis.intf.ICMLAnswer;
import eu.compassresearch.ast.declarations.AExpressionSingleDeclaration;
import eu.compassresearch.ast.declarations.ATypeSingleDeclaration;
import eu.compassresearch.ast.definitions.AActionDefinition;
import eu.compassresearch.ast.definitions.AActionsDefinition;
import eu.compassresearch.ast.definitions.AChannelNameDefinition;
import eu.compassresearch.ast.definitions.AChannelsDefinition;
import eu.compassresearch.ast.definitions.AChansetDefinition;
import eu.compassresearch.ast.definitions.AChansetsDefinition;
import eu.compassresearch.ast.definitions.AFunctionsDefinition;
import eu.compassresearch.ast.definitions.AInitialDefinition;
import eu.compassresearch.ast.definitions.ALogicalAccess;
import eu.compassresearch.ast.definitions.ANamesetDefinition;
import eu.compassresearch.ast.definitions.ANamesetsDefinition;
import eu.compassresearch.ast.definitions.AOperationsDefinition;
import eu.compassresearch.ast.definitions.AProcessDefinition;
import eu.compassresearch.ast.definitions.ATypesDefinition;
import eu.compassresearch.ast.definitions.AValuesDefinition;
import eu.compassresearch.ast.expressions.ABracketedExp;
import eu.compassresearch.ast.expressions.ACompVarsetExpression;
import eu.compassresearch.ast.expressions.AComprehensionRenameChannelExp;
import eu.compassresearch.ast.expressions.AEnumVarsetExpression;
import eu.compassresearch.ast.expressions.AEnumerationRenameChannelExp;
import eu.compassresearch.ast.expressions.AFatCompVarsetExpression;
import eu.compassresearch.ast.expressions.AFatEnumVarsetExpression;
import eu.compassresearch.ast.expressions.AIdentifierVarsetExpression;
import eu.compassresearch.ast.expressions.AInterVOpVarsetExpression;
import eu.compassresearch.ast.expressions.ANameChannelExp;
import eu.compassresearch.ast.expressions.ASubVOpVarsetExpression;
import eu.compassresearch.ast.expressions.ATupleSelectExp;
import eu.compassresearch.ast.expressions.AUnionVOpVarsetExpression;
import eu.compassresearch.ast.expressions.AUnresolvedPathExp;
import eu.compassresearch.ast.patterns.ARenamePair;
import eu.compassresearch.ast.process.AActionProcess;
import eu.compassresearch.ast.process.AAlphabetisedParallelismProcess;
import eu.compassresearch.ast.process.AAlphabetisedParallelismReplicatedProcess;
import eu.compassresearch.ast.process.AChannelRenamingProcess;
import eu.compassresearch.ast.process.AEndDeadlineProcess;
import eu.compassresearch.ast.process.AExternalChoiceProcess;
import eu.compassresearch.ast.process.AExternalChoiceReplicatedProcess;
import eu.compassresearch.ast.process.AGeneralisedParallelismProcess;
import eu.compassresearch.ast.process.AGeneralisedParallelismReplicatedProcess;
import eu.compassresearch.ast.process.AHidingProcess;
import eu.compassresearch.ast.process.AInstantiationProcess;
import eu.compassresearch.ast.process.AInterleavingProcess;
import eu.compassresearch.ast.process.AInterleavingReplicatedProcess;
import eu.compassresearch.ast.process.AInternalChoiceProcess;
import eu.compassresearch.ast.process.AInternalChoiceReplicatedProcess;
import eu.compassresearch.ast.process.AInterruptProcess;
import eu.compassresearch.ast.process.AReferenceProcess;
import eu.compassresearch.ast.process.ASequentialCompositionProcess;
import eu.compassresearch.ast.process.ASequentialCompositionReplicatedProcess;
import eu.compassresearch.ast.process.ASkipProcess;
import eu.compassresearch.ast.process.AStartDeadlineProcess;
import eu.compassresearch.ast.process.ASynchronousParallelismProcess;
import eu.compassresearch.ast.process.ASynchronousParallelismReplicatedProcess;
import eu.compassresearch.ast.process.ATimedInterruptProcess;
import eu.compassresearch.ast.process.ATimeoutProcess;
import eu.compassresearch.ast.process.AUntimedTimeoutProcess;
import eu.compassresearch.ast.program.AFileSource;
import eu.compassresearch.ast.program.AInputStreamSource;
import eu.compassresearch.ast.program.ATcpStreamSource;
import eu.compassresearch.ast.types.AActionParagraphType;
import eu.compassresearch.ast.types.AActionType;
import eu.compassresearch.ast.types.ABindType;
import eu.compassresearch.ast.types.AChannelType;
import eu.compassresearch.ast.types.AChannelsParagraphType;
import eu.compassresearch.ast.types.AChansetParagraphType;
import eu.compassresearch.ast.types.AChansetType;
import eu.compassresearch.ast.types.AErrorType;
import eu.compassresearch.ast.types.AFunctionParagraphType;
import eu.compassresearch.ast.types.AInitialParagraphType;
import eu.compassresearch.ast.types.ANamesetType;
import eu.compassresearch.ast.types.ANamesetsType;
import eu.compassresearch.ast.types.AOperationParagraphType;
import eu.compassresearch.ast.types.AParagraphType;
import eu.compassresearch.ast.types.AProcessParagraphType;
import eu.compassresearch.ast.types.AProcessType;
import eu.compassresearch.ast.types.ASourceType;
import eu.compassresearch.ast.types.AStateParagraphType;
import eu.compassresearch.ast.types.AStatementType;
import eu.compassresearch.ast.types.ATypeParagraphType;
import eu.compassresearch.ast.types.AValueParagraphType;
import eu.compassresearch.ast.types.AVarsetExpressionType;

public abstract class AbstractCmlDefinitionTypeFinder extends
		DefinitionTypeFinder implements ICMLAnswer<PType>
{

	public AbstractCmlDefinitionTypeFinder(ITypeCheckerAssistantFactory af)
	{
		super(af);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -7520136998582515658L;

	@Override
	public PType caseFile(File node) throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseInputStream(InputStream node) throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAFileSource(AFileSource node) throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseATcpStreamSource(ATcpStreamSource node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAInputStreamSource(AInputStreamSource node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseATypeSingleDeclaration(ATypeSingleDeclaration node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAExpressionSingleDeclaration(
			AExpressionSingleDeclaration node) throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAActionDefinition(AActionDefinition node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAChansetDefinition(AChansetDefinition node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseANamesetDefinition(ANamesetDefinition node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAProcessDefinition(AProcessDefinition node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAChannelNameDefinition(AChannelNameDefinition node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAChannelsDefinition(AChannelsDefinition node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAChansetsDefinition(AChansetsDefinition node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseANamesetsDefinition(ANamesetsDefinition node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAActionsDefinition(AActionsDefinition node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseATypesDefinition(ATypesDefinition node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAOperationsDefinition(AOperationsDefinition node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAFunctionsDefinition(AFunctionsDefinition node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAValuesDefinition(AValuesDefinition node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAInitialDefinition(AInitialDefinition node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseABracketedExp(ABracketedExp node) throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseATupleSelectExp(ATupleSelectExp node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAUnresolvedPathExp(AUnresolvedPathExp node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseANameChannelExp(ANameChannelExp node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAComprehensionRenameChannelExp(
			AComprehensionRenameChannelExp node) throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAEnumerationRenameChannelExp(
			AEnumerationRenameChannelExp node) throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAIdentifierVarsetExpression(
			AIdentifierVarsetExpression node) throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAEnumVarsetExpression(AEnumVarsetExpression node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseACompVarsetExpression(ACompVarsetExpression node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAFatEnumVarsetExpression(AFatEnumVarsetExpression node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAFatCompVarsetExpression(AFatCompVarsetExpression node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAUnionVOpVarsetExpression(AUnionVOpVarsetExpression node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAInterVOpVarsetExpression(AInterVOpVarsetExpression node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseASubVOpVarsetExpression(ASubVOpVarsetExpression node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAStatementType(AStatementType node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAProcessType(AProcessType node) throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAErrorType(AErrorType node) throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseABindType(ABindType node) throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAProcessParagraphType(AProcessParagraphType node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAChansetParagraphType(AChansetParagraphType node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAChannelsParagraphType(AChannelsParagraphType node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAActionParagraphType(AActionParagraphType node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAValueParagraphType(AValueParagraphType node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAFunctionParagraphType(AFunctionParagraphType node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseATypeParagraphType(ATypeParagraphType node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAOperationParagraphType(AOperationParagraphType node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAStateParagraphType(AStateParagraphType node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseASourceType(ASourceType node) throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAChannelType(AChannelType node) throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAChansetType(AChansetType node) throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseANamesetType(ANamesetType node) throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseANamesetsType(ANamesetsType node) throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAInitialParagraphType(AInitialParagraphType node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAActionType(AActionType node) throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAVarsetExpressionType(AVarsetExpressionType node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAParagraphType(AParagraphType node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseALogicalAccess(ALogicalAccess node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseARenamePair(ARenamePair node) throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAActionProcess(AActionProcess node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseASequentialCompositionProcess(
			ASequentialCompositionProcess node) throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAExternalChoiceProcess(AExternalChoiceProcess node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAInternalChoiceProcess(AInternalChoiceProcess node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAGeneralisedParallelismProcess(
			AGeneralisedParallelismProcess node) throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAAlphabetisedParallelismProcess(
			AAlphabetisedParallelismProcess node) throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseASynchronousParallelismProcess(
			ASynchronousParallelismProcess node) throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAInterleavingProcess(AInterleavingProcess node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAInterruptProcess(AInterruptProcess node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseATimedInterruptProcess(ATimedInterruptProcess node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAUntimedTimeoutProcess(AUntimedTimeoutProcess node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseATimeoutProcess(ATimeoutProcess node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAHidingProcess(AHidingProcess node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseASkipProcess(ASkipProcess node) throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAStartDeadlineProcess(AStartDeadlineProcess node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAEndDeadlineProcess(AEndDeadlineProcess node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAInstantiationProcess(AInstantiationProcess node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAReferenceProcess(AReferenceProcess node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAChannelRenamingProcess(AChannelRenamingProcess node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseASequentialCompositionReplicatedProcess(
			ASequentialCompositionReplicatedProcess node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAExternalChoiceReplicatedProcess(
			AExternalChoiceReplicatedProcess node) throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAInternalChoiceReplicatedProcess(
			AInternalChoiceReplicatedProcess node) throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAGeneralisedParallelismReplicatedProcess(
			AGeneralisedParallelismReplicatedProcess node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAAlphabetisedParallelismReplicatedProcess(
			AAlphabetisedParallelismReplicatedProcess node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseASynchronousParallelismReplicatedProcess(
			ASynchronousParallelismReplicatedProcess node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAInterleavingReplicatedProcess(
			AInterleavingReplicatedProcess node) throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseASkipAction(ASkipAction node) throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAStopAction(AStopAction node) throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAChaosAction(AChaosAction node) throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseADivAction(ADivAction node) throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAWaitAction(AWaitAction node) throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseACommunicationAction(ACommunicationAction node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAGuardedAction(AGuardedAction node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseASequentialCompositionAction(
			ASequentialCompositionAction node) throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAExternalChoiceAction(AExternalChoiceAction node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAInternalChoiceAction(AInternalChoiceAction node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAInterruptAction(AInterruptAction node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseATimedInterruptAction(ATimedInterruptAction node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAUntimedTimeoutAction(AUntimedTimeoutAction node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseATimeoutAction(ATimeoutAction node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAHidingAction(AHidingAction node) throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAStartDeadlineAction(AStartDeadlineAction node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAEndDeadlineAction(AEndDeadlineAction node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAChannelRenamingAction(AChannelRenamingAction node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAMuAction(AMuAction node) throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAParametrisedAction(AParametrisedAction node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAReferenceAction(AReferenceAction node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAInterleavingParallelAction(
			AInterleavingParallelAction node) throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAGeneralisedParallelismParallelAction(
			AGeneralisedParallelismParallelAction node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAAlphabetisedParallelismParallelAction(
			AAlphabetisedParallelismParallelAction node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseASynchronousParallelismParallelAction(
			ASynchronousParallelismParallelAction node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseASequentialCompositionReplicatedAction(
			ASequentialCompositionReplicatedAction node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAExternalChoiceReplicatedAction(
			AExternalChoiceReplicatedAction node) throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAInternalChoiceReplicatedAction(
			AInternalChoiceReplicatedAction node) throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseACommonInterleavingReplicatedAction(
			ACommonInterleavingReplicatedAction node) throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAInterleavingReplicatedAction(
			AInterleavingReplicatedAction node) throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAGeneralisedParallelismReplicatedAction(
			AGeneralisedParallelismReplicatedAction node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAAlphabetisedParallelismReplicatedAction(
			AAlphabetisedParallelismReplicatedAction node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseASynchronousParallelismReplicatedAction(
			ASynchronousParallelismReplicatedAction node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseADeclarationInstantiatedAction(
			ADeclarationInstantiatedAction node) throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAParametrisedInstantiatedAction(
			AParametrisedInstantiatedAction node) throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAReadCommunicationParameter(
			AReadCommunicationParameter node) throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAWriteCommunicationParameter(
			AWriteCommunicationParameter node) throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseASignalCommunicationParameter(
			ASignalCommunicationParameter node) throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAValParametrisation(AValParametrisation node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAResParametrisation(AResParametrisation node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAVresParametrisation(AVresParametrisation node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAStmAction(AStmAction node) throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAUnresolvedStateDesignator(AUnresolvedStateDesignator node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAActionStm(AActionStm node) throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseANewStm(ANewStm node) throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAIfNonDeterministicStm(AIfNonDeterministicStm node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseAAltNonDeterministicStm(AAltNonDeterministicStm node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PType caseADoNonDeterministicStm(ADoNonDeterministicStm node)
			throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}

}
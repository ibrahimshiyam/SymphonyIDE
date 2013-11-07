package eu.compassresearch.core.typechecker.visitors;

import static eu.compassresearch.core.typechecker.assistant.TypeCheckerUtil.findDefinition;
import static eu.compassresearch.core.typechecker.assistant.TypeCheckerUtil.setType;
import static eu.compassresearch.core.typechecker.assistant.TypeCheckerUtil.setTypeVoid;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.analysis.QuestionAnswerAdaptor;
import org.overture.ast.analysis.intf.IQuestionAnswer;
import org.overture.ast.definitions.ALocalDefinition;
import org.overture.ast.definitions.PDefinition;
import org.overture.ast.expressions.PExp;
import org.overture.ast.factory.AstFactory;
import org.overture.ast.intf.lex.ILexIdentifierToken;
import org.overture.ast.intf.lex.ILexNameToken;
import org.overture.ast.node.INode;
import org.overture.ast.patterns.PPattern;
import org.overture.ast.statements.PStm;
import org.overture.ast.typechecker.NameScope;
import org.overture.ast.types.ABooleanBasicType;
import org.overture.ast.types.AIntNumericBasicType;
import org.overture.ast.types.ANatNumericBasicType;
import org.overture.ast.types.AProductType;
import org.overture.ast.types.ASetType;
import org.overture.ast.types.PType;
import org.overture.typechecker.Environment;
import org.overture.typechecker.FlatCheckedEnvironment;
import org.overture.typechecker.TypeCheckException;
import org.overture.typechecker.TypeCheckInfo;
import org.overture.typechecker.TypeChecker;
import org.overture.typechecker.TypeComparator;
import org.overture.typechecker.assistant.definition.PDefinitionAssistantTC;
import org.overture.typechecker.assistant.pattern.PPatternAssistantTC;

import eu.compassresearch.ast.CmlAstFactory;
import eu.compassresearch.ast.actions.AAlphabetisedParallelismParallelAction;
import eu.compassresearch.ast.actions.ACallAction;
import eu.compassresearch.ast.actions.AChannelRenamingAction;
import eu.compassresearch.ast.actions.AChaosAction;
import eu.compassresearch.ast.actions.ACommonInterleavingReplicatedAction;
import eu.compassresearch.ast.actions.ACommunicationAction;
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
import eu.compassresearch.ast.actions.AVresParametrisation;
import eu.compassresearch.ast.actions.AWaitAction;
import eu.compassresearch.ast.actions.AWriteCommunicationParameter;
import eu.compassresearch.ast.actions.PAction;
import eu.compassresearch.ast.actions.PCommunicationParameter;
import eu.compassresearch.ast.actions.PParametrisation;
import eu.compassresearch.ast.analysis.QuestionAnswerCMLAdaptor;
import eu.compassresearch.ast.declarations.AExpressionSingleDeclaration;
import eu.compassresearch.ast.declarations.ATypeSingleDeclaration;
import eu.compassresearch.ast.declarations.PSingleDeclaration;
import eu.compassresearch.ast.definitions.AActionDefinition;
import eu.compassresearch.ast.definitions.AChannelDefinition;
import eu.compassresearch.ast.expressions.PVarsetExpression;
import eu.compassresearch.ast.lex.CmlLexNameToken;
import eu.compassresearch.ast.types.AChannelType;
import eu.compassresearch.core.typechecker.api.ITypeIssueHandler;
import eu.compassresearch.core.typechecker.api.TypeErrorMessages;
import eu.compassresearch.core.typechecker.api.TypeWarningMessages;
import eu.compassresearch.core.typechecker.assistant.TypeCheckerUtil;

public class CmlActionTypeChecker extends
		QuestionAnswerCMLAdaptor<TypeCheckInfo, PType>
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final ITypeIssueHandler issueHandler;

	/**
	 * Type checker for var set expressions used for channel sets
	 */
	private final QuestionAnswerAdaptor<TypeCheckInfo, PType> channelSetChecker;
	/**
	 * Type checker for var set expressions used for name sets
	 */
	private final QuestionAnswerAdaptor<TypeCheckInfo, PType> nameSetChecker;

	@SuppressWarnings("deprecation")
	public CmlActionTypeChecker(IQuestionAnswer<TypeCheckInfo, PType> root,
			ITypeIssueHandler issueHandler,
			QuestionAnswerAdaptor<TypeCheckInfo, PType> channelSetChecker,
			QuestionAnswerAdaptor<TypeCheckInfo, PType> nameSetChecker)
	{
		super(root);
		this.issueHandler = issueHandler;
		this.channelSetChecker = channelSetChecker;
		this.nameSetChecker = nameSetChecker;
	}

	/**
	 * Case to handle statements embedded in actions
	 */
	public PType caseAStmAction(AStmAction node, TypeCheckInfo question)
			throws AnalysisException
	{
		PStm stm = node.getStatement();
		node.setType(stm.apply(THIS, question));
		return node.getType();
	}

	@Override
	public PType caseACallAction(ACallAction node, TypeCheckInfo question)
			throws AnalysisException
	{
		// FIXME not implemented
		return AstFactory.newAVoidType(node.getLocation());
	}

	@Override
	public PType caseAUntimedTimeoutAction(AUntimedTimeoutAction node,
			TypeCheckInfo question) throws AnalysisException
	{
		PType leftType = node.getLeft().apply(THIS, question);

		PType rightType = node.getRight().apply(THIS, question);

		return setType(node, leftType, rightType);
	}

	@Override
	public PType caseATimeoutAction(ATimeoutAction node, TypeCheckInfo question)
			throws AnalysisException
	{

		PAction left = node.getLeft();
		PAction right = node.getRight();
		PExp timedExp = node.getTimeoutExpression();

		PType leftType = left.apply(THIS, question);

		PType rightType = right.apply(THIS, question);

		PType timedExpType = timedExp.apply(THIS, question);

		if (!TypeComparator.isSubType(timedExpType, new AIntNumericBasicType()))
		{
			issueHandler.addTypeError(timedExp, TypeErrorMessages.TIME_UNIT_EXPRESSION_MUST_BE_NAT, timedExp
					+ "", timedExpType + "");
		}

		return TypeCheckerUtil.setType(node, leftType, rightType);
	}

	@Override
	public PType caseAInternalChoiceReplicatedAction(
			AInternalChoiceReplicatedAction node, TypeCheckInfo question)
			throws AnalysisException
	{

		PAction repAction = node.getReplicatedAction();
		LinkedList<PSingleDeclaration> repDecl = node.getReplicationDeclaration();

		List<PDefinition> defs = new Vector<PDefinition>();

		for (PSingleDeclaration d : repDecl)
		{
			PType type = d.apply(THIS, question);

			for (PDefinition newDef : type.getDefinitions())
			{
				defs.add(newDef);
			}

		}

		PType actionType = repAction.apply(THIS, question.newScope(defs));

		return TypeCheckerUtil.setType(node, actionType);

	}

	@Override
	public PType caseAGeneralisedParallelismReplicatedAction(
			AGeneralisedParallelismReplicatedAction node, TypeCheckInfo question)
			throws AnalysisException
	{

		// TODO RWL: What is the semantics of this?
		PVarsetExpression csexp = node.getChansetExpression();
		PType csexpType = csexp.apply(channelSetChecker, question);

		if (csexpType == null)
		{
			issueHandler.addTypeError(node, TypeErrorMessages.EXPECTED_A_CHANNELSET, ""
					+ csexpType);
			return null;
		}

		List<PDefinition> defs = new Vector<PDefinition>();

		for (PDefinition chanDef : csexpType.getDefinitions())
		{
			if (!(chanDef instanceof AChannelDefinition))
			{
				issueHandler.addTypeError(node, TypeErrorMessages.TYPE_CHECK_INTERNAL_FAILURE, "Expected a Channel and got something of type AChannelType, however it is not AChannelDefinition.");
				return null;
			}
			AChannelDefinition chanNameDef = (AChannelDefinition) chanDef;
			defs.add(chanNameDef);
		}

		PVarsetExpression sexp = node.getNamesetExpression();
		PType sexpType = sexp.apply(nameSetChecker, question);

		if (sexpType == null)
		{
			issueHandler.addTypeError(node, TypeErrorMessages.EXPECTED_A_NAMESET, ""
					+ sexpType);
			return null;
		}

		for (PDefinition stateDef : sexpType.getDefinitions())
		{
			defs.add(stateDef);
		}

		PAction repAction = node.getReplicatedAction();

		LinkedList<PSingleDeclaration> repDecls = node.getReplicationDeclaration();

		for (PSingleDeclaration decl : repDecls)
		{

			if (decl instanceof AExpressionSingleDeclaration)
			{
				AExpressionSingleDeclaration singleDecl = (AExpressionSingleDeclaration) decl;
				PExp exp = singleDecl.getExpression();
				PType expType = exp.apply(THIS, question);

				if (expType instanceof ASetType)
				{
					ASetType st = (ASetType) expType;
					expType = st.getSetof();
				}

				CmlLexNameToken name = new CmlLexNameToken("", singleDecl.getIdentifier());

				ALocalDefinition def = AstFactory.newALocalDefinition(name.getLocation(), name, NameScope.LOCAL, expType);
				defs.add(def);
			}

			if (decl instanceof ATypeSingleDeclaration)
			{
				ATypeSingleDeclaration singleDecl = (ATypeSingleDeclaration) decl;
				CmlLexNameToken name = new CmlLexNameToken("", singleDecl.getIdentifier());
				ALocalDefinition def = AstFactory.newALocalDefinition(name.getLocation(), name, singleDecl.getNameScope(), singleDecl.getType());
				defs.add(def);
			}
		}

		PType repActionType = repAction.apply(THIS, question.newScope(defs, NameScope.NAMESANDANYSTATE));// new
																											// TypeCheckInfo(question.assistantFactory,
																											// env));

		issueHandler.addTypeWarning(node, TypeWarningMessages.INCOMPLETE_TYPE_CHECKING, ""
				+ node);

		return TypeCheckerUtil.setType(node, repActionType);
	}

	@Override
	public PType caseAExternalChoiceReplicatedAction(
			AExternalChoiceReplicatedAction node, TypeCheckInfo question)
			throws AnalysisException
	{

		PAction action = node.getReplicatedAction();
		LinkedList<PSingleDeclaration> decl = node.getReplicationDeclaration();

		List<PDefinition> defs = new Vector<PDefinition>();

		for (PSingleDeclaration d : decl)
		{
			PType declType = d.apply(THIS, question);
			for (PDefinition def : declType.getDefinitions())
			{
				defs.add(def);
			}
		}

		PType actionType = action.apply(THIS, question.newScope(defs));

		return TypeCheckerUtil.setType(node, actionType);
	}

	@Override
	public PType caseAInterruptAction(AInterruptAction node,
			TypeCheckInfo question) throws AnalysisException
	{
		// extract sub-stuff
		PAction leftAction = node.getLeft();
		PAction rightAction = node.getRight();

		// type-check sub-actions
		PType leftActionType = leftAction.apply(THIS, question);

		PType rightActionType = rightAction.apply(THIS, question);

		// All done!
		return TypeCheckerUtil.setType(node, leftActionType, rightActionType);
	}

	@Override
	public PType caseAInterleavingParallelAction(
			AInterleavingParallelAction node, TypeCheckInfo question)
			throws AnalysisException
	{
		// extract sub-stuff
		PAction leftAction = node.getLeftAction();
		PVarsetExpression leftNamesetExp = node.getLeftNamesetExpression();
		PAction rightAction = node.getRightAction();
		PVarsetExpression rightnamesetExp = node.getRightNamesetExpression();

		// type-check sub-actions
		PType leftActionType = leftAction.apply(THIS, question);

		PType rightActionType = rightAction.apply(THIS, question);

		// type-check the namesets
		if (leftNamesetExp != null)
		{
			leftNamesetExp.apply(nameSetChecker, question);

		}

		if (rightnamesetExp != null)
		{
			rightnamesetExp.apply(nameSetChecker, question);
		}

		// All done!
		return TypeCheckerUtil.setType(node, leftActionType, rightActionType);
	}

	@Override
	public PType caseAGeneralisedParallelismParallelAction(
			AGeneralisedParallelismParallelAction node, TypeCheckInfo question)
			throws AnalysisException
	{

		// Extract sub-stuff
		PVarsetExpression chansetExp = node.getChansetExpression();
		PAction leftAction = node.getLeftAction();
		PVarsetExpression leftNamesetExp = node.getLeftNamesetExpression();
		PAction rightAction = node.getRightAction();
		PVarsetExpression rightnamesetExp = node.getLeftNamesetExpression();

		// type-check sub-actions
		PType leftActionType = leftAction.apply(THIS, question);

		PType rightActionType = rightAction.apply(THIS, question);

		// type-check the chanset
		chansetExp.apply(channelSetChecker, question);

		// type-check the namesets
		if (leftNamesetExp != null)
		{
			leftNamesetExp.apply(nameSetChecker, question);
		}

		if (rightnamesetExp != null)
		{
			rightnamesetExp.apply(nameSetChecker, question);
		}
		// All done!
		return TypeCheckerUtil.setType(node, leftActionType, rightActionType);
	}

	@Override
	public PType caseAChannelRenamingAction(AChannelRenamingAction node,
			org.overture.typechecker.TypeCheckInfo question)
			throws AnalysisException
	{

		// PAction action = node.getAction();
		//
		// SRenameChannelExp renameExp = node.getRenameExpression();

		// FIXME throw new InternalException(0, "caseAChannelRenamingAction not implemented");
		return setTypeVoid(node);
		// return new AActionType(node.getLocation(), true);
	}

	@Override
	public PType caseAWaitAction(AWaitAction node,
			org.overture.typechecker.TypeCheckInfo question)
			throws AnalysisException
	{

		PExp timedExp = node.getExpression();
		PType timedExpType = timedExp.apply(THIS, question);

		if (!TypeComparator.isSubType(timedExpType, new AIntNumericBasicType()))
		{
			issueHandler.addTypeError(timedExpType, TypeErrorMessages.TIME_UNIT_EXPRESSION_MUST_BE_NAT, timedExp
					+ "", timedExp + " (" + timedExpType + ")");
			return null;
		}

		return TypeCheckerUtil.setType(node, AstFactory.newAVoidType(node.getLocation()));
	}

	@Override
	public PType caseAMuAction(AMuAction node,
			org.overture.typechecker.TypeCheckInfo question)
			throws AnalysisException
	{

		// extract elements from the node
		LinkedList<ILexIdentifierToken> ids = node.getIdentifiers();
		LinkedList<PAction> acts = node.getActions();

		// too many identifiers
		if (ids.size() > acts.size())
		{
			for (int i = acts.size(); i < ids.size(); i++)
			{
				issueHandler.addTypeError(ids.get(i), TypeErrorMessages.IDENTIFIER_IS_MISSING_ACTION_DEFINITION, ids.get(i).getName());
			}
		} else if (ids.size() < acts.size())
		{
			for (int i = ids.size(); i < acts.size(); i++)
			{
				issueHandler.addTypeWarning(acts.get(i), TypeWarningMessages.UNREACHABLE_DEFINITION);
			}
		}

		List<PDefinition> local = new Vector<PDefinition>();

		// add IDs to the environment
		for (ILexIdentifierToken id : ids)
		{
			local.add(CmlAstFactory.newAActionDefinition(id, null));
		}

		TypeCheckInfo info = new TypeCheckInfo(question.assistantFactory, new FlatCheckedEnvironment(question.assistantFactory, local, question.env, NameScope.LOCAL), question.scope);
		// check the actions
		List<PType> types = new Vector<PType>();
		for (PAction act : acts)
		{
			PType actType = act.apply(THIS, info);
			types.add(actType);
		}

		return TypeCheckerUtil.setType(node, types);
	}

	@Override
	public PType caseAChaosAction(AChaosAction node,
			org.overture.typechecker.TypeCheckInfo question)
			throws AnalysisException
	{
		return TypeCheckerUtil.setTypeVoid(node);
	}

	@Override
	public PType caseATimedInterruptAction(ATimedInterruptAction node,
			org.overture.typechecker.TypeCheckInfo question)
			throws AnalysisException
	{

		PAction left = node.getLeft();
		PAction right = node.getRight();
		PExp timeExp = node.getTimeExpression();

		PType leftType = left.apply(THIS, question);

		PType rightType = right.apply(THIS, question);

		PType timeExpType = timeExp.apply(THIS, question);

		if (!TypeComparator.isSubType(timeExpType, new ANatNumericBasicType()))
		{
			issueHandler.addTypeError(timeExp, TypeErrorMessages.TIME_UNIT_EXPRESSION_MUST_BE_NAT, timeExp
					+ "", timeExpType + "");

		}

		return setType(node, leftType, rightType);
	}

	@Override
	public PType caseASequentialCompositionReplicatedAction(
			ASequentialCompositionReplicatedAction node,
			org.overture.typechecker.TypeCheckInfo question)
			throws AnalysisException
	{

		PAction replicatedAction = node.getReplicatedAction();
		LinkedList<PSingleDeclaration> decls = node.getReplicationDeclaration();

		List<PDefinition> localDefinitions = new Vector<PDefinition>();
		Environment repActionEnv = new FlatCheckedEnvironment(question.assistantFactory, localDefinitions, question.env, NameScope.NAMES);

		for (PSingleDeclaration decl : decls)
		{
			PType declType = decl.apply(THIS, question);

			if (declType instanceof ASetType)
			{
				issueHandler.addTypeError(declType, TypeErrorMessages.SEQ_TYPE_EXPECTED, decl
						+ "", declType + "");
				return null;
			}

			for (PDefinition def : declType.getDefinitions())
			{
				localDefinitions.add(def);
			}
		}

		PType replicatedActionType = replicatedAction.apply(THIS, new TypeCheckInfo(question.assistantFactory, repActionEnv, NameScope.NAMES));

		return setType(node, replicatedActionType);
	}

	@Override
	public PType caseAAlphabetisedParallelismParallelAction(
			AAlphabetisedParallelismParallelAction node,
			org.overture.typechecker.TypeCheckInfo question)
			throws AnalysisException
	{

		PAction leftAction = node.getLeftAction();
		PVarsetExpression leftChanSet = node.getLeftChansetExpression();
		PVarsetExpression leftNameSet = node.getLeftNamesetExpression();

		PAction rightAction = node.getRightAction();
		PVarsetExpression rightChanSet = node.getRightChansetExpression();
		PVarsetExpression rightNameSet = node.getLeftNamesetExpression();

		PType leftActionType = leftAction.apply(THIS, question);

		leftChanSet.apply(channelSetChecker, question);

		leftNameSet.apply(nameSetChecker, question);

		PType rightActionType = rightAction.apply(THIS, question);

		rightChanSet.apply(channelSetChecker, question);

		rightNameSet.apply(nameSetChecker, question);

		return setType(node, leftActionType, rightActionType);
	}

	@Override
	public PType caseAGuardedAction(AGuardedAction node, TypeCheckInfo question)
			throws AnalysisException
	{

		PExp exp = node.getExpression();
		PAction action = node.getAction();

		PType expType = exp.apply(THIS, question);

		if (!TypeComparator.isSubType(expType, AstFactory.newABooleanBasicType(node.getLocation())))
		{
			issueHandler.addTypeError(exp, TypeErrorMessages.INCOMPATIBLE_TYPE, "bool", ""
					+ expType);
			return null;
		}

		PType actionType = action.apply(THIS, question);

		if (!(action instanceof PAction || action instanceof PStm))
		{
			issueHandler.addTypeError(action, TypeErrorMessages.EXPECTED_AN_ACTION_OR_OPERATION, ""
					+ action);
			return null;
		}

		return setType(node, actionType);

	}

	@Override
	public PType caseADivAction(ADivAction node, TypeCheckInfo question)
			throws AnalysisException
	{

		return setTypeVoid(node);
	}

	@Override
	public PType caseACommonInterleavingReplicatedAction(
			ACommonInterleavingReplicatedAction node, TypeCheckInfo question)
			throws AnalysisException
	{

		PVarsetExpression namesetExp = node.getNamesetExpression();
		PAction repAction = node.getReplicatedAction();
		LinkedList<PSingleDeclaration> decls = node.getReplicationDeclaration();

		namesetExp.apply(nameSetChecker, question);

		List<PDefinition> defs = new Vector<PDefinition>();

		for (PSingleDeclaration decl : decls)
		{
			PType declType = decl.apply(THIS, question);

			for (PDefinition def : declType.getDefinitions())
			{
				defs.add(def);
			}
		}

		PType repActionType = repAction.apply(THIS, question.newScope(defs));

		return setType(node, repActionType);
	}

	@Override
	public PType caseAInterleavingReplicatedAction(
			AInterleavingReplicatedAction node, TypeCheckInfo question)
			throws AnalysisException
	{

		PVarsetExpression namesetExp = node.getNamesetExpression();
		PAction repAction = node.getReplicatedAction();
		LinkedList<PSingleDeclaration> decls = node.getReplicationDeclaration();

		namesetExp.apply(nameSetChecker, question);

		List<PDefinition> defs = new Vector<PDefinition>();

		for (PSingleDeclaration decl : decls)
		{
			PType declType = decl.apply(THIS, question);

			for (PDefinition def : declType.getDefinitions())
			{
				defs.add(def);
			}
		}

		PType repActionType = repAction.apply(THIS, question.newScope(defs));

		return setType(node, repActionType);
	}

	@Override
	public PType caseASynchronousParallelismReplicatedAction(
			ASynchronousParallelismReplicatedAction node, TypeCheckInfo question)
			throws AnalysisException
	{

		PVarsetExpression namesetExp = node.getNamesetExpression();
		PAction repAction = node.getReplicatedAction();
		LinkedList<PSingleDeclaration> decls = node.getReplicationDeclaration();

		namesetExp.apply(nameSetChecker, question);

		List<PDefinition> defs = new Vector<PDefinition>();

		for (PSingleDeclaration decl : decls)
		{
			PType declType = decl.apply(THIS, question);

			for (PDefinition def : declType.getDefinitions())
			{
				defs.add(def);
			}
		}

		PType repActionType = repAction.apply(THIS, question.newScope(defs));

		return setType(node, repActionType);
	}

	@Override
	public PType caseAInternalChoiceAction(AInternalChoiceAction node,
			org.overture.typechecker.TypeCheckInfo question)
			throws AnalysisException
	{

		PAction left = node.getLeft();
		PAction right = node.getRight();

		PType leftType = left.apply(THIS, question);

		PType rightType = right.apply(THIS, question);

		return setType(node, leftType, rightType);
	}

	@SuppressWarnings("static-access")
	@Override
	public PType caseAReferenceAction(AReferenceAction node,
			org.overture.typechecker.TypeCheckInfo question)
			throws AnalysisException
	{

		PDefinition def = findDefinition(node.getName().getIdentifier(), question.env);

		if (def == null)
		{
			issueHandler.addTypeError(node, TypeErrorMessages.UNDEFINED_SYMBOL, node.getName()
					+ "");
			node.setType(AstFactory.newAUnknownType(node.getLocation()));
			return node.getType();
		}

		PType type = null;

		if (!(def instanceof AActionDefinition))
		{
			issueHandler.addTypeError(node, TypeErrorMessages.EXPECTED_AN_ACTION, " a "
					+ question.assistantFactory.createPDefinitionAssistant().kind(def)
					+ " deinition:" + node.getName());

		} else
		{

			AActionDefinition actionDef = (AActionDefinition) def;
			node.setActionDefinition(actionDef);
		}

		if (type == null)

		{
			type = AstFactory.newAVoidType(node.getLocation());
		}

		node.setType(type);
		return node.getType();
	}

	@SuppressWarnings("deprecation")
	@Override
	public PType caseACommunicationAction(ACommunicationAction node,
			org.overture.typechecker.TypeCheckInfo question)
			throws AnalysisException
	{
		AChannelType chanType = null;

		PDefinition channel = findDefinition(node.getIdentifier(), question.env);

		// There should be a channel defined with this name
		if (null == channel)
		{
			issueHandler.addTypeError(node, TypeErrorMessages.CHANNEL_NOT_DECLARED, node.getIdentifier().getName());
		} else if (!(channel instanceof AChannelDefinition))
		{
			issueHandler.addTypeError(channel, TypeErrorMessages.DEFINITION_X_BUT_FOUND_Y, "channel", PDefinitionAssistantTC.kind(channel), channel.getName().getName()
					+ "");
		} else
		{
			chanType = ((AChannelDefinition) channel).getType();
		}

		if (chanType == null)
		{
			chanType = new AChannelType(node.getLocation(), true, new Vector<PType>());
		}

		List<PDefinition> localDefinitions = new Vector<PDefinition>();
		Environment local = new FlatCheckedEnvironment(question.assistantFactory, localDefinitions, question.env, NameScope.NAMES);
		TypeCheckInfo info = new TypeCheckInfo(question.assistantFactory, local, NameScope.NAMESANDSTATE);

		LinkedList<PCommunicationParameter> commParams = node.getCommunicationParameters();

		if (chanType.getParameters().size() > commParams.size())
		{
			issueHandler.addTypeError(node.getIdentifier(), TypeErrorMessages.COMMUNICATION_TOO_FEW_ARGUMENTS, node.getIdentifier().getName(), ""
					+ commParams.size(), "" + chanType.getParameters().size());
		} else if (chanType.getParameters().size() < commParams.size())
		{
			issueHandler.addTypeError(node.getIdentifier(), TypeErrorMessages.COMMUNICATION_TOO_MANY_ARGUMENTS, node.getIdentifier().getName(), ""
					+ commParams.size(), "" + chanType.getParameters().size());
		}

		if (chanType.getParameters().isEmpty() && !commParams.isEmpty())
		{
			issueHandler.addTypeError(node.getIdentifier(), TypeErrorMessages.COMMUNICATION_NOT_ALLOWED_OVER_UNTYPED_CHANNEL, node.getIdentifier().getName());
		}

		PType expectedType = null;
		PType actualType = null;
		for (int i = 0; i < commParams.size(); i++)
		{
			if (chanType.getParameters().size() > i)
			{
				expectedType = chanType.getParameters().get(i);
			} else
			{
				expectedType = AstFactory.newAUnknownType(chanType.getLocation());
			}
			actualType = null;
			List<ILexNameToken> names = new Vector<ILexNameToken>();

			PCommunicationParameter commParam = commParams.get(i);

			if (commParam instanceof AReadCommunicationParameter)
			{
				AReadCommunicationParameter readParam = (AReadCommunicationParameter) commParam;
				PPattern p = readParam.getPattern();

				PPatternAssistantTC.typeResolve(p, THIS, question);

				actualType = PPatternAssistantTC.getPossibleType(p);

				names.addAll(PPatternAssistantTC.getVariableNames(p));

			} else if (commParam instanceof AWriteCommunicationParameter
					|| commParam instanceof ASignalCommunicationParameter)
			{
				if (commParam.getExpression() != null)
				{
					actualType = commParam.getExpression().apply(THIS, info);
				} else
				{
					issueHandler.addTypeError(commParam, TypeErrorMessages.COMMUNICATION_PARAMETER_MISSING, ""
							+ i, "" + expectedType);
					actualType = AstFactory.newAUnknownType(node.getLocation());
				}
			}

			// Type check parameter
			if (!TypeComparator.compatible(expectedType, actualType))
			{
				issueHandler.addTypeError(commParam, TypeErrorMessages.COMMUNICATION_PARAMETER_TYPE_NOT_COMPATIBLE, ""
						+ actualType, "" + i, "" + expectedType);

			}

			// Set the type to the expected one. If it was a write/signal parm this doesn't matter otherwise this gives
			// a better check downstream
			actualType = expectedType;

			// finally add the parameter to the available definitions for the -> action and constraint if the commparm
			// was a read
			if (!names.isEmpty())
			{
				List<PType> localTypes = new Vector<PType>();
				if (actualType instanceof AProductType)
				{
					localTypes.addAll(((AProductType) actualType).getTypes());
				} else
				{
					localTypes.add(actualType);
				}

				for (int j = 0; j < names.size(); j++)
				{
					PType t = null;
					if (localTypes.size() > j)
					{
						t = localTypes.get(j);
					} else
					{
						t = AstFactory.newAUnknownType(actualType.getLocation());
					}
					localDefinitions.add(AstFactory.newALocalDefinition(names.get(j).getLocation(), names.get(j), NameScope.LOCAL, t));
				}

				PExp constraintExp = commParam.getExpression();
				if (constraintExp != null)
				{
					PType constraintType = constraintExp.apply(THIS, new TypeCheckInfo(question.assistantFactory, local, NameScope.NAMESANDSTATE));

					if (!(constraintType instanceof ABooleanBasicType))
					{
						issueHandler.addTypeError(constraintExp, TypeErrorMessages.CONSTRAINT_MUST_BE_A_BOOLEAN_EXPRESSION, constraintExp.toString());
					}
				}
			}
		}

		PType commType = node.getAction().apply(this, info);

		return setType(node, commType);
	}

	@Override
	public PType caseASequentialCompositionAction(
			ASequentialCompositionAction node,
			org.overture.typechecker.TypeCheckInfo question)
			throws AnalysisException
	{

		PType leftType = node.getLeft().apply(THIS, question);
		PType rightType = node.getRight().apply(THIS, question);

		return setType(node, leftType, rightType);
	}

	@Override
	public PType caseASkipAction(ASkipAction node,
			org.overture.typechecker.TypeCheckInfo question)
			throws AnalysisException
	{
		return setTypeVoid(node);
	}

	@Override
	public PType caseAExternalChoiceAction(AExternalChoiceAction node,
			org.overture.typechecker.TypeCheckInfo question)
			throws AnalysisException
	{

		PAction left = node.getLeft();
		PAction right = node.getRight();

		PType leftType = left.apply(THIS, question);

		PType rightType = right.apply(THIS, question);

		return setType(node, leftType, rightType);
	}

	@Override
	public PType caseAHidingAction(AHidingAction node,
			org.overture.typechecker.TypeCheckInfo question)
			throws AnalysisException
	{

		PAction action = node.getLeft();
		PVarsetExpression chanSet = node.getChansetExpression();

		PType actionType = action.apply(THIS, question);

		chanSet.apply(channelSetChecker, question);

		return setType(node, actionType);
	}

	@Override
	public PType defaultPParametrisation(PParametrisation node,
			TypeCheckInfo question) throws AnalysisException
	{
		ALocalDefinition decl = node.getDeclaration();

		try
		{
			return question.assistantFactory.createPTypeAssistant().typeResolve(decl.getType(), null, THIS, question);
		} catch (TypeCheckException te)
		{
			TypeChecker.report(3427, te.getMessage(), te.location);
		}

		return null;
	}

	@Override
	public PType caseAVresParametrisation(AVresParametrisation node,
			TypeCheckInfo question) throws AnalysisException
	{
		return defaultPParametrisation(node, question);
	}

	@Override
	public PType caseAParametrisedInstantiatedAction(
			AParametrisedInstantiatedAction node, TypeCheckInfo question)
			throws AnalysisException
	{

		AParametrisedAction action = node.getAction();
		LinkedList<PExp> args = node.getArgs();

		List<PDefinition> defs = new Vector<PDefinition>();

		LinkedList<PParametrisation> parameterNames = node.getAction().getParametrisations();
		int i = 0;
		for (PExp exp : args)
		{
			PType expType = exp.apply(THIS, question);

			if (i > parameterNames.size())
			{
				continue;
			}
			PParametrisation pa = parameterNames.get(i++);
			pa.apply(THIS, question);
			ALocalDefinition localDef = pa.getDeclaration();
			defs.add(localDef);
		}

		PType actionType = action.apply(THIS, question.newScope(defs));

		return setType(node, actionType);
	}

	@Override
	public PType caseAStartDeadlineAction(AStartDeadlineAction node,
			org.overture.typechecker.TypeCheckInfo question)
			throws AnalysisException
	{

		PAction event = node.getLeft();
		PExp timeExp = node.getExpression();

		PType type = event.apply(THIS, question);

		PType expType = timeExp.apply(THIS, question);

		if (!(TypeComparator.isSubType(expType, new ANatNumericBasicType())))
		{
			issueHandler.addTypeError(timeExp, TypeErrorMessages.TIME_UNIT_EXPRESSION_MUST_BE_NAT, timeExp
					+ "", expType + "");
		}
		return setType(node, type);
	}

	@Override
	public PType caseAEndDeadlineAction(AEndDeadlineAction node,
			org.overture.typechecker.TypeCheckInfo question)
			throws AnalysisException
	{

		PAction event = node.getLeft();
		PExp timeExp = node.getExpression();

		PType eventType = event.apply(THIS, question);

		PType timeExpType = timeExp.apply(THIS, question);

		if (!(TypeComparator.isSubType(timeExpType, new ANatNumericBasicType())))
		{
			issueHandler.addTypeError(timeExp, TypeErrorMessages.TIME_UNIT_EXPRESSION_MUST_BE_NAT, timeExp
					+ "", timeExpType + "");
		}

		return setType(node, eventType);
	}

	@Override
	public PType caseAStopAction(AStopAction node,
			org.overture.typechecker.TypeCheckInfo question)
			throws AnalysisException
	{
		return setTypeVoid(node);
	}

	@Override
	public PType caseASynchronousParallelismParallelAction(
			ASynchronousParallelismParallelAction node,
			org.overture.typechecker.TypeCheckInfo question)
			throws AnalysisException
	{

		PAction leftAction = node.getLeftAction();
		PVarsetExpression leftNameSet = node.getLeftNamesetExpression();

		PAction rightAction = node.getRightAction();
		PVarsetExpression rightNameSet = node.getLeftNamesetExpression();

		PType leftActionType = leftAction.apply(THIS, question);

		if (leftNameSet != null)
		{
			leftNameSet.apply(nameSetChecker, question);

		}

		PType rightActionType = rightAction.apply(THIS, question);

		if (rightNameSet != null)
		{
			rightNameSet.apply(nameSetChecker, question);

		}
		return setType(node, leftActionType, rightActionType);
	}

	@Override
	public PType caseAParametrisedAction(AParametrisedAction node,
			TypeCheckInfo question) throws AnalysisException
	{
		// FIXME what does this node represent: AParametrisedAction
		PAction action = node.getAction();

		// Params are already added to the environment above as we have the
		// defining expressions there !
		// at least in the case of caseAParametrisedInstantiatedAction. See how
		// it is done there if your are in trouble
		// with this guy.
		LinkedList<PParametrisation> params = node.getParametrisations();

		PType actionType = action.apply(THIS, question);

		return setType(node, actionType);
	}

	public PType createNewReturnValue(INode node, TypeCheckInfo question)
			throws AnalysisException
	{
		return null;
	}

	@Override
	public PType createNewReturnValue(Object node, TypeCheckInfo question)
			throws AnalysisException
	{
		return null;
	}

}
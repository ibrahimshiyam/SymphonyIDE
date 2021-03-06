package eu.compassresearch.core.typechecker;

import java.util.List;

import org.overture.ast.definitions.PDefinition;

import eu.compassresearch.core.typechecker.api.ICmlTypeChecker;
import eu.compassresearch.core.typechecker.api.ITypeIssueHandler;

/**
 * Class to create instances of the Vanilla type checker package. To create a CmlTypeChecker instance use the (@code
 * newTypeChecher) method providing an AST in the form of a list of Paragraphs and an issue handler. The issue handler
 * is created with newColectingIssueHandler and will contain all errors and warnings produced by the type checker.
 * Additionally the provided AST-paragraph's will have their type fields updated. The type checker gives the following
 * guarantees (some in theory): [1] Any model that passes the type checker is referentially sound [2] Any model that
 * passes the type checker has an interpretable subset that the interpreter can interpret, unless a proof obligation is
 * created and not satisfied. Canonical Usage: CmlTypeChecker checker = VanillaFactory.newTypeChecker( listOfPSources,
 * errorsOut ); boolean modelIsWellTyped = checker.typeCheck(); if (!modelIsWellTyped) { [report errors using errorsOut]
 * } Is is possible to create instances of sub-components of the type checker for extensibility. This is still work in
 * progress.
 * 
 * @author rwl
 */
public final class VanillaFactory
{
	//
	/**
	 * create an instance of the Vanilla type checker.
	 * 
	 * @param cmlSources
	 *            - List of parsed CML source to type check
	 * @param issueHandler
	 *            - Optional parameter can be null in which case the type checker will collect any type errors to be
	 *            retrieved later. Otherwise an instance that handles errors when reported by the type checker.
	 * @return 
	 */
	public static ICmlTypeChecker newTypeChecker(
			List<? extends PDefinition> cmlSources,
			ITypeIssueHandler issueHandler)
	{
		VanillaCmlTypeChecker result = new VanillaCmlTypeChecker(cmlSources, issueHandler);
		return result;
	}

	//
	// /**
	// * Create a Declaration and Definition visitor
	// *
	// * @param parentChecker
	// * - A Root CML Type Checker to resolve other kinds of constructs;
	// * @param compareTypes
	// * - A strategy for comparing CML types and determining the sub type relation.
	// * @param issueHandler
	// * - A Error handler. The VanillaCmlTypeChecker is also a TypeIssueHandler.
	// */
	// public static IQuestionAnswer<org.overture.typechecker.TypeCheckInfo, PType>
	// newCmlDefinitionAndDeclarationVisitor(
	// ICmlRootVisitor parentChecker, ITypeComparator compareTypes,
	// ITypeIssueHandler issueHandler)
	// {
	// TCDeclAndDefVisitor v = new TCDeclAndDefVisitor(parentChecker, compareTypes, issueHandler);
	// return v;
	// }
	//
	// /**
	// * Create a statement visitor
	// *
	// * @param parentChecker
	// * - A Root CML Type Checker to resolve other kinds of constructs;
	// * @param issueHandler
	// * - A Error handler. The VanillaCmlTypeChecker is also a TypeIssueHandler.
	// */
	// public static IQuestionAnswer<org.overture.typechecker.TypeCheckInfo, PType> newCmStatementVisitor(
	// ICmlRootVisitor parentChecker, ITypeIssueHandler issueHandler,
	// ITypeComparator typeComparator)
	// {
	// TCActionVisitor v = new TCActionVisitor(parentChecker, issueHandler, typeComparator);
	// return v;
	// }
	//
	// /**
	// * Create an expression visitor
	// *
	// * @param parentChecker
	// * - A Root CML Type Checker to resolve other kinds of constructs;
	// * @param issueHandler
	// * - A Error handler. The VanillaCmlTypeChecker is also a TypeIssueHandler.
	// */
	// public static IQuestionAnswer<org.overture.typechecker.TypeCheckInfo, PType> newCmlTypeVisitor(
	// ICmlRootVisitor parentChecker, ITypeIssueHandler issueHandler)
	// {
	// TCTypeVisitor exprVisitor = new TCTypeVisitor(parentChecker, issueHandler);
	// return exprVisitor;
	// }
	//
	// /**
	// * Create an expression visitor
	// *
	// * @param parentChecker
	// * - A Root CML Type Checker to resolve other kinds of constructs;
	// * @param issueHandler
	// * - A Error handler. The VanillaCmlTypeChecker is also a TypeIssueHandler.
	// */
	// public static IQuestionAnswer<org.overture.typechecker.TypeCheckInfo, PType> newCmlExpressionVisitor(
	// ICmlRootVisitor parentChecker, ITypeIssueHandler issueHandler,
	// ITypeComparator typeComparator)
	// {
	// TCExpressionVisitor exprVisitor = new TCExpressionVisitor(parentChecker, issueHandler, typeComparator);
	// return exprVisitor;
	// }
	//
	// /**
	// * Get a fresh type checking question.
	// *
	// * @param issueHandler
	// * - A place to add errors.
	// * @return
	// */
	// public static ITypeCheckQuestion newTopLevelTypeCheckQuestion(
	// ITypeIssueHandler issueHandler)
	// {
	// return CmlTypeCheckInfo.getNewTopLevelInstance(new CmlTypeCheckerAssistantFactory(), issueHandler, new
	// LinkedList<PDefinition>(), new LinkedList<PDefinition>());
	// }
	//
	/**
	 * Returns an instance of the default issue handler in the TC package. Static access to the default common-Regsitry
	 * instance is used. NOTE! IssueHandlers and Registry are entangled behind the screens, this is unnecessary and
	 * should be re-factored at some point. RWL
	 * 
	 * @return
	 */
	public static ITypeIssueHandler newCollectingIssueHandle()
	{
		return new CollectingIssueHandler();
	}
	//
}

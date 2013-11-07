package eu.compassresearch.core.analysis.modelchecker.visitors;

import java.util.LinkedList;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.analysis.QuestionAnswerAdaptor;
import org.overture.ast.node.INode;
import org.overture.ast.types.AIntNumericBasicType;
import org.overture.ast.types.ANamedInvariantType;
import org.overture.ast.types.ANatNumericBasicType;
import org.overture.ast.types.AProductType;
import org.overture.ast.types.PType;

import eu.compassresearch.ast.analysis.QuestionAnswerCMLAdaptor;
import eu.compassresearch.ast.types.PCMLType;
import eu.compassresearch.core.analysis.modelchecker.ast.MCNode;
import eu.compassresearch.core.analysis.modelchecker.ast.expressions.MCPCMLExp;
import eu.compassresearch.core.analysis.modelchecker.ast.types.MCAChannelType;
import eu.compassresearch.core.analysis.modelchecker.ast.types.MCAIntNumericBasicType;
import eu.compassresearch.core.analysis.modelchecker.ast.types.MCANamedInvariantType;
import eu.compassresearch.core.analysis.modelchecker.ast.types.MCANatNumericBasicType;
import eu.compassresearch.core.analysis.modelchecker.ast.types.MCAProductType;
import eu.compassresearch.core.analysis.modelchecker.ast.types.MCPCMLType;

public class NewMCTypeAndValueVisitor extends
		QuestionAnswerCMLAdaptor<NewCMLModelcheckerContext, MCNode> {

	final private QuestionAnswerAdaptor<NewCMLModelcheckerContext, MCNode> rootVisitor;
	
	public NewMCTypeAndValueVisitor(QuestionAnswerAdaptor<NewCMLModelcheckerContext, MCNode> parentVisitor){
		this.rootVisitor = parentVisitor;
	}
	
	//FOR THE CASE OF TYPES WHOSE VISIT METHOD IS NOT IMPLEMENTED
	@Override
	public MCNode defaultPCMLType(PCMLType node,
			NewCMLModelcheckerContext question) throws AnalysisException {

		throw new ModelcheckerRuntimeException(ModelcheckerErrorMessages.CASE_NOT_IMPLEMENTED.customizeMessage(node.getClass().getSimpleName()));
	}
	
	///// TYPES
	@Override
	public MCNode caseANatNumericBasicType(ANatNumericBasicType node,
			NewCMLModelcheckerContext question) throws AnalysisException {
	
		return new MCANatNumericBasicType(node.toString());
	}
	
	@Override
	public MCNode caseAIntNumericBasicType(AIntNumericBasicType node,
			NewCMLModelcheckerContext question) throws AnalysisException {
		
		return new MCAIntNumericBasicType(node.toString());
	}

	
	@Override
	public MCNode caseANamedInvariantType(ANamedInvariantType node,
			NewCMLModelcheckerContext question) throws AnalysisException {
		
		MCANamedInvariantType result = new MCANamedInvariantType(node.getName().toString());
		
		return result;
	}

	/*
	@Override
	public MCNode caseAChannelType(AChannelType node,
			NewCMLModelcheckerContext question) throws AnalysisException {
		
		MCPCMLType chanType = null;
		
		if(node.getType() != null){
			chanType = (MCPCMLType) node.getType().apply(rootVisitor, question);
		}
		
		MCAChannelType result = new MCAChannelType(chanType);
		
		return result;
	}
	*/

	@Override
	public MCNode caseAProductType(AProductType node,
			NewCMLModelcheckerContext question) throws AnalysisException {
		
		LinkedList<MCPCMLType> mcTypes = new LinkedList<MCPCMLType>();
		for (PType pType : node.getTypes()) {
			mcTypes.add((MCPCMLType) pType.apply(this,question));
		}
		MCAProductType result = new MCAProductType(mcTypes);
		
		return result;
	}


	@Override
	public MCNode createNewReturnValue(INode node,
			NewCMLModelcheckerContext question) throws AnalysisException
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public MCNode createNewReturnValue(Object node,
			NewCMLModelcheckerContext question) throws AnalysisException {
		// TODO Auto-generated method stub
		return null;
	}	
	/////  VALUES

}
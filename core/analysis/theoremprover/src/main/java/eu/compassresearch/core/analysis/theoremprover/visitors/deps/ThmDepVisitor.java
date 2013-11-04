package eu.compassresearch.core.analysis.theoremprover.visitors.deps;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.definitions.AAssignmentDefinition;
import org.overture.ast.definitions.AValueDefinition;
import org.overture.ast.expressions.PExp;
import org.overture.ast.node.INode;
import org.overture.ast.types.PType;

import eu.compassresearch.ast.actions.PAction;
import eu.compassresearch.ast.analysis.QuestionAnswerCMLAdaptor;
import eu.compassresearch.ast.definitions.AChannelNameDefinition;
import eu.compassresearch.ast.definitions.AChansetDefinition;
import eu.compassresearch.ast.expressions.PCMLExp;
import eu.compassresearch.ast.expressions.PVarsetExpression;
import eu.compassresearch.ast.process.PProcess;
import eu.compassresearch.core.analysis.theoremprover.thms.NodeNameList;

@SuppressWarnings("serial")
public class ThmDepVisitor extends
QuestionAnswerCMLAdaptor<NodeNameList, NodeNameList> {
	
	
	private ThmTypeDepVisitor typeDepVisitor;
	private ThmExpDepVisitor expDepVisitor;
	private ThmValueDepVisitor valDepVisitor;
	private ThmChannelDepVisitor chanDepVisitor;
	private ThmStateDepVisitor stateDepVisitor;
	private ThmProcessDepVisitor processDepVisitor;
	private ThmActionDepVisitor actionDepVisitor;
	

	/**
	 * Construct a ThmVisitor with no source indicated
	 */
	public ThmDepVisitor() {
		initialize();
	}
	
	private void initialize()
	{
		typeDepVisitor = new ThmTypeDepVisitor(this);
		expDepVisitor = new ThmExpDepVisitor(this);
		chanDepVisitor = new ThmChannelDepVisitor(this);
		valDepVisitor = new ThmValueDepVisitor(this);
		stateDepVisitor = new ThmStateDepVisitor(this);
		processDepVisitor = new ThmProcessDepVisitor(this);
		actionDepVisitor = new ThmActionDepVisitor(this);
	}

	public NodeNameList defaultINode(INode node, NodeNameList bvars)
			throws AnalysisException {		
		return new NodeNameList();
	}	
	public  NodeNameList caseAValueDefinition(AValueDefinition node, NodeNameList bvars)
			throws AnalysisException {
		return node.apply(this.valDepVisitor, bvars);
	}
	
	@Override
	public NodeNameList caseAChannelNameDefinition(AChannelNameDefinition node, NodeNameList bvars)
			throws AnalysisException {		
		return node.apply(this.chanDepVisitor, bvars);
	}

	@Override
	public NodeNameList caseAChansetDefinition(AChansetDefinition node, NodeNameList bvars)
			throws AnalysisException {		
		return node.apply(this.chanDepVisitor, bvars);
	}	
	
	public NodeNameList caseAAssignmentDefinition(AAssignmentDefinition node, NodeNameList bvars)
			throws AnalysisException {		
		return node.apply(this.stateDepVisitor, bvars);
	}	

	public NodeNameList defaultPExp(PExp node, NodeNameList bvars)
			throws AnalysisException {		
		return node.apply(this.expDepVisitor, bvars);
	}	

	public NodeNameList defaultPType(PType node, NodeNameList bvars)
			throws AnalysisException {		
		return node.apply(this.typeDepVisitor, bvars);
	}	
	
	public NodeNameList defaultPProcess(PProcess node, NodeNameList bvars)
			throws AnalysisException {		
		return node.apply(this.processDepVisitor, bvars);
	}	
	
	public NodeNameList defaultPAction(PAction node, NodeNameList bvars)
			throws AnalysisException {		
		return node.apply(this.actionDepVisitor, bvars);
	}	
	
	public NodeNameList defaultPVarsetExpression(PVarsetExpression node, NodeNameList bvars)
			throws AnalysisException {		
		return node.apply(this.expDepVisitor, bvars);
	}	
	
	
	public NodeNameList defaultPCMLExp(PCMLExp node, NodeNameList bvars)
			throws AnalysisException {		
		return node.apply(this.expDepVisitor, bvars);
	}	
	

	@Override
	public NodeNameList createNewReturnValue(INode arg0, NodeNameList arg1)
			throws AnalysisException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeNameList createNewReturnValue(Object arg0, NodeNameList arg1)
			throws AnalysisException {
		// TODO Auto-generated method stub
		return null;
	}	
}
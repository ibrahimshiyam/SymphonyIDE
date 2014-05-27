package eu.compassresearch.core.analysis.pog.obligations;

import org.overture.ast.intf.lex.ILexLocation;
import org.overture.ast.node.INode;
import org.overture.pog.pub.IPOContextStack;

import eu.compassresearch.ast.statements.PCMLStm;

public class CmlStmRefineObligation extends CmlProofObligation {

	private PCMLStm fromStm = null;
	private PCMLStm toStm = null;
	
	public CmlStmRefineObligation(INode node, CmlPOType kind,
			IPOContextStack ctxt, ILexLocation location) {
		super(node, kind, ctxt, location);
		// TODO Auto-generated constructor stub
	}

	public CmlStmRefineObligation(INode node, CmlPOType kind,
			IPOContextStack ctxt, ILexLocation location,
			PCMLStm f, PCMLStm t) {
		super(node, kind, ctxt, location);
		fromStm = f;
		toStm = t;
	}
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public String toString() {
		return fromStm.toString() + " [= " + toStm.toString();
	}
	
}

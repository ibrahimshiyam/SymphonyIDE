package eu.compassresearch.ide.refinementtool;

import java.util.LinkedList;

import org.overture.ast.node.INode;

import eu.compassresearch.ast.actions.AChaosAction;
import eu.compassresearch.core.analysis.pog.obligations.CmlProofObligation;

public class ChaosStopRefineLaw implements IRefineLaw {

	@Override
	public String getName() {
		return "Chaos to Stop";
	}

	@Override
	public boolean isApplicable(INode node) {
		return (node instanceof AChaosAction);
	}

	@Override
	public Refinement apply(INode node) {
		// TODO Auto-generated method stub
		return new Refinement("Stop", new LinkedList<CmlProofObligation>());
	}

}

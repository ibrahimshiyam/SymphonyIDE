package eu.compassresearch.core.analysis.modelchecker.ast.actions;


public class MCADivAction implements MCPAction {

	@Override
	public String toFormula(String option) {
		return "Div";
	}

}

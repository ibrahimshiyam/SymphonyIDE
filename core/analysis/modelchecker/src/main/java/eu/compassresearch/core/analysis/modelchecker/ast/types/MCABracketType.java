package eu.compassresearch.core.analysis.modelchecker.ast.types;


public class MCABracketType implements MCPCMLType {

	private MCPCMLType type;
	
	
	public MCABracketType(MCPCMLType type) {
		this.type = type;
	}

	@Override
	public String toFormula(String option) {
		return this.type.toFormula(option);
	}

	@Override
	public MCPCMLType copy() {
		return new MCABracketType(this.type);
	}

	@Override
	public String getTypeAsName() {
		// TODO Auto-generated method stub
		return null;
	}

	public MCPCMLType getType() {
		return type;
	}

	public void setType(MCPCMLType type) {
		this.type = type;
	}
	
	
}

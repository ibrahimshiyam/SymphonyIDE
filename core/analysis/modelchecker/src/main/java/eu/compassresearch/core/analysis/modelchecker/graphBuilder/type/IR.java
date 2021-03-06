package eu.compassresearch.core.analysis.modelchecker.graphBuilder.type;

public class IR implements Type{
	
	private double value;
	
	
	
	public IR(double val) {
		this.value = val;
		
	}
	
	@Override
	public IR copy(){
		IR result = new IR(this.value);
		return result;
	}
	
	@Override
	public String toString() {
		return String.valueOf(value);
	}
	
	public String toFormula() {
		return "IR("+String.valueOf(value)+")";
	}

	@Override
	public String toFormulaWithState() {
		return "IR("+String.valueOf(value)+")";
	}
	
	public String toFormulaGeneric() {
		return "IR("+String.valueOf(value)+"_)";
	}
	
	public String toFormulaWithUnderscore(){
		return "IR(_)";
	}
}

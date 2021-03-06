package eu.compassresearch.core.analysis.modelchecker.graphBuilder.binding;

import eu.compassresearch.core.analysis.modelchecker.graphBuilder.type.Type;

public interface Binding {
	public String toFormula(String option);
	public String toFormula();
	public String toFormulaWithState();
	public String toFormulaWithUnderscore();
	public String toFormulaGeneric();
	public Binding addBinding(String procName, String varName, Type type);
	public void updateBinding(String varName, Type type);
	public Binding deleteBinding(String varName);
	public Binding copy();
	public void setProcName(String procName);
	public String getProcName();
	public StringBuilder generateAllFetchFacts(int number);
	public StringBuilder generateAllUpdFacts(int number);
	public StringBuilder generateAllDelFacts(int number);
}

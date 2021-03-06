package eu.compassresearch.core.analysis.modelchecker.ast.actions;

import java.util.ArrayList;
import java.util.LinkedList;

import eu.compassresearch.core.analysis.modelchecker.ast.auxiliary.MCActionCall;
import eu.compassresearch.core.analysis.modelchecker.ast.auxiliary.MCGenericCall;
import eu.compassresearch.core.analysis.modelchecker.ast.auxiliary.MCOperationCall;
import eu.compassresearch.core.analysis.modelchecker.ast.definitions.MCAActionDefinition;
import eu.compassresearch.core.analysis.modelchecker.ast.definitions.MCAExplicitCmlOperationDefinition;
import eu.compassresearch.core.analysis.modelchecker.ast.definitions.MCAProcessDefinition;
import eu.compassresearch.core.analysis.modelchecker.ast.definitions.MCSCmlOperationDefinition;
import eu.compassresearch.core.analysis.modelchecker.ast.expressions.MCPCMLExp;
import eu.compassresearch.core.analysis.modelchecker.visitors.NewCMLModelcheckerContext;

public class MCACallAction implements MCPAction {

	private String name;
	private LinkedList<MCPCMLExp> args;
	
	public MCACallAction(String name, LinkedList<MCPCMLExp> args) {
		super();
		this.name = name;
		this.args = args;
	}



	@Override
	public String toFormula(String option) {
		StringBuilder result = new StringBuilder();
		NewCMLModelcheckerContext context = NewCMLModelcheckerContext.getInstance();
		String nameToSearch = context.reverseNameMapping.get(this.name);
		
		ArrayList<MCAActionDefinition> localActions = context.localActions;
		boolean callResolved = false;
		MCGenericCall call = null;
		
		if(localActions.size() > 0){ //if there are auxiliary actions
			for (MCAActionDefinition localAction : localActions) {
				if(localAction.getName().toString().equals(nameToSearch)){
				//if(localAction.getName().toString().equals(this.name.toString())){
					callResolved = true;
					//call = new MCActionCall(name, args);
					call = new MCActionCall(nameToSearch, args,null);
					break;
				}
			}
		}
		if (!callResolved) {
			for (MCAProcessDefinition pDefinition : context.processDefinitions) {
				if(((MCAProcessDefinition) pDefinition).getName().toString().equals(this.name)){
					callResolved = true;
					call = new MCActionCall(name, args,null);
					break;
				}
			}
		}

		if (!callResolved) {
			for (MCSCmlOperationDefinition pDefinition : context.operations) {
				if(pDefinition instanceof MCAExplicitCmlOperationDefinition){
					if(((MCAExplicitCmlOperationDefinition) pDefinition).getName().toString().equals(this.name)){
						callResolved = true;
						call = new MCOperationCall(name, args, null,((MCAExplicitCmlOperationDefinition) pDefinition).getOperationType());
						break;
					}
				}
			}
		}
		
		result.append(call.toFormula(option));
		
		return result.toString();	
	}



	public String getName() {
		return name;
	}



	public void setName(String name) {
		this.name = name;
	}



	public LinkedList<MCPCMLExp> getArgs() {
		return args;
	}



	public void setArgs(LinkedList<MCPCMLExp> args) {
		this.args = args;
	}

	
}

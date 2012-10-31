package eu.compassresearch.core.interpreter.api;

public enum InterpretationErrorMessages {

	NO_PROCESS_WITH_DEFINED_NAME_FOUND("No process identified by '%s' exists")
	
	;
	
	private String template;
    
	private InterpretationErrorMessages(String templateString)
	{
		this.template = templateString;
	}

	public String customizeMessage(String... strs)
	{
		// Check Arity
		int arity = 0;
		for (Character c : template.toCharArray())
			if ('%' == c)
				arity++;
		if (arity != strs.length)
			throw new RuntimeException(
					"Error in the error-message. Template requires exactly " + arity
					+ " arguments but " + strs.length + " were given.");

		// Format String
		return String.format(template, (Object[]) strs);
	}

}
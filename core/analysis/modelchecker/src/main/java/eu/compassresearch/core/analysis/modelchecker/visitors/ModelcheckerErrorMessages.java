package eu.compassresearch.core.analysis.modelchecker.visitors;

public enum ModelcheckerErrorMessages {

	NO_PROCESS_WITH_DEFINED_NAME_FOUND("No process identified by '%s' exists"),
	FATAL_ERROR("A fatal unrecoverable error has occured"),
	CASE_NOT_IMPLEMENTED("%s case is not yet implemented")
	;
	
	private String template;
    
	private ModelcheckerErrorMessages(String templateString)
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

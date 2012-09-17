package eu.compassresearch.ast.util;

import eu.compassresearch.ast.node.ExternalNode;

public class ClonableString implements ExternalNode
{
	private static final long serialVersionUID = 1L;
	public String value;

	public ClonableString(String value)
	{
		this.value = value;
	}

	@Override
	public String toString()
	{
		return value;
	}
	
	public Object clone() 
	{
		return new ClonableString(value);
	}
}

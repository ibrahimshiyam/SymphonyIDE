package eu.compassresearch.core.interpreter.api.values;

import java.util.HashMap;
import java.util.Map;

import org.overture.interpreter.values.Value;

public class RenamingValue extends Value
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6088185706549337001L;
	private Map<ChannelValue, ChannelValue> renamingMap;

	public RenamingValue()
	{
		renamingMap = new HashMap<ChannelValue, ChannelValue>();
	}

	public RenamingValue(Map<ChannelValue, ChannelValue> renamingMap)
	{
		this.renamingMap = renamingMap;
	}

	@Override
	public String toString()
	{
		return renamingMap.toString();
	}

	@Override
	public boolean equals(Object other)
	{
		return this.renamingMap.equals(other);
	}

	@Override
	public int hashCode()
	{
		return this.renamingMap.hashCode();
	}

	@Override
	public String kind()
	{
		return "RenamingValue";
	}

	@Override
	public Object clone()
	{
		return new RenamingValue(new HashMap<ChannelValue, ChannelValue>(this.renamingMap));
	}

	public Map<ChannelValue, ChannelValue> renamingMap()
	{
		return this.renamingMap;
	}

}

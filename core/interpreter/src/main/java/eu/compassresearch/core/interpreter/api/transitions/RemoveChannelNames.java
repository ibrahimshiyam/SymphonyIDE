package eu.compassresearch.core.interpreter.api.transitions;

import eu.compassresearch.core.interpreter.api.values.ChannelNameSetValue;
import eu.compassresearch.core.interpreter.api.values.ChannelNameValue;

public class RemoveChannelNames implements Filter
{
	
	private final ChannelNameSetValue channelNameSetValue;
	
	public RemoveChannelNames(ChannelNameSetValue channelNameSetValue)
	{
		this.channelNameSetValue = channelNameSetValue;
	}
	
	public RemoveChannelNames(ChannelNameValue channelNameValue)
	{
		this.channelNameSetValue = new ChannelNameSetValue(channelNameValue);
	}
	
	private boolean isTransitionCompatible(LabelledTransition transition, ChannelNameValue channelNameValue)
	{
		return (transition.getChannelName().isComparable(channelNameValue) && 
				channelNameValue.isGTEQPrecise(transition.getChannelName()));
	}
	

	@Override
	public boolean isAccepted(CmlTransition transition)
	{
		if (transition instanceof LabelledTransition)
		{
			LabelledTransition lt = (LabelledTransition) transition;
			boolean retaintIt = true;

			for (ChannelNameValue channelNameValue : channelNameSetValue)
			{
				if (isTransitionCompatible(lt, channelNameValue))
				{
					retaintIt = false;
					break;
				}
			}

			return retaintIt;
		}
		else 
		{
			return true;
		}
	}

}

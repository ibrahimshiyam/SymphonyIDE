package eu.compassresearch.core.interpreter.cml;

import org.overture.interpreter.values.Value;

public interface CMLInputChannel<T extends Value> extends CMLChannel{
	
	public T read();

}
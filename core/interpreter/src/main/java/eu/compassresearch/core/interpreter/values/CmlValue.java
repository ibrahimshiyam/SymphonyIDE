package eu.compassresearch.core.interpreter.values;

import org.overture.interpreter.runtime.ValueException;
import org.overture.interpreter.values.Value;

import eu.compassresearch.core.interpreter.runtime.CmlContext;

public abstract class CmlValue extends Value {

	public CmlObjectValue objectValue(CmlContext ctxt) throws ValueException
	{
		abort(4105, "Can't get object value of " + kind(), ctxt.getVdmContext());
		return null;
	}

}

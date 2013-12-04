package eu.compassresearch.core.interpreter;

import java.util.HashMap;
import java.util.Map;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.lex.Dialect;
import org.overture.config.Release;
import org.overture.config.Settings;

import eu.compassresearch.core.interpreter.api.CmlInterpreter;
import eu.compassresearch.core.interpreter.api.CmlInterpreterState;
import eu.compassresearch.core.interpreter.api.SelectionStrategy;
import eu.compassresearch.core.interpreter.api.behaviour.CmlBehaviour;
import eu.compassresearch.core.interpreter.api.events.CmlInterpreterStateObserver;
import eu.compassresearch.core.interpreter.api.events.EventFireMediator;
import eu.compassresearch.core.interpreter.api.events.EventSource;
import eu.compassresearch.core.interpreter.api.events.EventSourceHandler;
import eu.compassresearch.core.interpreter.api.events.InterpreterStateChangedEvent;
import eu.compassresearch.core.interpreter.debug.Breakpoint;

public abstract class AbstractCmlInterpreter implements CmlInterpreter
{

	/**
	 * Event handler for notifying when the interpreter status changes
	 */
	protected EventSourceHandler<CmlInterpreterStateObserver, InterpreterStateChangedEvent> stateChangedEventHandler = new EventSourceHandler<CmlInterpreterStateObserver, InterpreterStateChangedEvent>(this, new EventFireMediator<CmlInterpreterStateObserver, InterpreterStateChangedEvent>()
	{

		@Override
		public void fireEvent(CmlInterpreterStateObserver observer,
				Object source, InterpreterStateChangedEvent event)
		{

			observer.onStateChanged(source, event);

		}
	});

	/**
	 * A map of the active breakpoints where the key has the following format "<filepath>:<linenumber>"
	 */
	protected Map<String, Breakpoint> breakpoints = new HashMap<>();
	protected CmlBehaviour runningTopProcess = null;
	protected SelectionStrategy environment;
	private boolean suspendBeforeTermination = false;

	/** A static instance pointer to the interpreter. */
	protected static CmlInterpreter instance = null;
	/**
	 * The current state of the interpreter
	 */
	private CmlInterpreterState currentState = null;

	/**
	 * Configuration of the interpreter
	 */
	protected final Config config;

	public AbstractCmlInterpreter(Config config)
	{
		this.config = config;
	}

	/**
	 * Set the new state of the interpreter
	 */
	protected void setNewState(CmlInterpreterState newState)
	{
		if (currentState != newState)
		{
			currentState = newState;
			stateChangedEventHandler.fireEvent(new InterpreterStateChangedEvent(this));
		}
	}

	/**
	 * Retrieves the current state of the interpreter
	 * 
	 * @return The current state of the interpreter
	 */
	@Override
	public CmlInterpreterState getState()
	{
		return currentState;
	}

	@Override
	public SelectionStrategy getEnvironment()
	{
		return environment;
	}

	@Override
	public EventSource<CmlInterpreterStateObserver> onStateChanged()
	{

		return stateChangedEventHandler;
	}

	@Override
	public void initialize() throws AnalysisException
	{
		Settings.dialect = Dialect.VDM_PP;
		Settings.release = Release.VDM_10;
	}

	// Breakpoints
	@Override
	public boolean addBreakpoint(Breakpoint bp)
	{

		String key = bp.getFile() + ":" + bp.getLine();

		if (breakpoints.containsKey(key))
		{
			return false;
		} else
		{
			breakpoints.put(key, bp);
			return true;
		}
	}

	@Override
	public boolean removeBreakpoint(Breakpoint bp)
	{
		String key = bp.getFile() + ":" + bp.getLine();

		if (!breakpoints.containsKey(key))
		{
			return false;
		} else
		{
			breakpoints.remove(key);
			return true;
		}
	}

	@Override
	public CmlBehaviour findBehaviorById(int id)
	{

		return findBehaviorById(this.runningTopProcess, id);
	}

	private CmlBehaviour findBehaviorById(CmlBehaviour behavior, int id)
	{

		CmlBehaviour foundBehavior = null;

		if (behavior.getId() == id)
		{
			foundBehavior = behavior;
		}

		if (behavior.getLeftChild() != null && foundBehavior == null)
		{
			foundBehavior = findBehaviorById(behavior.getLeftChild(), id);
		}

		if (behavior.getRightChild() != null && foundBehavior == null)
		{
			foundBehavior = findBehaviorById(behavior.getRightChild(), id);
		}

		return foundBehavior;
	}

	@Override
	public boolean suspendBeforeTermination()
	{
		return suspendBeforeTermination;
	}

	@Override
	public void setSuspendBeforeTermination(boolean suspendBeforeTermination)
	{
		this.suspendBeforeTermination = suspendBeforeTermination;
	}

	/**
	 * @return The Interpreter instance.
	 */
	public static CmlInterpreter getInstance()
	{
		return instance; // NB. last one created
	}
}

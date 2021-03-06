package eu.compassresearch.core.interpreter.cosim.external;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import org.overture.ast.lex.LexIdentifierToken;
import org.overture.ast.lex.LexLocation;
import org.overture.ast.types.AIntNumericBasicType;
import org.overture.ast.types.PType;
import org.overture.interpreter.values.IntegerValue;
import org.overture.interpreter.values.Value;

import eu.compassresearch.core.interpreter.NamespaceUtility;
import eu.compassresearch.core.interpreter.api.transitions.CmlTransition;
import eu.compassresearch.core.interpreter.api.transitions.CmlTransitionFactory;
import eu.compassresearch.core.interpreter.api.transitions.CmlTransitionSet;
import eu.compassresearch.core.interpreter.api.transitions.ObservableTransition;
import eu.compassresearch.core.interpreter.api.values.ChannelValue;
import eu.compassresearch.core.interpreter.api.values.CmlChannel;
import eu.compassresearch.core.interpreter.api.values.LatticeTopValue;
import eu.compassresearch.core.interpreter.cosim.IProcessDelegate;
import eu.compassresearch.core.interpreter.cosim.communication.Utils;

public class SubSystem implements IProcessDelegate
{

	@Override
	public CmlTransitionSet inspect() throws Exception
	{
		// ObservableTransition transition = untypedChannel("c");

		SortedSet<CmlTransition> events = new TreeSet<CmlTransition>();
		events.add(untypedChannel("c"));
		events.add(sendChannel("a", 1));
		events.add(readChannel("b", new AIntNumericBasicType()));

		CmlTransitionSet transitions = new CmlTransitionSet(events);

		return transitions;
	}

	private ObservableTransition untypedChannel(String name)
	{
		CmlChannel channel = createChannelValue(name);
		ChannelValue channelName = new ChannelValue(channel, new LinkedList<Value>(), null);
		ObservableTransition transition = CmlTransitionFactory.newLabelledTransition(null, channelName);
		return transition;
	}

	private ObservableTransition sendChannel(String name, int... value)
	{
		CmlChannel channel = createChannelValue(name, new AIntNumericBasicType());
		List<Value> values = new Vector<Value>();
		for (int v : value)
		{
			values.add(new IntegerValue(v));
		}

		// List<ValueConstraint> constraints = new Vector<ValueConstraint>();

		// constraints.add(new NoConstraint());

		ChannelValue channelName = new ChannelValue(channel, values, null);
		ObservableTransition transition = CmlTransitionFactory.newLabelledTransition(null, channelName);
		return transition;
	}

	private ObservableTransition readChannel(String name, PType... value)
	{
		CmlChannel channel = createChannelValue(name, value);
		List<Value> values = new Vector<Value>();
		for (PType v : value)
		{
			values.add(new LatticeTopValue(v));
		}

		ChannelValue channelName = new ChannelValue(channel, values, null);
		ObservableTransition transition = CmlTransitionFactory.newLabelledTransition(null, channelName);
		return transition;
	}

	private static CmlChannel createChannelValue(String name)
	{
		return createChannelValue(name, (PType[]) null);
	}

	private static CmlChannel createChannelValue(String name, PType... type)
	{
		// AChannelType channelType = null;
		// if(type!=null)
		// {
		// channelType = new AChannelType(new LexLocation(), false, Arrays.asList(type));
		// }else
		// {
		// channelType = new AChannelType();
		// }

		return new CmlChannel(null, NamespaceUtility.createChannelName(new LexIdentifierToken(name, false, new LexLocation())));
	}

	@Override
	public void execute(CmlTransition transition) throws Exception
	{
		System.out.println("Executing: " + transition);
		Utils.milliPause(1000);

	}

	@Override
	public String getProcessName()
	{
		return "A";
	}

	@Override
	public boolean isFinished() throws Exception
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void disconnect() throws Exception
	{
		// TODO Auto-generated method stub
	}

	public static void main(String[] args) throws InterruptedException,
			IOException
	{
		final SubSystem sys = new SubSystem();
		final ExternalCoSimulationClient client = new ExternalCoSimulationClient("localhost", 8088);
		client.setSubSystem(sys);

		client.connect();
		client.start();
		client.registerImplementation(sys.getProcessName());

		new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				while (true)
				{
					try
					{
						sys.execute(client.getExecutableTransition());
						client.executionCompleted();
					} catch (InterruptedException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (Exception e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}).start();

		client.join();
	}

}

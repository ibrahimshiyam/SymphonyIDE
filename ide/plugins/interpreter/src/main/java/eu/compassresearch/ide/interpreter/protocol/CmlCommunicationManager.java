package eu.compassresearch.ide.interpreter.protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;

import eu.compassresearch.core.interpreter.api.CmlInterpretationStatus;
import eu.compassresearch.core.interpreter.debug.CmlDbgCommandMessage;
import eu.compassresearch.core.interpreter.debug.CmlDbgStatusMessage;
import eu.compassresearch.core.interpreter.debug.CmlDebugCommand;
import eu.compassresearch.core.interpreter.utility.messaging.Message;
import eu.compassresearch.core.interpreter.utility.messaging.MessageCommunicator;
import eu.compassresearch.core.interpreter.utility.messaging.MessageContainer;
import eu.compassresearch.core.interpreter.utility.messaging.RequestMessage;
import eu.compassresearch.ide.interpreter.CmlDebugPlugin;
import eu.compassresearch.ide.interpreter.ICmlDebugConstants;
import eu.compassresearch.ide.interpreter.model.CmlDebugTarget;
import eu.compassresearch.ide.plugins.interpreter.protocol.MessageEventHandler;

public class CmlCommunicationManager extends Thread
{
	private Map<String, MessageEventHandler<RequestMessage>> requestHandlers;
	private Map<String, MessageEventHandler<CmlDbgStatusMessage>> statusHandlers;
	private BufferedReader fRequestReader;
	final CmlDebugTarget target;

	// socket to communicate with VM
	private Socket fRequestSocket;
	private OutputStream requestOutputStream;

	private CmlThreadManager threadManager;
	private int port;

	public CmlCommunicationManager(
			CmlDebugTarget target,
			CmlThreadManager threadManager,
			Map<String, MessageEventHandler<RequestMessage>> requestHandlers,
			Map<String, MessageEventHandler<CmlDbgStatusMessage>> statusHandlers,
			int port)
	{
		this.target = target;
		this.threadManager = threadManager;
		this.port = port;
		this.requestHandlers = requestHandlers;
		this.statusHandlers = statusHandlers;
	}

	/**
	 * Private methods
	 */
	/**
	 * Sends a command to the running interpreter
	 * 
	 * @param cmd
	 */
	public void sendCommandMessage(CmlDebugCommand cmd)
	{
		CmlDbgCommandMessage message = new CmlDbgCommandMessage(cmd);
		MessageCommunicator.sendMessage(requestOutputStream, message);
	}

	public void sendCommandMessage(CmlDebugCommand cmd, Object content)
	{
		CmlDbgCommandMessage message = new CmlDbgCommandMessage(cmd, content);
		MessageCommunicator.sendMessage(requestOutputStream, message);
	}

	public void sendMessage(Message message)
	{
		MessageCommunicator.sendMessage(requestOutputStream, message);
	}

	/**
	 * Initialisation methods
	 */
	// public void initializeHandlers()
	// {
	// requestHandlers = initializeRequestHandlers();
	// statusHandlers = initializeStatusHandlers();
	//
	// }

	/**
	 * Receives a message from the debugger
	 * 
	 * @return The received message wrapped in a MessageContainer
	 * @throws IOException
	 */
	public MessageContainer receiveMessage() throws IOException
	{
		return MessageCommunicator.receiveMessage(fRequestReader, new MessageContainer(new CmlDbgStatusMessage(CmlInterpretationStatus.TERMINATED)));
	}

	/**
	 * Dispatches the message to the corresponding message handler
	 * 
	 * @param handlers
	 *            The corresponding message handler map for this message type
	 * @param message
	 *            The message to be processed
	 * @return true if the event loop should continue otherwise false
	 */
	private <H extends Message> boolean dispatchMessageHandler(
			Map<String, MessageEventHandler<H>> handlers, H message)
	{
		boolean result = false;

		if (handlers.containsKey(message.getKey()))
			result = handlers.get(message.getKey()).handleMessage(message);

		return result;
	}

	/**
	 * Dispatches the message from messageContainer to the assigned handler of this message type
	 * 
	 * @param messageContainer
	 * @return
	 */
	public boolean processMessage(MessageContainer messageContainer)
	{
		boolean result = false;

		switch (messageContainer.getType())
		{
			case STATUS:
				return dispatchMessageHandler(statusHandlers, (CmlDbgStatusMessage) messageContainer.getMessage());
			case REQUEST:
				return dispatchMessageHandler(requestHandlers, (RequestMessage) messageContainer.getMessage());
			default:
				break;
		}

		return result;
	}

	private void connectionClosed()
	{
		try
		{
			this.fRequestSocket.close();
		} catch (IOException e)
		{
			e.printStackTrace();

		}
	}

	@Override
	public void run()
	{
		MessageContainer message = null;
		try
		{
			do
			{
				message = receiveMessage();
			} while (processMessage(message));
		} catch (IOException e)
		{
			CmlDebugPlugin.logError("Error while receving/processing incomming messages from the debugger", e);
		} finally
		{
			connectionClosed();
			threadManager.terminated();
		}
	}

	public boolean isDisconnected()
	{
		return fRequestSocket.isClosed();
	}

	public void connect() throws IOException
	{
		if (waitForConnect(port))
		{
			// initializeHandlers();
			start();
		}
	}

	private boolean waitForConnect(int requestPort) throws IOException
	{
		ServerSocket requestAcceptor = null;
		try
		{
			requestAcceptor = new ServerSocket(requestPort);
			// FIXME change to config
			int timeout = 5000;
			if (target.getLaunch().getLaunchConfiguration().getAttribute(ICmlDebugConstants.CML_LAUNCH_CONFIG_REMOTE_DEBUG, false))
			{
				timeout = 30000;
			}
			requestAcceptor.setSoTimeout(timeout);

			fRequestSocket = requestAcceptor.accept();
			requestOutputStream = fRequestSocket.getOutputStream();
			fRequestReader = new BufferedReader(new InputStreamReader(fRequestSocket.getInputStream()));
			return true;
		} catch (CoreException e)
		{
			CmlDebugPlugin.logError("Failed to obtain remote debug flag from launch config", e);
		} finally
		{
			if (requestAcceptor != null)
				requestAcceptor.close();
		}
		return false;
	}

	public void terminate()
	{
		if (fRequestSocket != null)
		{
			synchronized (fRequestSocket)
			{
				sendCommandMessage(CmlDebugCommand.STOP);
			}
		}

	}

	public void disconnect()
	{
		sendCommandMessage(CmlDebugCommand.DISCONNECT);
	}

	public boolean isConnected()
	{
		return (fRequestSocket == null ? false : !fRequestSocket.isClosed());
	}

	public void addBreakpoint(URI file, int linenumber, boolean enabled)
	{

	}

	public void removeBreakpoint(URI file, int linenumber)
	{

	}

	public void updateBreakpoint(URI file, int linenumber, boolean enabled)
	{

	}

}

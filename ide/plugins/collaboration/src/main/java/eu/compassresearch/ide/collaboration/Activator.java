package eu.compassresearch.ide.collaboration;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ecf.core.IContainerManager;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import eu.compassresearch.ide.collaboration.communication.ConnectionManager;
import eu.compassresearch.ide.collaboration.datamodel.CollaborationDataModelManager;
import eu.compassresearch.ide.collaboration.distributedsimulation.DistributedSimulationManager;
import eu.compassresearch.ide.collaboration.files.FileChangeListener;

public class Activator extends AbstractUIPlugin
{
	public static final String PLUGIN_ID = "eu.compassresearch.ide.collaboration"; 
	
	private BundleContext context;
	private static Activator plugin;
	
	@SuppressWarnings("rawtypes")
	private ServiceTracker containerManagerTracker;
	
	private CollaborationDataModelManager dataModelManager;
	private FileChangeListener fileChangeManager;
	private ConnectionManager connectionManager;
	private DistributedSimulationManager distributedSimulationManager;
	
	public Activator() {
		// nothing to do
	}
	
	public void start(BundleContext ctxt) throws Exception {
		super.start(ctxt);
		plugin = this;
		context = ctxt;

		//load data models
		dataModelManager = new CollaborationDataModelManager();
		dataModelManager.loadModels();
		
		//track changes in workspace
		fileChangeManager = new FileChangeListener();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.addResourceChangeListener(fileChangeManager);
		
		//init connections manager
		connectionManager = new ConnectionManager();
		
		//init distributed simulation
		distributedSimulationManager = new DistributedSimulationManager(connectionManager);
	}
	
	@Override
	public void stop(BundleContext context) throws Exception
	{
		if (containerManagerTracker != null) {
			containerManagerTracker.close();
			containerManagerTracker = null;
		}
		
		//Persist data model
		dataModelManager.saveModels();
	
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.removeResourceChangeListener(fileChangeManager);
		
		plugin = null;
		this.context = null;
		dataModelManager = null;
	
		super.stop(context);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public IContainerManager getContainerManager() {
		if (containerManagerTracker == null) {
			containerManagerTracker = new ServiceTracker(context, IContainerManager.class.getName(), null);
			containerManagerTracker.open();
		}
		return (IContainerManager) containerManagerTracker.getService();
	}
	
	public static Activator getDefault() {
		return plugin;
	}
	
	public CollaborationDataModelManager getDataModelManager()
	{
		return dataModelManager;
	}

	public ConnectionManager getConnectionManager()
	{
		return connectionManager;
	}

	public DistributedSimulationManager getDistributedSimulationManager()
	{
		return distributedSimulationManager;
	}
}

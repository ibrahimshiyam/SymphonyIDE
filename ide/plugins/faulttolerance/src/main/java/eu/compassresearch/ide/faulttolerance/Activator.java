/**
 * 
 */
package eu.compassresearch.ide.faulttolerance;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * @author Andr&eacute; Didier (<a href=
 *         "mailto:alrd@cin.ufpe.br?Subject=Package eu.compassresearch.ide.faulttolerance, class Activator"
 *         >alrd@cin.ufpe.br</a>)
 * 
 */
public class Activator extends AbstractUIPlugin implements IStartup {
	public static final String ID = "eu.compassresearch.ide.faulttolerance";
	public static final String FAULT_TOLERANCE_JOB_FAMILY = "eu.compassresearch.ide.faulttolerance.jobfamily";
	private static Activator plugin;

	public Activator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		Job.getJobManager().cancel(FAULT_TOLERANCE_JOB_FAMILY);
		plugin = null;
		super.stop(context);
	}

	public static Activator getDefault() {
		return plugin;
	}

	public boolean isModelCheckerOk() {
		return eu.compassresearch.ide.modelchecker.CmlMCPlugin.FORMULA_OK;
	}

	@Override
	protected void initializeImageRegistry(ImageRegistry reg) {
		for (Image image : Image.values()) {
			image.updateImageDescriptor(reg);
		}
	}

	@Override
	public void earlyStartup() {
	}

	public void log(Throwable exception) {
		getLog().log(
				new Status(IStatus.ERROR, ID, getNotNullMessage(exception),
						exception));
	}

	/**
	 * @param exception
	 * @return
	 */
	private String getNotNullMessage(Throwable exception) {
		String m = exception.getLocalizedMessage();
		if (m == null || "".equals(m.trim())) {
			m = exception.getMessage();
		}
		if ((m == null || "".equals(m.trim())) && exception.getCause() != null) {
			m = getNotNullMessage(exception.getCause());
		}
		return m;
	}

	/**
	 * @param message
	 */
	public void log(Message message, Object... args) {
		getLog().log(new Status(IStatus.INFO, ID, message.format(args)));
	}
}

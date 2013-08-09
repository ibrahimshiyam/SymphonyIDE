package eu.compassresearch.ide.pog.view;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.overture.ide.core.resources.IVdmProject;
import org.overture.ide.plugins.poviewer.Activator;
import org.overture.ide.plugins.poviewer.IPoviewerConstants;
import org.overture.ide.plugins.poviewer.view.PoOverviewTableView;
import org.overture.ide.ui.utility.EditorUtility;
import org.overture.pog.pub.IProofObligation;
import org.overture.pog.pub.POStatus;

import eu.compassresearch.ide.pog.POConstants;

public class PoListView extends PoOverviewTableView
{

	

	@Override
	public void createPartControl(Composite parent)
	{
		viewer = new TableViewer(parent, SWT.FULL_SELECTION | SWT.H_SCROLL
				| SWT.V_SCROLL);
		// test setup columns...
		TableLayout layout = new TableLayout();
		layout.addColumnData(new ColumnWeightData(20, true));
		layout.addColumnData(new ColumnWeightData(100, true));
		layout.addColumnData(new ColumnWeightData(60, false));
		layout.addColumnData(new ColumnWeightData(20, false));
		viewer.getTable().setLayout(layout);
		viewer.getTable().setLinesVisible(true);
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setSortDirection(SWT.NONE);
		viewer.setSorter(null);

		TableColumn column01 = new TableColumn(viewer.getTable(), SWT.LEFT);
		column01.setText("No.");
		column01.setToolTipText("No.");

		TableColumn column = new TableColumn(viewer.getTable(), SWT.LEFT);
		column.setText("PO Name");
		column.setToolTipText("PO Name");

		TableColumn column2 = new TableColumn(viewer.getTable(), SWT.LEFT);
		column2.setText("Type");
		column2.setToolTipText("Show Type");

		TableColumn column3 = new TableColumn(viewer.getTable(), SWT.CENTER);
		column3.setText("Status");
		column3.setToolTipText("Show status");

		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());

		
		makeActions();
		super.hookDoubleClickAction();

		viewer.addSelectionChangedListener(new ISelectionChangedListener()
		{

			public void selectionChanged(SelectionChangedEvent event)
			{

				Object first = ((IStructuredSelection) event.getSelection()).getFirstElement();
				if (first instanceof IProofObligation)
				{
					try
					{
						IViewPart v = getSite().getPage().showView(POConstants.PO_DETAIL_VIEW);

						if (v instanceof PoView)
							((PoView) v).setDataList(project, (IProofObligation) first);
					} catch (PartInitException e)
					{

						e.printStackTrace();
					}
				}

			}
		});
	}

	

	protected void makeActions()
	{
		doubleClickAction = new Action() {
			@Override
			public void run()
			{
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection) selection).getFirstElement();
				if (obj instanceof IProofObligation)
				{
					gotoDefinition((IProofObligation) obj);
					// showMessage(((ProofObligation) obj).toString());
				}
			}

			private void gotoDefinition(IProofObligation po)
			{
				IFile file = project.findIFile(po.getLocation().getFile());
				if(IVdmProject.externalFileContentType.isAssociatedWith(file.getName()))
				{
					EditorUtility.gotoLocation(IPoviewerConstants.ExternalEditorId,file, po.getLocation(), po.getName());
				}else{
					EditorUtility.gotoLocation(file, po.getLocation(), po.getName());	
				}
				
			}
		};
		
		
	
	}
	
	
	
	class ViewContentProvider implements IStructuredContentProvider
	{
		public void inputChanged(Viewer v, Object oldInput, Object newInput)
		{
		}

		public void dispose()
		{
		}

		public Object[] getElements(Object inputElement)
		{
			if (inputElement instanceof List)
			{
				@SuppressWarnings("rawtypes")
				List list = (List) inputElement;
				return list.toArray();
			}
			return new Object[0];
		}

	}

	class ViewLabelProvider extends LabelProvider implements
			ITableLabelProvider
	{

		public void resetCounter()
		{
			count = 0;
		}

		private Integer count = 0;

		public String getColumnText(Object element, int columnIndex)
		{
			IProofObligation data = (IProofObligation) element;
			String columnText;
			switch (columnIndex)
			{
				case 0:
					count++;
					columnText = new Integer(data.getNumber()).toString();// count.toString();
					break;
				case 1:
					// FIXME replace this with Node Name when we're using it
					if (!data.getLocation().getFile().toString().equals(""))
						columnText = data.getLocation().getFile().getName()  + " - "
								+ data.getName();
					else
						columnText = data.getName();
					break;
				case 2:
					columnText = data.getKind().toString();
					break;
				case 3:
					columnText = "";// data.status.toString();
					break;
				default:
					columnText = "not set";
			}
			return columnText;

		}

		public Image getColumnImage(Object obj, int index)
		{
			if (index == 3)
			{
				return getImage(obj);
			}
			return null;
		}

		@Override
		public Image getImage(Object obj)
		{
			IProofObligation data = (IProofObligation) obj;

			String imgPath = "icons/cview16/unproved.png";

			if (data.getStatus() == POStatus.PROVED)
				imgPath = "icons/cview16/proved.png";

			return Activator.getImageDescriptor(imgPath).createImage();
		}

	}
	
}
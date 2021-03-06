package eu.compassresearch.ide.ui.utility.ast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.definitions.PDefinition;
import org.overture.ast.expressions.AVariableExp;
import org.overture.ast.expressions.PExp;
import org.overture.ast.intf.lex.ILexLocation;
import org.overture.ast.node.INode;
import org.overture.ast.statements.PStm;
import org.overture.ast.types.AFieldField;
import org.overture.ast.types.AFunctionType;
import org.overture.ast.types.ARecordInvariantType;
import org.overture.ide.core.IVdmElement;

import eu.compassresearch.ast.actions.PAction;
import eu.compassresearch.ast.analysis.DepthFirstAnalysisCMLAdaptor;
import eu.compassresearch.ast.definitions.PCMLDefinition;
import eu.compassresearch.ast.expressions.PCMLExp;
import eu.compassresearch.ast.process.PProcess;
import eu.compassresearch.ast.types.PCMLType;

public class CmlAstLocationSearcher extends DepthFirstAnalysisCMLAdaptor
{
	/**
	 * Default serial version UID
	 */
	private static final long serialVersionUID = 1L;

	private static boolean DEBUG_PRINT = true;

	/**
	 * Best match to the offset. This means that this node has a location where the offset is within
	 */
	private INode bestHit = null;
	/**
	 * Best alternative hit is a location which is abs close to the offset
	 */
	private INode bestAlternativeHit = null;
	/**
	 * Best alternative hit is a node which has a location which is abs close to the offset
	 */
	private ILexLocation bestAlternativeLocation;
	/**
	 * The offset used when searching for nodes within this location of the source code
	 */
	private int offSet;

	private static final CmlAstLocationSearcher seacher = new CmlAstLocationSearcher();

	private static final Map<IVdmElement, Map<ILexLocation, INode>> elementNodeCache = new HashMap<IVdmElement, Map<ILexLocation, INode>>();

	private IVdmElement currentElement = null;

	private boolean indexing = false;
	
	public final static int[] NO_LOCATION = new int[] { -1, -1 };

	/**
	 * Private constructor, special care is needed to the state of the class this no instanciation allowed outside this
	 * class
	 */
	private CmlAstLocationSearcher()
	{
	}

	private void init()
	{
		seacher._visitedNodes.clear();// We cheat with undeclared exception, this breaks the state of the adaptor, and
										// we use
		// static so we need to clear the cache.
		seacher.bestHit = null;
		seacher.bestAlternativeHit = null;
		seacher.bestAlternativeLocation = null;
		seacher.currentElement = null;
		seacher.indexing = false;
	}

	/**
	 * Search method to find the closest node to a location specified by a test offset
	 * 
	 * @param nodes
	 *            The nodes to search within
	 * @param offSet
	 *            The offset to match a node to
	 * @return The node closest to the offset or null
	 */
	public static INode search(List<INode> nodes, int offSet)
	{
		synchronized (seacher)
		{
			if (DEBUG_PRINT)
			{
				System.out.println("Search start");
			}
			seacher.init();
			seacher.offSet = offSet;
			try
			{
				for (INode node : nodes)
				{
					node.apply(seacher);
				}
			} catch (AnalysisException e)
			{
				// We found what we are looking for
			}

			return seacher.bestHit != null ? seacher.bestHit
					: seacher.bestAlternativeHit;
		}

	}

	/**
	 * Search method to find the closest node to a location specified by a test offset
	 * 
	 * @param nodes
	 *            The nodes to search within
	 * @param offSet
	 *            The offset to match a node to
	 * @param element
	 * @return The node closest to the offset or null
	 */
	public static INode searchCache(List<INode> nodes, int offSet,
			IVdmElement element)
	{
		synchronized (seacher)
		{
			if (DEBUG_PRINT)
			{
				System.out.println("Search start");
			}
			seacher.init();
			seacher.offSet = offSet;
			seacher.currentElement = element;
			try
			{
				if (elementNodeCache.get(element) == null
						|| elementNodeCache.get(element).isEmpty())
				{
					// elementNodeCache.put(element, new HashMap<ILexLocation, INode>());
					// seacher.indexing = true;
					// for (INode node : nodes)
					// {
					// node.apply(seacher);
					// }
					return null;
				} else
				{
					for (Entry<ILexLocation, INode> entry : elementNodeCache.get(element).entrySet())
					{
						seacher.check(entry.getValue(), entry.getKey());
					}
				}

			} catch (AnalysisException e)
			{
				// We found what we are looking for
			}

			return seacher.bestHit != null ? seacher.bestHit
					: seacher.bestAlternativeHit;
		}

	}

	public static void createIndex(List<INode> nodes, IVdmElement element)
			throws Throwable
	{
		seacher.init();
		seacher.currentElement = element;
		elementNodeCache.put(element, new HashMap<ILexLocation, INode>());
		seacher.indexing = true;
		for (INode node : nodes)
		{
			node.apply(seacher);
		}
	}

	@Override
	public void defaultInPDefinition(PDefinition node) throws AnalysisException
	{
		check(node, node.getLocation());
	}

	@Override
	public void defaultInPExp(PExp node) throws AnalysisException
	{
		check(node, node.getLocation());
	}

	@Override
	public void defaultInPStm(PStm node) throws AnalysisException
	{
		check(node, node.getLocation());
	}

	@Override
	public void caseAVariableExp(AVariableExp node) throws AnalysisException
	{
		check(node, node.getLocation());
	}

	@Override
	public void caseAFunctionType(AFunctionType node)
	{
		// Skip
	}

	@Override
	public void caseARecordInvariantType(ARecordInvariantType node)
	{
		// Skip
	}

	private void check(INode node, ILexLocation location)
			throws AnalysisException
	{
		if (DEBUG_PRINT)
		{
			System.out.println("Checking location span " + offSet + ": "
					+ location.getStartOffset() + " to "
					+ location.getEndOffset() + " line: "
					+ location.getStartLine() + ":" + location.getStartPos());
		}
		if (currentElement != null)
		{
			elementNodeCache.get(currentElement).put(location, node);
		}
		if (location.getStartOffset() - 1 <= this.offSet
				&& location.getEndOffset() - 1 >= this.offSet)
		{
			bestHit = node;
			if (!indexing)
			{
				throw new AnalysisException("Hit found stop search");
			}
		}

		// Store the last best match where best is closest with abs
		if (bestAlternativeLocation == null
				|| Math.abs(offSet - location.getStartOffset()) <= Math.abs(offSet
						- bestAlternativeLocation.getStartOffset()))
		{
			bestAlternativeLocation = location;
			bestAlternativeHit = node;
			if (DEBUG_PRINT)
			{
				System.out.println("Now best is: " + offSet + ": "
						+ location.getStartOffset() + " to "
						+ location.getEndOffset() + " line: "
						+ location.getStartLine() + ":"
						+ location.getStartPos());
			}
		} else if (bestAlternativeLocation == null
				|| (offSet - bestAlternativeLocation.getStartOffset() > 0)
				&& Math.abs(offSet - location.getStartOffset()) > Math.abs(offSet
						- bestAlternativeLocation.getStartOffset()))
		{
			if (DEBUG_PRINT)
			{
				System.out.println("Going back...");
			}
		} else
		{
			if (DEBUG_PRINT)
			{
				System.out.println("Rejected is: " + offSet + ": "
						+ location.getStartOffset() + " to "
						+ location.getEndOffset() + " line: "
						+ location.getStartLine() + ":"
						+ location.getStartPos());
			}
			if (!indexing)
			{
				throw new AnalysisException("Hit found stop search");
			}
		}
	}

	public static int[] getNodeOffset(INode node)
	{
		ILexLocation loc = null;
		if (node instanceof PDefinition)
		{
			loc =((PDefinition) node).getLocation();
		} else if (node instanceof PAction)
		{
			loc =((PAction) node).getLocation();
		} else if (node instanceof PProcess)
		{
			loc = ((PProcess) node).getLocation();
		} else if (node instanceof PExp)
		{
			loc = ((PExp) node).getLocation();
		} else if (node instanceof PStm)
		{
			loc =((PStm) node).getLocation();
		}else if(node instanceof AFieldField)
		{
			loc = ((AFieldField) node).getTagname().getLocation();
		}
		
		if(loc!=null)
		{
			return getNodeOffset(loc);
		}
		return NO_LOCATION;
	}

	public static int[] getNodeOffset(ILexLocation location)
	{
		if(location== null)
		{
			return NO_LOCATION;
		}
		return new int[] { location.getStartOffset() ,
				location.getEndOffset() - location.getStartOffset() };
	}

	// ////////////////////////////////////////////////////////////////////////////
	// CML
	// ////////////////////////////////////////////////////////////////////////////

	@Override
	public void defaultInPCMLType(PCMLType node) throws AnalysisException
	{
		check(node, node.getLocation());
	}

	@Override
	public void defaultInPCMLDefinition(PCMLDefinition node)
			throws AnalysisException
	{
		check(node, node.getLocation());
	}

	@Override
	public void defaultInPCMLExp(PCMLExp node) throws AnalysisException
	{
		check(node, node.getLocation());
	}

	@Override
	public void defaultInPAction(PAction node) throws AnalysisException
	{
		check(node, node.getLocation());
	}

	@Override
	public void defaultInPProcess(PProcess node) throws AnalysisException
	{
		check(node, node.getLocation());
	}

}

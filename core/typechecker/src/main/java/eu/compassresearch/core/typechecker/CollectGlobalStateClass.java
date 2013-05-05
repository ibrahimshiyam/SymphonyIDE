package eu.compassresearch.core.typechecker;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.definitions.AExplicitFunctionDefinition;
import org.overture.ast.definitions.AImplicitFunctionDefinition;
import org.overture.ast.definitions.PDefinition;

import eu.compassresearch.ast.analysis.AnalysisCMLAdaptor;
import eu.compassresearch.ast.definitions.AChannelNameDefinition;
import eu.compassresearch.ast.definitions.AChannelsDefinition;
import eu.compassresearch.ast.definitions.AChansetDefinition;
import eu.compassresearch.ast.definitions.AChansetsDefinition;
import eu.compassresearch.ast.definitions.AClassDefinition;
import eu.compassresearch.ast.definitions.AFunctionsDefinition;
import eu.compassresearch.ast.definitions.AProcessDefinition;
import eu.compassresearch.ast.definitions.ATypesDefinition;
import eu.compassresearch.ast.definitions.AValuesDefinition;
import eu.compassresearch.ast.program.PSource;
import eu.compassresearch.core.typechecker.api.TypeIssueHandler;

@SuppressWarnings("serial")
public class CollectGlobalStateClass extends AnalysisCMLAdaptor {

	private final Collection<PDefinition> members;
	private final Collection<PDefinition> channels;

	public static class GlobalDefinitions {
		public final Collection<PDefinition> definitions;
		public final Collection<PDefinition> channels;

		private GlobalDefinitions(Collection<PDefinition> defs,
				Collection<PDefinition> chns) {
			this.definitions = defs;
			this.channels = chns;
		}
	}

	public static GlobalDefinitions getGlobalRoot(Collection<PSource> sources,
			TypeIssueHandler issueHandler) throws AnalysisException {

		// Create visitor and visit each source collecting global definitions
		List<PDefinition> members = new LinkedList<PDefinition>();
		List<PDefinition> channels = new LinkedList<PDefinition>();
		CollectGlobalStateClass me = new CollectGlobalStateClass(members,
				channels);
		for (PSource source : sources) {
			source.apply(me);
		}

		// That's it
		return new GlobalDefinitions(members, channels);
	}

	@Override
	public void caseAChannelNameDefinition(AChannelNameDefinition node)
			throws AnalysisException {
		channels.add(node);
	}

	@Override
	public void caseAChannelsDefinition(AChannelsDefinition node)
			throws AnalysisException {

		LinkedList<AChannelNameDefinition> channels = node
				.getChannelNameDeclarations();
		for (AChannelNameDefinition channel : channels)
			channel.apply(this);
	}

	@Override
	public void defaultPSource(PSource node) throws AnalysisException {
		LinkedList<PDefinition> paragraphs = node.getParagraphs();
		for (PDefinition paragraph : paragraphs) {
			paragraph.apply(this);
		}
	}

	private CollectGlobalStateClass(List<PDefinition> members,
			Collection<PDefinition> channels) {
		this.members = members;
		this.channels = channels;
	}

	@Override
	public void caseAClassDefinition(AClassDefinition node)
			throws AnalysisException {
		members.add(node);
	}

	@Override
	public void caseATypesDefinition(ATypesDefinition node)
			throws AnalysisException {

		List<PDefinition> defs = TCDeclAndDefVisitor
				.handleDefinitionsForOverture(node);
		members.addAll(defs);
		super.caseATypesDefinition(node);
	}

	@Override
	public void caseAValuesDefinition(AValuesDefinition node)
			throws AnalysisException {
		List<PDefinition> defs = TCDeclAndDefVisitor
				.handleDefinitionsForOverture(node);
		members.addAll(defs);
	}

	@Override
	public void caseAFunctionsDefinition(AFunctionsDefinition node)
			throws AnalysisException {

		List<PDefinition> defs = TCDeclAndDefVisitor
				.handleDefinitionsForOverture(node);

		for (PDefinition fdef : defs) {

			PDefinition predef = null;
			PDefinition postdef = null;
			if (fdef instanceof AExplicitFunctionDefinition) {
				predef = ((AExplicitFunctionDefinition) fdef).getPredef();
				postdef = ((AExplicitFunctionDefinition) fdef).getPostdef();
			}

			if (fdef instanceof AImplicitFunctionDefinition) {
				predef = ((AImplicitFunctionDefinition) fdef).getPredef();
				postdef = ((AImplicitFunctionDefinition) fdef).getPostdef();
			}

			if (predef != null)
				members.addAll(TCDeclAndDefVisitor
						.handleDefinitionsForOverture(predef));
			// if (postdef != null)
			// members.addAll(TCDeclAndDefVisitor.handleDefinitionsForOverture(postdef));

		}

		members.addAll(defs);
	}

	@Override
	public void caseAProcessDefinition(AProcessDefinition node)
			throws AnalysisException {
		members.add(node);
	}

	@Override
	public void caseAChansetDefinition(AChansetDefinition node)
			throws AnalysisException {
		channels.add(node);
	}

	@Override
	public void caseAChansetsDefinition(AChansetsDefinition node)
			throws AnalysisException {
		for (PDefinition d : node.getChansets())
			d.apply(this);
	}

}

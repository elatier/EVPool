package uk.ac.imperial.evpool.actions;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.drools.runtime.ObjectFilter;
import org.drools.runtime.StatefulKnowledgeSession;

import com.google.inject.Inject;

import uk.ac.imperial.evpool.EVPoolService;
import uk.ac.imperial.evpool.facts.Player;
import uk.ac.imperial.presage2.core.Action;
import uk.ac.imperial.presage2.core.environment.ActionHandler;
import uk.ac.imperial.presage2.core.environment.ActionHandlingException;
import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.messaging.Input;

public class EVPoolActionHandler implements ActionHandler {

	final private Logger logger = Logger.getLogger(EVPoolActionHandler.class);
	final StatefulKnowledgeSession session;
	Map<UUID, Player> players = new HashMap<UUID, Player>();
	final EnvironmentServiceProvider serviceProvider;
	EVPoolService lpgservice = null;

	@Inject
	public EVPoolActionHandler(StatefulKnowledgeSession session,
                               EnvironmentServiceProvider serviceProvider)
			throws UnavailableServiceException {
		super();
		this.session = session;
		this.serviceProvider = serviceProvider;
	}

	EVPoolService getLPGService() {
		if (this.lpgservice == null) {
			try {
				this.lpgservice = serviceProvider.getEnvironmentService(EVPoolService.class);
			} catch (UnavailableServiceException e) {
				logger.warn("Could not get lpg service", e);
			}
		}
		return this.lpgservice;
	}

	@Override
	public boolean canHandle(Action action) {
		return action instanceof PlayerAction;
	}

	private synchronized Player getPlayer(final UUID id) {
		if (!players.containsKey(id)) {
			Collection<Object> rawPlayers = session
					.getObjects(new ObjectFilter() {
						@Override
						public boolean accept(Object object) {
							return object instanceof Player;
						}
					});
			for (Object pObj : rawPlayers) {
				Player p = (Player) pObj;
				players.put(p.getId(), p);
			}
		}
		return players.get(id);
	}

	@Override
	public Input handle(Action action, UUID actor)
			throws ActionHandlingException {
		Player p = getPlayer(actor);
		if (action instanceof PlayerAction) {
			((PlayerAction) action).setPlayer(p);
		}
		if (action instanceof TimestampedAction) {
			((TimestampedAction) action).setT(getLPGService().getRoundNumber());
		}
		session.insert(action);
		logger.debug("Handling: " + action);
		return null;
	}
}

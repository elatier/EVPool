package uk.ac.imperial.evpool;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.drools.runtime.ObjectFilter;
import org.drools.runtime.StatefulKnowledgeSession;

import uk.ac.imperial.evpool.facts.Cluster;
import uk.ac.imperial.evpool.facts.MemberOf;
import uk.ac.imperial.evpool.facts.Player;
import uk.ac.imperial.evpool.facts.Round;
import uk.ac.imperial.evpool.facts.Role;
import uk.ac.imperial.presage2.core.environment.EnvironmentRegistrationRequest;
import uk.ac.imperial.presage2.core.environment.EnvironmentService;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.event.EventBus;
import uk.ac.imperial.presage2.core.event.EventListener;
import uk.ac.imperial.presage2.core.simulator.EndOfTimeCycle;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class EvEnvService extends EnvironmentService {

	private final Logger logger = Logger.getLogger(this.getClass());
	final StatefulKnowledgeSession session;
	Map<UUID, Player> players = new HashMap<UUID, Player>();
	Map<UUID, MemberOf> members = new HashMap<UUID, MemberOf>();

	RoundType round = RoundType.INIT;
	int roundNumber = 0;

	@Inject
	protected EvEnvService(EnvironmentSharedStateAccess sharedState,
                           StatefulKnowledgeSession session, EventBus eb) {
		super(sharedState);
		this.session = session;
		eb.subscribe(this);
	}

	@EventListener
	public void onIncrementTime(EndOfTimeCycle e) {
		if (round == RoundType.DEMAND) {
			round = RoundType.APPROPRIATE;
			session.insert(new Round(roundNumber, RoundType.APPROPRIATE));
		} else {
			round = RoundType.DEMAND;
			session.insert(new Round(++roundNumber, RoundType.DEMAND));
		}
		logger.info("Next round: " + round
        + " roundNumber=" + roundNumber
        );
	}

	@Override
	public void registerParticipant(EnvironmentRegistrationRequest req) {

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

	private synchronized MemberOf getMemberOf(final UUID id) {
		if (!members.containsKey(id)) {
			Collection<Object> rawMembers = session
					.getObjects(new ObjectFilter() {
						@Override
						public boolean accept(Object object) {
							return object instanceof MemberOf;
						}
					});
			for (Object mObj : rawMembers) {
				MemberOf m = (MemberOf) mObj;
				members.put(m.player.getId(), m);
			}
		}
		MemberOf m = members.get(id);
		if (m != null && session.getFactHandle(m) == null) {
			members.remove(m);
			return null;
		}
		return m;
	}

	public RoundType getRound() {
		return round;
	}

	public int getRoundNumber() {
		return roundNumber;
	}

	public double getBatteryCap(UUID player) {
		return getPlayer(player).getBatterCap();
	}

    public Role getRole(UUID player) {
        return getPlayer(player).getRole();
    }

	public double getChargeLevel(UUID player) {
		return getPlayer(player).getChargeLevel();
	}

	public double getAllocated(UUID player) {
		return getPlayer(player).getAllocated();
	}

	public double getAppropriated(UUID player) {
		return getPlayer(player).getAppropriated();
	}

	public Cluster getCluster(final UUID player) {
		MemberOf m = getMemberOf(player);
		if (m != null)
			return m.getCluster();
		else
			return null;
	}

    public double getMaxChargeRate(UUID player) {
        return getPlayer(player).getMaxChargeRate();
    }

    public int getArrivalRound(UUID player) {
        return getPlayer(player).getArrivalRound();
    }
}

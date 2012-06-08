package uk.ac.imperial.evpool.allocators;

import org.drools.runtime.StatefulKnowledgeSession;
import uk.ac.imperial.evpool.actions.Allocate;
import uk.ac.imperial.evpool.facts.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NeedBasedAllocator {

	public static void allocate(StatefulKnowledgeSession session,
			List<Player> players, double poolSize, int t) {
		players = new ArrayList<Player>(players);
        Collections.sort(players, Player.COMPARE_BY_TOTAL_DEMANDED_AND_DEADLINE);
		for (Player p : players) {
			double allocation = Math.min(p.getD(), poolSize);
			session.insert(new Allocate(p, allocation, t));
			poolSize -= allocation;
		}
	}
}

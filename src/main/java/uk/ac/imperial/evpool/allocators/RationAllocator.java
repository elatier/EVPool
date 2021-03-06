package uk.ac.imperial.evpool.allocators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.drools.runtime.StatefulKnowledgeSession;

import uk.ac.imperial.evpool.actions.Allocate;
import uk.ac.imperial.evpool.facts.Player;

public class RationAllocator {

	private final static Logger logger = Logger
			.getLogger(RationAllocator.class);

	public static void allocate(StatefulKnowledgeSession session,
			List<Player> players, double poolSize, int t) {
		logger.info("Allocating...");
		double playerCtr = players.size();
		double allocation = poolSize / playerCtr;
		players = new ArrayList<Player>(players);
		Collections.sort(players, new Comparator<Player>() {
			@Override
			public int compare(Player o1, Player o2) {
				return Double.compare(o1.getD(), o2.getD());
			}
		});
		for (Player p : players) {
            logger.debug("Comparison: D=:"+p.getD()+"allocation:"+allocation);
            //logger.debug("Comparison: deadlineTurn=:"+(p.getDeadlineSpecified()-p.getTotalDemanded()));
			double toAllocate = Math.min(allocation, p.getD());
			session.insert(new Allocate(p, toAllocate, t));
			//playerCtr--;
			//poolSize -= toAllocate;           p
			//allocation = poolSize / playerCtr;
		}
	}
}

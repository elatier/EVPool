package uk.ac.imperial.evpool.allocators;

import org.apache.log4j.Logger;
import org.drools.runtime.StatefulKnowledgeSession;
import uk.ac.imperial.evpool.actions.Allocate;
import uk.ac.imperial.evpool.facts.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NeedBasedAllocator {

    private static final Logger logger = Logger
            .getLogger("uk.ac.imperial.evpool.NeedBasedAllocator");

    public static Comparator<Player> COMPARE_BY_CHAR_DEADLINE = new Comparator<Player>() {
        public int compare(Player one, Player other) {
            if (one.getCharDeadline() <  other.getCharDeadline()) {
                return -1;
            }
            if (one.getCharDeadline() >  other.getCharDeadline()) {
                return 1;
            } else {
                return 0;
            }
        }
    };

	public static void allocate(StatefulKnowledgeSession session,
                                     List<Player> players, double poolSize, int t) {
        players = new ArrayList<Player>(players);
        Collections.sort(players, COMPARE_BY_CHAR_DEADLINE);
        for (Player p : players) {
            //logger.debug("Comparison: totalD=:"+p.getTotalDemanded()+" deadline="+p.getDeadlineSpecified());
            //logger.debug("Comparison: deadlineTurn=:"+(p.getDeadlineSpecified()-p.getTotalDemanded()));
            double allocation = Math.min(p.getD(), poolSize);
            session.insert(new Allocate(p, allocation, t));
            poolSize -= allocation;
        }
    }


}

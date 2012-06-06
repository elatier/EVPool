package evpool;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.google.inject.AbstractModule;

import uk.ac.imperial.presage2.core.TimeDriven;
import uk.ac.imperial.presage2.core.simulator.InjectedSimulation;
import uk.ac.imperial.presage2.core.simulator.Parameter;
import uk.ac.imperial.presage2.core.simulator.Scenario;
import uk.ac.imperial.presage2.core.util.random.Random;
import uk.ac.imperial.presage2.util.environment.AbstractEnvironmentModule;
import uk.ac.imperial.presage2.util.location.Location;
import uk.ac.imperial.presage2.util.location.MoveHandler;
import uk.ac.imperial.presage2.util.location.ParticipantLocationService;
import uk.ac.imperial.presage2.util.location.area.Area;
import uk.ac.imperial.presage2.util.network.NetworkModule;


public class EVPGameSimulation extends InjectedSimulation implements TimeDriven {

	public EVPGameSimulation() {
		// TODO Auto-generated constructor stub
	}

	public EVPGameSimulation(Set<AbstractModule> modules) {
		super(modules);
		// TODO Auto-generated constructor stub
	}

    private LPGService game;

	@Parameter(name="size")
	public int size;
	 
	@Parameter(name="agents")
	public int agents;

    @Override
    protected Set<AbstractModule> getModules() {
        Set<AbstractModule> modules = new HashSet<AbstractModule>();
        modules.add(new AbstractEnvironmentModule()
                .addActionHandler(LPGActionHandler.class)
                .addParticipantGlobalEnvironmentService(LPGService.class)
     //           .setStorage(RuleStorage.class));
      //  modules.add(new RuleModule().addClasspathDrlFile("LPGDash.drl")
       //         .addClasspathDrlFile("RationAllocation.drl")
       //         .addClasspathDrlFile("RandomAllocation.drl")
       //         .addClasspathDrlFile("LegitimateClaimsAllocation.drl")
      //          .addStateTranslator(SimParticipantsTranslator.class));
        modules.add(NetworkModule.noNetworkModule());
        return modules;
    }


	/*@Override
	protected void addToScenario(Scenario s) {
		for (int i = 0; i < agents; i++) {
			int initialX = Random.randomInt(size);
			int initialY = Random.randomInt(size);
			Location startLoc = new Location(initialX, initialY);
			s.addParticipant(new EVAgent(Random.randomUUID(), "agent"+ i, startLoc, size));
		}
	}*/

    @Override
    protected void addToScenario(Scenario s) {
        //Random.seed = this.seed;
        s.addTimeDriven(this);
        //LegitimateClaims.sto = this.storage;

        for (int n = 0; n < agents; n++) {
            UUID pid = Random.randomUUID();
            s.addParticipant(new EVAgent(pid, "c" + n,));
            //Player p = new Player(pid, "c" + n, "C", alpha, beta);
            //players.add(p);
            //session.insert(p);
            //session.insert(new JoinCluster(p, c));
            //session.insert(new Generate(p, game.getRoundNumber() + 1));
        }
    }

    @Override
    public void incrementTime() {
       // if (this.game.getRound() == RoundType.APPROPRIATE) {
            // generate new g and q
           // for (Player p : players) {
           //     session.insert(new Generate(p, game.getRoundNumber() + 1));
           // }
        }
    }


}

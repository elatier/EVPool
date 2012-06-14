package uk.ac.imperial.evpool;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.drools.runtime.StatefulKnowledgeSession;

import uk.ac.imperial.evpool.actions.EVPoolActionHandler;
import uk.ac.imperial.evpool.actions.JoinCluster;
import uk.ac.imperial.evpool.facts.Allocation;
import uk.ac.imperial.evpool.facts.Cluster;
import uk.ac.imperial.evpool.facts.Player;
import uk.ac.imperial.presage2.core.TimeDriven;
import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.simulator.InjectedSimulation;
import uk.ac.imperial.presage2.core.simulator.Parameter;
import uk.ac.imperial.presage2.core.simulator.Scenario;
import uk.ac.imperial.presage2.core.util.random.Random;
import uk.ac.imperial.presage2.rules.RuleModule;
import uk.ac.imperial.presage2.rules.RuleStorage;
import uk.ac.imperial.presage2.rules.facts.SimParticipantsTranslator;
import uk.ac.imperial.presage2.util.environment.AbstractEnvironmentModule;
import uk.ac.imperial.presage2.util.network.NetworkModule;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;

public class EVPoolSimulation extends InjectedSimulation implements TimeDriven {

	private final Logger logger = Logger
			.getLogger("uk.ac.imperial.evpool.RuleEngine");
	private StatefulKnowledgeSession session;

	private Set<Player> players = new HashSet<Player>();
	private EVPoolService game;

	@Parameter(name = "cCount")
	public int cCount;

    @Parameter(name = "mCPR")
    public double mCPR;

    @Parameter(name = "bC")
    public double bC;

    @Parameter(name = "mCR")
    public double mCR;

	@Parameter(name = "clusters")
	public String clusters;

	@Parameter(name = "seed")
	public int seed;

    @Parameter(name = "timeStepHour")
    public double timeStepHour;

    public EVPoolSimulation(Set<AbstractModule> modules) {
		super(modules);
    }

	@Inject
	public void setSession(StatefulKnowledgeSession session) {
		this.session = session;
	}

	@Inject
	public void setServiceProvider(EnvironmentServiceProvider serviceProvider) {
		try {
			this.game = serviceProvider.getEnvironmentService(EVPoolService.class);
			//LegitimateClaims.game = this.game;
		} catch (UnavailableServiceException e) {
			logger.warn("", e);
		}
	}

	@Override
	protected Set<AbstractModule> getModules() {
		Set<AbstractModule> modules = new HashSet<AbstractModule>();
		modules.add(new AbstractEnvironmentModule()
				.addActionHandler(EVPoolActionHandler.class)
				.addParticipantGlobalEnvironmentService(EVPoolService.class)
				.setStorage(RuleStorage.class)
				 );
		modules.add(new RuleModule().addClasspathDrlFile("LPGDash.drl")
                .addClasspathDrlFile("RationAllocation.drl")
                .addClasspathDrlFile("RandomAllocation.drl")
                .addClasspathDrlFile("NeedBasedAllocation.drl")
                        //.addClasspathDrlFile("LegitimateClaimsAllocation.drl")
                .addStateTranslator(SimParticipantsTranslator.class));
		modules.add(NetworkModule.noNetworkModule());
		return modules;
	}

	@Override
	protected void addToScenario(Scenario s) {
		Random.seed = this.seed;
		s.addTimeDriven(this);
		session.setGlobal("logger", this.logger);
		session.setGlobal("session", session);
		session.setGlobal("storage", this.storage);
		//LegitimateClaims.sto = this.storage;

        Allocation c0All = Allocation.RANDOM;
		for (Allocation a : Allocation.values()) {
			if (clusters.equalsIgnoreCase(a.name())) {
				c0All = a;
				break;
			}
		}

        int endRound = (finishTime-1)/2-1;
        double maxChargePointRate = mCPR*timeStepHour;
        double batteryCap = bC;
        double maxChargeRate = mCR*timeStepHour;
        double headProvision = cCount *0.15*maxChargePointRate;
        Cluster c = new Cluster(0, c0All, maxChargePointRate);
        session.insert(c);

		for (int n = 0; n < cCount; n++) {
			UUID pid = Random.randomUUID();
            //time starts at 15:00, so round 1 is at 15:00, arrive from 16:00 to 19:00
            int arrivalRound = (int) (1/timeStepHour) + (int) Math.round((3/timeStepHour) * Random.randomDouble());
            //depart from 6, up to 8 randomly, or round 60 to 68
            int evDepartureRound = (int) (15/timeStepHour) + (int) Math.round((2/timeStepHour) * Random.randomDouble());
            //initial capacity random from 20% to 100%
            double initialCapacity =  Math.round(batteryCap*0.9 - Random.randomDouble() * (batteryCap * 0.7)) ;

			s.addParticipant(new EVPoolPlayer(pid, "c" + n, headProvision, evDepartureRound));
			Player p = new Player(pid, "c" + n, "C", batteryCap, initialCapacity, maxChargeRate, arrivalRound, c);
			players.add(p);
			session.insert(p);
			//session.insert(new JoinCluster(p, c));
			//session.insert(new Generate(p, game.getRoundNumber() + 1));

		}
	}

	@Override
	public void incrementTime() {
		if (this.game.getRound() == RoundType.APPROPRIATE) {
			// generate new g and q
			for (Player p : players) {
                if (p.getArrivalRound() == this.game.getRoundNumber())
                    session.insert(new JoinCluster(p, p.getCluster()));
			}
		}
	}

}

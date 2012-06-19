package uk.ac.imperial.evpool;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import cern.jet.random.Normal;
import cern.jet.random.engine.RandomEngine;
import cern.jet.random.engine.MersenneTwister64;
import org.apache.log4j.Logger;
import org.drools.runtime.StatefulKnowledgeSession;

import uk.ac.imperial.evpool.actions.EVPoolActionHandler;
import uk.ac.imperial.evpool.actions.JoinCluster;
import uk.ac.imperial.evpool.db.CSVImport;
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

import static uk.ac.imperial.evpool.db.CSVImport.importGridLoad;


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

    @Parameter(name = "loadLevel")
    public double loadLevel;

    @Parameter(name = "timeStepHour")
    public double timeStepHour;

    @Parameter(name = "gridLoadFilename")
    public String gridLoadFilename;

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
                //.addClasspathDrlFile("RationAllocation.drl")
                .addClasspathDrlFile("RandomAllocation.drl")
                .addClasspathDrlFile("NeedBasedAllocation.drl")
                        //.addClasspathDrlFile("LegitimateClaimsAllocation.drl")
                .addStateTranslator(SimParticipantsTranslator.class));
		modules.add(NetworkModule.noNetworkModule());
		return modules;
	}

	@Override
	protected void addToScenario(Scenario s) {

        RandomEngine engine = new MersenneTwister64(seed);
        Normal normal = new Normal(0, 1, engine);

        double maxChargePointRate = mCPR*timeStepHour;
        double batteryCap = bC;
        double maxChargeRate = mCR*timeStepHour;
       // double headProvision = cCount *loadLevel*maxChargePointRate;

        Map<Integer,Double> gridLoad = importGridLoad(gridLoadFilename, 24, 2);
		Random.seed = this.seed;
		s.addTimeDriven(this);
        //this.storage = null;
		session.setGlobal("logger", this.logger);
		session.setGlobal("session", session);
		session.setGlobal("storage", this.storage);
        session.setGlobal("gridLoad", gridLoad);
        session.setGlobal("loadLevel", loadLevel);
        session.setGlobal("maxChargePointRate", maxChargePointRate);

        Allocation c0All = Allocation.RANDOM;
		for (Allocation a : Allocation.values()) {
			if (clusters.equalsIgnoreCase(a.name())) {
				c0All = a;
				break;
			}
		}

        Cluster c = new Cluster(0, c0All, maxChargePointRate);
        session.insert(c);

		for (int n = 0; n < cCount; n++) {
			UUID pid = Random.randomUUID();
            //time starts at 12:00pm, so round 1 is at 12:00, arrive home 18+-N(0,1)
            int arrivalRound = (int) (6/timeStepHour) + (int) Math.round((1/timeStepHour) * normal.nextDouble());
            //depart from 8 randomly, or
            int evDepartureRound = (int) (20/timeStepHour) + (int) Math.round((1/timeStepHour) * normal.nextDouble());
            //initial capacity random from 20% to 90%
            double initialCapacity =  batteryCap*0.9 - Random.randomDouble() * (batteryCap * 0.7);

			s.addParticipant(new EVPoolPlayer(pid, "c" + n, evDepartureRound,gridLoad));
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
			// add players to clusters when they arrival times comes
			for (Player p : players) {
                if (p.getArrivalRound() == this.game.getRoundNumber())
                    session.insert(new JoinCluster(p, p.getCluster()));
			}
		}
	}

}

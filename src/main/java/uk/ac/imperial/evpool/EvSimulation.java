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

import uk.ac.imperial.evpool.actions.EvActionHandler;
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

import static uk.ac.imperial.evpool.io.CSVImport.importGridLoad;


public class EvSimulation extends InjectedSimulation implements TimeDriven {

	private final Logger logger = Logger
			.getLogger("uk.ac.imperial.evpool.RuleEngine");
	private StatefulKnowledgeSession session;

	private Set<Player> players = new HashSet<Player>();
	private EvEnvService game;

	@Parameter(name = "agentCount")
	public int agentCount;

    @Parameter(name = "minSOC")
    public double minSOC;

    @Parameter(name = "maxSOC")
    public double maxSOC;


    @Parameter(name = "mCPR")
    public double mCPR;

    @Parameter(name = "bC")
    public double bC;

    @Parameter(name = "mCR")
    public double mCR;

	@Parameter(name = "allocM")
	public String allocM;

	@Parameter(name = "seed")
	public int seed;

    @Parameter(name = "loadLevel")
    public double loadLevel;

    @Parameter(name = "timeStepHour")
    public double timeStepHour;

    @Parameter(name = "gridLoadFilename")
    public String gridLoadFilename;

    @Parameter(name = "usageSteepness")
    public double usageSteepness;

    public EvSimulation(Set<AbstractModule> modules) {
		super(modules);
    }

	@Inject
	public void setSession(StatefulKnowledgeSession session) {
		this.session = session;
	}

	@Inject
	public void setServiceProvider(EnvironmentServiceProvider serviceProvider) {
		try {
			this.game = serviceProvider.getEnvironmentService(EvEnvService.class);
		} catch (UnavailableServiceException e) {
			logger.warn("", e);
		}
	}

    @Override
	protected Set<AbstractModule> getModules() {
		Set<AbstractModule> modules = new HashSet<AbstractModule>();
		modules.add(new AbstractEnvironmentModule()
				.addActionHandler(EvActionHandler.class)
				.addParticipantGlobalEnvironmentService(EvEnvService.class)
				.setStorage(RuleStorage.class)
				 );
		modules.add(new RuleModule()
                .addClasspathDrlFile("StorageRules.drl")
                .addClasspathDrlFile("HeadRules.drl")
                .addClasspathDrlFile("ActionRules.drl")
                .addClasspathDrlFile("ResourceAllocation.drl")
                .addClasspathDrlFile("LPGDash.drl")
                .addStateTranslator(SimParticipantsTranslator.class));
		modules.add(NetworkModule.noNetworkModule());
		return modules;
	}

	@Override
	protected void addToScenario(Scenario s) {

        Boolean useMinimumPool = true;
        Boolean randomChargingStart = false;

        double maxChargePointRate = mCPR*timeStepHour;
        double batteryCap = bC;
        double maxChargeRate = mCR*timeStepHour;

        Map<Integer,Double> gridLoad = importGridLoad(gridLoadFilename, 25, 2);
		Random.seed = this.seed;
		s.addTimeDriven(this);
        //this.storage = null;
		session.setGlobal("logger", this.logger);
		session.setGlobal("session", session);
		session.setGlobal("storage", this.storage);
        session.setGlobal("gridLoad", gridLoad);
        session.setGlobal("loadLevel", loadLevel);
        session.setGlobal("usageSteepness", usageSteepness);
        session.setGlobal("useMinimumPool", useMinimumPool);
        session.setGlobal("maxChargePointRate", maxChargePointRate);

        Allocation clusterAlloc = Allocation.RANDOM;
		for (Allocation a : Allocation.values()) {
			if (allocM.equalsIgnoreCase(a.name())) {
				clusterAlloc = a;
				break;
			}
		}

        Cluster c = new Cluster(0, clusterAlloc, maxChargePointRate);
        session.insert(c);

        RandomEngine engine = new MersenneTwister64(seed);
        Normal normal = new Normal(0, 2, engine);

		for (int n = 0; n < agentCount; n++) {
			UUID pid = Random.randomUUID();
            //time starts at 12:00pm, so round 1 is at 12:00, arrive home 18+-N(0,2)
            int arrivalRound = (int) (6/timeStepHour) + (int) Math.round((1/timeStepHour) * normal.nextDouble());
            //depart from home at 8+-N(0,2)
            int evDepartureRound = (int) (20/timeStepHour) + (int) Math.round((1/timeStepHour) * normal.nextDouble());
            //initial capacity random from minSOC% to maxSOC%
            double initialCapacity =  batteryCap*maxSOC - Random.randomDouble() * (batteryCap * (maxSOC-minSOC));

            if (randomChargingStart)   {
                double totalDemand = Math.max(batteryCap - initialCapacity, 0);
                double roundDemand = Math.min(totalDemand,
                        Math.min(maxChargePointRate, maxChargeRate)
                );
                double totalDemandInTurns = Math.ceil(totalDemand / Math.min(maxChargePointRate, maxChargeRate));
                 int chargingDeadline = (int) (evDepartureRound - totalDemandInTurns);
                //postpone charging randomly until charging deadline, implemented as postponing arrival
                arrivalRound += (int) ((chargingDeadline - arrivalRound) * Random.randomDouble());
            }

			s.addParticipant(new EvPlayer(pid, "c" + n, evDepartureRound));
			Player p = new Player(pid, "c" + n, "C", batteryCap, initialCapacity, maxChargeRate, arrivalRound, c);
			players.add(p);
			session.insert(p);
        }
        for (int n = 0; n < 1; n++) {
            UUID pid2 = Random.randomUUID();
            s.addParticipant(new GenPlayer(pid2, "g" + n));
            Player p2 = new Player(pid2, "g" + n, "C", 0, 0, maxChargePointRate*10, 1, c);
            players.add(p2);
            session.insert(p2);
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

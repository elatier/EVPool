package uk.ac.imperial.evpool;

import org.apache.log4j.Logger;
import uk.ac.imperial.evpool.actions.Appropriate;
import uk.ac.imperial.evpool.actions.Demand;
import uk.ac.imperial.evpool.actions.LeaveCluster;
import uk.ac.imperial.evpool.actions.Provision;
import uk.ac.imperial.evpool.facts.Cluster;
import uk.ac.imperial.evpool.facts.Role;
import uk.ac.imperial.presage2.core.db.persistent.TransientAgentState;
import uk.ac.imperial.presage2.core.environment.ActionHandlingException;
import uk.ac.imperial.presage2.core.environment.ParticipantSharedState;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.core.util.random.Random;
import uk.ac.imperial.presage2.util.participant.AbstractParticipant;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class GenPlayer extends AbstractParticipant {

   	Cluster cluster = null;

	protected EvEnvService game;

    private static final Logger logger = Logger
            .getLogger("uk.ac.imperial.evpool.EvPlayer");
    private Map<Integer,Double> gridLoad;


    public GenPlayer(UUID id, String name) {
		super(id, name);
	}

	public GenPlayer(UUID id, String name, int departureRound) {
		super(id, name);

	}

	@Override
	protected void processInput(Input in) {

	}

	@Override
	public void initialise() {
		super.initialise();
		try {
			this.game = this.getEnvironmentService(EvEnvService.class);
		} catch (UnavailableServiceException e) {
			logger.warn("Couldn't get environment service", e);
		}
	}

	@Override
	protected Set<ParticipantSharedState> getSharedState() {
		Set<ParticipantSharedState> ss = super.getSharedState();
		return ss;
	}

	@Override
	public void execute() {
		super.execute();

		this.cluster = this.game.getCluster(getID());
        if (this.cluster == null) {
			return;
		}

		if (game.getRound() == RoundType.DEMAND) {
            if (game.getRoundNumber() == game.getArrivalRound(getID())+1) {


            } else {
                //storeData();
            }
            //batteryCap = game.getBatteryCap(getID());
            double maxChargeRate = game.getMaxChargeRate(getID());
            //maxChargePointRate = cluster.getChargePointRate();
             provision(maxChargeRate* Random.randomDouble());
            //chargeLevel = game.getChargeLevel(getID());

            if (game.getRole(getID()) == Role.HEAD) {
                  //
            }   else {
                // provision(0);
            }
            // demand(roundDemand,departureRound,chargingDeadline);


		} else if (game.getRound() == RoundType.APPROPRIATE) {
			    appropriate(game.getAllocated(getID()));
         }
	}

	protected void demand(double d, int deadline, int chargingDeadline) {
		try {
			environment.act(new Demand(d, deadline, chargingDeadline), getID(), authkey);
		} catch (ActionHandlingException e) {
			logger.warn("Failed to demand", e);
		}
	}

	protected void provision(double p) {
		try {
			environment.act(new Provision(p), getID(), authkey);
		} catch (ActionHandlingException e) {
			logger.warn("Failed to provision", e);
		}
	}

	protected void appropriate(double r) {
		try {
			environment.act(new Appropriate(r), getID(), authkey);
		} catch (ActionHandlingException e) {
			logger.warn("Failed to appropriate", e);
		}
	}

	protected void leaveCluster() {
		try {
			environment.act(new LeaveCluster(this.cluster), getID(), authkey);
		} catch (ActionHandlingException e) {
			logger.warn("Failed to leave cluster", e);
		}
	}


}

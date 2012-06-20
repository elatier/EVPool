package uk.ac.imperial.evpool;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
import uk.ac.imperial.presage2.util.participant.AbstractParticipant;

public class EvPlayer extends AbstractParticipant {

    double batteryCap = 0;   //  full charge capacity
    double chargeLevel = 0;  // current charge level (units of charge)
    double maxChargeRate = 0.01;  //max charge rate for battery per timestep
    double maxChargePointRate = 0.01; //the max rate supported by the chargePoint

    int departureRound = 0;

    double p = 0;    //provision
    double d = 0;    //demanded
    int specifiedDeadline = 0;

    private double toAppropriate;
    private int charDeadline;

	Cluster cluster = null;

	protected EvEnvService game;

    private static final Logger logger = Logger
            .getLogger("uk.ac.imperial.evpool.EvPlayer");
    private Map<Integer,Double> gridLoad;


    public EvPlayer(UUID id, String name) {
		super(id, name);
	}

	public EvPlayer(UUID id, String name, int departureRound) {
		super(id, name);
        this.departureRound = departureRound;
        this.gridLoad = gridLoad;

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
                   //initial round
            } else {
                storeData();
            }

            batteryCap = game.getBatteryCap(getID());
            maxChargeRate = game.getMaxChargeRate(getID());
            maxChargePointRate = cluster.getChargePointRate();
            chargeLevel = game.getChargeLevel(getID());

            double totalDemand = Math.max(batteryCap - chargeLevel, 0);
            double roundDemand = Math.min(totalDemand,
                    Math.min(maxChargePointRate, maxChargeRate)
                    );
            double totalDemandInTurns = Math.ceil(totalDemand /
                    Math.min(maxChargePointRate, maxChargeRate));

            int chargingDeadline = Integer.MAX_VALUE;
            if (totalDemandInTurns != 0.0) {
                chargingDeadline = (int) (departureRound - totalDemandInTurns);
            }

            if (game.getRole(getID()) == Role.HEAD) {
                  //
            }   else {
                //provision(0);
            }

			if ( game.getRoundNumber() == departureRound) {

				if (totalDemand > 0.0) {
                    leaveCluster();
                    logger.warn("Player " + this + " not charged at:"+ departureRound +", "+ totalDemand
                            + " more was needed. "+"Charging deadline: " + chargingDeadline);
                }
                 else {
                    leaveCluster();
                    logger.info("Player " + this + " contract was satisfied.");
                }

			}
            else
            {
                demand(roundDemand,departureRound,chargingDeadline);
			}
		} else if (game.getRound() == RoundType.APPROPRIATE) {
			    appropriate(game.getAllocated(getID()));
        }
	}

	protected void demand(double d, int deadline, int chargingDeadline) {
		try {
			environment.act(new Demand(d, deadline, chargingDeadline), getID(), authkey);
			this.d = d;
            this.specifiedDeadline = deadline;
            this.charDeadline = chargingDeadline;
		} catch (ActionHandlingException e) {
			logger.warn("Failed to demand", e);
		}
	}

	protected void provision(double p) {
		try {
			environment.act(new Provision(p), getID(), authkey);
			this.p = p;
		} catch (ActionHandlingException e) {
			logger.warn("Failed to provision", e);
		}
	}

	protected void appropriate(double r) {
		try {
            this.toAppropriate = r;
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

    protected void storeData() {
        double r = game.getAllocated(getID());
        double rP = game.getAppropriated(getID());
        if (this.persist != null) {
            TransientAgentState state = this.persist.getState(game.getRoundNumber()-1);
            state.setProperty("bC", Double.toString(batteryCap));
            state.setProperty("cL", Double.toString(chargeLevel));
            state.setProperty("mCR", Double.toString(maxChargeRate));
            state.setProperty("mCPR", Double.toString(maxChargePointRate));
            state.setProperty("p", Double.toString(p));
            state.setProperty("d", Double.toString(d));
            state.setProperty("deadline", Double.toString(specifiedDeadline));
            state.setProperty("charDeadline", Double.toString(charDeadline));
            state.setProperty("r", Double.toString(r));
            state.setProperty("rP", Double.toString(rP));
        }
    }

}

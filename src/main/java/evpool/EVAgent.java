package evpool;


import java.util.Set;
import java.util.UUID;

import uk.ac.imperial.presage2.core.environment.ActionHandlingException;
import uk.ac.imperial.presage2.core.environment.ParticipantSharedState;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.core.simulator.SimTime;
import uk.ac.imperial.presage2.core.util.random.Random;
import uk.ac.imperial.presage2.util.location.Location;
import uk.ac.imperial.presage2.util.location.Move;
import uk.ac.imperial.presage2.util.location.ParticipantLocationService;
import uk.ac.imperial.presage2.util.participant.AbstractParticipant;



public class EVAgent extends AbstractParticipant {

	Location myLoc;
	private int size;
	 
	EVAgent(UUID id, String name) {
		super(id, name);
		this.myLoc = myLoc;
		this.size = size;
	}
	
	@Override
	protected void processInput(Input arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	protected Set<ParticipantSharedState> getSharedState() {
		Set<ParticipantSharedState> ss = super.getSharedState();
		ss.add(ParticipantLocationService.createSharedState(getID(), myLoc));
		return ss;
	}
	
	// variable to keep the location service.
	ParticipantLocationService locationService;
	 
	@Override
	public void initialise() {
		super.initialise();
		// get the ParticipantLocationService.
		try {
			this.locationService = getEnvironmentService(ParticipantLocationService.class);
		} catch (UnavailableServiceException e) {
			logger.warn(e);
		}
	}
	
	@Override
	public void execute() {
		myLoc = locationService.getAgentLocation(getID());
	 
		logger.info("My location is: "+ this.myLoc);
	 
		// Create a random Move.
		int dx = Random.randomInt(2);
		int dy = Random.randomInt(2);
		Move move = new Move(dx, dy);
		logger.info("My rand is: "+ dx + " "+ dy);
		// submit move action to the environment.
		try {
			environment.act(move, getID(), authkey);
		} catch (ActionHandlingException e) {
			logger.warn("Error trying to move", e);
		}
		
		// get current simulation time
		int time = SimTime.get().intValue();
		// check db is available
		if (this.persist != null) {
			// save our location for this timestep
			this.persist.getState(time).setProperty("location", this.myLoc.toString());
		}
		
	}
	
}

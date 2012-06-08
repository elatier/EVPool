package uk.ac.imperial.evpool.actions;

import uk.ac.imperial.evpool.facts.Player;

abstract class PlayerAction extends TimestampedAction {

	Player player;

	PlayerAction() {
		super();
	}

	PlayerAction(int t, Player player) {
		super(t);
		this.player = player;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

}

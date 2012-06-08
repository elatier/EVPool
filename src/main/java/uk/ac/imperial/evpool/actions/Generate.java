package uk.ac.imperial.evpool.actions;

import uk.ac.imperial.evpool.facts.Player;
import uk.ac.imperial.presage2.core.util.random.Random;

public class Generate extends TimestampedAction {

	public final Player player;
	public final double g;
	public final double q;

	public Generate(Player player, int roundNumber) {
		super(roundNumber);
		this.player = player;
		this.g = Random.randomDouble();
		this.q = this.g + (Random.randomDouble() * (1 - this.g));
	}

	public Player getPlayer() {
		return player;
	}

	public double getG() {
		return g;
	}

	public double getQ() {
		return q;
	}

	@Override
	public String toString() {
		return "Generate [player=" + player.getName() + ", g=" + g + ", q=" + q + ", roundNumber="
				+ t + "]";
	}

}

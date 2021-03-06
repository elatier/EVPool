package uk.ac.imperial.evpool.actions;

import uk.ac.imperial.evpool.facts.Player;

public class Provision extends PlayerAction {

	public double quantity;

	public Provision(double quantity) {
		this.quantity = quantity;
	}

	public Provision(int t, Player player, double quantity) {
		super(t, player);
		this.quantity = quantity;
	}

	@Override
	public String toString() {
		return "Provision [quantity=" + quantity + ", player="
				+ player.getName() + ", t=" + t + "]";
	}

	public Double getQuantity() {
		return quantity;
	}
}

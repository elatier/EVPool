package uk.ac.imperial.evpool.actions;

import uk.ac.imperial.evpool.facts.Player;

public class Demand extends PlayerAction {

	double quantity;
    int deadline;
    int charDeadline;

    public Demand(double quantity, int deadline, int chargingDeadline) {
		this.quantity = quantity;
        this.deadline = deadline;
        this.charDeadline = chargingDeadline;
	}

	public Demand(int t, Player player) {
		super(t, player);

	}

    @Override
    public String toString() {
        return "Demand{" +
                "quantity=" + quantity +
                ", deadline=" + deadline +
                ", charDeadline=" + charDeadline +
                '}';
    }

	public double getQuantity() {
		return quantity;
	}

    public int getDeadline() {
        return deadline;
    }

    public int getCharDeadline() {
        return charDeadline;
    }
}

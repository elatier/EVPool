package uk.ac.imperial.evpool.actions;

import uk.ac.imperial.evpool.facts.Player;

public class Demand extends PlayerAction {

	double quantity;
    double total;
    int deadline;
    double charDeadline;

    public Demand(double quantity, double total, int deadline, double chargingDeadline) {
		this.quantity = quantity;
        this.deadline = deadline;
        this.total = total;
        this.charDeadline = chargingDeadline;
	}

	public Demand(int t, Player player) {
		super(t, player);

	}

    @Override
    public String toString() {
        return "Demand[" +
                "quantity=" + quantity +
                ", player=" + player.getName() +
                ", total=" + total +
                ", deadline=" + deadline +
                ']';
    }

	public double getQuantity() {
		return quantity;
	}

    public double getTotal() {
        return total;
    }

    public int getDeadline() {
        return deadline;
    }

    public double getCharDeadline() {
        return charDeadline;
    }
}

package uk.ac.imperial.evpool.actions;

import uk.ac.imperial.evpool.facts.Player;

public class Demand extends PlayerAction {

	double quantity;
    double total;
    int deadline;

	public Demand(double quantity, double total, int deadline) {
		this.quantity = quantity;
        this.deadline = deadline;
        this.total = total;
	}

	public Demand(int t, Player player, double quantity, double total, int deadline) {
		super(t, player);
		this.quantity = quantity;
        this.deadline = deadline;
        this.total = total;
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
}
package uk.ac.imperial.evpool.facts;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;

public class Cluster {

	final int id;
	Allocation allocationMethod;
    double maxChargePointRate;

	public Cluster(int id, Allocation allocationMethod, double maxChargerRate) {
		super();
		this.id = id;
		this.allocationMethod = allocationMethod;
        this.maxChargePointRate = maxChargerRate;
	}

	@Override
	public String toString() {
		return "Cluster " + id + "";
	}

	public int getId() {
		return id;
	}

	public Allocation getAllocationMethod() {
		return allocationMethod;
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Cluster other = (Cluster) obj;
		if (id != other.id)
			return false;
		return true;
	}

    public double getChargePointRate() {
        return maxChargePointRate;
    }
}

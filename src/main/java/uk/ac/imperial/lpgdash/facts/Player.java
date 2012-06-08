package uk.ac.imperial.lpgdash.facts;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Player {

	UUID id;
	final String name;
	String type = "C";
	//double g = 0;
	//double q = 0;
    double batteryCap = 0;   //total units that can be stored int the battery
    double chargeLevel = 0;  // units of charge int the battery
	double d = 0;    //demanded

    double totalDemanded = 0;
    int deadlineSpecified = 0;
	double allocated = 0;
	double appropriated = 0;
	//double alpha = 0.1;
	//double beta = 0.1;
	Role role = Role.PROSUMER;

	Map<Cluster, PlayerHistory> history = new HashMap<Cluster, PlayerHistory>();

	public Player(UUID aid) {
		super();
		this.id = aid;
		this.name = "n/a";
	}

	public Player(UUID id, String name, String type, double batteryCap, double chargeLevel) {
		super();
		this.id = id;
		this.name = name;
		this.type = type;
		this.batteryCap = batteryCap;
		this.chargeLevel = chargeLevel;
	}

    public static Comparator<Player> COMPARE_BY_TOTAL_DEMANDED = new Comparator<Player>() {
        public int compare(Player one, Player other) {
            if (one.totalDemanded > other.totalDemanded) {
                return -1;
            }
            else {
                return 1;
            }

        }
    };

    public static Comparator<Player> COMPARE_BY_TOTAL_DEMANDED_AND_DEADLINE = new Comparator<Player>() {
        public int compare(Player one, Player other) {
            if ((one.deadlineSpecified - one.totalDemanded) <
                    (other.deadlineSpecified - other.totalDemanded)) {
                return -1;
            }
            else {
                return 1;
            }

        }
    };


    public double getTotalDemanded() {
        return totalDemanded;
    }

    public void setTotalDemanded(double totalDemanded) {
        this.totalDemanded = totalDemanded;
    }

    public int getDeadlineSpecified() {
        return deadlineSpecified;
    }

    public void setDeadlineSpecified(int deadlineSpecified) {
        this.deadlineSpecified = deadlineSpecified;
    }


	@Override
	public String toString() {
		return "Player [" + name + ", type=" + type +", role=" + role + ", bC=" + batteryCap + ", cL=" + chargeLevel
				+ "]";
	}

	public UUID getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public double getBatterCap() {
		return batteryCap;
	}

	public void setBatteryCap(double batteryCap) {
		this.batteryCap = batteryCap;
	}

	public double getChargeLevel() {
		return chargeLevel;
	}

	public void setChargeLevel(double chargeLevel) {
		this.chargeLevel = chargeLevel;
	}

	public double getD() {
		return d;
	}

	public void setD(double d) {
		this.d = d;
	}

	public double getAllocated() {
		return allocated;
	}

	public void setAllocated(double allocated) {
		this.allocated = allocated;
	}

	public double getAppropriated() {
		return appropriated;
	}

	public void setAppropriated(double appropriated) {
		this.appropriated = appropriated;
	}

    public void chargeBattery(double howMuch) {
        chargeLevel+=howMuch;
    }

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public Map<Cluster, PlayerHistory> getHistory() {
		return history;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Player other = (Player) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}

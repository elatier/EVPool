package uk.ac.imperial.evpool.facts;

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
    double batteryCap = 0;   //  full charge capacity
    double chargeLevel = 0;  // current charge level (units of charge)
	double maxChargeRate = 0;  //max charge rate for battery per timestep


    double d = 0;    //demanded
    double totalDemanded = 0;
    int deadlineSpecified = 0;
    
	double allocated = 0;
	double appropriated = 0;
	Role role = Role.PROSUMER;

	Map<Cluster, PlayerHistory> history = new HashMap<Cluster, PlayerHistory>();
    int arrivalRound = 0;

    public Cluster getCluster() {
        return cluster;
    }

    Cluster cluster;

    public int getArrivalRound() {
        return arrivalRound;
    }

    public Player(UUID aid) {
		super();
		this.id = aid;
		this.name = "n/a";
	}

	public Player(UUID id, String name, String type, double batteryCap, double chargeLevel,
                  double maxChargeRate, int arrivalRound, Cluster c) {
		super();
		this.id = id;
		this.name = name;
		this.type = type;
		this.batteryCap = batteryCap;
		this.chargeLevel = chargeLevel;
        this.maxChargeRate = maxChargeRate;
        this.arrivalRound = arrivalRound;
        this.cluster = c;
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
        return "Player[" +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", batteryCap=" + batteryCap +
                ", chargeLevel=" + chargeLevel +
                ", maxChargeRate=" + maxChargeRate +
                ", d=" + d +
                ", totalDemanded=" + totalDemanded +
                ", deadlineSpecified=" + deadlineSpecified +
                ", allocated=" + allocated +
                ", appropriated=" + appropriated +
                ", role=" + role +
                ", arrivalRound=" + arrivalRound +
                ", cluster=" + cluster +
                ']';
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

    public double getMaxChargeRate() {
        return maxChargeRate;
    }
}

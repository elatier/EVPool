package uk.ac.imperial.evpool.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import uk.ac.imperial.evpool.EVPoolService;
import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.db.sql.Agent;
import uk.ac.imperial.presage2.db.sql.Environment;
import uk.ac.imperial.presage2.db.sql.SqlStorage;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class EVPoolStorage extends SqlStorage {

	int maxRound = -1;

	boolean shutdown = false;

	private EVPoolService game = null;

	@Inject
	public EVPoolStorage(@Named(value = "sql.info") Properties jdbcInfo) {
		super(jdbcInfo);
	}

	@Inject(optional = true)
	public void setGame(EnvironmentServiceProvider serviceProvider) {
		try {
			this.game = serviceProvider.getEnvironmentService(EVPoolService.class);
		} catch (UnavailableServiceException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void initTables() {
		super.initTables();
		Statement createTables = null;
		try {

			createTables = conn.createStatement();
			createTables.execute("CREATE TABLE IF NOT EXISTS `playerScore` ("
					+ "`simID` int(11) NOT NULL,"
					+ "`player` varchar(10) NOT NULL,"
					+ "`round` int(11) NOT NULL," + "`cL` double NOT NULL,"
					+ "`bC` double NOT NULL," + "`mCR` double NOT NULL,"
					+ "`mCPR` double NOT NULL," + "`p` double NOT NULL,"
					+ "`rP` double NOT NULL,"
					+ "`deadline` double NOT NULL,"
                    + "`charDeadline` double NOT NULL,"
					+ "`d` double NOT NULL,"
                    + "`r` double NOT NULL,"
					+ "PRIMARY KEY (`simID`,`player`,`round`),"
					+ "KEY `simID` (`simID`)," + "KEY `player` (`player`),"
					+ "KEY `round` (`round`)" + ")");
			createTables.execute("CREATE TABLE IF NOT EXISTS `roundGlobals` ("
					+ "`simID` int(11) NOT NULL," + "`round` int(11) NOT NULL,"
					+ "`headProvision` double DEFAULT NULL,"
					+ "`allocPoolSurplus` double DEFAULT NULL,"
                    + "`intProvisionPool` double DEFAULT NULL,"
                    + "`agentCount` double DEFAULT NULL,"
                    + "`minPool` double DEFAULT NULL,"
                    + "`totalDemand` double DEFAULT NULL,"
                    + "`chDeadUnmet` double DEFAULT NULL,"
                    + "`gridLoad` double DEFAULT NULL,"
                    + "`extPool` double DEFAULT NULL,"
                    + "`allocPool` double DEFAULT NULL,"
  					+ "PRIMARY KEY (`simID`,`round`)" + ")");

		} catch (SQLException e) {
			logger.warn("", e);
			throw new RuntimeException(e);
		} finally {
			if (createTables != null) {
				try {
					createTables.close();
				} catch (SQLException e) {
				}
			}
		}
	}

	@Override
	protected void updateTransientEnvironment() {
		PreparedStatement insertRound = null;

		try {
			insertRound = conn
					.prepareStatement("INSERT INTO roundGlobals "
							+ "(simID, round, headProvision, allocPoolSurplus, intProvisionPool, agentCount,"
                            + " minPool, totalDemand, chDeadUnmet, gridLoad, extPool, allocPool )"
							+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ");
		} catch (SQLException e) {
			logger.warn(e);
			throw new RuntimeException(e);
		}

		try {
			Set<Environment> notfullyProcessed = new HashSet<Environment>();
			for (Environment e : environmentTransientQ) {
				List<Integer> forRemoval = new LinkedList<Integer>();
				for (Map.Entry<Integer, Map<String, String>> round : e.transientProperties
						.entrySet()) {
					if (!shutdown && game != null
							&& round.getKey() >= game.getRoundNumber() - 1) {
						notfullyProcessed.add(e);
						continue;
					}

					Map<String, String> props = round.getValue();

					insertRound.setLong(1, e.simId);
					insertRound.setInt(2, round.getKey());
					insertRound.setDouble(3,
							getProperty(props, "c0-headProvision", 0));
                    insertRound.setDouble(4,
                            getProperty(props, "c0-allocPoolSurplus", 0));
                    insertRound.setDouble(5,
                            getProperty(props, "c0-intProvisionPool", 0));
                    insertRound.setDouble(6,
                            getProperty(props, "c0-agentCount", 0));
                    insertRound.setDouble(7,
                            getProperty(props, "c0-minPool", 0));
                    insertRound.setDouble(8,
                            getProperty(props, "c0-totalDemand", 0));
                    insertRound.setDouble(9,
                            getProperty(props, "c0-chDeadUnmet", 0));
                    insertRound.setDouble(10,
                            getProperty(props, "gridLoad", 0));
                    insertRound.setDouble(11,
                            getProperty(props, "c0-extPool", 0));
                    insertRound.setDouble(12,
                            getProperty(props, "c0-allocPool", 0));
                    insertRound.addBatch();

					forRemoval.add(round.getKey());
				}
				for (Integer round : forRemoval) {
					e.transientProperties.remove(round);
				}
			}
			environmentTransientQ.clear();
			environmentTransientQ.addAll(notfullyProcessed);
			batchQueryQ.put(insertRound);
		} catch (SQLException e) {
			logger.warn(e);
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	private double getProperty(Map<String, String> properties, String key,
			double defaultValue) {
		if (properties.containsKey(key))
			return Double.parseDouble(properties.get(key));
		else
			return defaultValue;
	}

	@Override
	protected void updateTransientAgents() {
		PreparedStatement insertPlayer = null;
		try {

    		insertPlayer = conn
					.prepareStatement("INSERT INTO playerScore "
							+ "(simID, player, round, bC, cL, mCR, mCPR, p, d, deadline, charDeadline, r, rP)  "
							+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ");
		} catch (SQLException e) {
			logger.warn(e);
			throw new RuntimeException(e);
		}

		try {
			Set<Agent> notfullyProcessed = new HashSet<Agent>();
			for (Agent a : agentTransientQ) {
				List<Integer> forRemoval = new LinkedList<Integer>();
				for (Map.Entry<Integer, Map<String, String>> round : a.transientProperties
						.entrySet()) {
					if (!shutdown && game != null
							&& round.getKey() >= game.getRoundNumber() - 2) {
						notfullyProcessed.add(a);
						continue;
					}

					Map<String, String> props = round.getValue();

					if (!props.containsKey("bC"))
						continue;

					insertPlayer.setLong(1, a.simId);
					insertPlayer.setString(2, a.getName());
					insertPlayer.setInt(3, round.getKey());

    				insertPlayer.setDouble(4, getProperty(props, "bC", 0.0));
					insertPlayer.setDouble(5, getProperty(props, "cL", 0.0));
					insertPlayer.setDouble(6, getProperty(props, "mCR", 0.0));
					insertPlayer.setDouble(7, getProperty(props, "mCPR", 0.0));
					insertPlayer.setDouble(8, getProperty(props, "p", 0.0));
					insertPlayer.setDouble(9, getProperty(props, "d", 0.0));
					insertPlayer.setDouble(10, getProperty(props, "deadline", 0.0));
                    insertPlayer.setDouble(11, getProperty(props, "charDeadline", 0.0));
					insertPlayer.setDouble(12, getProperty(props, "r", 0.0));
                    insertPlayer.setDouble(13, getProperty(props, "rP", 0.0));
                    //insertPlayer.setString(15, getProperty(props, "cluster", 0.0));

					insertPlayer.addBatch();

					forRemoval.add(round.getKey());
				}
				for (Integer round : forRemoval) {
					a.transientProperties.remove(round);
				}
			}
			batchQueryQ.put(insertPlayer);
			agentTransientQ.clear();
			agentTransientQ.addAll(notfullyProcessed);
		} catch (SQLException e) {
			logger.warn(e);
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void stop() {
		this.shutdown = true;
		super.stop();
	}

}

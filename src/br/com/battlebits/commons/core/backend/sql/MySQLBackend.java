package br.com.battlebits.commons.core.backend.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.core.backend.Backend;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MySQLBackend implements Backend {
	private Connection connection = null;
	private final String hostname, database, username, password;
	private final int port;

	public MySQLBackend() {
		this("localhost", "commons", "root", "", 3306);
	}

	@Override
	public void startConnection()
			throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		BattlebitsAPI.getLogger().info("Conectando ao MySQL");
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		connection = DriverManager.getConnection("jdbc:mysql://" + hostname + ":" + port + "/" + database, username,
				password);
	}

	@Override
	public void closeConnection() throws SQLException {
		if (isConnected())
			connection.close();
	}

	@Override
	public boolean isConnected() throws SQLException {
		if (connection == null)
			return false;
		if (connection.isClosed())
			return false;
		return true;
	}

	@Override
	public void recallConnection()
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		if (!isConnected()) {
			BattlebitsAPI.getLogger().info("Reconectando ao MySQL");
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			connection = DriverManager.getConnection("jdbc:mysql://" + hostname + ":" + port + "/" + database, username,
					password);
		}
	}

	public void update(String sqlString)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		if (!isConnected()) {
			recallConnection();
		}
		Statement stmt = connection.createStatement();
		stmt.executeUpdate(sqlString);
		stmt.close();
		stmt = null;
	}

	public PreparedStatement prepareStatment(String sql)
			throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		if (!isConnected()) {
			recallConnection();
		}
		return connection.prepareStatement(sql);
	}

	public Connection getConnection()
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		if (!isConnected()) {
			recallConnection();
		}
		return connection;
	}

}

package utils;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnexionBD {
	public static Connection connect() throws SQLException {
		String url = System.getenv().getOrDefault(
				"DB_URL",
				"jdbc:postgresql://localhost:5432/gestion_bib");
		String user = System.getenv().getOrDefault("DB_USER", "postgres");
		String password = System.getenv().getOrDefault("DB_PASSWORD", "");

		if (password == null || password.isEmpty()) {
			throw new SQLException(
					"Database password not configured. Set DB_PASSWORD environment variable.");
		}

		return DriverManager.getConnection(url, user, password);
	}
}

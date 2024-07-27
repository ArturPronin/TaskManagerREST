package config;

import exception.DatabaseOperationException;
import exception.SQLExceptionWrapper;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

public class InitSQLScheme {
    private static final String SCHEME = "sql/schema.sql";
    private static String schemeSql;

    static {
        loadInitSQL();
    }

    private InitSQLScheme() {}

    public static void initSqlScheme() {
        try (Connection connection = DatabaseConfig.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(schemeSql);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Database error", e);
        }
    }

    private static void loadInitSQL() {
        try (InputStream inFile = InitSQLScheme.class.getClassLoader().getResourceAsStream(SCHEME)) {
            schemeSql = new String(Objects.requireNonNull(inFile).readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new SQLExceptionWrapper("SQL Exception", e);
        }

    }

}

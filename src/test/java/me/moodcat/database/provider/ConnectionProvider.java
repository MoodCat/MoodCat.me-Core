package me.moodcat.database.provider;

import geodb.GeoDB;
import org.hibernate.c3p0.internal.C3P0ConnectionProvider;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * For H2 to work with Spatial queries, GeoDB has to be initialized.
 */
public class ConnectionProvider extends C3P0ConnectionProvider {

    private static final long serialVersionUID = -1332406974241438558L;

    @Override
    public Connection getConnection() throws SQLException {
        final Connection connection = super.getConnection();
        GeoDB.InitGeoDB(connection);
        return connection;
    }

}

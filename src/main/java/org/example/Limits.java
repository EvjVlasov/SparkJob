package org.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

public class Limits {
    private static final Logger LOG = LogManager.getLogger(Limits.class);

    static String DB_URL;
    static String USER;
    static String PASS;

    private Connection getDBConnection() {
        Connection connection = null;

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
        } catch (SQLException e) {
            LOG.error("Exception: ", e);
        }
        return connection;
    }

    public long getLimit(String nameLimit) {
        long limit = 0;
        String selectLimits = "SELECT limit_value FROM traffic_limits.limits_per_hour WHERE limit_name ='" + nameLimit + "'";

        try (Connection dbConnection = getDBConnection(); Statement statement = dbConnection.createStatement()) {
            ResultSet rs = statement.executeQuery(selectLimits);
            while (rs.next()) {
                limit = rs.getLong("limit_value");
            }
        } catch (SQLException e) {
            LOG.error("Exception: ", e);
        }

        return limit;
    }

    public void updateLimit(String nameLimit, long newLimit, Timestamp newDate) {
        String selectLimits = "UPDATE traffic_limits.limits_per_hour SET limit_value = " + newLimit + ", effective_date = '" + newDate + "'  WHERE limit_name ='" + nameLimit + "'";

        try (Connection dbConnection = getDBConnection(); Statement statement = dbConnection.createStatement()) {
            statement.execute(selectLimits);
        } catch (SQLException e) {
            LOG.error("Exception: ", e);
        }

    }

}

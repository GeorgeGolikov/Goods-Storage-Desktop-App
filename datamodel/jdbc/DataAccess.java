package sample.datamodel.jdbc;

import java.sql.*;

public final class DataAccess
{
    public static DataAccess getDataAccessObject() throws SQLException
    {
        if (driver_ == null) driver_ = new DataAccess();
        return driver_;
    }

    private Connection getNewConnection() throws SQLException
    {
        String url = "jdbc:mysql://localhost:3306/data_lab4";
        String user = "root";
        String password = "root";
        Connection connection = DriverManager.getConnection(url, user, password);

        if (connection.isValid(1))
        {
            System.out.println("Connection successful!\n");
        }

        return connection;
    }

    private DataAccess() throws SQLException
    {
        connection_ = getNewConnection();
    }
    private static DataAccess driver_;
    private static Connection connection_;

    private void printResults(ResultSet rs, ResultSetMetaData rsmd) throws SQLException
    {
        int columnCount = rsmd.getColumnCount();

        int i = 1;
        for (; i < columnCount; ++i)
        {
            System.out.print(rsmd.getColumnName(i) + "       ");
        }
        System.out.print("    ");
        System.out.println(rsmd.getColumnName(i));

        int k = 0;
        while (rs.next())
        {
            ++k;

            int j = 1;
            for (; j < columnCount; ++j)
            {
                System.out.print(rs.getString(j) + "         ");
            }
            System.out.println(rs.getString(j));
        }
        if (k == 0) System.out.println("There are no items with such prices or no items at all!");
    }

    private String getAttributes(String[] attributes)
    {
        StringBuilder result = new StringBuilder("");
        for (int i = 0; i < attributes.length - 1; i += 2)
        {
            result.append(attributes[i])
                    .append(" ")
                    .append(attributes[i + 1])
                    .append(",");
        }
//        result.deleteCharAt(result.length() - 1);
        result.append("UNIQUE KEY (`prodid`),UNIQUE KEY (`title`)");

        return result.toString();
    }

    public void createTable(String name, String[] attributes) throws SQLException
    {
        Statement statement = connection_.createStatement();
        statement.execute("CREATE TABLE IF NOT EXISTS " + name +
                            "(" + getAttributes(attributes) + ");");
//        statement.execute("DELETE FROM " + name + ";");
        statement.execute("TRUNCATE TABLE " + name + ";");

        try
        {
            statement.execute("ALTER TABLE " + name + " ADD COLUMN id INT NOT NULL " +
                    "AUTO_INCREMENT PRIMARY KEY FIRST;");

            statement.execute("ALTER TABLE " + name + " MODIFY COLUMN prodid INT NOT NULL;");
            statement.execute("ALTER TABLE " + name + " MODIFY COLUMN title VARCHAR(50) NOT NULL;");
        }
        catch (SQLException e)
        {

        }
        finally
        {
            statement.close();
        }
    }

    // заполняет только таблицу типа goods
    public void fillTable(String tableName, int goodAmount) throws SQLException
    {
        String sqlQuery = "INSERT INTO " + tableName + " (prodid, title, cost)" + " VALUES (?, ?, ?);";
        try (PreparedStatement statement = connection_.prepareStatement(sqlQuery))
        {
            for (int i = 0; i < goodAmount; ++i)
            {
                int prodid = 500 + i;
                double cost = 50 * (i + 1);
                statement.setInt(1, prodid);
                statement.setString(2, "item" + prodid);
                statement.setDouble(3, cost);
                statement.executeUpdate();
            }
        }
        System.out.println("Filling table successful!");
    }

    public void addItem(String tableName, int prodId,
                        String title, double cost) throws SQLException
    {
        if (prodId > 0 && cost > 0 && !title.equals(""))
        {
            String sqlQuery = "INSERT INTO " + tableName + " (prodid, title, cost)" + " VALUES (?, ?, ?);";
            try (PreparedStatement statement = connection_.prepareStatement(sqlQuery))
            {
                statement.setInt(1, prodId);
                statement.setString(2, title);
                statement.setDouble(3, cost);
                statement.executeUpdate();
            }
            System.out.println("Item added!");
        }
        else
        {
            if (prodId <= 0)
            {
                if (cost <= 0)
                {
                    if (title.equals("")) System.out.println("Id, cost and title are not valid!");
                    else System.out.println("Id and cost are not valid!");
                }
                else if (title.equals("")) System.out.println("Id and title are not valid!");
                     else System.out.println("Id is not valid!");
            }
            else
            {
                if (cost <= 0)
                {
                    if (title.equals("")) System.out.println("Cost and title are not valid!");
                    else System.out.println("Cost is not valid!");
                }
                else System.out.println("Title is not valid!");
            }
        }
    }

    public void deleteItem(String tableName, String title) throws SQLException
    {
        String sqlQuery = "SELECT * FROM " + tableName + " WHERE title = '" + title + "';";
        try (PreparedStatement statement = connection_.prepareStatement(sqlQuery);
             ResultSet rs = statement.executeQuery())
        {
            if (!rs.next())
            {
                System.out.println("There is no item with such a title!");
                return;
            }
        }

        sqlQuery = "DELETE FROM " + tableName + " WHERE title = '" + title + "';";
        try (PreparedStatement statement = connection_.prepareStatement(sqlQuery))
        {
            statement.execute();
        }
        System.out.println("Item deleted!");
    }

    public void showAllConsole(String tableName) throws SQLException
    {
        String sqlQuery = "SELECT * FROM " + tableName + ";";
        try (PreparedStatement statement = connection_.prepareStatement(sqlQuery);
             ResultSet rs = statement.executeQuery())
        {
            ResultSetMetaData rsmd = rs.getMetaData();
            printResults(rs, rsmd);
        }
    }

    public ResultSet showAllGui(String tableName) throws SQLException
    {
        String sqlQuery = "SELECT * FROM " + tableName + ";";
        PreparedStatement statement = connection_.prepareStatement(sqlQuery);
        return statement.executeQuery();
    }

    public double getItemPrice(String tableName, String title) throws SQLException
    {
        String sqlQuery = "SELECT cost FROM " + tableName + " WHERE title = '" + title + "';";
        try (PreparedStatement statement = connection_.prepareStatement(sqlQuery);
             ResultSet rs = statement.executeQuery())
        {
            if (rs.next())
            {
                return rs.getDouble(1);
            }
            else
            {
                System.out.println("There is no item with such a title!");
                return -1;
            }
        }
    }

    public int getItemID(String tableName, String title) throws SQLException
    {
        String sqlQuery = "SELECT id FROM " + tableName + " WHERE title = '" + title + "';";
        try (PreparedStatement statement = connection_.prepareStatement(sqlQuery);
             ResultSet rs = statement.executeQuery())
        {
            if (rs.next())
            {
                return rs.getInt(1);
            }
            else
            {
                System.out.println("There is no item with such a title!");
                return -1;
            }
        }
    }

    public void changeItemPrice(String tableName, String title, double newCost) throws SQLException
    {
        String sqlQuery = "SELECT * FROM " + tableName + " WHERE title = '" + title + "';";
        try (PreparedStatement statement = connection_.prepareStatement(sqlQuery);
             ResultSet rs = statement.executeQuery())
        {
            if (!rs.next())
            {
                System.out.println("There is no item with such a title!");
                return;
            }
        }

        if (newCost > 0)
        {
            sqlQuery = "UPDATE " + tableName + " SET cost = " + newCost + " WHERE title = '" + title + "';";
            try (PreparedStatement statement = connection_.prepareStatement(sqlQuery))
            {
                statement.execute();
            }
            System.out.println("Price changed!");
        }
        else System.out.println("Cost is not valid!");
    }

    public void filterByPrice(String tableName, double costBegin, double costEnd) throws SQLException
    {
        String sqlQuery = "SELECT * FROM " + tableName +
                          " WHERE cost >= " + costBegin + " AND cost <= " + costEnd + ";";
        try (PreparedStatement statement = connection_.prepareStatement(sqlQuery);
             ResultSet rs = statement.executeQuery())
        {
            ResultSetMetaData rsmd = rs.getMetaData();
            printResults(rs, rsmd);
        }
    }
}
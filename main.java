import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class main {

    private static Connection conn;

    // Connect once, globally
    public static void connect() throws SQLException {
        if (conn == null || conn.isClosed()) {
            String url = "jdbc:mysql://localhost:3306/cars";
            String user = "User1";
            String password = "Password1";

            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to database!");
        }
    }

    public static void createTablesIfNotExists() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(String.join("\n", 
                "CREATE TABLE IF NOT EXISTS brands (",
                    "brand VARCHAR(255) PRIMARY KEY",
                ");"
            ));

            stmt.executeUpdate(String.join("\n",
                "CREATE TABLE IF NOT EXISTS models (",
                    "model VARCHAR(255) PRIMARY KEY,",
                    "brand VARCHAR(255),",
                    "FOREIGN KEY (brand) REFERENCES brands(brand)",
                ");"
            ));

            stmt.executeUpdate(String.join("\n",
                "CREATE TABLE IF NOT EXISTS colors (",
                    "color VARCHAR(255),",
                    "model VARCHAR(255),",
                    "PRIMARY KEY (color, model),",
                    "FOREIGN KEY (model) REFERENCES models(model)",
                ");"
            ));

            stmt.executeUpdate(String.join("\n",
                "CREATE TABLE IF NOT EXISTS engineTypes (",
                    "engineType VARCHAR(255),",
                    "model VARCHAR(255),",
                    "PRIMARY KEY (engineType, model),",
                    "FOREIGN KEY (model) REFERENCES models(model)",
                ");"
            ));

            stmt.executeUpdate(String.join("\n",
                "CREATE TABLE IF NOT EXISTS companyPlants (",
                    "plant VARCHAR(255),",
                    "suppliedPart VARCHAR(255),",
                    "forModel VARCHAR(255),",
                    "PRIMARY KEY (plant, suppliedPart, forModel),",
                    "FOREIGN KEY (forModel) REFERENCES models(model)",
                ");"
            ));

            stmt.executeUpdate(String.join("\n",
                "CREATE TABLE IF NOT EXISTS supplier (",
                    "supplier VARCHAR(255),",
                    "brandSupplier VARCHAR(255),",
                    "PRIMARY KEY (supplier, brandSupplier),",
                    "FOREIGN KEY (brandSupplier) REFERENCES brands(brand)",
                ");"
            ));

            stmt.executeUpdate(String.join("\n",
                "CREATE TABLE IF NOT EXISTS customers (",
                    "customerID VARCHAR(255) PRIMARY KEY,",
                    "name VARCHAR(255),",
                    "phoneNumb VARCHAR(255),",
                    "gender VARCHAR(50),",
                    "address VARCHAR(255),",
                    "income VARCHAR(255),",
                    "isCompany BOOLEAN",
                ");"
            ));

            stmt.executeUpdate(String.join("\n",
                    "CREATE TABLE IF NOT EXISTS vehicles (",
                        "vin VARCHAR(255) PRIMARY KEY,",
                        "color VARCHAR(255),",
                        "engineType VARCHAR(255),",
                        "timeKept INT,",
                        "customerID VARCHAR(255),",
                        "model VARCHAR(255),",
                        "salePrice INT,",
                        "FOREIGN KEY (color, model) REFERENCES colors(color, model),",
                        "FOREIGN KEY (engineType, model) REFERENCES engineTypes(engineType, model),",
                        "FOREIGN KEY (model) REFERENCES models(model),",
                        "FOREIGN KEY (customerID) REFERENCES customers(customerID)",
                    ");"
            ));
        }
    }

    public static int showMenuAndGetSelection(String[] options) {
        try {
            // Build the whiptail command dynamically
            StringBuilder cmdBuilder = new StringBuilder();
            cmdBuilder.append("whiptail --title 'Fruit Picker' --menu 'Choose a fruit:' 15 50 6 ");

            for (int i = 0; i < options.length; i++) {
                // Tag = index+1, description = option name
                cmdBuilder.append("\"").append(i + 1).append("\" \"").append(options[i]).append("\" ");
            }

            // Redirect stderr to stdout so we can capture the choice
            cmdBuilder.append("3>&1 1>&2 2>&3");

            // Use bash -c to run it
            String[] cmd = {"bash", "-c", cmdBuilder.toString()};

            ProcessBuilder pb = new ProcessBuilder(cmd);
            Process process = pb.start();

            // Capture output (the chosen tag)
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );
            String choiceTag = reader.readLine();

            int exitCode = process.waitFor();

            if (exitCode == 0 && choiceTag != null) {
                int choiceIndex = Integer.parseInt(choiceTag) - 1;
                System.out.println("You chose: " + options[choiceIndex]);
                return choiceIndex;
            } else {
                System.out.println("Cancelled or no choice made.");
                return -1;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    // This just executes the string as SQL and returns the output. We can just make functions that use this funtion.
    public static String execute(String sql) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            String trimmed = sql.trim().toUpperCase();

            // Detect if it's a SELECT query, if it is, the output needs to be read
            if (trimmed.startsWith("SELECT")) {
                try (ResultSet rs = stmt.executeQuery(sql)) {
                    StringBuilder result = new StringBuilder();
                    ResultSetMetaData meta = rs.getMetaData();
                    int columnCount = meta.getColumnCount();

                    // Column names
                    for (int i = 1; i <= columnCount; i++) {
                        result.append(meta.getColumnName(i)).append("\t");
                    }
                    result.append("\n");

                    // Rows
                    while (rs.next()) {
                        for (int i = 1; i <= columnCount; i++) {
                            result.append(rs.getString(i)).append("\t");
                        }
                        result.append("\n");
                    }

                    return result.toString();
                }
            } else {
                int affected = stmt.executeUpdate(sql);
                return (
                    "Query executed successfully. Rows affected: " + affected
                );
            }
        }
    }

    public static void showOptionsAndGetSelectedForever() {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                showMenuAndGetSelection("Select which command would like to do:", { 
                    "st 3 - Show SALES TRENDS for various brands over the past 3 years (can be any number).",
                    "fvp 2.0L Engine - FIND VIN numbers for cars which were made with a given PART 2.0L Engine (can be any part).",
                    "tb 2 1 - FIND The top 2 BRANDS by revinue in the past 1 year (can be any number of brands or years).",
                    "fmb Convertibles - FIND the MONTH which has the BEST revinue for Convertibles (can be any vehicle type).",
                    "ftatv F150 - FIND the dealers which have the TOP AVERAGE TIME a given VEHICLE model is kept (can be any model)."
                });
                System.out.print("Input ('exit' to exit): ");
                String command = scanner.nextLine();
                if (command.equalsIgnoreCase("exit")) {
                    break;
                } else if(command.startsWith("st")) {
                    try {
                        int years = Integer.parseInt(command.substring(3, command.length()));
                        String result = execute(String.format(String.join("\n",
                                "SELECT brand, SUM(salePrice) totalSales",
                                "FROM vehicles",
                                "NATURAL JOIN models",
                                "WHERE timeKept <= %d",
                                "GROUP BY brand",
                                "ORDER BY totalSales;"
                        ), years * 365));
                        System.out.println(result);
                    } catch (SQLException e) {
                        System.out.println("SQL Error: " + e.getMessage());
                    }
                } else if(command.startsWith("fvp")) {
                    
                } else if(command.startsWith("tb")) {

                } else if(command.startsWith("fmb")) {

                } else if(command.startsWith("ftatv")) {

                } else if(command.startsWith("sql")) { // for debugging. Ex: 'sql SELECT * FROM vehicles;'
                    try {
                        System.out.println(execute(command.substring(4, command.length())));
                    } catch (SQLException e) {
                        System.out.println("SQL Error: " + e.getMessage());
                    }
                } else {
                    System.out.println("Invalid Command");
                }

            }
    }

    // this is just for testing, we wouldn't remove this and put options to select custom queries
    public static void getSQLFromInputForever() {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.println("Enter command:");
                String command = scanner.nextLine();
                if (command.equalsIgnoreCase("exit")) {
                    break;
                }

                try {
                    String result = execute(command);
                    System.out.println(result);
                } catch (SQLException e) {
                    System.out.println("SQL Error: " + e.getMessage());
                }
            }
    }

    public static void main(String[] args) {
        try {
            connect();
            createTablesIfNotExists();

            showOptionsAndGetSelectedForever();
        } catch (SQLException e) {
            System.out.println("Connection or setup error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                    System.out.println("Connection closed.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}

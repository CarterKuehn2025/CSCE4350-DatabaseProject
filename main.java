import java.nio.file.*;
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

    public static int showMenuAndGetSelection(String text, String[] options) {
        try {
            Path tempFile = Files.createTempFile("whiptail_choice_", ".txt");
                StringBuilder cmd = new StringBuilder();
                cmd.append("(");
                cmd.append("whiptail --title 'Select Query' --menu 'Pick an option:' 15 120 6 ");
                for (int i = 0; i < options.length; i++) {
                    cmd.append("\"").append(i + 1).append("\" \"").append(options[i]).append("\" ");
                }
                cmd.append("3>&1 1>&2 2>&3");
                cmd.append(") > ").append(tempFile.toAbsolutePath());
                
                ProcessBuilder pb = new ProcessBuilder("bash", "-c", cmd.toString());
                pb.inheritIO();
                Process process = pb.start();
                process.waitFor();
                
                String result = Files.readString(tempFile).trim();
                if(!result.isEmpty()) return Integer.parseInt(result) - 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static void showTextBox(String text) {
        try {
            Path tempFile = Files.createTempFile("whiptail_output_", ".txt");
            Files.writeString(tempFile, text); // preserves newlines and special chars

            String cmd = "whiptail --title 'Output' --textbox " + tempFile.toAbsolutePath() + " 20 80";
            ProcessBuilder pb = new ProcessBuilder("bash", "-c", cmd);
            pb.inheritIO(); // show menu interactively
            Process process = pb.start();
            process.waitFor();
            Files.deleteIfExists(tempFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public static String getInput(String description) {
        // TODO Whiplash can do a text field page, figure out how to do that and get the value similar to how the selection value was gotten
        return "3";
    }

    public static void showOptionsAndGetSelectedForever() {
        Scanner scanner = new Scanner(System.in);
        //while (true) { // only do one iteration of selection so that the printed stuff can be seen for debugging
            int selection = showMenuAndGetSelection("Select which command would like to do:", new String[] { 
                "Show SALES TRENDS for various brands over the past 3 years (can be any number).",
                    "FIND VIN numbers for cars which were made with a given PART 2.0L Engine (can be any part).",
                    "FIND The top 2 BRANDS by revinue in the past 1 year (can be any number of brands or years).",
                    "FIND the MONTH which has the BEST revinue for Convertibles (can be any vehicle type).",
                    "FIND the dealers which have the TOP AVERAGE TIME a given VEHICLE model is kept (can be any model).",
                    "exit"
            });
            System.out.println(selection);
            if(selection == 0) {
                try {
                    int years = Integer.parseInt(getInput("Enter how many years"));
                    String result = execute(String.format(String.join("\n",
                                    "SELECT brand, SUM(salePrice) totalSales",
                                    "FROM vehicles",
                                    "NATURAL JOIN models",
                                    "WHERE timeKept <= %d",
                                    "GROUP BY brand",
                                    "ORDER BY totalSales;"
                                    ), years * 365));
                    System.out.println(result);
                    showTextBox(result);
                } catch (SQLException e) {
                    showTextBox("SQL Error: " + e.getMessage());
                }
            } else if(selection == 1) {

            } else if(selection == 2) {

            } else if(selection == 3) {

            } else if(selection == 4) {

            } else if(selection == 5) {
                //break;
            } else {
                System.out.println("Invalid Command");
            }

        //}
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

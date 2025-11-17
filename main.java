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
    // uses local host bc it is intended to be ran on the cell machines
    public static void connect(String user, String password) throws SQLException {
        if (conn == null || conn.isClosed()) {
            String url = "jdbc:mysql://localhost:3306/csce4350_258_team6_proj";

            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to database!");
        }
    }

    public static void createTablesIfNotExists() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // brands table
            stmt.executeUpdate(String.join("\n", 
                "CREATE TABLE IF NOT EXISTS brands (",
                    "brand VARCHAR(255) PRIMARY KEY",
                ");"
            ));

            // models table
            stmt.executeUpdate(String.join("\n",
                "CREATE TABLE IF NOT EXISTS models (",
                    "model VARCHAR(255) PRIMARY KEY,",
                    "brand VARCHAR(255) NOT NULL,",
                    "bodyStyle VARCHAR(255), ",
                    "FOREIGN KEY (brand) REFERENCES brands(brand)",
                ");"
            ));

            // dealers table
            stmt.executeUpdate(String.join("\n",
                "CREATE TABLE IF NOT EXISTS dealers (",
                    "dealerID VARCHAR(255) PRIMARY KEY,",
                    "name VARCHAR(255) NOT NULL,",
                    "address VARCHAR(255),",
                    "city VARCHAR(255),",
                    "state CHAR(2),",
                    "zip VARCHAR(10)",
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

            // company productive plants table
            stmt.executeUpdate(String.join("\n",
                "CREATE TABLE IF NOT EXISTS companyPlants (",
                    "plant VARCHAR(255),",
                    "suppliedPart VARCHAR(255),",
                    "forModel VARCHAR(255),",
                    "PRIMARY KEY (plant, suppliedPart, forModel),",
                    "FOREIGN KEY (forModel) REFERENCES models(model)",
                ");"
            ));

            // supplier table
            stmt.executeUpdate(String.join("\n",
                "CREATE TABLE IF NOT EXISTS supplier (",
                    "supplier VARCHAR(255),",
                    "brandSupplier VARCHAR(255),",
                    "address VARCHAR(255),",
                    "phone VARCHAR(255),",
                    "PRIMARY KEY (supplier, brandSupplier),",
                    "FOREIGN KEY (brandSupplier) REFERENCES brands(brand)",
                ");"
            ));

            // customers table
            stmt.executeUpdate(String.join("\n",
                "CREATE TABLE IF NOT EXISTS customers (",
                    "customerID VARCHAR(255) PRIMARY KEY,",
                    "name VARCHAR(255),",
                    "phoneNumb VARCHAR(255),",
                    "gender VARCHAR(50),",
                    "address VARCHAR(255),",
                    "income DECIMAL(10,2),",
                    "isCompany BOOLEAN",
                ");"
            ));

            // vehicles table
            stmt.executeUpdate(String.join("\n",
                    "CREATE TABLE IF NOT EXISTS vehicles (",
                        "vin VARCHAR(255) PRIMARY KEY,",
                        "color VARCHAR(255),",
                        "engineType VARCHAR(255),",
                        "timeKept INT,",
                        "dateAcquired DATE,",
                        "dealerID VARCHAR(255),",
                        "customerID VARCHAR(255),",
                        "model VARCHAR(255),",
                        "salePrice INT,",
                        "FOREIGN KEY (color, model) REFERENCES colors(color, model),",
                        "FOREIGN KEY (engineType, model) REFERENCES engineTypes(engineType, model),",
                        "FOREIGN KEY (model) REFERENCES models(model),",
                        "FOREIGN KEY (customerID) REFERENCES customers(customerID),",
                        "FOREIGN KEY (dealerID) REFERENCES dealers(dealerID)",
                    ");"
            ));

            // sales table
            stmt.executeUpdate(String.join("\n",
                "CREATE TABLE IF NOT EXISTS sales (",
                    "saleID INT AUTO_INCREMENT PRIMARY KEY,",
                    "vin VARCHAR(255) NOT NULL,",
                    "dealerID VARCHAR(255) NOT NULL,",
                    "customerID VARCHAR(255) NOT NULL,",
                    "saleDate DATE NOT NULL,",
                    "salePrice INT NOT NULL,",
                    "FOREIGN KEY (vin) REFERENCES vehicles(vin),",
                    "FOREIGN KEY (dealerID) REFERENCES dealers(dealerID),",
                    "FOREIGN KEY (customerID) REFERENCES customers(customerID)",
                    ");"
            ));

            System.out.println("All tables created (if they did not exist)");
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
        // implementing todo
        // first, we need to build a command-line-safe command, so I'll escape any double quotes
        // should keep bash from breaking
        String safeDesc = description.replace("\"", "\\\"");

        try {
            // temp file pattern used above
            Path tempFile = Files.createTempFile("whiptail_input_", ".txt");
            
            // building the whiptail command to feed into bash
            // bash command, gives user inbox box, "10 60" = height/width, "3>&...>&3" redirects our file descriptor to stdout, which we pipe into the temp file
            StringBuilder cmdLine = new StringBuilder();
            cmdLine.append("(whiptail --title \"Input\" --inputbox \"").append(safeDesc).append("\" 10 60 3>&1 1>&2 2>&3");
            cmdLine.append(") > ").append(tempFile.toAbsolutePath());

            // now to use bash/process builder to run the command
            ProcessBuilder userInputProcess = new ProcessBuilder("bash", "-c", cmdLine.toString());

            // grab the terminal
            userInputProcess.inheritIO();

            // start and grab exit code
            Process inputProcess = userInputProcess.start();
            int exitCode = inputProcess.waitFor();

            // check if user pressed ok (returns 0)
            String result = "";
            if(exitCode == 0) {
                result = Files.readString(tempFile).trim();
            }

            // don't save file
            Files.deleteIfExists(tempFile);

            // return user input
            // yay so fun java
            return result;
        }
        catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getPassword(String description) {
        String escapeChars = description.replace("\"", "\\\"");
        try {
            Path tempFile = Files.createTempFile("whiptail_pass_", ".txt");

            StringBuilder cmd = new StringBuilder();
            cmd.append("(whiptail --title \"Password\" --passwordbox \"")    // Hides the text input
            .append(escapeChars)
            .append("\" 10 60 3>&1 1>&2 2>&3")
            .append(") > ")
            .append(tempFile.toAbsolutePath());

            ProcessBuilder pb = new ProcessBuilder("bash", "-c", cmd.toString());
            pb.inheritIO();
            Process process = pb.start();
            int exitCode = process.waitFor();

            String result = "";
            if (exitCode == 0) {
                result = Files.readString(tempFile).trim();
            }

            Files.deleteIfExists(tempFile);
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static void showOptionsAndGetSelectedForever() {
        Scanner scanner = new Scanner(System.in);
        //while (true) { // only do one iteration of selection so that the printed stuff can be seen for debugging
            int selection = showMenuAndGetSelection("Select which command would like to do:", new String[] { 
                "Show SALES TRENDS by brand over the past N years, given by year, month, week, gender, and income range",
                    "FIND VIN numbers for cars which were made with a given PART 2.0L Engine (can be any part).",
                    "FIND The top 2 BRANDS by revinue in the past 1 year (can be any number of brands or years).",
                    "FIND the MONTH which has the BEST revenue for a body style (defaults to Convertible).",
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
                try {
                    // get user input for the part name
                    String part = getInput("Enter the part name (i.e. Headlight):");
                    // Escape the single quotes
                    part = part.replace("'", "''");

                    String sql =
                        "SELECT vin, model, suppliedPart\n" + 
                        "FROM vehicles\n" +
                        "JOIN companyPlants ON model = forModel\n" +
                        "WHERE suppliedPart = '" + part + "';";

                        String result = execute(sql);
                        showTextBox(result);
                }
                catch (SQLException e) {
                    showTextBox("SQL Error: " + e.getMessage());
                }

            } else if(selection == 2) {
                try {
                    //Get user input for number of years
                    int years = Integer.parseInt(getInput("Enter how many years"));

                    String sql =
                        "SELECT brand, SUM(salePrice) AS totalSales\n" +
                        "FROM sales\n" +
                        "JOIN vehicles ON  sales.vin = vehicles.vin\n" +
                        "JOIN models ON vehicles.model = models.model\n" +
                        "WHERE saleDate >= CURDATE() - " + years + "\n" +
                        "GROUP BY brand\n" +
                        "ORDER BY totalSales DESC\n" +
                        "LIMIT 2;";

                    String result = execute(sql);
                    showTextBox(result);
                }
                catch (SQLException e) {
                    showTextBox ("SQL Error: " + e.getMessage());
                }

            } else if(selection == 3) {
                try {
                    // get user input for body style to query for
                    String bodyStyle = getInput("Enter vehicle body style (i.e. Convertible):");
                    // default to convertible
                    if (bodyStyle == null || bodyStyle.trim().isEmpty()) {
                        bodyStyle = "Convertible";
                    }
                    else {
                        // clean whitespace from user
                        bodyStyle = bodyStyle.trim();
                    }

                    // bug fix, need to escape the single quotes otherwise SQL string is broken
                    bodyStyle = bodyStyle.replace("'", "''");

                    // query builds monthly sales for body style and we'll order by revenue
                    // notes:
                    // - learned you can define tables as char singletons, significantly cleans up the query
                    // - we want sale year/month, units sold, and total sales, three of which are in sales
                    // - so sales join (inner) on vehicles vin and models join on vehicles?
                    // - then simple group by sale dates and order by total sales
                    // - thank god for aliasing 
                    // - !!! """...""" does not work to make a string, ig cell machines use an older version of java < 15
                    String sqlQuery = 
                        "Select\n" +
                            "YEAR(s.saleDate) AS saleYear,\n" +
                            "MONTH(s.saleDate) AS saleMonth,\n" +
                            "COUNT(*) AS unitsSold,\n" +
                            "SUM(s.salePrice) AS totalSales\n" +
                        "FROM sales s\n" +
                        "JOIN vehicles v ON s.vin = v.vin\n" +
                        "JOIN models m ON v.model = m.model\n" +
                        "WHERE m.bodyStyle = '" + bodyStyle + "'\n" +
                        "GROUP BY YEAR(s.saleDate), MONTH(s.saleDate)\n" +
                        "ORDER BY totalSales DESC;";

                    // get the string
                    String queryResult = execute(sqlQuery);
                    showTextBox(queryResult);
                }
                catch (SQLException e) {
                    showTextBox("SQL Error: " + e.getMessage());
                }

            } else if(selection == 4) {
                try {
                    // need to allow for an optional filter, same getInput process for user
                    String specificModel = getInput("Enter vehicle MODEL to filter by (exact name), or blank for all models:");
                    // can still use bool logic similar to python, java just requires more method calls
                    boolean filterByModel = specificModel != null && !specificModel.trim().isEmpty();
                    if (filterByModel) {
                        // user entered a model name, trim possible whitespace
                        specificModel = specificModel.trim();
                    }

                    // same query assembly method for selection 3
                    // notes:
                    // - want average inventory time by dealer, then show dealers with highest
                    // - base table will be sales again, easiest way I think to get saleDate, and we want vehicles table
                    // - which has date acquired, I saw a query online that nests:
                    //          - AVG(DATEDIFF(...)), which we can use to get the difference between sale date and date aquired
                    // - we'll want the dealer ID and name as well for display
                    // - welp here we go again thankful for aliasing/table char singles
                    // - if a date acquired is null it might break the query, so I'll add a little clause for the end
                    // - first bit is default not filtered by model
                    String sqlQuery = 
                        "Select\n" +
                            "d.dealerID,\n" +
                            "d.name,\n" +
                            "AVG(DATEDIFF(s.saleDate, v.dateAcquired)) AS avgDaysInInventory,\n" +
                            "COUNT(*) AS vehiclesSold\n" +
                        "FROM sales s\n" + 
                        "JOIN vehicles v ON s.vin = v.vin\n" +
                        "JOIN dealers d ON s.dealerID = d.dealerID\n" +
                        "WHERE v.dateAcquired IS NOT NULL\n";

                    // not done yet, we'll append the join here based on model name entered by user
                    if (filterByModel) {
                        // forgot to escape single quotes again
                        specificModel = specificModel.replace("'", "''");
                        // "WHERE ... AND v.model = specificModel" should filter correctly
                        sqlQuery = sqlQuery + "AND v.model = '" + specificModel + "'\n";
                    }

                    // now to group by dealer and order by our aliased AVG date difference
                    sqlQuery = sqlQuery +
                        "GROUP BY d.dealerID, d.name\n" +
                        "ORDER BY avgDaysInInventory DESC;";

                    String queryResult = execute(sqlQuery);
                    showTextBox(queryResult);
                }
                catch (SQLException e) {
                    showTextBox("SQL Error: " + e.getMessage());
                }
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
            
            String user = getInput("EUID: ");
            String password = getPassword("Password: ");
            
            connect(user, password);
            createTablesIfNotExists();

            // testing
            String answer = getInput("Test: type anything");
            System.out.println("You typed: " + answer);

            // had trouble getting this to compile on my end, just going to use the old method
            showOptionsAndGetSelectedForever();
            // getSQLFromInputForever();
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

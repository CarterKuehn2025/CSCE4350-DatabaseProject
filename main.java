import java.nio.file.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

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

    public static int showMenuAndGetSelection(String title, String[] options) {
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("whiptail_choice_", ".txt");

            StringBuilder cmd = new StringBuilder();
            cmd.append("(");
            cmd.append("whiptail --title '" + title + "' --menu 'Pick an option:' 15 120 6 --ok-button 'Choose' --cancel-button 'Back' ");
            for (int i = 0; i < options.length; i++) {
                cmd.append("\"").append(i + 1).append("\" \"").append(options[i]).append("\" ");
            }
            cmd.append("3>&1 1>&2 2>&3");
            cmd.append(") > ").append(tempFile.toAbsolutePath());
            
            ProcessBuilder pb = new ProcessBuilder("bash", "-c", cmd.toString());
            pb.inheritIO();
            Process process = pb.start();
            int exitCode = process.waitFor();
            
            if(exitCode == 1) return -2; // They clicked 'Back'

            String result = Files.readString(tempFile).trim();
            if(!result.isEmpty()) return Integer.parseInt(result) - 1;
            else return -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        } finally {
            if (tempFile != null) {
                try {Files.deleteIfExists(tempFile); } catch(Exception ignored) {}
            }
        }
    }

    public static void showTextBox(String text) {
        try {
            Path tempFile = Files.createTempFile("whiptail_output_", ".txt");
            Files.writeString(tempFile, text); // preserves newlines and special chars

            String cmd = "whiptail --title 'Output' --textbox " + tempFile.toAbsolutePath() + " 20 120";
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
    // refactoring to pretty up the display
    public static String execute(String sql) throws SQLException {
        System.out.println(sql); // Print for debugging
        // going to try and pretty up the formatting using some java indexing length strategies
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            // grab the meta data, we can use it to format the results
            // automatically returned when a query is processed as long as we capture in a result set
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // first, grab all rows (yes including header)
            List<String[]> rows = new ArrayList<>();

            // handle header row first
            String[] header = new String[columnCount];
            // iterate across the column count
            for(int i = 1; i <= columnCount; i++ ) {
                // 0 indexing
                header[i - 1] = metaData.getColumnLabel(i);
            }
            rows.add(header);

            // now for data rows
            while (rs.next()) {
                String[] row = new String[columnCount];
                // walk along
                for (int i = 1; i <= columnCount; i++) {
                    // grab the value
                    String value = rs.getString(i);
                    // add it to the @ index, little guard check for null values
                    row[i - 1] = (value == null ? "NULL" : value);
                }
                rows.add(row);
            }

            // now we need the max width for each column (aka header + data)
            int[] widths = new int[columnCount];
            for (String[] row : rows) {
                for (int c = 0; c < columnCount; c++) {
                    int length = row[c] != null ? row[c].length() : 0;
                    if (length > widths[c]) {
                        widths[c] = length;
                    }
                }
            }

            // now for string builder, yay android development use case here
            StringBuilder stringBuild = new StringBuilder();

            // allows for easier formatting, first iterate over each row
            for (int r = 0; r < rows.size(); r++) {
                String[] row = rows.get(r);

                // now we need to iterate across the columns and add padding to its max width + maybe 2 spaces??
                for (int c = 0; c < columnCount; c++) {
                    stringBuild.append(String.format("%-" + (widths[c] + 2) + "s", row[c]));
                }
                // move to next row
                stringBuild.append("\n");

                // for user display, I want to add a seperator '-' after the header row
                if (r == 0) {
                    // we'll walk the columns and append singletons based on the width
                    for (int c = 0; c < columnCount; c++) {
                        for (int i = 0; i < widths[c] + 2; i++) {
                            stringBuild.append("-");
                        }
                    }
                    // again new line to move into another row
                    stringBuild.append("\n");
                }
            }

            return stringBuild.toString();
        }
    }

/*    // old version
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
*/ 

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
                result = Files.readString(tempFile);
            }

            // don't save file
            Files.deleteIfExists(tempFile);

            // return user input
            // yay so fun java
            return result.trim();
        }
        catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
        // main driver behind CSV read -> write to table
    // need to specify that it can throw an I/O or SQL exception to receive clear error messages
    public static int importCSV(String tableName, String csvPath, boolean clearFirst) throws IOException, SQLException {
        // basic file open -> buffered reader
        Path filePath = Paths.get(csvPath);
        if (!Files.exists(filePath)) {
            // custom IOException
            throw new IOException("CSV file does not exist: " + csvPath);
        }

        try (BufferedReader BuffReader = Files.newBufferedReader(filePath)) {
            // !!! - function assume csv is formatted as:
            //     1st line: header containing column names for example ("vin,color,engineType,timeKept,dateAcquired,dealerID,...,")
            //     2nd line+ : body containing row entries
            
            // first, grab header
            String headerLine = BuffReader.readLine();
            if (headerLine == null) {
                throw new IOException("CSV file is empty: " + csvPath);
            }

            // now split the column names apart using the ',' as our delimiter
            // assumes no commas inside of fields (otherwise change to escape the commas)
            String[] columnNames = headerLine.split(",");
            // iterate across the length of our array
            for (int i = 0; i < columnNames.length; i++ ) {
                // and trim potential whitespace
                columnNames[i] = columnNames[i].trim();
            }

            // check user option to clear existing data/table
            if (clearFirst) {
                // simple DELETE FROM sql
                try (Statement stmt = conn.createStatement()) {
                    String deleteTable = "DELETE FROM " + tableName;
                    stmt.executeUpdate(deleteTable);
                }
            }

            // now for the fun part
            // first the INSERT statement built using header columns
            // string builder simplifies this process thankfully
            StringBuilder sqlInsert = new StringBuilder();
            sqlInsert.append("INSERT INTO ").append(tableName).append(" (");            // "INSERT INTO TABLE"
            // iterate over column names from header line
            for (int i = 0; i < columnNames.length; i++) {
                if(i > 0) {
                    // should build "INSERT INTO TABLE (name, age, address, ...)"
                    sqlInsert.append(", ");
                }
                sqlInsert.append(columnNames[i]);
            }
            // now values
            sqlInsert.append(") VALUES (");
            for (int i = 0; i < columnNames.length; i++) {
                if (i > 0) {
                    sqlInsert.append(", ");
                }
                sqlInsert.append("?");
            }
            sqlInsert.append(")");

            String insertSQL = sqlInsert.toString();

            // barebones SQL string has been made, now to dynamically build the query
            // to count rows
            int totalInserted = 0;
            // prepared statement gives us options for editing
            try (PreparedStatement prepStatment = conn.prepareStatement(insertSQL)) {
                String line;
                // to track size/maybe limit
                int batchSize = 0;
                final int BATCHLIMIT = 500; // random high number

                // now to read each "row" (line)
                while ((line = BuffReader.readLine()) != null) {
                    if (line.trim().isEmpty()) {
                        continue; // should skip blank lines
                    }

                    // split the line on commas, -1 will keep empty strings
                    String[] values = line.split(",", -1);
                    // breif sanity check
                    if (values.length != columnNames.length) {
                        throw new IOException("Column count mismatch in line: " + line);
                    }

                    // mySQL should handle typing automatically (surprisingly) as long as everything
                    // is a string, so we'll set everything to a string
                    for (int i = 0; i < columnNames.length; i++) {
                        // handles each data entry in a row
                        prepStatment.setString(i + 1, values[i].trim());
                    }

                    prepStatment.addBatch();
                    batchSize++;

                    // basic limiting
                    if (batchSize >= BATCHLIMIT) {
                        int[] results = prepStatment.executeBatch();
                        totalInserted += results.length;
                        batchSize = 0;
                    }
                }

                // flush remaining
                if (batchSize > 0) {
                    int[] results = prepStatment.executeBatch();
                    totalInserted += results.length;
                }
            }

            return totalInserted;
            
        }
    }

    // menu to to assemble options for loading data from a csv
    public static void loadDataCSVMenu() {
        try {
            // first, user has to specify table name
            String tableName = getInput("Enter target table exact name (vehicles, sales, customers):");
            // basic saftey check
            if (tableName.isEmpty()) {
                showTextBox("No table name provided");
                return;
            }

            // next, user has to specify CSV file name, might need to use PATH not sure (in Cell machine file system)
            String csvPath = getInput("Enter file name of CSV file (ex: vechiles.csv):");
            if (csvPath.isEmpty()) {
                showTextBox("No CSV file name provided, canceling import");
                return;
            }

            // ask user if they want to clear the table first, boolean to handle the logic
            String clearPrompt = getInput("Clear existing rows " + tableName + " before import? (yes/no):");
            boolean clearFirst = clearPrompt.equalsIgnoreCase("yes");

            // we'll handle the sql query in a different file for readability
            // I want to tell the user how many rows were added, thus we'll return an int
            // counting the amount of rows inserted
            int rowsInserted = importCSV(tableName, csvPath, clearFirst);

            // I'll wrap up this function before building out importCSV
            showTextBox("Imported " + rowsInserted + "rows into table " + tableName + ".");
        }
        catch (Exception e) {
            // can be I/O based or SQL error
            e.printStackTrace();
            showTextBox("Error importing CSV: " + e.getMessage());
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
                result = Files.readString(tempFile);
            }

            Files.deleteIfExists(tempFile);
            return result.trim();

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    // dealer portal generation/SQL queries
    public static void showDealerPortal() {
        String dealerID = getInput("Please enter your dealer ID (i.e: D001):");
        // check for null value
        if (dealerID == null || dealerID.trim().isEmpty()) {
            showTextBox("No dealer ID entered; exiting to main menu.");
            return;
        }

        // escape any possible single quotes to prevent problems
        dealerID = dealerID.trim();
        String dealerIDEsc = dealerID.replace("'", "''");
        dealerIDEsc = "'" + dealerIDEsc + "'";

        while (true) {
            int selection = showMenuAndGetSelection("Dealer Portal", new String[] {
                "View my current inventory",
                "View vehicles I sold recently",
                "See my average inventory time per model"
            });
            if(selection == 0) {
                // need to find cars owned by the dealer and not yet sold
                // grab most values from vehicles, we'll join on model and brand (model and brand)
                // left join on sales using the vin should allow us to check if it is null aka not sold
                String sql = "SELECT\n" +
                "v.vin, b.brand, m.model, m.bodyStyle, v.color, v.engineType, v.dateAcquired, v.salePrice\n" +
                "FROM vehicles v\n" +
                "JOIN models m ON v.model = m.model\n" +
                "JOIN brands b ON m.brand = b.brand\n" + 
                "LEFT JOIN sales s ON v.vin = s.vin\n" +
                "WHERE v.dealerID = " + dealerIDEsc + "\n" +
                "AND s.vin IS NULL;";

                try {
                    // execute command
                    String result = execute(sql);
                    showTextBox(result);
                }
                catch (SQLException e) {
                    showTextBox("SQL Error: " + e.getMessage());
                }
            }
            else if(selection == 1) {
                // want to allow the dealer to view vehicles they have solid in the last 90 days
                // base table will be sales, but we want the sale listed to contain the vin (vehicles)
                // brand (brands), model (models), customer name (customers), thus we will join on all these
                // tables
                // then use date subtraction against the current date and SQL 90 day interval (and a order by descending)
                String sql = "SELECT\n" +
                "s.saleDate, v.vin, b.brand, m.model, c.name AS customerName, s.salePrice\n" +
                "FROM sales s\n" +
                "JOIN vehicles v ON s.vin = v.vin\n" +
                "JOIN models m ON v.model = m.model\n" +
                "JOIN brands b ON m.brand = b.brand\n" +
                "JOIN customers c ON s.customerID = c.customerID\n" +
                "WHERE s.dealerID = " + dealerIDEsc + "\n" +
                "AND s.saleDate >= DATE_SUB(CURDATE(), INTERVAL 90 DAY)\n" +
                "ORDER BY s.saleDate DESC;";
                
                try {
                    String result = execute(sql);
                    showTextBox(result);
                }
                catch (SQLException e) {
                    showTextBox("SQL Error: " + e.getMessage());
                }
            }
            else if(selection == 2) {
                // want to allow specific dealer to see their "average time kept" per model
                // base table will again be sales, and we need model (models), vin (vehicles)
                // only "slightly" complex part is taking the AVG of the date difference between the sale date and the date the vehicle was aquired
                String sql = "SELECT\n" +
                "m.model, AVG(DATEDIFF(s.saleDate, v.dateAcquired)) AS avgDaysInInventory, COUNT(*) AS vehiclesSold\n" +
                "FROM sales s\n" +
                "JOIN vehicles v ON s.vin = v.vin\n" +
                "JOIN models m ON v.model = m.model\n" +
                "WHERE s.dealerID = " + dealerIDEsc + "\n" +
                "AND v.dateAcquired IS NOT NULL\n" +
                "GROUP BY m.model\n" +
                "ORDER BY avgDaysInInventory DESC;";

                try {
                    String result = execute(sql);
                    showTextBox(result);
                }
                catch (SQLException e) {
                    showTextBox("SQL Error: " + e.getMessage());
                }
            }
            else if(selection == -2) { // back button
                // return to menu
                break;
            }
            else {
                showTextBox("Invalid selection.");
            }
        }
    }

    public static void showCustomerPortal() {
        while (true) {
            int selection = showMenuAndGetSelection("Customer portal", new String[] {
                "Show all brands",
                "Show all models for a given brand",
                "View all vehicles for sale"
            });
            if(selection == 0) {
                // simple grab all brands from brands table and order alphabetically
                String sql = "SELECT\n" +
                    "brand\n" +
                    "FROM brands\n" +
                    "ORDER BY brand;";

                try {
                    // execute command
                    String result = execute(sql);
                    showTextBox(result);
                }
                catch (SQLException e) {
                    showTextBox("SQL Error: " + e.getMessage());
                }
            }
            else if(selection == 1) {
                // want to allow the user to browse models for a brand
                // first, user input
                String brand = getInput("Enter brand name (Get available brands from 'Show all brands', ex: Toyota):");
                if (brand == null || brand.trim().isEmpty()) {
                    showTextBox("No brand entered. Returning to customer menu.");
                    continue;
                }

                // we'll trim, escape and append quotes
                brand = brand.trim();
                String brandEsc = brand.replace("'", "''");
                brandEsc = "'" + brandEsc + "'";

                // for the query, we only need the models table
                // and need to use WHERE to find where the user selected brand matches any models with that brand
                String sql = "SELECT\n" + 
                    "m.model, m.bodyStyle\n" +
                    "FROM models m\n" +
                    "WHERE m.brand = " + brandEsc + "\n" +
                    "ORDER BY m.model;";
                
                try {
                    String result = execute(sql);
                    showTextBox(result);
                }
                catch (SQLException e) {
                    showTextBox("SQL Error: " + e.getMessage());
                }
            }
            else if(selection == 2) {
                // want to allow the customer to view all available (unsold) vehicles
                // base table will be vehicles, but we the user to see vin, brand (brands), model bodyStyle (models), color, engineType, salePrice
                // and we only want available, so any vins listed within sales are sold, and not available
                // thus we'll join all those tables on vehicles, and I think a left join of sales where the VIN is null will ensure we get all instances
                String sql = "SELECT\n" +
                    "v.vin, b.brand, m.model, m.bodyStyle, v.color, v.engineType, v.salePrice\n" +
                    "FROM vehicles v\n" +
                    "JOIN models m ON v.model = m.model\n" +
                    "JOIN brands b ON m.brand = b.brand\n" +
                    "LEFT JOIN sales s ON v.vin = s.vin\n" +
                    "WHERE s.vin IS NULL\n" +
                    "ORDER BY b.brand, m.model, v.salePrice;";

                try {
                    String result = execute(sql);
                    showTextBox(result);
                }
                catch (SQLException e) {
                    showTextBox("SQL Error: " + e.getMessage());
                }
            }
            else if(selection == -2) { // back button
                // return to menu
                break;
            }
            else {
                showTextBox("Invalid selection.");
            }
        }
    }

    public static void showMarketingPortal() {
        while (true) {
            int selection = showMenuAndGetSelection("Marketing portal", new String[] {
                "Show SALES TRENDS by brand over the past N years, given by year, month, week, gender, and income range",
                "FIND The top M BRANDS by revenue in the past N years",
                "FIND The top M BRANDS by units sold in the past N years",
                "FIND the MONTH which has the BEST revenue for a body style (ex Convertible).",
            });
            if(selection == 0) {
                try {
                    int years = Integer.parseInt(getInput("Enter how many years"));
                    String sql = "SELECT \n" +
                        "    b.brand,\n" +
                        "    YEAR(s.saleDate) AS year,\n" +
                        "    MONTH(s.saleDate) AS month,\n" +
                        "    WEEK(s.saleDate) AS week,\n" +
                        "    c.gender,\n" +
                        "    FLOOR(c.income / 25000) * 25000 AS incomeRange,\n" +
                        "    SUM(s.salePrice) AS totalSales\n" +
                        "FROM sales s\n" +
                        "JOIN vehicles v ON s.vin = v.vin\n" +
                        "JOIN models m ON v.model = m.model\n" +
                        "JOIN brands b ON m.brand = b.brand\n" +
                        "JOIN customers c ON s.customerID = c.customerID\n" +
                        "WHERE s.saleDate >= DATE_SUB(CURDATE(), INTERVAL " + years + " YEAR)\n" +
                        "GROUP BY b.brand, year, month, week, c.gender, incomeRange\n" +
                        "ORDER BY year DESC, month DESC, week DESC, totalSales DESC;";
                    String result = execute(sql);
                    showTextBox(result);
                } catch (SQLException e) {
                    showTextBox("SQL Error: " + e.getMessage());
                } catch (Exception e) { // will catch if the user doesn't enter a number
                    break;
                }
            } else if(selection == 1) {
                try {
                    //Get user input for number of years
                    int brands = Integer.parseInt(getInput("Enter how many brands to show (ex 5)"));
                    int years = Integer.parseInt(getInput("Enter how many years (ex 5)"));

                    String sql =
                        "SELECT brand, SUM(sales.salePrice) AS totalSales, COUNT(*) AS UnitsSold\n" +
                        "FROM sales\n" +
                        "JOIN vehicles ON  sales.vin = vehicles.vin\n" +
                        "JOIN models ON vehicles.model = models.model\n" +
                        "WHERE saleDate >= DATE_SUB(CURDATE(), INTERVAL " + years + " YEAR)\n" +
                        "GROUP BY brand\n" +
                        "ORDER BY totalSales DESC\n" +
                        "LIMIT " + brands + ";";

                    String result = execute(sql);
                    showTextBox(result);
                }
                catch (SQLException e) {
                    showTextBox ("SQL Error: " + e.getMessage());
                } catch (Exception e) { // will catch if the user doesn't enter a number
                    break;
                }
            } else if(selection == 2) {
                try {
                    //Get user input for number of years
                    int brands = Integer.parseInt(getInput("Enter how many brands to show (ex 5)"));
                    int years = Integer.parseInt(getInput("Enter how many years (ex 5)"));

                    String sql =
                        "SELECT brand, SUM(sales.salePrice) AS totalSales, COUNT(*) AS UnitsSold\n" +
                        "FROM sales\n" +
                        "JOIN vehicles ON  sales.vin = vehicles.vin\n" +
                        "JOIN models ON vehicles.model = models.model\n" +
                        "WHERE saleDate >= DATE_SUB(CURDATE(), INTERVAL " + years + " YEAR)\n" +
                        "GROUP BY brand\n" +
                        "ORDER BY UnitsSold DESC\n" +
                        "LIMIT " + brands + ";";

                    String result = execute(sql);
                    showTextBox(result);
                }
                catch (SQLException e) {
                    showTextBox ("SQL Error: " + e.getMessage());
                } catch (Exception e) { // will catch if the user doesn't enter a number
                    break;
                }
            } else if(selection == 3) {
                try {
                    // get user input for body style to query for
                    String bodyStyle = getInput("Enter vehicle body style (default: Convertible):");
                    // default to convertible
                    if (bodyStyle.isEmpty()) {
                        bodyStyle = "Convertible";
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
                } catch (Exception e) { // will catch if the user doesn't enter a number
                    break;
                }
            }
            else if(selection == -2) { // back button
                // return to menu
                break;
            }
            else {
                showTextBox("Invalid selection.");
            }
        }
    }
    
    public static void showAdminPortal() {
        while (true) {
            int selection = showMenuAndGetSelection("Admin portal", new String[] {
                "Enter SQL command/query",
                "Import data from CSV"
            });
            if(selection == 0) {
                String sql = getInput("Enter command");
                if (sql == null || sql.trim().isEmpty()) {
                    showTextBox("No command entered. Returning to admin menu.");
                    continue;
                }

                try {
                    // execute command
                    String result = execute(sql);
                    showTextBox(result);
                }
                catch (SQLException e) {
                    showTextBox("SQL Error: " + e.getMessage());
                }
            }
            else if(selection == 1) {
                loadDataCSVMenu();
            }
            else if(selection == -2) { // back button
                // return to menu
                break;
            }
            else {
                showTextBox("Invalid selection.");
            }
        }
    }
    
    public static void showAnalystPortal() {
        while (true) {
            int selection = showMenuAndGetSelection("Analyst Portal", new String[] {
                "FIND VIN numbers for cars which were made from a given part made in a given plant within a given date range.",
                "FIND the dealers who keep a given vehicle model in inventory for the longest average time (Can search over all vehicles)."
            });
            if(selection == 0) {
                try {
                    // get user input for the part name
                    String part = getInput("Enter the part name (ex Engine):");
                    String plant = getInput("Enter the plant name (ex Ford Plant 4):");
                    String dateRange = getInput("Enter the date range (ex: 2021-01-01 2022-01-01 or leave empty to show from all dates)");
                    // Escape the single quotes
                    part = part.replace("'", "''");
                    plant = plant.replace("'", "''");
                    dateRange = dateRange.replace("'", "''");

                    String sql =
                        "SELECT vin, model, suppliedPart, dateAcquired\n" + 
                        "FROM vehicles\n" +
                        "JOIN companyPlants ON model = forModel\n" +
                        "WHERE suppliedPart LIKE '" + part + "' AND plant LIKE '" + plant + "'\n";
                    if(!dateRange.isEmpty() && dateRange.split(" ").length == 2) {
                        sql += "AND dateAcquired BETWEEN '" + dateRange.split(" ")[0] + "' AND '" + dateRange.split(" ")[1] + "'\n";
                    }
                    sql += "ORDER BY dateAcquired;";

                    String result = execute(sql);
                    showTextBox(result);
                }
                catch (SQLException e) {
                    showTextBox("SQL Error: " + e.getMessage());
                } catch (Exception e) {
                    break;
                }
            } else if(selection == 1) {
                try {
                    // need to allow for an optional filter, same getInput process for user
                    String specificModel = getInput("Enter vehicle model to filter by (ex: F150), or blank for all models:");
                    // can still use bool logic similar to python, java just requires more method calls
                    boolean filterByModel = !specificModel.isEmpty();

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
                } catch (Exception e ) {
                    break;
                }
            }
            else if(selection == -2) { // back button
                // return to menu
                break;
            }
            else {
                showTextBox("Invalid selection.");
            }
        }
    }

    public static void showOptionsAndGetSelectedForever() {
        Scanner scanner = new Scanner(System.in);
        while (true) { // only do one iteration of selection so that the printed stuff can be seen for debugging
            int selection = showMenuAndGetSelection("Which portal would you like to use", new String[] { 
                "Dealer portal",
                "Customer portal",
                "Marketing portal",
                "Admin portal",
                "Analyst portal"
            });
            System.out.println(selection);
            if(selection == 0) {
                showDealerPortal();
            }
            else if(selection == 1) {
                showCustomerPortal();
            }
            else if(selection == 2) {
                showMarketingPortal();
            }
            else if(selection == 3) {
                showAdminPortal();
            }
            else if(selection == 4) {
                showAnalystPortal();
            }
            else if (selection == -2) { // back button
                break;
            }
            else {
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
            
            String user = getInput("EUID: ");
            if(user.isEmpty()) return;
            String password = getPassword("Your 8-digit UNT ID Number: ");
            if(password.isEmpty()) return;
            
            connect(user, password);
            createTablesIfNotExists();

            // had trouble getting this to compile on my end, just going to use the old method
            showOptionsAndGetSelectedForever();
            // getSQLFromInputForever();
        } catch (SQLException e) {
            if (e.getMessage().startsWith("No suitable driver found for")) {
                System.out.println("Error, make sure you're running with including the driver:\njava -cp \".:mysql-connector-j-8.4.0/mysql-connector-j-8.4.0.jar\" main");
            } else if (e.getMessage().startsWith("Communications link failure")) {
                System.out.println("Error, incorrect password or you're not running in the cell machine CELLDB-CSE.ENG.UNT.ED");
            } else {
                System.out.println("Connection or setup error: " + e.getMessage());
                e.printStackTrace();
            }
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


Automobile Company requires a major redesign of their database system. 

## Diagram
ER diagram [link](https://dbdiagram.io/d/691ff550228c5bbc1ada7333)

## How to use
1. Login to UNT's database cell machine using host name: "CELLDB-CSE.ENG.UNT.EDU" w/ normal login credentials using Putty + WinSCP

2. Use WinSCP to create a folder, drag and drop files "main.java" and the folder containing mysql connector installation

3. Compile:
`javac main.java`

4. Run w/ Connector.jar file:
`java -cp ".:mysql-connector-j-8.4.0/mysql-connector-j-8.4.0.jar" main`

5. Input UNT EUID, then your 8-digit UNT ID found on homescreen of myUNT
 
6. Select the query by moving with the arrow keys and pressing enter. Press the right arrow key twice to get to the back button.

## Note
The sample queries were all integrated into each relevent portal:
1. Show sales trends for various brands over the past 3 years, by year, month, week. Then break these data out by gender of the buyer and then by income range. **The 1st option in the marketing portal**
2. Suppose that it is found that transmissions made by supplier Getrag between two given dates are defective. Find the VIN of each car containing such a transmission and the customer to which it was sold. If your design allows, suppose the defective transmissions all come from only one of Getrag’s plants. **The 1st option in the analyst portal**
3. Find the top 2 brands by dollar-amount sold in the past year. **The 2nd option in the marketing portal**
4. Find the top 2 brands by unit sales in the past year. **The 3rd option in the marketing portal**
5. In what month(s) do convertibles sell best? **The 4th option in the marketing portal**
6. Find those dealers who keep a vehicle in inventory for the longest average time. **The 2nd option in the analyst portal**

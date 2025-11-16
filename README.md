
Automobile Company requires a major redesign of their database system. 

## Diagram
ER diagram [link](https://dbdiagram.io/d/Copy-of-Untitled-Diagram-6909529d6735e1117011a0b8)

## How to use
1. Login to UNT's database cell machine using host name: "CELLDB-CSE.ENG.UNT.EDU" w/ normal login credentials using Putty + WinSCP

2. : Use WinSCP to create a folder/directory, drag and drop files "main.java" + folder containing mysql connector installation

3. Open "main.java" and update lines 21 & 22 (user & password) to UNT EUID and profile ID found on homescreen of myUNT

4. Compile:
"javac main.java"

4. Run w/ Connector.jar file:
`java -cp ".:mysql-connector-j-8.4.0/mysql-connector-j-8.4.0.jar" main`

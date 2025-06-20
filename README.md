# ErgasiaEApp

**ErgasiaEApp** is a Java desktop application designed for employee management and automated payroll generation. It supports different types of employees (e.g., full-time, hourly, with or without leave), and includes functionality to export payroll data to Excel.

## Technologies Used

- Java
- NetBeans
- MySQL (Database connection)
- Apache POI (Excel export functionality)
- JDBC (Database connectivity)
- Log4j (Logging)
- JAR packaging for distribution

## Project Structure

- `src/com/university/ergasiae/`: Source code (Java classes)
- `dist/ErgasiaEApp.jar`: Executable JAR file
- `dist/lib/`: External libraries (Apache POI, MySQL Connector, etc.)
- `build.xml`: Ant build script
- `manifest.mf`: JAR manifest configuration

## How to Run

### Prerequisites

- Java 21 or higher
- MySQL database (with appropriate schema and test data)
- NetBeans IDE (optional but recommended)

### Running the Application

```bash
java -jar dist/ErgasiaEApp.jar

Database Setup
The application expects the following tables to exist:

salaryhistory
workhours
leavedays
sickdays
unpaidabsences
employees

Login
The application opens with a login form. Users must be registered in the database to access the main menu.

Features
Employee record management
Payroll calculation based on hours worked, leave days, and other absence types
Support for various employee types
Payroll export to Excel (Apache POI)
Modular class design for scalability and maintenance
```
![Login](https://raw.githubusercontent.com/kirkosm/EmployeePayrollSystem/master/Login.png)

![Main Menu](https://raw.githubusercontent.com/kirkosm/EmployeePayrollSystem/master/MainMenu.png)

![Payroll](https://raw.githubusercontent.com/kirkosm/EmployeePayrollSystem/master/Payroll.png)

![Excel Export](https://raw.githubusercontent.com/kirkosm/EmployeePayrollSystem/master/excel.png)

![Receipt](https://raw.githubusercontent.com/kirkosm/EmployeePayrollSystem/master/receipt.png)

![Word Export](https://raw.githubusercontent.com/kirkosm/EmployeePayrollSystem/master/word.png)


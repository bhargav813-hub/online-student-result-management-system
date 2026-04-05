Online Student Result Management System

A robust desktop application built using Java Swing and Oracle Database to streamline the academic grading process. This system provides distinct, secure dashboards for Administrators, Faculty members, and Students, allowing educational institutions to effectively manage academic records, assign subjects, and distribute results.

📁 Project Structure

Here is an overview of the recommended folder structure for this project:

StudentResultSystem/
├── src/
│   ├── Main.java                  # Application entry point
│   ├── db/
│   │   └── DatabaseConnection.java# JDBC connection logic
│   ├── model/
│   │   └── UserSession.java       # User session management
│   └── ui/
│       ├── LoginPage.java         # Main login screen
│       ├── AdminModule.java       # Administrator dashboard
│       ├── FacultyModule.java     # Faculty dashboard
│       ├── StudentModule.java     # Student dashboard
│       └── UIUtils.java           # Shared styling and UI components
├── lib/
│   └── ojdbc11.jar                # Oracle JDBC Driver
├── sql/
│   └── Database.sql               # Database schema and mock data
└── README.md                      # Project documentation


🌟 Key Features

The system is divided into three main modules:

1. Admin Module

Student Management: Add, update, and delete student records (Name, Roll No, Semester, Department, Password).

Faculty Management: Register new faculty members, manage their login credentials, and securely remove them when necessary.

Subject Assignment: Assign specific subjects to faculty members across different semesters.

2. Faculty Module

Dashboard & Subject Overview: View all assigned subjects filtered by semester.

Result Entry: Input student marks effortlessly.

Special Grading Cases: Seamlessly handle Absent (AB) and Malpractice (MP) cases, which are intelligently processed by the system.

Account Security: Built-in password change functionality.

3. Student Module

Secure Result Portal: Students log in using their Roll Number and password.

Comprehensive Grading: View subject-wise marks, final percentage, and automatically calculated grades.

Fail/Pass Logic: Automatic detection of failing grades (below 35), absences, and malpractice.

🛠️ Technologies Used

Frontend UI: Java Swing & AWT (Custom styled components)

Backend Logic: Java (JDK 8 or higher)

Database Connectivity: JDBC API

Database Management: Oracle Database (Express Edition / XEPDB1)

External Libraries: ojdbc11.jar or ojdbc8.jar (Oracle JDBC Driver)

🚀 Installation & Setup

Prerequisites

Java Development Kit (JDK): Ensure JDK 8+ is installed.

Oracle Database: Ensure Oracle DB (e.g., 11g XE, 18c XE, or 21c XE) is installed and running.

Oracle JDBC Driver: Download the ojdbc11.jar file from the official Oracle website.

Step 1: Database Setup

Open your Oracle SQL client (SQL Developer, SQL*Plus, etc.).

Connect to your database (use your system user or a dedicated schema).

Execute the provided SQL Schema script to create the necessary tables (admin, faculty, students, subjects, faculty_subject_mapping, results).

Ensure the default admin user is inserted via the script.

Step 2: Project Configuration

Clone or download this project and open it in your preferred IDE (IntelliJ IDEA, Eclipse, VS Code).

Add the JDBC Driver: Add ojdbc11.jar to your project's Build Path/Libraries.

Configure Database Connection: Open DatabaseConnection.java and update the database credentials:

private static final String URL = "jdbc:oracle:thin:@localhost:1521/XEPDB1"; // or :xe
private static final String USER = "system"; // Your DB username
private static final String PASS = "oracle"; // Your DB password


Step 3: Run the Application

Compile and run the Main.java file.

The Login Screen will appear.

Default Admin Login:

Role: Admin

Username: admin

Password: admin123

💡 Usage Guide

Entering Marks (Faculty): When entering marks, you can type a number between 0-100, type AB for an absent student, or MP for malpractice. The system translates these automatically.

Deleting Faculty (Admin): The system prevents constraint errors by automatically detaching subject assignments before permanently deleting a faculty profile.

🔒 Security Architecture

Prepared Statements: The entire application utilizes JDBC Prepared Statements to prevent SQL Injection attacks.

Session Management: Static session singletons ensure that unauthorized cross-dashboard access is prevented.

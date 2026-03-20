-- Oracle Database Schema
-- Run this script as the 'system' user or another user with sufficient privileges.

-- Create admin table
CREATE TABLE admin (
    admin_id VARCHAR2(50) PRIMARY KEY,
    username VARCHAR2(50) UNIQUE NOT NULL,
    password VARCHAR2(255) NOT NULL
);

-- Insert default admin
INSERT INTO admin (admin_id, username, password) VALUES ('A001', 'admin', 'admin123');

-- Create faculty table
CREATE TABLE faculty (
    faculty_id VARCHAR2(50) PRIMARY KEY,
    name VARCHAR2(100) NOT NULL,
    username VARCHAR2(50) UNIQUE NOT NULL,
    password VARCHAR2(255) NOT NULL
);

-- Create students table
CREATE TABLE students (
    roll_no VARCHAR2(50) PRIMARY KEY,
    name VARCHAR2(100) NOT NULL,
    semester NUMBER NOT NULL,
    department VARCHAR2(100) NOT NULL,
    password VARCHAR2(255) NOT NULL
);

-- Create subjects table
CREATE TABLE subjects (
    subject_id VARCHAR2(50) PRIMARY KEY,
    subject_name VARCHAR2(100) NOT NULL,
    semester NUMBER NOT NULL
);

-- Note: We might want some default subjects to test with
INSERT INTO subjects (subject_id, subject_name, semester) VALUES ('S101', 'Mathematics-I', 1);
INSERT INTO subjects (subject_id, subject_name, semester) VALUES ('S102', 'Physics', 1);
INSERT INTO subjects (subject_id, subject_name, semester) VALUES ('S201', 'Data Structures', 2);
INSERT INTO subjects (subject_id, subject_name, semester) VALUES ('S202', 'Digital Logic', 2);

-- Create faculty_subject_mapping table
CREATE TABLE faculty_subject_mapping (
    faculty_id VARCHAR2(50),
    subject_id VARCHAR2(50),
    PRIMARY KEY (faculty_id, subject_id),
    FOREIGN KEY (faculty_id) REFERENCES faculty(faculty_id) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subjects(subject_id) ON DELETE CASCADE
);

-- Create results table
CREATE TABLE results (
    roll_no VARCHAR2(50),
    subject_id VARCHAR2(50),
    marks NUMBER(5, 2) NOT NULL,
    PRIMARY KEY (roll_no, subject_id),
    FOREIGN KEY (roll_no) REFERENCES students(roll_no) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subjects(subject_id) ON DELETE CASCADE
);

COMMIT;

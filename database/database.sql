-- ==============================================================
-- Student Management System - Database Setup Script
-- Database: MySQL
-- ==============================================================

-- Step 1: Create the Database if it does not already exist
CREATE DATABASE IF NOT EXISTS student_management;

-- Step 2: Select and use the database
USE student_management;

-- Step 3: Create the 'students' table
CREATE TABLE IF NOT EXISTS students (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    reg_no VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    department VARCHAR(100) NOT NULL,
    year_of_study VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Step 4: Insert sample records for demonstration and testing
INSERT INTO students (name, reg_no, email, phone, department, year_of_study) VALUES
('Aarav Sharma', 'REG2024001', 'aarav.sharma@college.edu', '9876543210', 'Computer Science', '3rd Year'),
('Priya Patel', 'REG2024002', 'priya.patel@college.edu', '9876543211', 'Information Technology', '2nd Year'),
('Rahul Verma', 'REG2024003', 'rahul.verma@college.edu', '9876543212', 'Electronics & Communication', '4th Year'),
('Sneha Reddy', 'REG2024004', 'sneha.reddy@college.edu', '9876543213', 'Computer Science', '1st Year'),
('Vikram Singh', 'REG2024005', 'vikram.singh@college.edu', '9876543214', 'Mechanical Engineering', '3rd Year'),
('Ananya Iyer', 'REG2024006', 'ananya.iyer@college.edu', '9876543215', 'Civil Engineering', '2nd Year'),
('Karthik Nair', 'REG2024007', 'karthik.nair@college.edu', '9876543216', 'Electrical & Electronics', '4th Year'),
('Diya Mukherjee', 'REG2024008', 'diya.m@college.edu', '9876543217', 'Information Technology', '1st Year');

-- Step 5: Verify the inserted records
SELECT * FROM students;

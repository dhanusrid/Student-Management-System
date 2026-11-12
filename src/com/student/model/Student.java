package com.student.model;

/**
 * Student.java
 * 
 * Model class representing a Student entity in the Student Management System.
 * Encapsulates all student details with private variables and public getter/setter methods.
 */
public class Student {

    // Private fields to ensure data encapsulation
    private int id;
    private String name;
    private String regNo;
    private String email;
    private String phone;
    private String department;
    private String yearOfStudy;

    // Default No-Argument Constructor
    public Student() {
    }

    // Constructor with all fields (useful when reading records from Database)
    public Student(int id, String name, String regNo, String email, String phone, String department, String yearOfStudy) {
        this.id = id;
        this.name = name;
        this.regNo = regNo;
        this.email = email;
        this.phone = phone;
        this.department = department;
        this.yearOfStudy = yearOfStudy;
    }

    // Constructor without ID (useful when adding a new student before DB assigns an auto-increment ID)
    public Student(String name, String regNo, String email, String phone, String department, String yearOfStudy) {
        this.name = name;
        this.regNo = regNo;
        this.email = email;
        this.phone = phone;
        this.department = department;
        this.yearOfStudy = yearOfStudy;
    }

    // --- GETTERS AND SETTERS ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegNo() {
        return regNo;
    }

    public void setRegNo(String regNo) {
        this.regNo = regNo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getYearOfStudy() {
        return yearOfStudy;
    }

    public void setYearOfStudy(String yearOfStudy) {
        this.yearOfStudy = yearOfStudy;
    }

    /**
     * Converts the Student object into a JSON string format.
     * This allows the Java backend to communicate easily with the frontend JavaScript.
     */
    public String toJson() {
        return "{"
                + "\"id\":" + id + ","
                + "\"name\":\"" + escapeJson(name) + "\","
                + "\"regNo\":\"" + escapeJson(regNo) + "\","
                + "\"email\":\"" + escapeJson(email) + "\","
                + "\"phone\":\"" + escapeJson(phone) + "\","
                + "\"department\":\"" + escapeJson(department) + "\","
                + "\"yearOfStudy\":\"" + escapeJson(yearOfStudy) + "\""
                + "}";
    }

    /**
     * Utility method to escape special JSON characters to prevent malformed responses.
     */
    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\b", "\\b")
                    .replace("\f", "\\f")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", regNo='" + regNo + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", department='" + department + '\'' +
                ", yearOfStudy='" + yearOfStudy + '\'' +
                '}';
    }
}

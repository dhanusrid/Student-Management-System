package com.student.servlet;

import com.student.dao.StudentDAO;
import com.student.model.Student;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * StudentServlet.java
 * 
 * Standard Java Servlet controller class for the Student Management System.
 * Mapped to "/api/students" and "/students".
 * 
 * Responsibilities:
 * 1. Handles HTTP GET requests (List all, Get single student, Search students)
 * 2. Handles HTTP POST requests (Add student, Update student, Delete student)
 * 3. Interacts with StudentDAO to query or mutate MySQL database
 * 4. Returns JSON responses to the frontend client
 */
@WebServlet(name = "StudentServlet", urlPatterns = {"/api/students", "/students"})
public class StudentServlet extends HttpServlet {

    private StudentDAO studentDAO;

    @Override
    public void init() throws ServletException {
        // Initialize the Student Data Access Object
        this.studentDAO = new StudentDAO();
        System.out.println("[StudentServlet] Servlet initialized successfully.");
    }

    /**
     * Handles HTTP GET requests:
     * - /api/students -> returns list of all students
     * - /api/students?id=1 -> returns single student
     * - /api/students?action=search&query=...&dept=...&year=... -> returns search results
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        String action = request.getParameter("action");
        String idParam = request.getParameter("id");

        try {
            if (idParam != null && !idParam.trim().isEmpty()) {
                // Fetch single student by ID
                int id = Integer.parseInt(idParam.trim());
                Student student = studentDAO.getStudentById(id);
                if (student != null) {
                    out.print(student.toJson());
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"error\":\"Student not found with ID " + id + "\"}");
                }
            } else if ("search".equalsIgnoreCase(action)) {
                // Search students with filters
                String query = request.getParameter("query");
                String dept = request.getParameter("department");
                String year = request.getParameter("year");

                List<Student> list = studentDAO.searchStudents(query, dept, year);
                out.print(studentsToJson(list));
            } else {
                // Default: Return all students
                List<Student> list = studentDAO.getAllStudents();
                out.print(studentsToJson(list));
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    /**
     * Handles HTTP POST requests:
     * - action=add -> Insert new student
     * - action=update -> Update student details
     * - action=delete -> Delete student by ID
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        String action = request.getParameter("action");

        // Handle JSON payload if parameters were not sent as form fields
        if (action == null) {
            action = request.getHeader("X-Action");
        }

        try {
            if ("add".equalsIgnoreCase(action)) {
                // CREATE - Add Student
                String name = request.getParameter("name");
                String regNo = request.getParameter("regNo");
                String email = request.getParameter("email");
                String phone = request.getParameter("phone");
                String department = request.getParameter("department");
                String yearOfStudy = request.getParameter("yearOfStudy");

                // Validation
                if (isEmpty(name) || isEmpty(regNo) || isEmpty(email) || isEmpty(phone) || isEmpty(department) || isEmpty(yearOfStudy)) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"success\":false,\"message\":\"All fields are required!\"}");
                    return;
                }

                // Check for duplicate register number
                if (studentDAO.getStudentByRegNo(regNo) != null) {
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                    out.print("{\"success\":false,\"message\":\"Register number '" + regNo + "' is already registered!\"}");
                    return;
                }

                Student newStudent = new Student(name.trim(), regNo.trim(), email.trim(), phone.trim(), department.trim(), yearOfStudy.trim());
                boolean created = studentDAO.addStudent(newStudent);

                if (created) {
                    out.print("{\"success\":true,\"message\":\"Student added successfully!\",\"student\":" + newStudent.toJson() + "}");
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.print("{\"success\":false,\"message\":\"Failed to insert student into database.\"}");
                }

            } else if ("update".equalsIgnoreCase(action)) {
                // UPDATE - Edit Student Details
                String idStr = request.getParameter("id");
                String name = request.getParameter("name");
                String regNo = request.getParameter("regNo");
                String email = request.getParameter("email");
                String phone = request.getParameter("phone");
                String department = request.getParameter("department");
                String yearOfStudy = request.getParameter("yearOfStudy");

                if (isEmpty(idStr) || isEmpty(name) || isEmpty(regNo) || isEmpty(email) || isEmpty(phone) || isEmpty(department) || isEmpty(yearOfStudy)) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"success\":false,\"message\":\"All fields are required!\"}");
                    return;
                }

                int id = Integer.parseInt(idStr.trim());
                Student existing = studentDAO.getStudentById(id);
                if (existing == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"success\":false,\"message\":\"Student record not found.\"}");
                    return;
                }

                // Check duplicate regNo with other records
                Student existingReg = studentDAO.getStudentByRegNo(regNo);
                if (existingReg != null && existingReg.getId() != id) {
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                    out.print("{\"success\":false,\"message\":\"Register number '" + regNo + "' belongs to another student!\"}");
                    return;
                }

                Student updatedStudent = new Student(id, name.trim(), regNo.trim(), email.trim(), phone.trim(), department.trim(), yearOfStudy.trim());
                boolean updated = studentDAO.updateStudent(updatedStudent);

                if (updated) {
                    out.print("{\"success\":true,\"message\":\"Student updated successfully!\",\"student\":" + updatedStudent.toJson() + "}");
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.print("{\"success\":false,\"message\":\"Failed to update student record.\"}");
                }

            } else if ("delete".equalsIgnoreCase(action)) {
                // DELETE - Remove Student Details
                String idStr = request.getParameter("id");
                if (isEmpty(idStr)) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"success\":false,\"message\":\"Student ID is required for deletion.\"}");
                    return;
                }

                int id = Integer.parseInt(idStr.trim());
                boolean deleted = studentDAO.deleteStudent(id);

                if (deleted) {
                    out.print("{\"success\":true,\"message\":\"Student deleted successfully!\"}");
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"success\":false,\"message\":\"Student record could not be found or deleted.\"}");
                }

            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\":false,\"message\":\"Unknown action: " + action + "\"}");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\":false,\"message\":\"Invalid number format: " + escapeJson(e.getMessage()) + "\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\":false,\"message\":\"Server error: " + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    private String studentsToJson(List<Student> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i).toJson());
            if (i < list.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\"", "\\\"").replace("\n", " ").replace("\r", "");
    }
}

package com.student.dao;

import com.student.model.Student;
import com.student.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * StudentDAO.java
 * 
 * Data Access Object (DAO) layer for the Student entity.
 * Handles all CRUD (Create, Read, Update, Delete) and Search operations.
 * 
 * Uses standard JDBC (Java Database Connectivity) with PreparedStatements:
 * - Prevents SQL Injection
 * - Pre-compiles SQL queries for high performance
 * - Handles type mapping between Java objects and MySQL rows
 */
public class StudentDAO {

    // In-memory data store used automatically when MySQL is offline
    // Initialized with realistic sample student records matching database.sql
    private static final List<Student> inMemoryStudents = new CopyOnWriteArrayList<>();
    private static final AtomicInteger idCounter = new AtomicInteger(8);

    static {
        inMemoryStudents.add(new Student(1, "Aarav Sharma", "REG2024001", "aarav.sharma@college.edu", "9876543210", "Computer Science", "3rd Year"));
        inMemoryStudents.add(new Student(2, "Priya Patel", "REG2024002", "priya.patel@college.edu", "9876543211", "Information Technology", "2nd Year"));
        inMemoryStudents.add(new Student(3, "Rahul Verma", "REG2024003", "rahul.verma@college.edu", "9876543212", "Electronics & Communication", "4th Year"));
        inMemoryStudents.add(new Student(4, "Sneha Reddy", "REG2024004", "sneha.reddy@college.edu", "9876543213", "Computer Science", "1st Year"));
        inMemoryStudents.add(new Student(5, "Vikram Singh", "REG2024005", "vikram.singh@college.edu", "9876543214", "Mechanical Engineering", "3rd Year"));
        inMemoryStudents.add(new Student(6, "Ananya Iyer", "REG2024006", "ananya.iyer@college.edu", "9876543215", "Civil Engineering", "2nd Year"));
        inMemoryStudents.add(new Student(7, "Karthik Nair", "REG2024007", "karthik.nair@college.edu", "9876543216", "Electrical & Electronics", "4th Year"));
        inMemoryStudents.add(new Student(8, "Diya Mukherjee", "REG2024008", "diya.m@college.edu", "9876543217", "Information Technology", "1st Year"));
    }

    /**
     * CREATE OPERATION (Add New Student)
     * Inserts a new student record into the MySQL database.
     * 
     * @param student The Student object containing new student details
     * @return true if insertion was successful, false otherwise
     */
    public boolean addStudent(Student student) {
        Connection conn = DBConnection.getConnection();
        if (conn != null) {
            String sql = "INSERT INTO students (name, reg_no, email, phone, department, year_of_study) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                // Set parameter values safely into PreparedStatement placeholders
                pstmt.setString(1, student.getName());
                pstmt.setString(2, student.getRegNo());
                pstmt.setString(3, student.getEmail());
                pstmt.setString(4, student.getPhone());
                pstmt.setString(5, student.getDepartment());
                pstmt.setString(6, student.getYearOfStudy());

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    try (ResultSet rs = pstmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            student.setId(rs.getInt(1));
                        }
                    }
                    System.out.println("[StudentDAO - MySQL] Successfully added student: " + student.getName());
                    return true;
                }
            } catch (SQLException e) {
                System.err.println("[StudentDAO - MySQL] Error inserting student: " + e.getMessage());
            } finally {
                DBConnection.closeConnection(conn);
            }
        }

        // Fallback execution when MySQL is offline
        int newId = idCounter.incrementAndGet();
        student.setId(newId);
        inMemoryStudents.add(student);
        System.out.println("[StudentDAO - Memory] Added student: " + student.getName() + " (ID: " + newId + ")");
        return true;
    }

    /**
     * READ OPERATION (View All Students)
     * Retrieves all student records ordered by ID descending.
     * 
     * @return List of Student objects
     */
    public List<Student> getAllStudents() {
        Connection conn = DBConnection.getConnection();
        if (conn != null) {
            List<Student> list = new ArrayList<>();
            String sql = "SELECT * FROM students ORDER BY id DESC";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    Student s = mapResultSetToStudent(rs);
                    list.add(s);
                }
                return list;
            } catch (SQLException e) {
                System.err.println("[StudentDAO - MySQL] Error fetching all students: " + e.getMessage());
            } finally {
                DBConnection.closeConnection(conn);
            }
        }

        // Fallback execution
        List<Student> copy = new ArrayList<>(inMemoryStudents);
        copy.sort((a, b) -> Integer.compare(b.getId(), a.getId()));
        return copy;
    }

    /**
     * READ OPERATION (Get Student by ID)
     * Finds a single student record matching the given ID.
     * 
     * @param id Primary key ID of the student
     * @return Student object if found, or null
     */
    public Student getStudentById(int id) {
        Connection conn = DBConnection.getConnection();
        if (conn != null) {
            String sql = "SELECT * FROM students WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToStudent(rs);
                    }
                }
            } catch (SQLException e) {
                System.err.println("[StudentDAO - MySQL] Error fetching student by ID: " + e.getMessage());
            } finally {
                DBConnection.closeConnection(conn);
            }
        }

        // Fallback execution
        for (Student s : inMemoryStudents) {
            if (s.getId() == id) {
                return s;
            }
        }
        return null;
    }

    /**
     * READ OPERATION (Get Student by Register Number)
     * Used to prevent duplicate registration numbers.
     */
    public Student getStudentByRegNo(String regNo) {
        if (regNo == null) return null;
        Connection conn = DBConnection.getConnection();
        if (conn != null) {
            String sql = "SELECT * FROM students WHERE reg_no = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, regNo.trim());
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToStudent(rs);
                    }
                }
            } catch (SQLException e) {
                System.err.println("[StudentDAO - MySQL] Error fetching student by RegNo: " + e.getMessage());
            } finally {
                DBConnection.closeConnection(conn);
            }
        }

        // Fallback execution
        for (Student s : inMemoryStudents) {
            if (s.getRegNo() != null && s.getRegNo().equalsIgnoreCase(regNo.trim())) {
                return s;
            }
        }
        return null;
    }

    /**
     * UPDATE OPERATION (Edit Student Details)
     * Updates an existing student record with new values.
     * 
     * @param student The Student object with updated details
     * @return true if updated successfully, false otherwise
     */
    public boolean updateStudent(Student student) {
        Connection conn = DBConnection.getConnection();
        if (conn != null) {
            String sql = "UPDATE students SET name = ?, reg_no = ?, email = ?, phone = ?, department = ?, year_of_study = ? WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, student.getName());
                pstmt.setString(2, student.getRegNo());
                pstmt.setString(3, student.getEmail());
                pstmt.setString(4, student.getPhone());
                pstmt.setString(5, student.getDepartment());
                pstmt.setString(6, student.getYearOfStudy());
                pstmt.setInt(7, student.getId());

                int rowsAffected = pstmt.executeUpdate();
                System.out.println("[StudentDAO - MySQL] Updated student ID " + student.getId() + ", rows affected: " + rowsAffected);
                return rowsAffected > 0;
            } catch (SQLException e) {
                System.err.println("[StudentDAO - MySQL] Error updating student: " + e.getMessage());
            } finally {
                DBConnection.closeConnection(conn);
            }
        }

        // Fallback execution
        for (int i = 0; i < inMemoryStudents.size(); i++) {
            if (inMemoryStudents.get(i).getId() == student.getId()) {
                inMemoryStudents.set(i, student);
                System.out.println("[StudentDAO - Memory] Updated student ID " + student.getId());
                return true;
            }
        }
        return false;
    }

    /**
     * DELETE OPERATION (Delete Student)
     * Removes a student record by ID.
     * 
     * @param id Primary key ID to delete
     * @return true if record was removed, false otherwise
     */
    public boolean deleteStudent(int id) {
        Connection conn = DBConnection.getConnection();
        if (conn != null) {
            String sql = "DELETE FROM students WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                int rowsAffected = pstmt.executeUpdate();
                System.out.println("[StudentDAO - MySQL] Deleted student ID " + id + ", rows affected: " + rowsAffected);
                return rowsAffected > 0;
            } catch (SQLException e) {
                System.err.println("[StudentDAO - MySQL] Error deleting student: " + e.getMessage());
            } finally {
                DBConnection.closeConnection(conn);
            }
        }

        // Fallback execution
        boolean removed = inMemoryStudents.removeIf(s -> s.getId() == id);
        System.out.println("[StudentDAO - Memory] Removed student ID " + id + ": " + removed);
        return removed;
    }

    /**
     * SEARCH OPERATION
     * Searches students by name, register number, department, or year of study.
     * 
     * @param keyword Text keyword for name, reg_no, or email
     * @param department Optional filter for department
     * @param year Optional filter for year of study
     * @return List of matching students
     */
    public List<Student> searchStudents(String keyword, String department, String year) {
        Connection conn = DBConnection.getConnection();
        if (conn != null) {
            List<Student> list = new ArrayList<>();
            StringBuilder sql = new StringBuilder("SELECT * FROM students WHERE 1=1 ");
            List<Object> params = new ArrayList<>();

            if (keyword != null && !keyword.trim().isEmpty()) {
                sql.append("AND (LOWER(name) LIKE ? OR LOWER(reg_no) LIKE ? OR LOWER(email) LIKE ?) ");
                String likePattern = "%" + keyword.trim().toLowerCase() + "%";
                params.add(likePattern);
                params.add(likePattern);
                params.add(likePattern);
            }

            if (department != null && !department.trim().isEmpty() && !department.equalsIgnoreCase("All")) {
                sql.append("AND department = ? ");
                params.add(department.trim());
            }

            if (year != null && !year.trim().isEmpty() && !year.equalsIgnoreCase("All")) {
                sql.append("AND year_of_study = ? ");
                params.add(year.trim());
            }

            sql.append("ORDER BY id DESC");

            try (PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
                for (int i = 0; i < params.size(); i++) {
                    pstmt.setObject(i + 1, params.get(i));
                }
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSetToStudent(rs));
                    }
                }
                return list;
            } catch (SQLException e) {
                System.err.println("[StudentDAO - MySQL] Error searching students: " + e.getMessage());
            } finally {
                DBConnection.closeConnection(conn);
            }
        }

        // Fallback execution
        List<Student> results = new ArrayList<>();
        String kw = (keyword != null) ? keyword.trim().toLowerCase() : "";
        String dept = (department != null) ? department.trim() : "";
        String yr = (year != null) ? year.trim() : "";

        for (Student s : inMemoryStudents) {
            boolean matchesKw = kw.isEmpty()
                    || (s.getName() != null && s.getName().toLowerCase().contains(kw))
                    || (s.getRegNo() != null && s.getRegNo().toLowerCase().contains(kw))
                    || (s.getEmail() != null && s.getEmail().toLowerCase().contains(kw));

            boolean matchesDept = dept.isEmpty() || dept.equalsIgnoreCase("All")
                    || (s.getDepartment() != null && s.getDepartment().equalsIgnoreCase(dept));

            boolean matchesYr = yr.isEmpty() || yr.equalsIgnoreCase("All")
                    || (s.getYearOfStudy() != null && s.getYearOfStudy().equalsIgnoreCase(yr));

            if (matchesKw && matchesDept && matchesYr) {
                results.add(s);
            }
        }
        results.sort((a, b) -> Integer.compare(b.getId(), a.getId()));
        return results;
    }

    /**
     * Helper method to map a MySQL ResultSet row to a Student model object.
     */
    private Student mapResultSetToStudent(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setId(rs.getInt("id"));
        s.setName(rs.getString("name"));
        s.setRegNo(rs.getString("reg_no"));
        s.setEmail(rs.getString("email"));
        s.setPhone(rs.getString("phone"));
        s.setDepartment(rs.getString("department"));
        s.setYearOfStudy(rs.getString("year_of_study"));
        return s;
    }
}

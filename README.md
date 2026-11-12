# 🎓 Student Management System

A complete, professional, and beginner-friendly **College Student Management System** web application built with **Core Java, Java Servlets, JDBC, MySQL, HTML, CSS, and JavaScript**.

Designed with a modern **blue, white, and purple** dashboard aesthetic, fully responsive across desktop, tablet, and mobile devices.

---

## 🌟 Key Features

1. **🏠 Home Page**: College hero banner, key statistics, quick navigation, and feature overview.
2. **📊 Student Dashboard**: Real-time analytical metric cards, department distribution progress bars, and recent 5 registrations.
3. **➕ Add Student**: Comprehensive registration form with client-side validation and duplicate register number detection.
4. **👥 View All Students**: Interactive table with custom avatar badges, color-coded department tags, year pills, and action buttons.
5. **🔍 Search Student**: Instant search across name, register number, and email with department and year filters.
6. **✏️ Edit Student Details**: Pre-populated modal dialog to update any student's record with instant database reflection.
7. **🗑️ Delete Student**: Confirmation modal to safely remove student profiles.

---

## 🛠️ Technologies Used

### Frontend
- **HTML5**: Semantic and clean structure for pages, cards, and tables.
- **CSS3**: Modern custom styling using CSS variables, flexbox, CSS grid, glassmorphic accents, and responsive media queries.
- **JavaScript (ES6)**: Clean DOM manipulation, asynchronous `fetch()` API calls to Java endpoints, validation, and modal control.

### Backend
- **Core Java (JDK 17+)**: Object-Oriented Programming (OOP) concepts, encapsulation, models, and collections.
- **Java Servlets (`jakarta.servlet` / `javax.servlet`)**: Web controllers handling HTTP `GET` and `POST` requests.
- **JDBC (Java Database Connectivity)**: Database communication using secure `PreparedStatement` queries to prevent SQL Injection.
- **Built-in Standalone Java Server**: Allows instant 1-click execution without needing complex Tomcat configuration!

### Database
- **MySQL 8.0+**: Relational database with the `student_management` schema and `students` table.

---

## 📁 Project Structure

```
Student Management System/
├── database/
│   └── database.sql               # MySQL database & table creation script with sample records
├── src/
│   └── com/
│       └── student/
│           ├── model/
│           │   └── Student.java               # Student POJO (encapsulation, getters/setters)
│           ├── util/
│           │   └── DBConnection.java          # JDBC MySQL connection manager & fallback
│           ├── dao/
│           │   └── StudentDAO.java            # CRUD operations with JDBC PreparedStatements
│           ├── servlet/
│           │   └── StudentServlet.java        # Standard Java Servlet for Tomcat / War
│           └── server/
│               └── StudentManagementServer.java # Built-in standalone HTTP server
├── web/
│   ├── index.html                 # Main single-page application dashboard
│   ├── css/
│   │   └── style.css              # Blue, white & purple responsive stylesheet
│   └── js/
│       └── app.js                 # Frontend API requests, search, filters & modals
├── WEB-INF/
│   └── web.xml                    # Standard Java Web Application deployment descriptor
├── pom.xml                        # Maven configuration file (for Eclipse/IntelliJ/VS Code)
├── build.bat                      # Windows build script
├── run.bat                        # 1-Click launcher for Windows
├── run.ps1                        # PowerShell launcher
└── README.md                      # Complete project documentation
```

---

## 🗄️ Database Setup (MySQL)

### Step 1: Open MySQL Command Line or MySQL Workbench
Log in with your MySQL root account:
```bash
mysql -u root -p
```

### Step 2: Run the SQL Script
Copy and run the contents of [`database/database.sql`](file:///c:/Users/8438851838/OneDrive/Desktop/Student%20Management%20System/database/database.sql):

```sql
-- Create Database
CREATE DATABASE IF NOT EXISTS student_management;
USE student_management;

-- Create Students Table
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

-- Insert Sample Records
INSERT INTO students (name, reg_no, email, phone, department, year_of_study) VALUES
('Aarav Sharma', 'REG2024001', 'aarav.sharma@college.edu', '9876543210', 'Computer Science', '3rd Year'),
('Priya Patel', 'REG2024002', 'priya.patel@college.edu', '9876543211', 'Information Technology', '2nd Year'),
('Rahul Verma', 'REG2024003', 'rahul.verma@college.edu', '9876543212', 'Electronics & Communication', '4th Year'),
('Sneha Reddy', 'REG2024004', 'sneha.reddy@college.edu', '9876543213', 'Computer Science', '1st Year'),
('Vikram Singh', 'REG2024005', 'vikram.singh@college.edu', '9876543214', 'Mechanical Engineering', '3rd Year'),
('Ananya Iyer', 'REG2024006', 'ananya.iyer@college.edu', '9876543215', 'Civil Engineering', '2nd Year'),
('Karthik Nair', 'REG2024007', 'karthik.nair@college.edu', '9876543216', 'Electrical & Electronics', '4th Year'),
('Diya Mukherjee', 'REG2024008', 'diya.m@college.edu', '9876543217', 'Information Technology', '1st Year');
```

### Step 3: Configure Database Password (if different)
Open [`src/com/student/util/DBConnection.java`](file:///c:/Users/8438851838/OneDrive/Desktop/Student%20Management%20System/src/com/student/util/DBConnection.java) and verify your password:
```java
private static final String URL = "jdbc:mysql://localhost:3306/student_management?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
private static final String USER = "root";
private static final String PASSWORD = "root"; // Change to your MySQL password or "" if no password
```

> **Note for Beginners**: If MySQL is not running yet or you haven't configured it, the application automatically runs in safe in-memory demo mode with the 8 sample students preloaded so all buttons, searches, edits, and deletions work immediately!

---

## 🚀 How to Run the Project

### Method 1: One-Click Quick Run (Easiest)
Simply double-click the **`run.bat`** file in the project folder.
It will:
1. Automatically compile the Java classes.
2. Launch the Java Web Server.
3. Automatically open your browser to **`http://localhost:8080`**!

---

### Method 2: Run via Terminal (Command Prompt / PowerShell)

1. Open Command Prompt in the project folder.
2. Compile the Java files:
   ```cmd
   javac -encoding UTF-8 -d bin src\com\student\model\*.java src\com\student\util\*.java src\com\student\dao\*.java src\com\student\server\*.java
   ```
3. Run the Java server:
   ```cmd
   java -cp "bin;lib/*" com.student.server.StudentManagementServer
   ```
4. Open your browser and visit:
   ```
   http://localhost:8080
   ```

---

### Method 3: Run with Apache Tomcat / Eclipse / IntelliJ IDEA

1. **Import as Maven Webapp**:
   - Open your IDE (Eclipse / IntelliJ / NetBeans).
   - Select **File -> Open / Import Project -> Existing Maven Project**.
   - Point to this folder (`pom.xml`).
2. **Deploy to Apache Tomcat**:
   - Add Apache Tomcat 10+ server to your IDE.
   - Add `student-management-system` to the Tomcat deployment list.
   - Click **Run / Debug**.
   - The servlet will be accessible at:
     ```
     http://localhost:8080/student-management/
     ```

---

## 📖 Java Code Explanation for Beginners

### 1. `Student.java` (Model / POJO)
- Represents a student record in Java memory.
- Uses **Encapsulation**: all fields (`id`, `name`, `regNo`, `email`, `phone`, `department`, `yearOfStudy`) are `private`.
- Provides **Public Getters & Setters** to read and modify data safely.
- Contains a `toJson()` helper method to serialize student objects into JSON strings for the frontend web page.

### 2. `DBConnection.java` (Database Connection Utility)
- Loads the MySQL JDBC Driver using `Class.forName("com.mysql.cj.jdbc.Driver")`.
- Calls `DriverManager.getConnection(...)` to connect to MySQL database `student_management`.
- Has proper exception handling (`try-catch` blocks) and a safe fallback if MySQL is offline.

### 3. `StudentDAO.java` (Data Access Object)
- **DAO Pattern**: Separates database SQL queries from the rest of the application.
- **PreparedStatements**:
  - `addStudent(Student s)`: `INSERT INTO students (...) VALUES (?, ?, ?, ?, ?, ?)`
  - `getAllStudents()`: `SELECT * FROM students ORDER BY id DESC`
  - `getStudentById(int id)`: `SELECT * FROM students WHERE id = ?`
  - `updateStudent(Student s)`: `UPDATE students SET name = ?, reg_no = ?, ... WHERE id = ?`
  - `deleteStudent(int id)`: `DELETE FROM students WHERE id = ?`
  - `searchStudents(query, dept, year)`: Dynamic SQL search matching names, register numbers, departments, and years.
- Prevents SQL Injection attacks by substituting user inputs with parameter placeholders (`?`).

### 4. `StudentServlet.java` (Servlet Controller)
- Extends `HttpServlet` and is mapped to `/api/students`.
- `doGet(request, response)`: Reads query parameters (`id`, `action=search`) and returns JSON lists.
- `doPost(request, response)`: Reads action parameters (`add`, `update`, `delete`) and invokes the corresponding method in `StudentDAO`.

### 5. `StudentManagementServer.java` (Standalone Web Server)
- Uses Java's built-in `com.sun.net.httpserver.HttpServer`.
- Serves static assets (`index.html`, `style.css`, `app.js`) and routes `/api/students` calls to `StudentDAO`.
- Gives beginners a zero-setup, instant way to test and demonstrate their project.

---

## 🧪 Testing the 7 Required Features

| Feature | How to Test in the Browser |
|---|---|
| **1. Home Page** | Click **"Home"** in the sidebar. See the hero welcome banner and college feature cards. |
| **2. Dashboard** | Click **"Dashboard"**. View metrics (Total Students, Departments, Years), department breakdown bars, and recent 5 students. |
| **3. Add Student** | Click **"Add Student"**. Enter Name, unique Reg No (e.g. `REG2024099`), Email, Phone, Dept, and Year. Click **Save**. Observe the success toast! |
| **4. View All Students** | Click **"View All Students"**. View the table displaying all students with avatar badges and department tags. |
| **5. Search Student** | Type in the search box (e.g., `"Priya"` or `"REG2024003"`). Notice instant table filtering! Also test the dedicated **"Search Student"** page. |
| **6. Edit Student** | In the table, click the **✏️ (Pencil)** icon on any row. Modify details and click **"Save Changes"**. Notice the updated data. |
| **7. Delete Student** | Click the **🗑️ (Trash)** icon. Confirm deletion in the warning modal. Notice the student is removed. |

---

## 🎨 Design Highlights
- **Color Palette**: Royal Blue (`#2563eb`), Purple (`#7c3aed`), Soft Lavender (`#eff6ff`), and Crisp White (`#ffffff`).
- **Responsive Layout**: Adapts cleanly from full 4K monitors down to mobile smartphone screens with a slide-out drawer menu.
- **Interactive UI**: Toast notifications, animated progress bars, modal popups, and hover effects.

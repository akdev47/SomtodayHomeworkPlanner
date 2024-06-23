package utilities;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/addAssignment")
public class AddAssignmentServlet extends HttpServlet {
    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=somtoday6";
    private static final String DB_USER = "dab_di23242b_168";
    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int personId = Integer.parseInt(request.getParameter("person-id"));
        String homeworkName = request.getParameter("homeworkName");
        Date dueDate = Date.valueOf(request.getParameter("due-date"));
        Date publishDate = Date.valueOf(request.getParameter("publish-date"));
        Time timeIndication = null;
        int splitCount = 0; // UPDATE
        String description = request.getParameter("description");
        int lessonId = Integer.parseInt(request.getParameter("lessonId"));
        int teacherId = 0; // Will be fetched from DB
        int classId = Integer.parseInt(request.getParameter("classId"));

        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            connection.setAutoCommit(false);

            // Use PreparedStatement to avoid SQL injection
            String fetchTeacherSql = "SELECT teacher_id FROM somtoday6.teacher t WHERE t.person_id = ?";
            PreparedStatement fetchTeacherStmt = connection.prepareStatement(fetchTeacherSql);
            fetchTeacherStmt.setInt(1, personId);
            ResultSet resultSet = fetchTeacherStmt.executeQuery();

            if (resultSet.next()) {
                teacherId = resultSet.getInt("teacher_id");
            } else {
                throw new SQLException("Teacher not found for personId: " + personId);
            }

            String fetchStudentsSql = "SELECT student_id FROM somtoday6.student WHERE class_id = ?";
            PreparedStatement fetchStudentsStmt = connection.prepareStatement(fetchStudentsSql);
            fetchStudentsStmt.setInt(1, classId);
            ResultSet rs = fetchStudentsStmt.executeQuery();

            String insertHomeworkSql = "INSERT INTO somtoday6.Homework (homework_name, due_date, publish_date, time_indication, split_count, description, lesson_id, student_id, class_id, homeworksubmittable, homeworksplittable, teacher_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement insertHomeworkStmt = connection.prepareStatement(insertHomeworkSql);

            while (rs.next()) {
                int studentId = rs.getInt("student_id");

                insertHomeworkStmt.setString(1, homeworkName);
                insertHomeworkStmt.setDate(2, dueDate);
                insertHomeworkStmt.setDate(3, publishDate);
                insertHomeworkStmt.setTime(4, timeIndication);
                insertHomeworkStmt.setInt(5, splitCount);
                insertHomeworkStmt.setString(6, description);
                insertHomeworkStmt.setInt(7, lessonId);
                insertHomeworkStmt.setInt(8, studentId);
                insertHomeworkStmt.setInt(9, classId);
                insertHomeworkStmt.setBoolean(10, true);
                insertHomeworkStmt.setBoolean(11, true);
                insertHomeworkStmt.setInt(12, teacherId);

                insertHomeworkStmt.executeUpdate();
            }

            connection.commit(); // Commit the transaction
            connection.close();
            response.sendRedirect("assignments-teacher.html?timestamp=" + System.currentTimeMillis());
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred: " + e.getMessage());
        }
    }
}

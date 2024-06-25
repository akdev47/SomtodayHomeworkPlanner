package utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.time.LocalDate;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;

@WebServlet("/deleteLesson")
public class DeleteLessonServlet extends HttpServlet {
    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=somtoday6";
    private static final String DB_USER = "dab_di23242b_168";
    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        StringBuilder sb = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        JSONObject jsonRequest = new JSONObject(sb.toString());

        int lessonId = jsonRequest.getInt("lesson_id");

        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            connection.setAutoCommit(false);

            // get lessons details
            String lessonName = "";
            int classId = 0;
            int teacherId = 0;
            String fetchLessonDetailsSql = "SELECT lesson_name, class_id, teacher_id FROM somtoday6.Lesson WHERE lesson_id = ?";
            try (PreparedStatement fetchLessonDetailsStmt = connection.prepareStatement(fetchLessonDetailsSql)) {
                fetchLessonDetailsStmt.setInt(1, lessonId);
                ResultSet rs = fetchLessonDetailsStmt.executeQuery();
                if (rs.next()) {
                    lessonName = rs.getString("lesson_name");
                    classId = rs.getInt("class_id");
                    teacherId = rs.getInt("teacher_id");
                } else {
                    throw new SQLException("Lesson not found for lessonId: " + lessonId);
                }
            }

            // get person_id of teacher
            int teacherPersonId = 0;
            String fetchTeacherPersonIdSql = "SELECT person_id FROM somtoday6.Teacher WHERE teacher_id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(fetchTeacherPersonIdSql)) {
                pstmt.setInt(1, teacherId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    teacherPersonId = rs.getInt("person_id");
                }
            }

            // get class name
            String className = "";
            String fetchClassNameSql = "SELECT class_name FROM somtoday6.Class WHERE class_id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(fetchClassNameSql)) {
                pstmt.setInt(1, classId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    className = rs.getString("class_name");
                }
            }

            // Delete homework associated with the lesson
            String deleteHomeworkSql = "DELETE FROM somtoday6.Homework WHERE lesson_id = ?";
            try (PreparedStatement deleteHomeworkStmt = connection.prepareStatement(deleteHomeworkSql)) {
                deleteHomeworkStmt.setInt(1, lessonId);
                deleteHomeworkStmt.executeUpdate();
            }

            // Delete the lesson
            String deleteLessonSql = "DELETE FROM somtoday6.Lesson WHERE lesson_id = ?";
            try (PreparedStatement deleteLessonStmt = connection.prepareStatement(deleteLessonSql)) {
                deleteLessonStmt.setInt(1, lessonId);
                deleteLessonStmt.executeUpdate();
            }

            // send notification to teacer
            String insertNotificationSql = "INSERT INTO somtoday6.notification (date, sender, info, person_id) " +
                    "VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(insertNotificationSql)) {
                pstmt.setDate(1, Date.valueOf(LocalDate.now()));
                pstmt.setString(2, "Admin");
                pstmt.setString(3, "Admin removed your lesson: " + lessonName + " in class: " + className);
                pstmt.setInt(4, teacherPersonId);
                pstmt.executeUpdate();
            }

            // get all students in class
            String fetchStudentsSql = "SELECT person_id FROM somtoday6.Student WHERE class_id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(fetchStudentsSql)) {
                pstmt.setInt(1, classId);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    int studentPersonId = rs.getInt("person_id");

                    // send notification for each student
                    try (PreparedStatement studentPstmt = connection.prepareStatement(insertNotificationSql)) {
                        studentPstmt.setDate(1, Date.valueOf(LocalDate.now()));
                        studentPstmt.setString(2, "Admin");
                        studentPstmt.setString(3, "Admin removed a lesson (" + lessonName + ") in your class: " + className);
                        studentPstmt.setInt(4, studentPersonId);
                        studentPstmt.executeUpdate();
                    }
                }
            }

            connection.commit();
            connection.close();

            PrintWriter out = response.getWriter();
            out.write("{\"success\": true}");
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred.");
        }
    }
}

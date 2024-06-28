package jaxRS;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.json.JSONObject;

import java.sql.*;
import java.time.LocalDate;

@Path("/deleteLesson")
public class DeleteLessonResource {
    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=somtoday6";
    private static final String DB_USER = "dab_di23242b_168";
    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteLesson(String lessonData) {
        JSONObject jsonRequest = new JSONObject(lessonData);
        int lessonId = jsonRequest.getInt("lesson_id");

        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            connection.setAutoCommit(false);

            // Get lesson details
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

            // Get person_id of teacher
            int teacherPersonId = 0;
            String fetchTeacherPersonIdSql = "SELECT person_id FROM somtoday6.Teacher WHERE teacher_id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(fetchTeacherPersonIdSql)) {
                pstmt.setInt(1, teacherId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    teacherPersonId = rs.getInt("person_id");
                }
            }

            // Get class name
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

            // Send notification to teacher
            String insertNotificationSql = "INSERT INTO somtoday6.notification (date, sender, info, person_id) " +
                    "VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(insertNotificationSql)) {
                pstmt.setDate(1, Date.valueOf(LocalDate.now()));
                pstmt.setString(2, "Admin");
                pstmt.setString(3, "Admin removed your lesson: " + lessonName + " in class: " + className);
                pstmt.setInt(4, teacherPersonId);
                pstmt.executeUpdate();
            }

            // Get all students in class
            String fetchStudentsSql = "SELECT person_id FROM somtoday6.Student WHERE class_id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(fetchStudentsSql)) {
                pstmt.setInt(1, classId);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    int studentPersonId = rs.getInt("person_id");

                    // Send notification for each student
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

            return Response.ok("{\"success\": true}").build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\": \"An error occurred.\"}").build();
        }
    }
}

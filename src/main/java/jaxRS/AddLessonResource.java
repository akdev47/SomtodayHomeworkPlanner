package jaxRS;


import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.json.JSONObject;
import somtoday.model.Lesson;

import java.io.BufferedReader;
import java.sql.*;
import java.time.LocalDate;

@Path("/addLesson")
public class AddLessonResource {
    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=somtoday6";
    private static final String DB_USER = "dab_di23242b_168";
    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addLesson(String lessonData) {
        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            JSONObject jsonRequest = new JSONObject(lessonData);

            String lessonName = jsonRequest.getString("lesson_name");
            String lessonDescription = jsonRequest.getString("lesson_description");
            int teacherId = jsonRequest.getInt("teacher_id");
            int classId = jsonRequest.getInt("class_id");
            int schoolYear = LocalDate.now().getYear();

            String insertLessonSql = "INSERT INTO somtoday6.Lesson (lesson_name, lesson_description, teacher_id, class_id, school_year) " +
                    "VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement pstmt = connection.prepareStatement(insertLessonSql)) {
                pstmt.setString(1, lessonName);
                pstmt.setString(2, lessonDescription);
                pstmt.setInt(3, teacherId);
                pstmt.setInt(4, classId);
                pstmt.setInt(5, schoolYear);
                pstmt.executeUpdate();
            }

            // Get person_id of teacher
            int personId = 0;
            String fetchPersonIdSql = "SELECT person_id FROM somtoday6.Teacher WHERE teacher_id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(fetchPersonIdSql)) {
                pstmt.setInt(1, teacherId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    personId = rs.getInt("person_id");
                }
            }

            // Send notification to teacher
            String insertNotificationSql = "INSERT INTO somtoday6.notification (date, sender, info, person_id) " +
                    "VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(insertNotificationSql)) {
                pstmt.setDate(1, Date.valueOf(LocalDate.now()));
                pstmt.setString(2, "Admin");
                pstmt.setString(3, "Admin added you to a lesson: " + lessonName);
                pstmt.setInt(4, personId);
                pstmt.executeUpdate();
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

            // Send notification to teacher about class
            try (PreparedStatement pstmt = connection.prepareStatement(insertNotificationSql)) {
                pstmt.setDate(1, Date.valueOf(LocalDate.now()));
                pstmt.setString(2, "Admin");
                pstmt.setString(3, "Admin added you to class: " + className);
                pstmt.setInt(4, personId);
                pstmt.executeUpdate();
            }

            // Get all students in the class
            String fetchStudentsSql = "SELECT person_id FROM somtoday6.Student WHERE class_id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(fetchStudentsSql)) {
                pstmt.setInt(1, classId);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    int studentPersonId = rs.getInt("person_id");

                    // Add notification for each student
                    try (PreparedStatement studentPstmt = connection.prepareStatement(insertNotificationSql)) {
                        studentPstmt.setDate(1, Date.valueOf(LocalDate.now()));
                        studentPstmt.setString(2, "Admin");
                        studentPstmt.setString(3, "Admin added a new lesson (" + lessonName + ") to your class: " + className);
                        studentPstmt.setInt(4, studentPersonId);
                        studentPstmt.executeUpdate();
                    }
                }
            }

            connection.close();

            return Response.ok("{\"success\": true}").build();

        } catch (NumberFormatException e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\": \"Invalid number format\"}").build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\": \"An error occurred.\"}").build();
        }
    }
}


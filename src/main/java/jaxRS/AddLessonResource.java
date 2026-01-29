package jaxRS;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.json.JSONObject;
import somtoday.db.Db;

import java.sql.*;
import java.time.LocalDate;

@Path("/addLesson")
public class AddLessonResource {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addLesson(String lessonData) {
        try {
            JSONObject jsonRequest = new JSONObject(lessonData);

            String lessonName = jsonRequest.getString("lesson_name");
            String lessonDescription = jsonRequest.getString("lesson_description");
            int teacherId = jsonRequest.getInt("teacher_id");
            int classId = jsonRequest.getInt("class_id");
            int schoolYear = LocalDate.now().getYear();

            // ✅ use env-based connection (Supabase)
            try (Connection connection = Db.getConnection()) {
                connection.setAutoCommit(false);

                // ✅ removed "somtoday6." prefixes, and normalized table names to lowercase
                String insertLessonSql =
                        "INSERT INTO lesson (lesson_name, lesson_description, teacher_id, class_id, school_year) " +
                                "VALUES (?, ?, ?, ?, ?)";

                try (PreparedStatement pstmt = connection.prepareStatement(insertLessonSql)) {
                    pstmt.setString(1, lessonName);
                    pstmt.setString(2, lessonDescription);
                    pstmt.setInt(3, teacherId);
                    pstmt.setInt(4, classId);
                    pstmt.setInt(5, schoolYear);
                    pstmt.executeUpdate();
                }

                // get person_id of teacher
                int personId;
                String fetchPersonIdSql = "SELECT person_id FROM teacher WHERE teacher_id = ?";
                try (PreparedStatement pstmt = connection.prepareStatement(fetchPersonIdSql)) {
                    pstmt.setInt(1, teacherId);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Teacher not found for teacher_id: " + teacherId);
                        }
                        personId = rs.getInt("person_id");
                    }
                }

                // notification insert (re-used)
                String insertNotificationSql =
                        "INSERT INTO notification (date, sender, info, person_id) VALUES (?, ?, ?, ?)";

                // notify teacher: lesson added
                try (PreparedStatement pstmt = connection.prepareStatement(insertNotificationSql)) {
                    pstmt.setDate(1, Date.valueOf(LocalDate.now()));
                    pstmt.setString(2, "Admin");
                    pstmt.setString(3, "Admin added you to a lesson: " + lessonName);
                    pstmt.setInt(4, personId);
                    pstmt.executeUpdate();
                }

                // get class name
                String className;
                String fetchClassNameSql = "SELECT class_name FROM class WHERE class_id = ?";
                try (PreparedStatement pstmt = connection.prepareStatement(fetchClassNameSql)) {
                    pstmt.setInt(1, classId);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Class not found for class_id: " + classId);
                        }
                        className = rs.getString("class_name");
                    }
                }

                // notify teacher: class added
                try (PreparedStatement pstmt = connection.prepareStatement(insertNotificationSql)) {
                    pstmt.setDate(1, Date.valueOf(LocalDate.now()));
                    pstmt.setString(2, "Admin");
                    pstmt.setString(3, "Admin added you to class: " + className);
                    pstmt.setInt(4, personId);
                    pstmt.executeUpdate();
                }

                // notify students in that class
                String fetchStudentsSql = "SELECT person_id FROM student WHERE class_id = ?";
                try (PreparedStatement pstmt = connection.prepareStatement(fetchStudentsSql)) {
                    pstmt.setInt(1, classId);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            int studentPersonId = rs.getInt("person_id");

                            try (PreparedStatement studentPstmt = connection.prepareStatement(insertNotificationSql)) {
                                studentPstmt.setDate(1, Date.valueOf(LocalDate.now()));
                                studentPstmt.setString(2, "Admin");
                                studentPstmt.setString(
                                        3,
                                        "Admin added a new lesson (" + lessonName + ") to your class: " + className
                                );
                                studentPstmt.setInt(4, studentPersonId);
                                studentPstmt.executeUpdate();
                            }
                        }
                    }
                }

                connection.commit();
            }

            return Response.ok("{\"success\": true}").build();

        } catch (NumberFormatException e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Invalid number format\"}")
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"An error occurred.\"}")
                    .build();
        }
    }
}

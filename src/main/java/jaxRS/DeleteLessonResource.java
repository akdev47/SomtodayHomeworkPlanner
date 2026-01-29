package jaxRS;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.json.JSONObject;
import somtoday.db.Db;

import java.sql.*;
import java.time.LocalDate;

@Path("/deleteLesson")
public class DeleteLessonResource {

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteLesson(String lessonData) {
        JSONObject jsonRequest = new JSONObject(lessonData);
        int lessonId = jsonRequest.getInt("lesson_id");

        if (lessonId <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Invalid lesson_id\"}")
                    .build();
        }

        try (Connection connection = Db.getConnection()) {
            connection.setAutoCommit(false);

            // 1) Get lesson details
            String lessonName;
            int classId;
            int teacherId;

            String fetchLessonDetailsSql =
                    "SELECT lesson_name, class_id, teacher_id FROM lesson WHERE lesson_id = ?";

            try (PreparedStatement stmt = connection.prepareStatement(fetchLessonDetailsSql)) {
                stmt.setInt(1, lessonId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        connection.rollback();
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity("{\"error\":\"Lesson not found\"}")
                                .build();
                    }
                    lessonName = rs.getString("lesson_name");
                    classId = rs.getInt("class_id");
                    teacherId = rs.getInt("teacher_id");
                }
            }

            // 2) teacher person_id
            int teacherPersonId;
            String fetchTeacherPersonIdSql = "SELECT person_id FROM teacher WHERE teacher_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(fetchTeacherPersonIdSql)) {
                stmt.setInt(1, teacherId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Teacher not found for teacher_id: " + teacherId);
                    }
                    teacherPersonId = rs.getInt("person_id");
                }
            }

            // 3) class name
            String className = "";
            String fetchClassNameSql = "SELECT class_name FROM class WHERE class_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(fetchClassNameSql)) {
                stmt.setInt(1, classId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) className = rs.getString("class_name");
                }
            }

            // 4) Delete homework dependencies for all homework rows under this lesson
            // (Goal / split_request / splitted_homework)
            try (PreparedStatement del = connection.prepareStatement(
                    "DELETE FROM splitted_homework WHERE homework_id IN (SELECT homework_id FROM homework WHERE lesson_id = ?)")) {
                del.setInt(1, lessonId);
                del.executeUpdate();
            }

            try (PreparedStatement del = connection.prepareStatement(
                    "DELETE FROM split_request WHERE homework_id IN (SELECT homework_id FROM homework WHERE lesson_id = ?)")) {
                del.setInt(1, lessonId);
                del.executeUpdate();
            }

            try (PreparedStatement del = connection.prepareStatement(
                    "DELETE FROM goal WHERE homework_id IN (SELECT homework_id FROM homework WHERE lesson_id = ?)")) {
                del.setInt(1, lessonId);
                del.executeUpdate();
            }

            // 5) Delete homework associated with the lesson
            try (PreparedStatement del = connection.prepareStatement(
                    "DELETE FROM homework WHERE lesson_id = ?")) {
                del.setInt(1, lessonId);
                del.executeUpdate();
            }

            // 6) Delete lesson
            try (PreparedStatement del = connection.prepareStatement(
                    "DELETE FROM lesson WHERE lesson_id = ?")) {
                del.setInt(1, lessonId);
                del.executeUpdate();
            }

            // 7) Notifications
            String insertNotificationSql =
                    "INSERT INTO notification (date, sender, info, person_id) VALUES (?, ?, ?, ?)";

            // notify teacher
            try (PreparedStatement stmt = connection.prepareStatement(insertNotificationSql)) {
                stmt.setDate(1, Date.valueOf(LocalDate.now()));
                stmt.setString(2, "Admin");
                stmt.setString(3, "Admin removed your lesson: " + lessonName + " in class: " + className);
                stmt.setInt(4, teacherPersonId);
                stmt.executeUpdate();
            }

            // notify all students in class
            String fetchStudentsSql = "SELECT person_id FROM student WHERE class_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(fetchStudentsSql)) {
                stmt.setInt(1, classId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        int studentPersonId = rs.getInt("person_id");
                        try (PreparedStatement n = connection.prepareStatement(insertNotificationSql)) {
                            n.setDate(1, Date.valueOf(LocalDate.now()));
                            n.setString(2, "Admin");
                            n.setString(3, "Admin removed a lesson (" + lessonName + ") in your class: " + className);
                            n.setInt(4, studentPersonId);
                            n.executeUpdate();
                        }
                    }
                }
            }

            connection.commit();
            return Response.ok("{\"success\": true}").build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"An error occurred.\"}")
                    .build();
        }
    }
}

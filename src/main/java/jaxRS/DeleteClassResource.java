package jaxRS;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.json.JSONObject;
import somtoday.db.Db;

import java.sql.*;
import java.time.LocalDate;

@Path("/deleteClass")
public class DeleteClassResource {

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteClass(String classData) {
        JSONObject jsonRequest = new JSONObject(classData);
        int classId = jsonRequest.getInt("classId");

        if (classId <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Invalid classId\"}")
                    .build();
        }

        try (Connection connection = Db.getConnection()) {
            connection.setAutoCommit(false);

            // Fetch class name (needed for notifications)
            String className;
            String fetchClassNameSql = "SELECT class_name FROM class WHERE class_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(fetchClassNameSql)) {
                stmt.setInt(1, classId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        connection.rollback();
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity("{\"error\":\"Class not found\"}")
                                .build();
                    }
                    className = rs.getString("class_name");
                }
            }

            // Fetch students in this class (for notifications later)
            String fetchStudentsSql = "SELECT student_id, person_id FROM student WHERE class_id = ?";
            PreparedStatement fetchStudentsStmt = connection.prepareStatement(fetchStudentsSql);
            fetchStudentsStmt.setInt(1, classId);
            ResultSet studentResultSet = fetchStudentsStmt.executeQuery();

            // Fetch teachers linked through lessons (for notifications later)
            String fetchTeachersSql =
                    "SELECT DISTINCT t.person_id " +
                            "FROM teacher t " +
                            "JOIN lesson l ON t.teacher_id = l.teacher_id " +
                            "WHERE l.class_id = ?";
            PreparedStatement fetchTeachersStmt = connection.prepareStatement(fetchTeachersSql);
            fetchTeachersStmt.setInt(1, classId);
            ResultSet teacherResultSet = fetchTeachersStmt.executeQuery();

            // Delete dependent rows that reference homework in this class
            // (Order matters if you add foreign keys in Supabase)

            // 1) splitted_homework
            try (PreparedStatement stmt = connection.prepareStatement(
                    "DELETE FROM splitted_homework " +
                            "WHERE homework_id IN (SELECT homework_id FROM homework WHERE class_id = ?)")) {
                stmt.setInt(1, classId);
                stmt.executeUpdate();
            }

            // 2) split_request
            try (PreparedStatement stmt = connection.prepareStatement(
                    "DELETE FROM split_request " +
                            "WHERE homework_id IN (SELECT homework_id FROM homework WHERE class_id = ?)")) {
                stmt.setInt(1, classId);
                stmt.executeUpdate();
            }

            // 3) goal  ✅ important if goal.homework_id has FK
            try (PreparedStatement stmt = connection.prepareStatement(
                    "DELETE FROM goal " +
                            "WHERE homework_id IN (SELECT homework_id FROM homework WHERE class_id = ?)")) {
                stmt.setInt(1, classId);
                stmt.executeUpdate();
            }

            // 4) homework
            try (PreparedStatement stmt = connection.prepareStatement(
                    "DELETE FROM homework WHERE class_id = ?")) {
                stmt.setInt(1, classId);
                stmt.executeUpdate();
            }

            // 5) set students.class_id = NULL (detach students)
            try (PreparedStatement stmt = connection.prepareStatement(
                    "UPDATE student SET class_id = NULL WHERE class_id = ?")) {
                stmt.setInt(1, classId);
                stmt.executeUpdate();
            }

            // 6) delete lessons for the class
            try (PreparedStatement stmt = connection.prepareStatement(
                    "DELETE FROM lesson WHERE class_id = ?")) {
                stmt.setInt(1, classId);
                stmt.executeUpdate();
            }

            // 7) delete class row itself
            try (PreparedStatement stmt = connection.prepareStatement(
                    "DELETE FROM class WHERE class_id = ?")) {
                stmt.setInt(1, classId);
                stmt.executeUpdate();
            }

            // Notifications (after deletes are scheduled)
            while (studentResultSet.next()) {
                int studentPersonId = studentResultSet.getInt("person_id");
                insertNotification(connection, studentPersonId, "Admin removed you from class: " + className);
            }
            while (teacherResultSet.next()) {
                int teacherPersonId = teacherResultSet.getInt("person_id");
                insertNotification(connection, teacherPersonId, "Admin removed you from class: " + className);
            }

            // close the two ResultSets + Statements we left open intentionally
            teacherResultSet.close();
            fetchTeachersStmt.close();
            studentResultSet.close();
            fetchStudentsStmt.close();

            connection.commit();
            return Response.ok("{\"success\": true}").build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"An error occurred.\"}")
                    .build();
        }
    }

    private void insertNotification(Connection connection, int personId, String info) throws Exception {
        String sql = "INSERT INTO notification (date, sender, info, person_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDate(1, Date.valueOf(LocalDate.now()));
            pstmt.setString(2, "Admin");
            pstmt.setString(3, info);
            pstmt.setInt(4, personId);
            pstmt.executeUpdate();
        }
    }

    public static class DeleteClassRequest {
        private String classId;

        public String getClassId() { return classId; }
        public void setClassId(String classId) { this.classId = classId; }
    }
}

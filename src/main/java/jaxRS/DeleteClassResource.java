package jaxRS;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.json.JSONObject;

import java.sql.*;
import java.time.LocalDate;

@Path("/deleteClass")
public class DeleteClassResource {
    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=somtoday6";
    private static final String DB_USER = "dab_di23242b_168";
    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteClass(String classData) {
        JSONObject jsonRequest = new JSONObject(classData);
        int classId = jsonRequest.getInt("classId");

        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            connection.setAutoCommit(false);

            // Fetch related data
            String fetchStudentsSql = "SELECT student_id, person_id FROM somtoday6.Student WHERE class_id = ?";
            PreparedStatement fetchStudentsStmt = connection.prepareStatement(fetchStudentsSql);
            fetchStudentsStmt.setInt(1, classId);
            ResultSet studentResultSet = fetchStudentsStmt.executeQuery();

            String fetchTeachersSql = "SELECT t.person_id FROM somtoday6.Teacher t " +
                    "JOIN somtoday6.Lesson l ON t.teacher_id = l.teacher_id " +
                    "WHERE l.class_id = ?";
            PreparedStatement fetchTeachersStmt = connection.prepareStatement(fetchTeachersSql);
            fetchTeachersStmt.setInt(1, classId);
            ResultSet teacherResultSet = fetchTeachersStmt.executeQuery();

            String fetchClassNameSql = "SELECT class_name FROM somtoday6.Class WHERE class_id = ?";
            PreparedStatement fetchClassNameStmt = connection.prepareStatement(fetchClassNameSql);
            fetchClassNameStmt.setInt(1, classId);
            ResultSet classResultSet = fetchClassNameStmt.executeQuery();

            String className = "";
            if (classResultSet.next()) {
                className = classResultSet.getString("class_name");
            } else {
                throw new SQLException("Class not found for classId: " + classId);
            }

            // Delete related entries in splitted_homework
            String deleteSplittedHomeworkSQL = "DELETE FROM somtoday6.splitted_homework WHERE homework_id IN (SELECT homework_id FROM somtoday6.homework WHERE class_id = ?)";
            PreparedStatement deleteSplittedHomeworkStmt = connection.prepareStatement(deleteSplittedHomeworkSQL);
            deleteSplittedHomeworkStmt.setInt(1, classId);
            deleteSplittedHomeworkStmt.executeUpdate();

            // Delete related entries in split_request
            String deleteSplitRequestSQL = "DELETE FROM somtoday6.split_request WHERE homework_id IN (SELECT homework_id FROM somtoday6.homework WHERE class_id = ?)";
            PreparedStatement deleteSplitRequestStmt = connection.prepareStatement(deleteSplitRequestSQL);
            deleteSplitRequestStmt.setInt(1, classId);
            deleteSplitRequestStmt.executeUpdate();

            // Delete related entries in homework
            String deleteHomeworkSQL = "DELETE FROM somtoday6.homework WHERE class_id = ?";
            PreparedStatement deleteHomeworkStmt = connection.prepareStatement(deleteHomeworkSQL);
            deleteHomeworkStmt.setInt(1, classId);
            deleteHomeworkStmt.executeUpdate();

            // Update students to set class_id to NULL
            String updateStudentsSql = "UPDATE somtoday6.Student SET class_id = NULL WHERE class_id = ?";
            try (PreparedStatement updateStudentsStmt = connection.prepareStatement(updateStudentsSql)) {
                updateStudentsStmt.setInt(1, classId);
                updateStudentsStmt.executeUpdate();
            }

            // Delete lessons associated with the class
            String deleteLessonsSql = "DELETE FROM somtoday6.Lesson WHERE class_id = ?";
            try (PreparedStatement deleteLessonsStmt = connection.prepareStatement(deleteLessonsSql)) {
                deleteLessonsStmt.setInt(1, classId);
                deleteLessonsStmt.executeUpdate();
            }

            // Delete the class
            String deleteClassSql = "DELETE FROM somtoday6.Class WHERE class_id = ?";
            try (PreparedStatement deleteClassStmt = connection.prepareStatement(deleteClassSql)) {
                deleteClassStmt.setInt(1, classId);
                deleteClassStmt.executeUpdate();
            }

            // Send notifications for students
            while (studentResultSet.next()) {
                int studentPersonId = studentResultSet.getInt("person_id");
                insertNotification(connection, studentPersonId, "Admin removed you from class: " + className);
            }

            // Send notifications for teachers
            while (teacherResultSet.next()) {
                int teacherPersonId = teacherResultSet.getInt("person_id");
                insertNotification(connection, teacherPersonId, "Admin removed you from class: " + className);
            }

            connection.commit();

            return Response.ok("{\"success\": true}").build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\": \"An error occurred.\"}").build();
        }
    }

    private void insertNotification(Connection connection, int personId, String info) throws Exception {
        String sql = "INSERT INTO somtoday6.notification (date, sender, info, person_id) VALUES (?, ?, ?, ?)";
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

        public String getClassId() {
            return classId;
        }

        public void setClassId(String classId) {
            this.classId = classId;
        }
    }
}

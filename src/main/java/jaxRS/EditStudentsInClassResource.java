package jaxRS;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.json.JSONArray;
import org.json.JSONObject;
import somtoday.db.Db;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Path("/editStudentsInClass")
public class EditStudentsInClassResource {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response editStudentsInClass(String requestJson) {
        JSONObject jsonRequest = new JSONObject(requestJson);
        int classId = jsonRequest.getInt("classId");

        JSONArray studentIdsJsonArray = jsonRequest.getJSONArray("studentIds");
        List<Integer> desiredStudentIds = new ArrayList<>();
        for (int i = 0; i < studentIdsJsonArray.length(); i++) {
            desiredStudentIds.add(studentIdsJsonArray.getInt(i));
        }

        if (classId <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Invalid classId\"}")
                    .build();
        }

        try (Connection connection = Db.getConnection()) {
            connection.setAutoCommit(false);

            // current students in class
            List<Integer> currentStudentIds = fetchCurrentStudents(connection, classId);

            // diff sets
            List<Integer> studentsToAdd = desiredStudentIds.stream()
                    .filter(id -> !currentStudentIds.contains(id))
                    .collect(Collectors.toList());

            List<Integer> studentsToRemove = currentStudentIds.stream()
                    .filter(id -> !desiredStudentIds.contains(id))
                    .collect(Collectors.toList());

            // remove students: set class_id NULL
            String removeStudentsSql = "UPDATE student SET class_id = NULL WHERE class_id = ? AND student_id = ?";
            try (PreparedStatement removeStmt = connection.prepareStatement(removeStudentsSql)) {
                for (Integer studentId : studentsToRemove) {
                    removeStmt.setInt(1, classId);
                    removeStmt.setInt(2, studentId);
                    removeStmt.addBatch();
                }
                removeStmt.executeBatch();
            }

            // assign students: set class_id
            String assignStudentsSql = "UPDATE student SET class_id = ? WHERE student_id = ?";
            try (PreparedStatement assignStmt = connection.prepareStatement(assignStudentsSql)) {
                for (Integer studentId : studentsToAdd) {
                    assignStmt.setInt(1, classId);
                    assignStmt.setInt(2, studentId);
                    assignStmt.addBatch();
                }
                assignStmt.executeBatch();
            }

            // class name for notifications
            String className = fetchClassName(connection, classId);

            // notify added
            for (int studentId : studentsToAdd) {
                int personId = fetchPersonId(connection, studentId);
                insertNotification(connection, personId, "Admin added you to class: " + className);
            }

            // notify removed
            for (int studentId : studentsToRemove) {
                int personId = fetchPersonId(connection, studentId);
                insertNotification(connection, personId, "Admin removed you from class: " + className);
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

    private List<Integer> fetchCurrentStudents(Connection connection, int classId) throws SQLException {
        String sql = "SELECT student_id FROM student WHERE class_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, classId);
            try (ResultSet rs = pstmt.executeQuery()) {
                List<Integer> studentIds = new ArrayList<>();
                while (rs.next()) {
                    studentIds.add(rs.getInt("student_id"));
                }
                return studentIds;
            }
        }
    }

    private String fetchClassName(Connection connection, int classId) throws SQLException {
        String sql = "SELECT class_name FROM class WHERE class_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, classId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? rs.getString("class_name") : "";
            }
        }
    }

    private int fetchPersonId(Connection connection, int studentId) throws SQLException {
        String sql = "SELECT person_id FROM student WHERE student_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt("person_id");
            }
        }
        throw new SQLException("Person ID not found for student ID: " + studentId);
    }

    private void insertNotification(Connection connection, int personId, String info) throws SQLException {
        String sql = "INSERT INTO notification (date, sender, info, person_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDate(1, Date.valueOf(LocalDate.now()));
            pstmt.setString(2, "Admin");
            pstmt.setString(3, info);
            pstmt.setInt(4, personId);
            pstmt.executeUpdate();
        }
    }
}

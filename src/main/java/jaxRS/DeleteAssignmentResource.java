package jaxRS;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import somtoday.db.Db;

import java.sql.*;

@Path("/deleteHomeworks")
public class DeleteAssignmentResource {

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteHomework(@QueryParam("homeworkId") int homeworkId) {
        if (homeworkId <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\":\"Missing or invalid homeworkId parameter\"}")
                    .build();
        }

        try (Connection connection = Db.getConnection()) {
            connection.setAutoCommit(false);

            // 1) Fetch original homework details
            String selectHomeworkQuery =
                    "SELECT homework_name, teacher_id, publish_date, due_date " +
                            "FROM homework WHERE homework_id = ?";

            String originalHomeworkName;
            int originalTeacherId;
            Date originalPublishDate;
            Date originalDueDate;

            try (PreparedStatement stmt = connection.prepareStatement(selectHomeworkQuery)) {
                stmt.setInt(1, homeworkId);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        connection.rollback();
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity("{\"message\":\"Invalid homework_id\"}")
                                .build();
                    }

                    originalHomeworkName = rs.getString("homework_name");
                    originalTeacherId = rs.getInt("teacher_id");
                    originalPublishDate = rs.getDate("publish_date");
                    originalDueDate = rs.getDate("due_date");
                }
            }

            // 2) Fetch all matching homework instances (teacher copy + all student copies)
            String fetchHomeworkInstancesSql =
                    "SELECT homework_id, student_id " +
                            "FROM homework " +
                            "WHERE homework_name = ? AND due_date = ? AND publish_date = ? AND teacher_id = ?";

            try (PreparedStatement fetchStmt = connection.prepareStatement(fetchHomeworkInstancesSql)) {
                fetchStmt.setString(1, originalHomeworkName);
                fetchStmt.setDate(2, originalDueDate);
                fetchStmt.setDate(3, originalPublishDate);
                fetchStmt.setInt(4, originalTeacherId);

                try (ResultSet homeworkInstancesRs = fetchStmt.executeQuery()) {
                    while (homeworkInstancesRs.next()) {
                        int hwId = homeworkInstancesRs.getInt("homework_id");
                        Integer stId = (Integer) homeworkInstancesRs.getObject("student_id"); // null if teacher copy

                        // 3) Delete from splitted_homework first (depends on homework_id + maybe student_id)
                        if (stId != null) {
                            String deleteSplitHomeworkQuery =
                                    "DELETE FROM splitted_homework WHERE homework_id = ? AND student_id = ?";
                            try (PreparedStatement del = connection.prepareStatement(deleteSplitHomeworkQuery)) {
                                del.setInt(1, hwId);
                                del.setInt(2, stId);
                                del.executeUpdate();
                            }
                        } else {
                            String deleteSplitHomeworkQuery =
                                    "DELETE FROM splitted_homework WHERE homework_id = ? AND student_id IS NULL";
                            try (PreparedStatement del = connection.prepareStatement(deleteSplitHomeworkQuery)) {
                                del.setInt(1, hwId);
                                del.executeUpdate();
                            }
                        }

                        // 4) Delete goal rows (safe even if none exist)
                        String deleteGoalQuery = "DELETE FROM goal WHERE homework_id = ?";
                        try (PreparedStatement del = connection.prepareStatement(deleteGoalQuery)) {
                            del.setInt(1, hwId);
                            del.executeUpdate();
                        }

                        // 5) Delete split requests (safe even if none exist)
                        String deleteSplitRequestQuery = "DELETE FROM split_request WHERE homework_id = ?";
                        try (PreparedStatement del = connection.prepareStatement(deleteSplitRequestQuery)) {
                            del.setInt(1, hwId);
                            del.executeUpdate();
                        }

                        // 6) Finally delete homework itself
                        String deleteHomeworkQuery = "DELETE FROM homework WHERE homework_id = ?";
                        try (PreparedStatement del = connection.prepareStatement(deleteHomeworkQuery)) {
                            del.setInt(1, hwId);
                            del.executeUpdate();
                        }
                    }
                }
            }

            connection.commit();
            return Response.ok("{\"message\":\"Homeworks deleted successfully.\"}").build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\":\"An error occurred: " + escapeForJson(e.getMessage()) + "\"}")
                    .build();
        }
    }

    private String escapeForJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

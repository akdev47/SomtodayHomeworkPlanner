package jaxRS;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import somtoday.db.Db;

import java.sql.*;

@Path("/fetchSplitRequestAnswer")
public class FetchSplitRequestAnswerResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetchSplitRequestAnswer(@QueryParam("id") String requestIdParam,
                                            @QueryParam("answer") String answerParam) {

        if (requestIdParam == null || requestIdParam.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\":\"Missing id parameter\"}")
                    .build();
        }
        if (answerParam == null || answerParam.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\":\"Missing answer parameter\"}")
                    .build();
        }

        int requestId;
        try {
            requestId = Integer.parseInt(requestIdParam);
        } catch (NumberFormatException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\":\"id must be a number\"}")
                    .build();
        }

        // only accept "true" or "false" explicitly
        if (!answerParam.equalsIgnoreCase("true") && !answerParam.equalsIgnoreCase("false")) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\":\"answer must be true or false\"}")
                    .build();
        }
        boolean answer = Boolean.parseBoolean(answerParam);

        try (Connection connection = Db.getConnection()) {
            connection.setAutoCommit(false);

            try {
                // Find which homework + student this request is about
                String selectReqSql = "SELECT homework_id, student_id FROM split_request WHERE split_request_id = ?";
                int homeworkId;
                int studentId;

                try (PreparedStatement stmt = connection.prepareStatement(selectReqSql)) {
                    stmt.setInt(1, requestId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (!rs.next()) {
                            connection.rollback();
                            return Response.status(Response.Status.BAD_REQUEST)
                                    .entity("{\"message\":\"Invalid id\"}")
                                    .build();
                        }
                        homeworkId = rs.getInt("homework_id");
                        studentId = rs.getInt("student_id");
                    }
                }

                // NOTE: Your old code updated split_request.accepted and then deleted the row anyway.
                // Keeping end-result the same (request gets deleted), so we skip that update.

                // Fetch all splits for this (student, homework)
                String fetchSplitsSql =
                        "SELECT splitted_homework_id " +
                                "FROM splitted_homework " +
                                "WHERE student_id = ? AND homework_id = ?";

                try (PreparedStatement fetchSplitsStmt = connection.prepareStatement(fetchSplitsSql)) {
                    fetchSplitsStmt.setInt(1, studentId);
                    fetchSplitsStmt.setInt(2, homeworkId);

                    try (ResultSet splitsRs = fetchSplitsStmt.executeQuery()) {
                        while (splitsRs.next()) {
                            int splitId = splitsRs.getInt("splitted_homework_id");

                            if (answer) {
                                String updateSplitSql =
                                        "UPDATE splitted_homework SET accepted = true WHERE splitted_homework_id = ?";
                                try (PreparedStatement updateSplitStmt = connection.prepareStatement(updateSplitSql)) {
                                    updateSplitStmt.setInt(1, splitId);
                                    updateSplitStmt.executeUpdate();
                                }
                            } else {
                                String deleteSplitSql =
                                        "DELETE FROM splitted_homework WHERE splitted_homework_id = ?";
                                try (PreparedStatement deleteSplitStmt = connection.prepareStatement(deleteSplitSql)) {
                                    deleteSplitStmt.setInt(1, splitId);
                                    deleteSplitStmt.executeUpdate();
                                }
                            }
                        }
                    }
                }

                // Delete the split request (final state matches your original code)
                String deleteReqSql = "DELETE FROM split_request WHERE split_request_id = ?";
                try (PreparedStatement deleteReqStmt = connection.prepareStatement(deleteReqSql)) {
                    deleteReqStmt.setInt(1, requestId);
                    deleteReqStmt.executeUpdate();
                }

                connection.commit();
                return Response.ok("{\"message\":\"Request processed successfully.\"}").build();

            } catch (Exception inner) {
                connection.rollback();
                inner.printStackTrace();
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("{\"message\":\"An error occurred: " + inner.getMessage() + "\"}")
                        .build();
            }

        } catch (Exception outer) {
            outer.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\":\"An error occurred: " + outer.getMessage() + "\"}")
                    .build();
        }
    }
}

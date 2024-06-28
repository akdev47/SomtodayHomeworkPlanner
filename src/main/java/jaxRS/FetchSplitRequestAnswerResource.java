package jaxRS;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.sql.*;

@Path("/fetchSplitRequestAnswer")
public class FetchSplitRequestAnswerResource {
    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=somtoday6";
    private static final String DB_USER = "dab_di23242b_168";
    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetchSplitRequestAnswer(@QueryParam("id") String requestIdParam, @QueryParam("answer") String answerParam) {
        if (requestIdParam == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"message\": \"Missing id parameter\"}").build();
        }
        int requestId = Integer.parseInt(requestIdParam);
        boolean answer = Boolean.parseBoolean(answerParam);

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            Class.forName("org.postgresql.Driver");

            String selectHomeworkQuery = "SELECT homework_id, student_id FROM split_request WHERE split_request_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(selectHomeworkQuery)) {
                stmt.setInt(1, requestId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        return Response.status(Response.Status.BAD_REQUEST).entity("{\"message\": \"Invalid id\"}").build();
                    }

                    int homeworkId = rs.getInt("homework_id");
                    int studentId = rs.getInt("student_id");

                    String updateSplitRequestQuery = "UPDATE split_request SET accepted = ? WHERE split_request_id = ?";
                    try (PreparedStatement updateReqStmt = connection.prepareStatement(updateSplitRequestQuery)) {
                        updateReqStmt.setBoolean(1, answer);
                        updateReqStmt.setInt(2, requestId);
                        updateReqStmt.executeUpdate();
                    }

                    String fetchSplitsSql = "SELECT splitted_homework_id FROM somtoday6.splitted_homework WHERE student_id = ? AND homework_id = ?";
                    try (PreparedStatement fetchSplitsStmt = connection.prepareStatement(fetchSplitsSql)) {
                        fetchSplitsStmt.setInt(1, studentId);
                        fetchSplitsStmt.setInt(2, homeworkId);
                        try (ResultSet splitsRs = fetchSplitsStmt.executeQuery()) {
                            while (splitsRs.next()) {
                                int splitId = splitsRs.getInt("splitted_homework_id");

                                if (answer) {
                                    String updateSplitHomeworkQuery = "UPDATE splitted_homework SET accepted = ? WHERE splitted_homework_id = ?";
                                    try (PreparedStatement updateSplitStmt = connection.prepareStatement(updateSplitHomeworkQuery)) {
                                        updateSplitStmt.setBoolean(1, answer);
                                        updateSplitStmt.setInt(2, splitId);
                                        updateSplitStmt.executeUpdate();
                                    }
                                } else {
                                    String deleteSplitHomeworkQuery = "DELETE FROM splitted_homework WHERE splitted_homework_id = ?";
                                    try (PreparedStatement deleteSplitStmt = connection.prepareStatement(deleteSplitHomeworkQuery)) {
                                        deleteSplitStmt.setInt(1, splitId);
                                        deleteSplitStmt.executeUpdate();
                                    }
                                }
                                String deleteSplitReqQuery = "DELETE FROM split_request WHERE split_request_id = ?";
                                try (PreparedStatement deleteSplitReqStmt = connection.prepareStatement(deleteSplitReqQuery)) {
                                    deleteSplitReqStmt.setInt(1, requestId);
                                    deleteSplitReqStmt.executeUpdate();
                                }
                            }
                        }
                    }

                    return Response.ok("{\"message\": \"Request processed successfully.\"}").build();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"message\": \"Error processing request: " + e.getMessage() + "\"}").build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"message\": \"An error occurred: " + e.getMessage() + "\"}").build();
        }
    }
}

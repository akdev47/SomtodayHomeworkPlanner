package utilities;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.sql.*;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

@WebServlet("/fetchSplitRequestAnswer")
public class FetchSplitRequestAnswerServlet extends HttpServlet {
    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=somtoday6";
    private static final String DB_USER = "dab_di23242b_168";
    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String requestIdParam = request.getParameter("id");
        String answerParam = request.getParameter("answer");
        if (requestIdParam == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing id parameter");
            return;
        }
        int requestId = Integer.parseInt(requestIdParam);
        boolean answer = Boolean.parseBoolean(answerParam);

        try {
            Connection connection = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                java.lang.Class.forName("org.postgresql.Driver");
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

                String selectHomeworkQuery = "SELECT homework_id, student_id FROM split_request WHERE split_request_id = ?";
                stmt = connection.prepareStatement(selectHomeworkQuery);
                stmt.setInt(1, requestId);
                rs = stmt.executeQuery();

                if (!rs.next()) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid id");
                    return;
                }

                int homeworkId = rs.getInt("homework_id");
                int studentId = rs.getInt("student_id");


                String updateSplitRequestQuery = "UPDATE split_request SET accepted = ? WHERE split_request_id = ?";
                PreparedStatement updateReqStmt = connection.prepareStatement(updateSplitRequestQuery);
                updateReqStmt.setBoolean(1, answer);
                updateReqStmt.setInt(2, requestId);
                updateReqStmt.executeUpdate();


                String fetchSplitsSql = "SELECT splitted_homework_id " +
                        "FROM somtoday6.splitted_homework " +
                        "WHERE student_id = ? AND homework_id = ?";
                PreparedStatement fetchSplitsStmt = connection.prepareStatement(fetchSplitsSql);
                fetchSplitsStmt.setInt(1, studentId);
                fetchSplitsStmt.setInt(2, homeworkId);
                ResultSet splitsRs = fetchSplitsStmt.executeQuery();

                while (splitsRs.next()){
                    int splitId = splitsRs.getInt("splitted_homework_id");

                    if (answer){

                        String updateSplitHomeworkQuery = "UPDATE splitted_homework SET accepted = ? WHERE splitted_homework_id = ?";
                        PreparedStatement updateSplitStmt = connection.prepareStatement(updateSplitHomeworkQuery);
                        updateSplitStmt.setBoolean(1, answer);
                        updateSplitStmt.setInt(2, splitId);
                        updateSplitStmt.executeUpdate();
                    } else {

                        String deleteSplitHomeworkQuery = "DELETE FROM splitted_homework WHERE splitted_homework_id = ?";
                        PreparedStatement deleteSplitStmt = connection.prepareStatement(deleteSplitHomeworkQuery);
                        deleteSplitStmt.setInt(1, splitId);
                        deleteSplitStmt.executeUpdate();
                    }

                    String deleteSplitReqQuery = "DELETE FROM split_request WHERE split_request_id = ?";
                    PreparedStatement deleteSplitReqStmt = connection.prepareStatement(deleteSplitReqQuery);
                    deleteSplitReqStmt.setInt(1, requestId);
                    deleteSplitReqStmt.executeUpdate();
                }

                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("{\"message\": \"Request processed successfully.\"}");

            } catch (SQLException e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"message\": \"Error processing request: " + e.getMessage() + "\"}");
            } finally {
                try {
                    if (rs != null) rs.close();
                    if (stmt != null) stmt.close();
                    if (connection != null) connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred: " + e.getMessage());
        }
    }
}

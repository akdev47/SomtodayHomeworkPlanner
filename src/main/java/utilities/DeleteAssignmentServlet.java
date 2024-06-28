//package utilities;
//
//import jakarta.servlet.ServletException;
//import jakarta.servlet.annotation.WebServlet;
//import jakarta.servlet.http.HttpServlet;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.sql.*;
//
//@WebServlet("/deleteHomeworks")
//public class DeleteAssignmentServlet extends HttpServlet {
//    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=somtoday6";
//    private static final String DB_USER = "dab_di23242b_168";
//    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";
//
//    @Override
//    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        response.setContentType("application/json");
//        response.setCharacterEncoding("UTF-8");
//
//        String homeworkIdParam = request.getParameter("homeworkId");
//        if (homeworkIdParam == null) {
//            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing homeworkId parameter");
//            return;
//        }
//        int homeworkId = Integer.parseInt(homeworkIdParam);
//        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
//            // Fetch original homework details
//            String selectHomeworkQuery = "SELECT homework_name, teacher_id, publish_date, due_date FROM homework WHERE homework_id = ?";
//            try (PreparedStatement stmt = connection.prepareStatement(selectHomeworkQuery)) {
//                stmt.setInt(1, homeworkId);
//                try (ResultSet rs = stmt.executeQuery()) {
//                    if (!rs.next()) {
//                        response.sendError(HttpServletResponse.SC_BAD_REQUEST,
//                                           "Invalid homework_id");
//                        return;
//                    }
//
//                    String originalHomeworkName = rs.getString("homework_name");
//                    int originalTeacherId = rs.getInt("teacher_id");
//                    Date originalPublishDate = rs.getDate("publish_date");
//                    Date originalDueDate = rs.getDate("due_date");
//
//                    // Fetch matching homework instances
//                    String fetchHomeworkInstancesSql = "SELECT h.homework_id, h.student_id " + "FROM somtoday6.homework h " + "WHERE h.homework_name = ? AND h.due_date = ? AND h.publish_date = ? AND h.teacher_id = ?";
//                    try (PreparedStatement fetchHomeworkInstancesStmt = connection.prepareStatement(
//                            fetchHomeworkInstancesSql)) {
//                        fetchHomeworkInstancesStmt.setString(1, originalHomeworkName);
//                        fetchHomeworkInstancesStmt.setDate(2, originalDueDate);
//                        fetchHomeworkInstancesStmt.setDate(3, originalPublishDate);
//                        fetchHomeworkInstancesStmt.setInt(4, originalTeacherId);
//
//                        try (ResultSet homeworkInstancesRs = fetchHomeworkInstancesStmt.executeQuery()) {
//                            while (homeworkInstancesRs.next()) {
//                                int hwId = homeworkInstancesRs.getInt("homework_id");
//                                int stId = homeworkInstancesRs.getInt("student_id");
//
//                                // Fetch goal IDs
//                                String fetchGoalQuery = "SELECT g.goal_id FROM goal g, homework h WHERE " + "h.homework_id = g.homework_id AND h.homework_id = ?";
//                                try (PreparedStatement fetchGoalStmt = connection.prepareStatement(
//                                        fetchGoalQuery)) {
//                                    fetchGoalStmt.setInt(1, hwId);
//                                    try (ResultSet goalIds = fetchGoalStmt.executeQuery()) {
//                                        boolean hasGoals = goalIds.next(); // Check if there are any goals
//
//                                        if (stId != 0) {
//                                            String deleteSplitHomeworkQuery = "DELETE FROM splitted_homework WHERE homework_id = ? AND student_id = ?";
//                                            try (PreparedStatement deleteSplitStmt = connection.prepareStatement(
//                                                    deleteSplitHomeworkQuery)) {
//                                                deleteSplitStmt.setInt(1, hwId);
//                                                deleteSplitStmt.setInt(2, stId);
//                                                deleteSplitStmt.executeUpdate();
//                                            }
//                                        } else {
//                                            String deleteSplitHomeworkQuery = "DELETE FROM splitted_homework WHERE homework_id = ? AND student_id IS NULL";
//                                            try (PreparedStatement deleteSplitStmt = connection.prepareStatement(
//                                                    deleteSplitHomeworkQuery)) {
//                                                deleteSplitStmt.setInt(1, hwId);
//                                                deleteSplitStmt.executeUpdate();
//                                            }
//                                        }
//
//                                        if (hasGoals) {
//                                            String deleteGoalQuery = "DELETE FROM goal g WHERE g.homework_id = ?";
//                                            try (PreparedStatement deleteStmt = connection.prepareStatement(
//                                                    deleteGoalQuery)) {
//                                                deleteStmt.setInt(1, hwId);
//                                                deleteStmt.executeUpdate();
//                                            }
//                                            String deleteHomeworkQuery = "DELETE FROM homework h USING goal g " + "WHERE h.homework_id = g.homework_id AND h.homework_id = ?";
//                                            try (PreparedStatement deleteStmt = connection.prepareStatement(
//                                                    deleteHomeworkQuery)) {
//                                                deleteStmt.setInt(1, hwId);
//                                                deleteStmt.executeUpdate();
//                                            }
//                                        } else {
//                                            String deleteSplitRequestQuery = "DELETE FROM split_request s WHERE s.teacher_homework_id = ?";
//                                            try (PreparedStatement deleteStmt = connection.prepareStatement(
//                                                    deleteSplitRequestQuery)) {
//                                                deleteStmt.setInt(1, hwId);
//                                                deleteStmt.executeUpdate();
//                                            }
//                                            String deleteHomeworkQuery = "DELETE FROM homework h WHERE h.homework_id = ?";
//                                            try (PreparedStatement deleteStmt = connection.prepareStatement(
//                                                    deleteHomeworkQuery)) {
//                                                deleteStmt.setInt(1, hwId);
//                                                deleteStmt.executeUpdate();
//                                            }
//
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//
//                // Send success response
//                response.setStatus(HttpServletResponse.SC_OK);
//                response.getWriter().write("{\"message\": \"Homeworks deleted successfully.\"}");
//
//            } catch (SQLException e) {
//                e.printStackTrace();
//                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//                response.getWriter()
//                        .write("{\"message\": \"Error processing request: " + e.getMessage() + "\"}");
//            } catch (Exception e) {
//                e.printStackTrace();
//                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
//                                   "An error occurred: " + e.getMessage());
//            }
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//    }
//}

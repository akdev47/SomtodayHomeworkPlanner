//package utilities;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.sql.*;
//import java.util.TimeZone;
//import java.util.concurrent.TimeUnit;
//import org.json.JSONArray;
//import org.json.JSONObject;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.annotation.WebServlet;
//import jakarta.servlet.http.HttpServlet;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//
//@WebServlet("/splitHomeworkTeacherServlet")
//public class SplitHomeworkTeacherServlet extends HttpServlet {
//    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=somtoday6";
//    private static final String DB_USER = "dab_di23242b_168";
//    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";
//
//    @Override
//    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        response.setContentType("application/json");
//        response.setCharacterEncoding("UTF-8");
//
//        String homeworkIdParam = request.getParameter("homeworkId");
//        if (homeworkIdParam == null) {
//            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing homeworkId parameter");
//            return;
//        }
//        int homeworkId = Integer.parseInt(homeworkIdParam);
//
//        // Parse JSON from request body
//        StringBuilder jb = new StringBuilder();
//        String line;
//        try (BufferedReader reader = request.getReader()) {
//            while ((line = reader.readLine()) != null)
//                jb.append(line);
//        } catch (Exception e) {
//            throw new IOException("Error reading request body: " + e.getMessage());
//        }
//
//        try {
//            JSONObject jsonObject = new JSONObject(jb.toString());
//            int splitCount = jsonObject.getInt("split_count");
//            JSONArray splits = jsonObject.getJSONArray("splits");
//
//            Connection connection = null;
//            PreparedStatement stmt = null;
//            ResultSet rs = null;
//
//            try {
//                java.lang.Class.forName("org.postgresql.Driver");
//                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
//
//                // Retrieve homework information using homework_id
//                String selectHomeworkQuery = "SELECT homework_name, teacher_id, publish_date, due_date FROM homework WHERE homework_id = ?";
//                stmt = connection.prepareStatement(selectHomeworkQuery);
//                stmt.setInt(1, homeworkId);
//                rs = stmt.executeQuery();
//
//                if (!rs.next()) {
//                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid homework_id");
//                    return;
//                }
//
//                String originalHomeworkName = rs.getString("homework_name");
//                int originalTeacherId = rs.getInt("teacher_id");
//                Date originalPublishDate = rs.getDate("publish_date");
//                Date originalDueDate = rs.getDate("due_date");
//
//
//                // Fetch matching homework instances
//                String fetchHomeworkInstancesSql = "SELECT h.homework_id, h.student_id, h.split_count " +
//                        "FROM somtoday6.Homework h " +
//                        "WHERE h.homework_name = ? AND h.due_date = ? AND h.publish_date = ? AND h.teacher_id = ?";
//                PreparedStatement fetchHomeworkInstancesStmt = connection.prepareStatement(fetchHomeworkInstancesSql);
//                fetchHomeworkInstancesStmt.setString(1, originalHomeworkName);
//                fetchHomeworkInstancesStmt.setDate(2, originalDueDate);
//                fetchHomeworkInstancesStmt.setDate(3, originalPublishDate);
//                fetchHomeworkInstancesStmt.setInt(4, originalTeacherId);
//                ResultSet homeworkInstancesRs = fetchHomeworkInstancesStmt.executeQuery();
//
//                while (homeworkInstancesRs.next()){
//                    // Prepare statement for inserting split homework
//                    String insertSplitHomeworkQuery = "INSERT INTO splitted_homework (homework_id, student_id, time_indication, accepted, student_calendar_date, split_name) VALUES (?, ?, ?, ?, ?, ?)";
//                    PreparedStatement insertStmt = connection.prepareStatement(insertSplitHomeworkQuery);
//
//                    // Insert split homework for each split entry in the request
//                    for (int i = 0; i < splits.length(); i++) {
//                        JSONObject split = splits.getJSONObject(i);
//                        Time duration = convertMinutesToTime(split.getInt("duration"));
//                        int studentId = homeworkInstancesRs.getInt("student_id");
//                        Date studentCalendarDate = null;
//                        boolean accepted = true;  // Default value, can be set based on your logic
//                        String splitName = split.getString("name");
//
//                        insertStmt.setInt(1, homeworkInstancesRs.getInt("homework_id"));
//
//                        if (studentId != 0){
//                            insertStmt.setInt(1, homeworkInstancesRs.getInt("homework_id"));
//                            insertStmt.setInt(2, studentId);
//                        } else {
//                            insertStmt.setNull(2, Types.INTEGER);
//                        }
//
//                        insertStmt.setTime(3, duration);
//                        insertStmt.setBoolean(4, accepted);
//                        insertStmt.setDate(5, studentCalendarDate);
//                        insertStmt.setString(6, splitName);
//
//                        insertStmt.addBatch();
//                    }
//
//                    // Update split count for the homework
//                    String updateSplitCountQuery = "UPDATE homework SET split_count = ? WHERE homework_id = ?";
//                    PreparedStatement updateStmt = connection.prepareStatement(updateSplitCountQuery);
//                    updateStmt.setInt(1, splitCount);
//                    updateStmt.setInt(2, homeworkInstancesRs.getInt("homework_id"));
//                    updateStmt.executeUpdate();
//
//                    // Execute batch insert
//                    insertStmt.executeBatch();
//                }
//
//                // Send success response
//                response.setStatus(HttpServletResponse.SC_OK);
//                response.getWriter().write("{\"message\": \"Split homeworks created successfully.\"}");
//
//            } catch (SQLException e) {
//                e.printStackTrace();
//                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//                response.getWriter().write("{\"message\": \"Error processing request: " + e.getMessage() + "\"}");
//            } finally {
//                // Close resources
//                try {
//                    if (rs != null) rs.close();
//                    if (stmt != null) stmt.close();
//                    if (connection != null) connection.close();
//                } catch (SQLException e) {
//                    e.printStackTrace();
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred: " + e.getMessage());
//        }
//    }
//
//    private Time convertMinutesToTime(int minutes) {
//        long millis = TimeUnit.MINUTES.toMillis(minutes);
//        return new Time(millis - TimeZone.getDefault().getRawOffset());
//    }
//}

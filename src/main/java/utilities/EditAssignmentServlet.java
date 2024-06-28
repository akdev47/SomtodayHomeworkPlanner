//package utilities;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.sql.*;
//import java.time.LocalDate;
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
//@WebServlet("/editAssignment")
//public class EditAssignmentServlet extends HttpServlet {
//    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=somtoday6";
//    private static final String DB_USER = "dab_di23242b_168";
//    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";
//
//    @Override
//    protected void doGet(HttpServletRequest request, HttpServletResponse response)
//            throws ServletException, IOException {
//        System.out.println(request);
//        int homeworkId = Integer.parseInt(request.getParameter("homeworkId"));
//
//        try {
//            Class.forName("org.postgresql.Driver");
//            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
//
//            String fetchHomeworkSql = "SELECT * FROM somtoday6.Homework WHERE homework_id = ?";
//            PreparedStatement fetchHomeworkStmt = connection.prepareStatement(fetchHomeworkSql);
//            fetchHomeworkStmt.setInt(1, homeworkId);
//            ResultSet homeworkRs = fetchHomeworkStmt.executeQuery();
//
//            JSONObject homeworkData = new JSONObject();
//            if (homeworkRs.next()) {
//                homeworkData.put("homeworkName", homeworkRs.getString("homework_name"));
//                homeworkData.put("dueDate", homeworkRs.getDate("due_date").toString());
//                homeworkData.put("publishDate", homeworkRs.getDate("publish_date").toString());
//                homeworkData.put("timeIndication", homeworkRs.getTime("time_indication").toString());
//                homeworkData.put("description", homeworkRs.getString("description"));
//                homeworkData.put("lessonId", homeworkRs.getInt("lesson_id"));
//                homeworkData.put("classId", homeworkRs.getInt("class_id"));
//                homeworkData.put("homeworkSplittable", homeworkRs.getBoolean("homework_splitable"));
//            }
//
//            String fetchGoalsSql = "SELECT goal_name FROM somtoday6.goal WHERE homework_id = ?";
//            PreparedStatement fetchGoalsStmt = connection.prepareStatement(fetchGoalsSql);
//            fetchGoalsStmt.setInt(1, homeworkId);
//            ResultSet goalsRs = fetchGoalsStmt.executeQuery();
//
//            JSONArray goals = new JSONArray();
//            while (goalsRs.next()) {
//                JSONObject goal = new JSONObject();
//                goal.put("name", goalsRs.getString("goal_name"));
//                goals.put(goal);
//            }
//            homeworkData.put("goals", goals);
//
//            connection.close();
//
//            response.setContentType("application/json");
//            response.getWriter().write(homeworkData.toString());
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred: " + e.getMessage());
//        }
//    }
//
//    @Override
//    protected void doPost(HttpServletRequest request, HttpServletResponse response)
//            throws ServletException, IOException {
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
//
//            int homeworkId = jsonObject.getInt("homeworkId");
//            String homeworkName = jsonObject.getString("homeworkName");
//            Date dueDate = Date.valueOf(jsonObject.getString("dueDate"));
//            Date publishDate = Date.valueOf(jsonObject.getString("publishDate"));
//            Time timeIndication = convertMinutesToTime(Integer.parseInt(jsonObject.getString("timeIndication")));
//            int splitCount = 0; // Set to 0 as per new requirement
//            String description = jsonObject.getString("description");
//            int lessonId = jsonObject.getInt("lessonId");
//            int classId = jsonObject.getInt("classId");
//            boolean homeworkSplittable = jsonObject.getBoolean("homeworkSplittable");
//
//            JSONArray goals = jsonObject.getJSONArray("goals");
//
//            Class.forName("org.postgresql.Driver");
//            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
//            connection.setAutoCommit(false);
//
//            // Fetch the original homework data to identify the relevant homework instances
//            String fetchOriginalHomeworkSql = "SELECT homework_name, due_date, publish_date, teacher_id FROM somtoday6.Homework WHERE homework_id = ?";
//            PreparedStatement fetchOriginalHomeworkStmt = connection.prepareStatement(fetchOriginalHomeworkSql);
//            fetchOriginalHomeworkStmt.setInt(1, homeworkId);
//            ResultSet originalHomeworkRs = fetchOriginalHomeworkStmt.executeQuery();
//
//            if (originalHomeworkRs.next()) {
//                String originalHomeworkName = originalHomeworkRs.getString("homework_name");
//                Date originalDueDate = originalHomeworkRs.getDate("due_date");
//                Date originalPublishDate = originalHomeworkRs.getDate("publish_date");
//                int originalTeacherId = originalHomeworkRs.getInt("teacher_id");
//
//                // Fetch the homework instances to be updated
//                String fetchHomeworkInstancesSql = "SELECT homework_id FROM somtoday6.Homework WHERE homework_name = ? AND due_date = ? AND publish_date = ? AND teacher_id = ?";
//                PreparedStatement fetchHomeworkInstancesStmt = connection.prepareStatement(fetchHomeworkInstancesSql);
//                fetchHomeworkInstancesStmt.setString(1, originalHomeworkName);
//                fetchHomeworkInstancesStmt.setDate(2, originalDueDate);
//                fetchHomeworkInstancesStmt.setDate(3, originalPublishDate);
//                fetchHomeworkInstancesStmt.setInt(4, originalTeacherId);
//                ResultSet homeworkInstancesRs = fetchHomeworkInstancesStmt.executeQuery();
//
//                while (homeworkInstancesRs.next()) {
//                    int currentHomeworkId = homeworkInstancesRs.getInt("homework_id");
//
//                    // Update the homework instance
//                    String updateHomeworkSql = "UPDATE somtoday6.Homework SET homework_name = ?, due_date = ?, publish_date = ?, time_indication = ?, split_count = ?, description = ?, lesson_id = ?, class_id = ?, homework_splitable = ? WHERE homework_id = ?";
//                    PreparedStatement updateHomeworkStmt = connection.prepareStatement(updateHomeworkSql);
//                    updateHomeworkStmt.setString(1, homeworkName);
//                    updateHomeworkStmt.setDate(2, dueDate);
//                    updateHomeworkStmt.setDate(3, publishDate);
//                    updateHomeworkStmt.setTime(4, timeIndication);
//                    updateHomeworkStmt.setInt(5, splitCount);
//                    updateHomeworkStmt.setString(6, description);
//                    updateHomeworkStmt.setInt(7, lessonId);
//                    updateHomeworkStmt.setInt(8, classId);
//                    updateHomeworkStmt.setBoolean(9, homeworkSplittable);
//                    updateHomeworkStmt.setInt(10, currentHomeworkId);
//                    updateHomeworkStmt.executeUpdate();
//
//                    // Delete old goals
//                    String deleteGoalsSql = "DELETE FROM somtoday6.goal WHERE homework_id = ?";
//                    PreparedStatement deleteGoalsStmt = connection.prepareStatement(deleteGoalsSql);
//                    deleteGoalsStmt.setInt(1, currentHomeworkId);
//                    deleteGoalsStmt.executeUpdate();
//
//                    // Insert new goals
//                    for (int i = 0; i < goals.length(); i++) {
//                        JSONObject goal = goals.getJSONObject(i);
//                        String goalName = goal.getString("name");
//
//                        String insertGoalSql = "INSERT INTO somtoday6.goal (homework_id, goal_name) VALUES (?, ?)";
//                        PreparedStatement insertGoalStmt = connection.prepareStatement(insertGoalSql);
//                        insertGoalStmt.setInt(1, currentHomeworkId);
//                        insertGoalStmt.setString(2, goalName);
//                        insertGoalStmt.executeUpdate();
//                    }
//                }
//
//                connection.commit();
//                connection.close();
//                response.sendRedirect("assignments-teacher.html?timestamp=" + System.currentTimeMillis());
//            } else {
//                throw new SQLException("Original homework not found for homeworkId: " + homeworkId);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred: " + e.getMessage());
//        }
//    }
//    private Time convertMinutesToTime(int minutes) {
//        long millis = TimeUnit.MINUTES.toMillis(minutes);
//        return new Time(millis - TimeZone.getDefault().getRawOffset());
//    }
//
//
//}

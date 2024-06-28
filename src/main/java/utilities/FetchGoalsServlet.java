//package utilities;
//
//import jakarta.servlet.ServletException;
//import jakarta.servlet.annotation.WebServlet;
//import jakarta.servlet.http.HttpServlet;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.util.ArrayList;
//import java.util.List;
//import somtoday.model.Goal;
//
//@WebServlet("/fetchGoals")
//public class FetchGoalsServlet extends HttpServlet {
//    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=somtoday6";
//    private static final String DB_USER = "dab_di23242b_168";
//    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";
//
//    @Override
//    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        response.setContentType("application/json");
//        response.setCharacterEncoding("UTF-8");
//
//        String role = request.getParameter("role");
//        String assignmentId = request.getParameter("id");
//
//        List<Goal> goalList = new ArrayList<>();
//
//        try {
//            java.lang.Class.forName("org.postgresql.Driver");
//            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
//
//            String sql = "SELECT * FROM \"somtoday6\".\"goal\" WHERE homework_id = ?";
//            PreparedStatement preparedStatement = connection.prepareStatement(sql);
//            preparedStatement.setInt(1, Integer.parseInt(assignmentId));
//
//            if (!role.equals("student") && !role.equals("teacher") && !role.equals("admin")) {
//                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid role");
//                return;
//            }
//
//            ResultSet resultSet = preparedStatement.executeQuery();
//
//            while (resultSet.next()) {
//                Goal g = new Goal();
//                g.setGoalID(resultSet.getInt("goal_id"));
//                g.setGoalName(resultSet.getString("goal_name"));
//                goalList.add(g);
//            }
//
//            connection.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred.");
//            return;
//        }
//
//        PrintWriter out = response.getWriter();
//        out.write(toJson(goalList));
//        out.close();
//    }
//
//    private String toJson(List<Goal> goalList) {
//        StringBuilder json = new StringBuilder();
//        json.append("[");
//        for (int i = 0; i < goalList.size(); i++) {
//            Goal g = goalList.get(i);
//            json.append("{");
//            json.append("\"goal_id\":").append(g.getGoalID()).append(",");
//            json.append("\"goal_name\":\"").append(g.getGoalName().replace("\"", "\\\"")).append("\"");
//            json.append("}");
//
//            if (i < goalList.size() - 1) {
//                json.append(",");
//            }
//        }
//        json.append("]");
//        return json.toString();
//    }
//}

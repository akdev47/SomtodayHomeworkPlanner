//package utilities;
//
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.sql.*;
//import org.json.JSONArray;
//import org.json.JSONObject;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.annotation.WebServlet;
//import jakarta.servlet.http.HttpServlet;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//
//@WebServlet("/fetchHomework")
//public class FetchHomeworkForCalendar extends HttpServlet {
//    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=somtoday6";
//    private static final String DB_USER = "dab_di23242b_168";
//    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";
//
//    @Override
//    protected void doGet(HttpServletRequest request, HttpServletResponse response)
//            throws ServletException, IOException {
//
//        int personId = Integer.parseInt(request.getParameter("personId"));
//        String role = request.getParameter("role");
//
//        response.setContentType("application/json");
//        response.setCharacterEncoding("UTF-8");
//
//        try {
//            Class.forName("org.postgresql.Driver");
//            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
//
//            String sql = "";
//            if ("student".equals(role)) {
//                sql = "SELECT homework_id, homework_name, due_date, description, time_indication FROM somtoday6.Homework WHERE student_id = (SELECT student_id FROM somtoday6.Student WHERE person_id = ?)";
//            } else if ("teacher".equals(role)) {
//                sql = "SELECT homework_id, homework_name, due_date, description, time_indication FROM somtoday6.Homework WHERE teacher_id = (SELECT teacher_id FROM somtoday6.Teacher WHERE person_id = ?)";
//            } else {
//                connection.close();
//                PrintWriter out = response.getWriter();
//                out.write(new JSONArray().toString()); // nothing for admins
//                out.close();
//                return;
//            }
//
//            PreparedStatement preparedStatement = connection.prepareStatement(sql);
//            preparedStatement.setInt(1, personId);
//            ResultSet rs = preparedStatement.executeQuery();
//
//            JSONArray homeworkArray = new JSONArray();
//
//            while (rs.next()) {
//                JSONObject homework = new JSONObject();
//                homework.put("homework_id", rs.getInt("homework_id"));
//                homework.put("homework_name", rs.getString("homework_name"));
//                homework.put("due_date", rs.getDate("due_date"));
//                homework.put("description", rs.getString("description"));
//                homework.put("time_indication", rs.getString("time_indication"));
//                homeworkArray.put(homework);
//            }
//
//            connection.close();
//
//            PrintWriter out = response.getWriter();
//            out.write(homeworkArray.toString());
//            out.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred.");
//        }
//    }
//}

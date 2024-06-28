//package utilities;
//
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.annotation.WebServlet;
//import jakarta.servlet.http.HttpServlet;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.json.JSONObject;
//
//@WebServlet("/login")
//public class LoginServlet extends HttpServlet {
//    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=somtoday6";
//    private static final String DB_USER = "dab_di23242b_168";
//    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";
//
//    @Override
//    protected void doPost(HttpServletRequest request, HttpServletResponse response)
//            throws ServletException, IOException {
//        String username = request.getParameter("username");
//        String password = request.getParameter("password");
//
//        response.setContentType("application/json");
//        response.setCharacterEncoding("UTF-8");
//        PrintWriter out = response.getWriter();
//
//        try {
//            Class.forName("org.postgresql.Driver");
//            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
//
//            // check to see if user is in person table
//            String sql = "SELECT person_id, person_name FROM somtoday6.Person WHERE username = ? AND user_password = ?";
//            PreparedStatement preparedStatement = connection.prepareStatement(sql);
//            preparedStatement.setString(1, username);
//            preparedStatement.setString(2, password);
//            ResultSet resultSet = preparedStatement.executeQuery();
//
//            if (resultSet.next()) {
//                int personId = resultSet.getInt("person_id");
//                String personName = resultSet.getString("person_name");
//
//                // check role of logged in user, by default it is student
//                String roleSql;
//                String role = "student";
//
//                // check for admin
//                roleSql = "SELECT COUNT(*) AS count FROM somtoday6.Admin WHERE person_id = ?";
//                preparedStatement = connection.prepareStatement(roleSql);
//                preparedStatement.setInt(1, personId);
//                resultSet = preparedStatement.executeQuery();
//                if (resultSet.next() && resultSet.getInt("count") > 0) {
//                    role = "admin";
//                } else {
//                    // check for student
//                    roleSql = "SELECT COUNT(*) AS count FROM somtoday6.Teacher WHERE person_id = ?";
//                    preparedStatement = connection.prepareStatement(roleSql);
//                    preparedStatement.setInt(1, personId);
//                    resultSet = preparedStatement.executeQuery();
//                    if (resultSet.next() && resultSet.getInt("count") > 0) {
//                        role = "teacher";
//                    }
//                }
//
//                // Create the JSON response
//                JSONObject jsonResponse = new JSONObject();
//                jsonResponse.put("personId", personId);
//                jsonResponse.put("role", role);
//                jsonResponse.put("personName", personName);
//
//                out.print(jsonResponse.toString());
//            } else {
//                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                out.print("{\"error\":\"Invalid credentials\"}");
//            }
//
//            connection.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//            out.print("{\"error\":\"An error occurred\"}");
//        } finally {
//            out.close();
//        }
//    }
//}

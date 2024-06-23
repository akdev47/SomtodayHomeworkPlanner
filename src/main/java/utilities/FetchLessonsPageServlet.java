package utilities;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/fetchLessonsPage")
public class FetchLessonsPageServlet extends HttpServlet {
    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=somtoday6";
    private static final String DB_USER = "dab_di23242b_168";
    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        String role = request.getParameter("role");
        String personId = request.getParameter("personId");

        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            Statement statement = connection.createStatement();

            String sql;
            if ("teacher".equals(role)) {
                sql = "SELECT l.lesson_id, l.lesson_name, l.lesson_description, c.class_name, p.person_name AS teacher_name " +
                        "FROM somtoday6.Lesson l " +
                        "JOIN somtoday6.Class c ON l.class_id = c.class_id " +
                        "JOIN somtoday6.Teacher t ON l.teacher_id = t.teacher_id " +
                        "JOIN somtoday6.Person p ON t.person_id = p.person_id " +
                        "WHERE t.person_id = " + personId;
            } else if ("student".equals(role)) {
                sql = "SELECT l.lesson_id, l.lesson_name, l.lesson_description, c.class_name, p.person_name AS teacher_name " +
                        "FROM somtoday6.Lesson l " +
                        "JOIN somtoday6.Class c ON l.class_id = c.class_id " +
                        "JOIN somtoday6.Student s ON c.class_id = s.class_id " +
                        "JOIN somtoday6.Teacher t ON l.teacher_id = t.teacher_id " +
                        "JOIN somtoday6.Person p ON t.person_id = p.person_id " +
                        "WHERE s.person_id = " + personId;
            } else if ("admin".equals(role)) {
                sql = "SELECT l.lesson_id, l.lesson_name, l.lesson_description, c.class_name, p.person_name AS teacher_name " +
                        "FROM somtoday6.Lesson l " +
                        "JOIN somtoday6.Class c ON l.class_id = c.class_id " +
                        "JOIN somtoday6.Teacher t ON l.teacher_id = t.teacher_id " +
                        "JOIN somtoday6.Person p ON t.person_id = p.person_id";
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid role");
                return;
            }

            ResultSet resultSet = statement.executeQuery(sql);

            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("[");

            boolean first = true;
            while (resultSet.next()) {
                if (!first) {
                    jsonBuilder.append(",");
                } else {
                    first = false;
                }
                jsonBuilder.append("{")
                        .append("\"lesson_id\":").append(resultSet.getInt("lesson_id")).append(",")
                        .append("\"lesson_name\":\"").append(resultSet.getString("lesson_name")).append("\",")
                        .append("\"lesson_description\":\"").append(resultSet.getString("lesson_description")).append("\",")
                        .append("\"class_name\":\"").append(resultSet.getString("class_name")).append("\",")
                        .append("\"teacher_name\":\"").append(resultSet.getString("teacher_name")).append("\"")
                        .append("}");
            }

            jsonBuilder.append("]");
            connection.close();

            out.print(jsonBuilder.toString());
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

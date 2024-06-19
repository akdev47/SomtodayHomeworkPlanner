package utilities;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import somtoday.model.Class;

@WebServlet("/fetchClasses")
public class FetchClassesServlet extends HttpServlet {
    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=somtoday6";
    private static final String DB_USER = "dab_di23242b_168";
    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("Getting classes in fetch servlet...");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String role = request.getParameter("role");
        String personId = request.getParameter("personId");

        List<Class> classList = new ArrayList<>();

        try {
            java.lang.Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            String sql;
            if ("teacher".equals(role)) {
                sql = "SELECT c.class_id, c.class_name, c.class_capacity " +
                        "FROM somtoday6.Class c " +
                        "JOIN somtoday6.Lesson l ON c.class_id = l.class_id " +
                        "JOIN somtoday6.Teacher t ON l.teacher_id = t.teacher_id " +
                        "WHERE t.person_id = " + personId;
            } else if ("student".equals(role)) {
                sql = "SELECT c.class_id, c.class_name, c.class_capacity " +
                        "FROM somtoday6.Class c " +
                        "JOIN somtoday6.Student s ON c.class_id = s.class_id " +
                        "WHERE s.person_id = " + personId;
            } else if ("admin".equals(role)) {
                sql = "SELECT c.class_id, c.class_name, c.class_capacity FROM somtoday6.Class c";
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid role");
                return;
            }

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                Class cls = new Class();
                cls.setClassID(resultSet.getInt("class_id"));
                cls.setClassName(resultSet.getString("class_name"));
                cls.setClassCapacity(resultSet.getInt("class_capacity"));
                classList.add(cls);
            }

            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred.");
            return;
        }

        PrintWriter out = response.getWriter();
        out.write(toJson(classList));
        out.close();
    }

    private String toJson(List<Class> classList) {
        StringBuilder json = new StringBuilder();
        json.append("[");

        for (int i = 0; i < classList.size(); i++) {
            Class cls = classList.get(i);
            json.append("{");
            json.append("\"class_id\":").append(cls.getClassID()).append(",");
            json.append("\"class_name\":\"").append(cls.getClassName()).append("\",");
            json.append("\"class_capacity\":").append(cls.getClassCapacity());
            json.append("}");

            if (i < classList.size() - 1) {
                json.append(",");
            }
        }

        json.append("]");
        return json.toString();
    }
}

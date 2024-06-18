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

@WebServlet("/fetchStudents")
public class FetchStudentsServlet extends HttpServlet {
    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=topicus6";
    private static final String DB_USER = "dab_di23242b_168";
    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(
                    "SELECT s.student_id, p.person_name " +
                            "FROM topicus6.Student s " +
                            "JOIN topicus6.Person p ON s.person_id = p.person_id " +
                            "WHERE s.class_id IS NULL"
            );

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
                        .append("\"student_id\":").append(resultSet.getInt("student_id")).append(",")
                        .append("\"person_name\":\"").append(resultSet.getString("person_name")).append("\"")
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

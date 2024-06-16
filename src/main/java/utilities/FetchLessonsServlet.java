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

@WebServlet("/fetchLessons")
public class FetchLessonsServlet extends HttpServlet {
    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=topicus6";
    private static final String DB_USER = "dab_di23242b_168";
    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT lesson_id, course_name FROM topicus6.Lesson JOIN topicus6.Course ON topicus6.Lesson.course_id = topicus6.Course.course_id");

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
                        .append("\"id\":").append(resultSet.getInt("lesson_id")).append(",")
                        .append("\"name\":\"").append(resultSet.getString("course_name")).append("\"")
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

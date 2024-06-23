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
import somtoday.model.Lesson;

@WebServlet("/fetchLessonsE")
public class FetchLessonsForEdit extends HttpServlet{
    /**
     * This class is going to be deleted and functions will be into original fetch lessons.
     * This is short-term solution in order to not break any functionality with changing the code.
      */
    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=somtoday6";
    private static final String DB_USER = "dab_di23242b_168";
    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("Getting lessons in fetch servlet...");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String role = request.getParameter("role");
        String personId = request.getParameter("personId");

        List<Lesson> lessonList = new ArrayList<>();

        try {
            java.lang.Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            String sql;
            if ("teacher".equals(role)) {
                sql = "SELECT l.lesson_id, l.lesson_name " +
                        "FROM lesson l, teacher t " +
                        "WHERE l.teacher_id = t.teacher_id AND " +
                        "t.person_id = " + personId;
            }  else if ("admin".equals(role)) {
                sql = "SELECT l.lesson_id, l.lesson_name " +
                        "FROM lesson l, teacher t ";
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid role");
                return;
            }

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                Lesson ls = new Lesson();
                ls.setLessonID(resultSet.getInt("lesson_id"));
                ls.setLessonName(resultSet.getString("lesson_name"));
                lessonList.add(ls);
            }

            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred.");
            return;
        }

        PrintWriter out = response.getWriter();
        out.write(toJson(lessonList));
        out.close();
    }

    private String toJson(List<Lesson> lessonList) {
        StringBuilder json = new StringBuilder();
        json.append("[");

        for (int i = 0; i < lessonList.size(); i++) {
            Lesson ls = lessonList.get(i);
            json.append("{");
            json.append("\"lesson_id\":").append(ls.getLessonID()).append(",");
            json.append("\"lesson_name\":\"").append(ls.getLessonName()).append("\"");
            json.append("}");

            if (i < lessonList.size() - 1) {
                json.append(",");
            }
        }

        json.append("]");
        return json.toString();
    }
}

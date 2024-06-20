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
import somtoday.model.Homework;

@WebServlet("/fetchAssignments")
public class FetchAssignmentsServlet extends HttpServlet {
    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=somtoday6";
    private static final String DB_USER = "dab_di23242b_168";
    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String role = request.getParameter("role");
        String personId = request.getParameter("personId");

        List<Class> classList = new ArrayList<>();
        List<Homework> assignmentList = new ArrayList<>();

        try {
            java.lang.Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            String sql;
            if ("teacher".equals(role)) {
                sql = "WITH HomeworkCTE AS (\n" +
                        "    SELECT \n" +
                        "        h.class_id, \n" +
                        "        h.homework_name, \n" +
                        "        h.publish_date, \n" +
                        "        h.due_date, \n" +
                        "        h.teacher_id, \n" +
                        "        h.homework_id,\n" +
                        "        ROW_NUMBER() OVER (PARTITION BY h.class_id, h.homework_name, h.publish_date, h.due_date, h.teacher_id \n" +
                        "                           ORDER BY h.homework_id) AS rn\n" +
                        "    FROM homework h\n" + ")\n" + "SELECT \n" + "    c.class_name, \n" + "    HomeworkCTE.homework_name, \n" + "    HomeworkCTE.publish_date, \n" + "    HomeworkCTE.due_date, \n" + "    HomeworkCTE.homework_id\n" + "FROM HomeworkCTE\n" + "JOIN class c ON HomeworkCTE.class_id = c.class_id\n" + "JOIN teacher t ON HomeworkCTE.teacher_id = t.teacher_id\n" + "JOIN person p ON t.person_id = p.person_id\n" + "WHERE HomeworkCTE.rn = 1\n" + "AND t.person_id = " + personId + " ORDER BY HomeworkCTE.publish_date DESC;";
            } else if ("student".equals(role)) {
                sql = "SELECT DISTINCT c.class_name, h.homework_id, h.homework_name, h.publish_date, h.due_date "  +
                        "FROM homework h, student s, person p, class c " +
                        "WHERE h.student_id = s.student_id " +
                        "AND h.class_id = c.class_id " +
                        "AND s.person_id = " + personId +
                        "ORDER BY h.publish_date DESC";

            }  else if ("admin".equals(role)) {
                sql = "SELECT DISTINCT c.class_name, h.homework_id, h.homework_name, h.publish_date, h.due_date " +
                        "FROM somtoday6.homework h, class c " +
                        "WHERE h.class_id = c.class_id" +
                        "ORDER BY h.publish_date DESC";
            }  else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid role");
                return;
            }

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                Class c = new Class();
                c.setClassName(resultSet.getString("class_name"));
                Homework hw = new Homework();
                hw.setHomeworkID(resultSet.getInt("homework_id"));
                hw.setHomeworkName(resultSet.getString("homework_name"));
                hw.setPublishDate(resultSet.getDate("publish_date"));
                hw.setDueDate(resultSet.getDate("due_date"));
                classList.add(c);
                assignmentList.add(hw);
            }

            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred.");
            return;
        }

        PrintWriter out = response.getWriter();
        out.write(toJson(classList,assignmentList));
        out.close();
    }

    private String toJson(List<Class> classList,List<Homework> assignmentList) {
        StringBuilder json = new StringBuilder();
        json.append("[");
        if (classList.size() == assignmentList.size()){
        for (int i = 0; i < assignmentList.size(); i++) {
            Homework hw = assignmentList.get(i);
            Class c = classList.get(i);
            json.append("{");
            json.append("\"class_name\":\"").append(c.getClassName()).append("\",");
            json.append("\"homework_id\":").append(hw.getHomeworkID()).append(",");
            json.append("\"homework_name\":\"").append(hw.getHomeworkName()).append("\",");
            json.append("\"publish_date\":\"").append(hw.getPublishDate()).append("\",");
            json.append("\"due_date\":\"").append(hw.getDueDate()).append("\"");
            json.append("}");

            if (i < assignmentList.size() - 1) {
                json.append(",");
            }
        }

        json.append("]");
        return json.toString();
        } else {
            return "ERROR IN THE SERVER";
        }
    }
}

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
    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=topicus6";
    private static final String DB_USER = "dab_di23242b_168";
    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String role = request.getParameter("role");
        String personId = request.getParameter("personId");

        List<Homework> assignmentList = new ArrayList<>();

        try {
            java.lang.Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            String sql;
            if ("teacher".equals(role)) {
                sql = "SELECT DISTINCT h.homework_type, h.publish_date, h.due_date " +
                        "FROM homework h, teacher t, lesson l, course c, person p " +
                        "WHERE h.lesson_id = l.lesson_id AND " +
                        "l.course_id = c.course_id AND " +
                        "c.teacher_id = t.teacher_id AND " +
                        "t.person_id = " + personId;
            } else if ("student".equals(role)) {
                sql = "SELECT DISTINCT h.homework_type, h.publish_date, h.due_date " +
                        "FROM homework h, student s, lesson l, class c, person p " +
                        "WHERE h.lesson_id = l.lesson_id AND " +
                        "l.class_id = c.class_id AND " +
                        "c.class_id = s.class_id AND " +
                        "s.person_id = " + personId;
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid role");
                return;
            }

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                Homework hw = new Homework();
                hw.setHomeworkType(resultSet.getString("homework_type"));
                hw.setPublishDate(resultSet.getDate("publish_date"));
                hw.setDueDate(resultSet.getDate("due_date"));
                assignmentList.add(hw);
            }

            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred.");
            return;
        }

        PrintWriter out = response.getWriter();
        out.write(toJson(assignmentList));
        out.close();
    }

    private String toJson(List<Homework> assignmentList) {
        StringBuilder json = new StringBuilder();
        json.append("[");

        for (int i = 0; i < assignmentList.size(); i++) {
            Homework hw = assignmentList.get(i);
            json.append("{");
            json.append("\"homework_type\":\"").append(hw.getHomeworkType()).append("\",");
            json.append("\"publish_date\":\"").append(hw.getPublishDate()).append("\",");
            json.append("\"due_date\":\"").append(hw.getDueDate()).append("\"");
            json.append("}");

            if (i < assignmentList.size() - 1) {
                json.append(",");
            }
        }

        json.append("]");
        return json.toString();
    }
}

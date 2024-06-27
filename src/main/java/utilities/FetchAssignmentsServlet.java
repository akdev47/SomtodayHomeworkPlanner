package utilities;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import somtoday.model.Class;
import somtoday.model.Homework;
import somtoday.model.SplitHomework;

@WebServlet("/fetchAssignments")
public class FetchAssignmentsServlet extends HttpServlet {
    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=somtoday6";
    private static final String DB_USER = "dab_di23242b_168";
    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String role = request.getParameter("role");
        String personId = request.getParameter("personId");

        Map<Integer, Class> classMap = new HashMap<>();
        Map<Integer, Homework> assignmentMap = new HashMap<>();
        List<SplitHomework> splitList = new ArrayList<>();

        try {
            java.lang.Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            String sql;
            if ("teacher".equals(role)) {
                sql = "SELECT DISTINCT c.class_name, h.homework_id, h.homework_name, h.publish_date, h.due_date " + "FROM homework h " + "JOIN class c ON h.class_id = c.class_id " + "JOIN teacher t ON h.teacher_id = t.teacher_id " + "WHERE h.student_id IS NULL " + "AND t.person_id = " + personId + " ORDER BY h.publish_date DESC;";
            } else if ("student".equals(role)) {
                sql = "SELECT DISTINCT c.class_name, h.homework_id, h.homework_name, h.publish_date, h.due_date, sh.splitted_homework_id, sh.split_name  " +
                        "FROM homework h " +
                        "LEFT JOIN splitted_homework sh ON h.homework_id = sh.homework_id AND sh.accepted = true " +
                        "JOIN class c ON h.class_id = c.class_id " +
                        "JOIN student s ON h.student_id = s.student_id " +
                        "WHERE s.person_id = " + personId +
                        " ORDER BY h.publish_date DESC ";
            } else if ("admin".equals(role)) {
                sql = "SELECT DISTINCT c.class_name, h.homework_id, h.homework_name, h.publish_date, h.due_date " + "FROM homework h " + "JOIN class c ON h.class_id = c.class_id " + "WHERE h.student_id IS NULL " + "ORDER BY h.publish_date DESC;";
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid role");
                return;
            }

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                int homeworkId = resultSet.getInt("homework_id");

                if (!assignmentMap.containsKey(homeworkId)) {
                    Class c = new Class();
                    c.setClassName(resultSet.getString("class_name"));
                    Homework hw = new Homework();
                    hw.setHomeworkID(homeworkId);
                    hw.setHomeworkName(resultSet.getString("homework_name"));
                    hw.setPublishDate(resultSet.getDate("publish_date"));
                    hw.setDueDate(resultSet.getDate("due_date"));

                    classMap.put(homeworkId, c);
                    assignmentMap.put(homeworkId, hw);
                }

                if ("student".equals(role) && resultSet.getInt("splitted_homework_id") != 0) {
                    SplitHomework sh = new SplitHomework();
                    sh.setSplittedHomeworkId(resultSet.getInt("splitted_homework_id"));
                    sh.setSplitName(resultSet.getString("split_name"));
                    sh.setHomeworkId(homeworkId);
                    splitList.add(sh);
                }
            }

            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred.");
            return;
        }

        PrintWriter out = response.getWriter();
        out.write(toJson(new ArrayList<>(classMap.values()), new ArrayList<>(assignmentMap.values()), splitList));
        out.close();
    }

    private String toJson(List<Class> classList, List<Homework> assignmentList, List<SplitHomework> splitList) {
        StringBuilder json = new StringBuilder();
        json.append("{ \"homeworks\": [");
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
        json.append("], \"splits\": [");
        for (int i = 0; i < splitList.size(); i++) {
            SplitHomework sh = splitList.get(i);
            json.append("{");
            json.append("\"split_id\":").append(sh.getSplittedHomeworkId()).append(",");
            json.append("\"homework_id\":").append(sh.getHomeworkId()).append(",");
            json.append("\"split_name\":\"").append(sh.getSplitName()).append("\"");
            json.append("}");

            if (i < splitList.size() - 1) {
                json.append(",");
            }
        }
        json.append("] }");
        return json.toString();
    }
}

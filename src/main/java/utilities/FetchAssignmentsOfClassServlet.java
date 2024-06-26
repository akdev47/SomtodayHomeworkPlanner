package utilities;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet("/fetchAssignmentsOfClass")
public class FetchAssignmentsOfClassServlet extends HttpServlet {
    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=somtoday6";
    private static final String DB_USER = "dab_di23242b_168";
    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String homeworkIdParam = request.getParameter("homeworkId");
        if (homeworkIdParam == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing homeworkId parameter");
            return;
        }
        int homeworkId = Integer.parseInt(homeworkIdParam);

        List<JSONObject> assignmentList = new ArrayList<>();

        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            // Fetch the original homework details to identify matching instances
            String fetchOriginalHomeworkSql = "SELECT homework_name, due_date, publish_date, teacher_id FROM somtoday6.Homework WHERE homework_id = ?";
            PreparedStatement fetchOriginalHomeworkStmt = connection.prepareStatement(fetchOriginalHomeworkSql);
            fetchOriginalHomeworkStmt.setInt(1, homeworkId);
            ResultSet originalHomeworkRs = fetchOriginalHomeworkStmt.executeQuery();

            if (originalHomeworkRs.next()) {
                String originalHomeworkName = originalHomeworkRs.getString("homework_name");
                Date originalDueDate = originalHomeworkRs.getDate("due_date");
                Date originalPublishDate = originalHomeworkRs.getDate("publish_date");
                int originalTeacherId = originalHomeworkRs.getInt("teacher_id");

                // Fetch matching homework instances
                String fetchHomeworkInstancesSql = "SELECT h.homework_id, h.homework_name, h.publish_date, h.due_date, s.student_id, p.person_name as student_name " +
                        "FROM somtoday6.Homework h " +
                        "LEFT JOIN somtoday6.student s ON h.student_id = s.student_id " +
                        "LEFT JOIN somtoday6.person p ON s.person_id = p.person_id " +
                        "WHERE h.homework_name = ? AND h.due_date = ? AND h.publish_date = ? AND h.teacher_id = ?";
                PreparedStatement fetchHomeworkInstancesStmt = connection.prepareStatement(fetchHomeworkInstancesSql);
                fetchHomeworkInstancesStmt.setString(1, originalHomeworkName);
                fetchHomeworkInstancesStmt.setDate(2, originalDueDate);
                fetchHomeworkInstancesStmt.setDate(3, originalPublishDate);
                fetchHomeworkInstancesStmt.setInt(4, originalTeacherId);
                ResultSet homeworkInstancesRs = fetchHomeworkInstancesStmt.executeQuery();

                boolean teacherAssignmentAdded = false;

                while (homeworkInstancesRs.next()) {
                    JSONObject assignment = new JSONObject();
                    assignment.put("homework_id", homeworkInstancesRs.getInt("homework_id"));
                    assignment.put("homework_name", homeworkInstancesRs.getString("homework_name"));
                    assignment.put("publish_date", homeworkInstancesRs.getDate("publish_date").toString());
                    assignment.put("due_date", homeworkInstancesRs.getDate("due_date").toString());

                    if (homeworkInstancesRs.getObject("student_id") == null) {
                        assignment.put("student_name", "yours");
                        // Add the teacher's assignment at the top
                        assignmentList.add(0, assignment);
                        teacherAssignmentAdded = true;
                    } else {
                        assignment.put("student_name", homeworkInstancesRs.getString("student_name"));
                        assignmentList.add(assignment);
                    }
                }

                // If no teacher assignment was found, create a placeholder at the top
                if (!teacherAssignmentAdded) {
                    JSONObject placeholderAssignment = new JSONObject();
                    placeholderAssignment.put("homework_id", homeworkId);
                    placeholderAssignment.put("homework_name", originalHomeworkName);
                    placeholderAssignment.put("publish_date", originalPublishDate.toString());
                    placeholderAssignment.put("due_date", originalDueDate.toString());
                    placeholderAssignment.put("student_name", "yours");
                    assignmentList.add(0, placeholderAssignment);
                }
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Homework not found for homeworkId: " + homeworkId);
                return;
            }

            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred.");
            return;
        }

        JSONArray jsonArray = new JSONArray(assignmentList);
        PrintWriter out = response.getWriter();
        out.write(jsonArray.toString());
        out.close();
    }
}

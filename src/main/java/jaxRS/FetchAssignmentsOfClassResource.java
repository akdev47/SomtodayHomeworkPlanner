package jaxRS;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Path("/fetchAssignmentsOfClass")
public class FetchAssignmentsOfClassResource {

    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=somtoday6";
    private static final String DB_USER = "dab_di23242b_168";
    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetchAssignmentsOfClass(@QueryParam("homeworkId") int homeworkId) {

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
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Homework not found for homeworkId: " + homeworkId)
                        .build();
            }

            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred.")
                    .build();
        }

        JSONArray jsonArray = new JSONArray(assignmentList);
        return Response.ok(jsonArray.toString()).build();
    }
}

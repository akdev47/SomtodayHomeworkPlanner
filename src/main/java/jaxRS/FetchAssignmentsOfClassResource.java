package jaxRS;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.sql.Date;
import org.json.JSONArray;
import org.json.JSONObject;
import somtoday.db.Db;

import java.sql.*;
import java.util.*;

@Path("/fetchAssignmentsOfClass")
public class FetchAssignmentsOfClassResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetchAssignmentsOfClass(@QueryParam("homeworkId") int homeworkId) {

        if (homeworkId <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Invalid homeworkId\"}")
                    .build();
        }

        List<JSONObject> assignmentList = new ArrayList<>();

        try (Connection connection = Db.getConnection()) {

            // Fetch original homework details to identify matching instances
            String fetchOriginalHomeworkSql =
                    "SELECT homework_name, due_date, publish_date, teacher_id " +
                            "FROM homework WHERE homework_id = ?";

            String originalHomeworkName;
            Date originalDueDate;
            Date originalPublishDate;
            int originalTeacherId;

            try (PreparedStatement stmt = connection.prepareStatement(fetchOriginalHomeworkSql)) {
                stmt.setInt(1, homeworkId);

                try (ResultSet originalHomeworkRs = stmt.executeQuery()) {
                    if (!originalHomeworkRs.next()) {
                        return Response.status(Response.Status.NOT_FOUND)
                                .entity("Homework not found for homeworkId: " + homeworkId)
                                .build();
                    }

                    originalHomeworkName = originalHomeworkRs.getString("homework_name");
                    originalDueDate = originalHomeworkRs.getDate("due_date");
                    originalPublishDate = originalHomeworkRs.getDate("publish_date");
                    originalTeacherId = originalHomeworkRs.getInt("teacher_id");
                }
            }

            String fetchHomeworkInstancesSql =
                    "SELECT " +
                            "  c.class_name, " +
                            "  h.homework_id, h.homework_name, h.publish_date, h.due_date, h.student_id, " +
                            "  sh.splitted_homework_id, sh.split_name, " +
                            "  s.student_id AS st_student_id, " +
                            "  p.person_name AS student_name " +
                            "FROM homework h " +
                            "LEFT JOIN (SELECT * FROM splitted_homework WHERE accepted = true) sh " +
                            "  ON h.homework_id = sh.homework_id " +
                            "LEFT JOIN class c ON h.class_id = c.class_id " +
                            "LEFT JOIN student s ON h.student_id = s.student_id " +
                            "LEFT JOIN person p ON s.person_id = p.person_id " +
                            "WHERE h.homework_name = ? AND h.due_date = ? AND h.publish_date = ? AND h.teacher_id = ? " +
                            "ORDER BY h.publish_date DESC";

            try (PreparedStatement stmt = connection.prepareStatement(fetchHomeworkInstancesSql)) {
                stmt.setString(1, originalHomeworkName);
                stmt.setDate(2, originalDueDate);
                stmt.setDate(3, originalPublishDate);
                stmt.setInt(4, originalTeacherId);

                try (ResultSet rs = stmt.executeQuery()) {
                    Set<Integer> processedHomeworkIds = new HashSet<>();

                    while (rs.next()) {
                        int hwId = rs.getInt("homework_id");
                        Integer studentId = (Integer) rs.getObject("student_id");
                        boolean isTeacher = (studentId == null);

                        if (!processedHomeworkIds.contains(hwId)) {
                            JSONObject assignment = new JSONObject();
                            assignment.put("homework_id", hwId);
                            assignment.put("homework_name", rs.getString("homework_name"));
                            assignment.put("publish_date", rs.getDate("publish_date").toString());
                            assignment.put("due_date", rs.getDate("due_date").toString());
                            assignment.put("is_teacher", isTeacher);

                            if (isTeacher) {
                                assignment.put("student_name", "yours");
                            } else {
                                assignment.put("student_name", rs.getString("student_name"));
                            }

                            assignmentList.add(assignment);
                            processedHomeworkIds.add(hwId);
                        }

                        Integer splitId = (Integer) rs.getObject("splitted_homework_id");
                        if (splitId != null) {
                            JSONObject splitAssignment = new JSONObject();
                            splitAssignment.put("homework_id", hwId);
                            splitAssignment.put("split_id", splitId);
                            splitAssignment.put(
                                    "homework_name",
                                    rs.getString("homework_name") + " - " + rs.getString("split_name")
                            );
                            splitAssignment.put("publish_date", rs.getDate("publish_date").toString());
                            splitAssignment.put("due_date", rs.getDate("due_date").toString());
                            splitAssignment.put("student_name", rs.getString("student_name"));
                            splitAssignment.put("is_teacher", isTeacher);
                            assignmentList.add(splitAssignment);
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred.")
                    .build();
        }

        return Response.ok(new JSONArray(assignmentList).toString()).build();
    }
}

package jaxRS;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import somtoday.db.Db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Path("/fetchHomeworkForLesson")
public class FetchHomeworkForLessonResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetchHomeworkForLesson(
            @QueryParam("lessonId") int lessonId,
            @QueryParam("personId") int personId,
            @QueryParam("role") String role
    ) {
        JSONArray homeworkArray = new JSONArray();

        if (lessonId <= 0 || personId <= 0 || role == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Missing or invalid parameters\"}")
                    .build();
        }

        String sql;
        if ("student".equals(role)) {
            sql =
                    "SELECT h.homework_id, h.homework_name, h.due_date, h.description, h.time_indication, c.class_name " +
                            "FROM homework h " +
                            "JOIN class c ON h.class_id = c.class_id " +
                            "WHERE h.lesson_id = ? " +
                            "  AND h.student_id = (SELECT student_id FROM student WHERE person_id = ?)";

        } else if ("teacher".equals(role)) {
            sql =
                    "SELECT DISTINCT ON (h.homework_name, h.due_date) " +
                            "  h.homework_id, h.homework_name, h.due_date, h.description, h.time_indication, c.class_name " +
                            "FROM homework h " +
                            "JOIN class c ON h.class_id = c.class_id " +
                            "WHERE h.lesson_id = ? " +
                            "  AND h.teacher_id = (SELECT teacher_id FROM teacher WHERE person_id = ?)";

        } else {
            // admin sees no homeworks (as in your original)
            return Response.ok(homeworkArray.toString()).build();
        }

        try (Connection connection = Db.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, lessonId);
            preparedStatement.setInt(2, personId);

            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    JSONObject homework = new JSONObject();
                    homework.put("homework_id", rs.getInt("homework_id"));
                    homework.put("homework_name", rs.getString("homework_name"));
                    homework.put("due_date", rs.getDate("due_date").toString());
                    homework.put("description", rs.getString("description"));

                    // keep as string in JSON like your original endpoint
                    homework.put("time_indication",
                                 rs.getTime("time_indication") == null ? JSONObject.NULL : rs.getTime("time_indication").toString());

                    homework.put("class_name", rs.getString("class_name"));
                    homeworkArray.put(homework);
                }
            }

            return Response.ok(homeworkArray.toString()).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"An error occurred\"}")
                    .build();
        }
    }
}

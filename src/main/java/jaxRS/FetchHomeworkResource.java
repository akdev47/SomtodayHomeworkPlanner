package jaxRS;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import somtoday.db.Db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Path("/fetchHomework")
public class FetchHomeworkResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetchHomework(@QueryParam("personId") int personId, @QueryParam("role") String role) {

        if (personId <= 0 || role == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Missing or invalid parameters\"}")
                    .build();
        }

        String sql;
        if ("student".equals(role)) {
            sql =
                    "SELECT homework_id, homework_name, due_date, description, time_indication " +
                            "FROM homework " +
                            "WHERE student_id = (SELECT student_id FROM student WHERE person_id = ?)";
        } else if ("teacher".equals(role)) {
            sql =
                    "SELECT homework_id, homework_name, due_date, description, time_indication " +
                            "FROM homework " +
                            "WHERE teacher_id = (SELECT teacher_id FROM teacher WHERE person_id = ?)";
        } else {
            // nothing for admins (as in your original)
            return Response.ok(new JSONArray().toString()).build();
        }

        JSONArray homeworkArray = new JSONArray();

        try (Connection connection = Db.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, personId);

            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    JSONObject homework = new JSONObject();
                    homework.put("homework_id", rs.getInt("homework_id"));
                    homework.put("homework_name", rs.getString("homework_name"));
                    homework.put("due_date", rs.getDate("due_date") == null ? JSONObject.NULL : rs.getDate("due_date").toString());
                    homework.put("description", rs.getString("description"));
                    homework.put("time_indication",
                                 rs.getTime("time_indication") == null ? JSONObject.NULL : rs.getTime("time_indication").toString());
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

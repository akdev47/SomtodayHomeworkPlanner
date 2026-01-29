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

@Path("/fetchLessons")
public class FetchLessonsResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetchLessons(@QueryParam("classId") int classId) {
        if (classId <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Invalid classId\"}")
                    .build();
        }

        JSONArray lessonsArray = new JSONArray();

        String sql =
                "SELECT lesson_id, lesson_name " +
                        "FROM lesson " +
                        "WHERE class_id = ?";

        try (Connection connection = Db.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, classId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    JSONObject lesson = new JSONObject();
                    lesson.put("lesson_id", rs.getInt("lesson_id"));
                    lesson.put("lesson_name", rs.getString("lesson_name"));
                    lessonsArray.put(lesson);
                }
            }

            return Response.ok(lessonsArray.toString()).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"An error occurred.\"}")
                    .build();
        }
    }
}

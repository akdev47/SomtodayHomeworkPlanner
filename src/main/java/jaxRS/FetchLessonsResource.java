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
import java.sql.ResultSet;
import java.sql.Statement;

@Path("/fetchLessons")
public class FetchLessonsResource {

    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=somtoday6";
    private static final String DB_USER = "dab_di23242b_168";
    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetchLessons(@QueryParam("classId") int classId) {
        JSONArray lessonsArray = new JSONArray();

        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(
                    "SELECT l.lesson_id, l.lesson_name " +
                            "FROM somtoday6.Lesson l " +
                            "WHERE l.class_id = " + classId
            );

            while (resultSet.next()) {
                JSONObject lesson = new JSONObject();
                lesson.put("lesson_id", resultSet.getInt("lesson_id"));
                lesson.put("lesson_name", resultSet.getString("lesson_name"));
                lessonsArray.put(lesson);
            }

            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An error occurred.").build();
        }

        return Response.ok(lessonsArray.toString()).build();
    }
}

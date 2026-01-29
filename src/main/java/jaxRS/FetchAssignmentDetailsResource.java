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

@Path("/fetchAssignmentDetails")
public class FetchAssignmentDetailsResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetchAssignmentDetails(@QueryParam("id") int assignmentId) {

        if (assignmentId <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Invalid id\"}")
                    .build();
        }

        JSONArray assignmentArray = new JSONArray();

        String sql =
                "SELECT homework_name, description, time_indication, homework_splitable " +
                        "FROM homework WHERE homework_id = ?";

        try (Connection connection = Db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, assignmentId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    JSONObject assignment = new JSONObject();
                    assignment.put("homework_name", resultSet.getString("homework_name"));
                    assignment.put("hw_description", resultSet.getString("description"));
                    assignment.put("time_indication", resultSet.getTime("time_indication").toString());
                    assignment.put("homework_splitable", resultSet.getBoolean("homework_splitable"));
                    assignmentArray.put(assignment);
                }
            }

            return Response.ok(assignmentArray.toString()).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"An error occurred.\"}")
                    .build();
        }
    }
}

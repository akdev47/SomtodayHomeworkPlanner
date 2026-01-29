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
import java.util.ArrayList;
import java.util.List;

@Path("/fetchGoals")
public class FetchGoalsResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetchGoals(@QueryParam("id") int assignmentId) {

        if (assignmentId <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Invalid id\"}")
                    .build();
        }

        List<JSONObject> goalList = new ArrayList<>();

        String sql = "SELECT goal_id, goal_name FROM goal WHERE homework_id = ?";

        try (Connection connection = Db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, assignmentId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    JSONObject goal = new JSONObject();
                    goal.put("goal_id", resultSet.getInt("goal_id"));
                    goal.put("goal_name", resultSet.getString("goal_name"));
                    goalList.add(goal);
                }
            }

            return Response.ok(new JSONArray(goalList).toString()).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"An error occurred.\"}")
                    .build();
        }
    }
}

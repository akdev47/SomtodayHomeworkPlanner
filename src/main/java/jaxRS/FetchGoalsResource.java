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
import java.util.ArrayList;
import java.util.List;

@Path("/fetchGoals")
public class FetchGoalsResource {

    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=somtoday6";
    private static final String DB_USER = "dab_di23242b_168";
    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetchGoals(@QueryParam("id") int assignmentId) {

        List<JSONObject> goalList = new ArrayList<>();

        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            String sql = "SELECT goal_id, goal_name FROM somtoday6.Goal WHERE homework_id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, assignmentId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                JSONObject goal = new JSONObject();
                goal.put("goal_id", resultSet.getInt("goal_id"));
                goal.put("goal_name", resultSet.getString("goal_name"));
                goalList.add(goal);
            }

            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred.")
                    .build();
        }

        JSONArray jsonArray = new JSONArray(goalList);
        return Response.ok(jsonArray.toString()).build();
    }
}

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

@Path("/fetchAssignmentDetails")
public class FetchAssignmentDetailsResource {

    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=somtoday6";
    private static final String DB_USER = "dab_di23242b_168";
    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetchAssignmentDetails(@QueryParam("id") int assignmentId) {

        JSONArray assignmentArray = new JSONArray();

        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            String sql = "SELECT homework_name, description, time_indication FROM somtoday6.Homework WHERE homework_id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, assignmentId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                JSONObject assignment = new JSONObject();
                assignment.put("homework_name", resultSet.getString("homework_name"));
                assignment.put("hw_description", resultSet.getString("description"));
                assignment.put("time_indication", resultSet.getTime("time_indication").toString());
                assignmentArray.put(assignment);
            }

            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred.")
                    .build();
        }

        return Response.ok(assignmentArray.toString()).build();
    }
}


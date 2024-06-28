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

@Path("/fetchSplitRequest")
public class FetchSplitRequestResource {

    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=somtoday6";
    private static final String DB_USER = "dab_di23242b_168";
    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetchSplitRequest(@QueryParam("homeworkId") int homeworkId) {

        List<JSONObject> splitRequestList = new ArrayList<>();

        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            String sql = "SELECT p.person_name, sr.split_request_id, sr.request_description " +
                    "FROM somtoday6.Split_Request sr " +
                    "JOIN somtoday6.Student s ON sr.student_id = s.student_id " +
                    "JOIN somtoday6.Person p ON s.person_id = p.person_id " +
                    "WHERE sr.teacher_homework_id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, homeworkId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                JSONObject splitRequest = new JSONObject();
                splitRequest.put("person_name", resultSet.getString("person_name"));
                splitRequest.put("split_request_id", resultSet.getInt("split_request_id"));
                splitRequest.put("request_description", resultSet.getString("request_description"));
                splitRequestList.add(splitRequest);
            }

            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred.")
                    .build();
        }

        JSONArray jsonArray = new JSONArray(splitRequestList);
        return Response.ok(jsonArray.toString()).build();
    }
}

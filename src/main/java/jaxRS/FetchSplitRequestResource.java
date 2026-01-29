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

@Path("/fetchSplitRequest")
public class FetchSplitRequestResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetchSplitRequest(@QueryParam("homeworkId") int homeworkId) {

        if (homeworkId <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Invalid homeworkId\"}")
                    .build();
        }

        List<JSONObject> splitRequestList = new ArrayList<>();

        String sql =
                "SELECT p.person_name, sr.split_request_id, sr.request_description " +
                        "FROM split_request sr " +
                        "JOIN student s ON sr.student_id = s.student_id " +
                        "JOIN person p ON s.person_id = p.person_id " +
                        "WHERE sr.teacher_homework_id = ?";

        try (Connection connection = Db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, homeworkId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    JSONObject splitRequest = new JSONObject();
                    splitRequest.put("person_name", resultSet.getString("person_name"));
                    splitRequest.put("split_request_id", resultSet.getInt("split_request_id"));
                    splitRequest.put("request_description", resultSet.getString("request_description"));
                    splitRequestList.add(splitRequest);
                }
            }

            return Response.ok(new JSONArray(splitRequestList).toString()).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"An error occurred.\"}")
                    .build();
        }
    }
}

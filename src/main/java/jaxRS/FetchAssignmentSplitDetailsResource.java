package jaxRS;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import somtoday.db.Db;
import somtoday.model.Homework;
import somtoday.model.SplitHomework;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.*;

@Path("/fetchAssignmentSplitDetails")
public class FetchAssignmentSplitDetailsResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetchAssignmentSplitDetails(
            @QueryParam("role") String role,
            @QueryParam("personId") String personId,
            @QueryParam("splitId") String splitId
    ) {
        if (role == null || personId == null || splitId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Missing required parameters\"}")
                    .build();
        }

        if (!("teacher".equals(role) || "student".equals(role) || "admin".equals(role))) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Invalid role\"}")
                    .build();
        }

        int splitIdInt;
        try {
            splitIdInt = Integer.parseInt(splitId);
        } catch (NumberFormatException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"splitId must be a number\"}")
                    .build();
        }

        Homework homeW = new Homework();
        SplitHomework sh = new SplitHomework();

        String sql =
                "SELECT h.homework_name, sh.split_name, h.description, sh.time_indication " +
                        "FROM homework h " +
                        "JOIN splitted_homework sh ON sh.homework_id = h.homework_id " +
                        "WHERE sh.splitted_homework_id = ?";

        try (Connection connection = Db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, splitIdInt);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    homeW.setHomeworkName(resultSet.getString("homework_name"));
                    sh.setSplitName(resultSet.getString("split_name"));
                    homeW.setDescription(resultSet.getString("description"));
                    sh.setTimeIndication(resultSet.getTime("time_indication"));
                }
            }

            return Response.ok(toJson(homeW, sh)).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"An error occurred.\"}")
                    .build();
        }
    }

    private String toJson(Homework homeW, SplitHomework sh) {
        JSONObject obj = new JSONObject();
        obj.put("homework_name", homeW.getHomeworkName());
        obj.put("split_name", sh.getSplitName());
        obj.put("hw_description", homeW.getDescription());
        obj.put("time_indication", sh.getTimeIndication() == null ? JSONObject.NULL : sh.getTimeIndication().toString());

        JSONArray arr = new JSONArray();
        arr.put(obj);
        return arr.toString();
    }
}

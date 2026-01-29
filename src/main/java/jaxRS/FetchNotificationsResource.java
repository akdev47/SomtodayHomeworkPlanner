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

@Path("/fetchNotifications")
public class FetchNotificationsResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetchNotifications(@QueryParam("personId") int personId) {
        if (personId <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Invalid personId\"}")
                    .build();
        }

        JSONArray jsonArray = new JSONArray();

        String sql =
                "SELECT notification_id, date, sender, info, person_id " +
                        "FROM notification " +
                        "WHERE person_id = ? " +
                        "ORDER BY date DESC, notification_id DESC";

        try (Connection connection = Db.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, personId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    JSONObject notification = new JSONObject();
                    notification.put("notification_id", rs.getInt("notification_id"));
                    notification.put("date", rs.getDate("date") == null ? JSONObject.NULL : rs.getDate("date").toString());
                    notification.put("sender", rs.getString("sender"));
                    notification.put("info", rs.getString("info"));
                    notification.put("person_id", rs.getInt("person_id"));
                    jsonArray.put(notification);
                }
            }

            return Response.ok(jsonArray.toString()).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"An error occurred.\"}")
                    .build();
        }
    }
}

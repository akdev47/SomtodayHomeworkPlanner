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

@Path("/fetchNotifications")
public class FetchNotificationsResource {

    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=somtoday6";
    private static final String DB_USER = "dab_di23242b_168";
    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetchNotifications(@QueryParam("personId") int personId) {
        JSONArray jsonArray = new JSONArray();

        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            Statement statement = connection.createStatement();
            String query = "SELECT * FROM somtoday6.Notification WHERE person_id = " + personId;
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                JSONObject notification = new JSONObject();
                notification.put("notification_id", resultSet.getInt("notification_id"));
                notification.put("date", resultSet.getDate("date").toString());
                notification.put("sender", resultSet.getString("sender"));
                notification.put("info", resultSet.getString("info"));
                notification.put("person_id", resultSet.getInt("person_id"));
                jsonArray.put(notification);
            }

            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An error occurred.").build();
        }

        return Response.ok(jsonArray.toString()).build();
    }
}

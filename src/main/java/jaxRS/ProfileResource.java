package jaxRS;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

@Path("/profile")
public class ProfileResource {

    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=somtoday6";
    private static final String DB_USER = "dab_di23242b_168";
    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProfileInfo(@QueryParam("personId") int personId) {
        Map<String, Object> profile = new HashMap<>();
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "SELECT person_name, birth_date, person_gender, email_address, username, user_password FROM person WHERE person_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, personId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        profile.put("person_name", rs.getString("person_name"));
                        profile.put("birth_date", rs.getDate("birth_date") != null ? rs.getDate("birth_date").toString() : null);
                        profile.put("person_gender", rs.getString("person_gender"));
                        profile.put("email_address", rs.getString("email_address"));
                        profile.put("username", rs.getString("username"));
                        profile.put("user_password", rs.getString("user_password"));
                    } else {
                        return Response.status(Response.Status.NOT_FOUND).entity("{\"message\": \"Profile not found\"}").build();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"message\": \"Error fetching profile info\"}").build();
        }
        return Response.ok(profile).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateProfileInfo(@QueryParam("personId") int personId, Map<String, Object> profile) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "UPDATE person SET person_name = ?, birth_date = ?, person_gender = ?, email_address = ?, username = ?, user_password = ? WHERE person_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, profile.getOrDefault("person_name", "").toString());
                stmt.setDate(2, profile.get("birth_date") != null ? Date.valueOf(profile.get("birth_date").toString()) : null);
                stmt.setString(3, profile.getOrDefault("person_gender", "").toString());
                stmt.setString(4, profile.getOrDefault("email_address", "").toString());
                stmt.setString(5, profile.getOrDefault("username", "").toString());
                stmt.setString(6, profile.getOrDefault("user_password", "").toString());
                stmt.setInt(7, personId);
                int rowsUpdated = stmt.executeUpdate();
                if (rowsUpdated > 0) {
                    return Response.ok("{\"success\": true}").build();
                } else {
                    return Response.status(Response.Status.NOT_FOUND).entity("{\"message\": \"Profile not found\"}").build();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"message\": \"Error updating profile info\"}").build();
        }
    }
}

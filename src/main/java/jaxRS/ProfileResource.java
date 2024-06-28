package jaxRS;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
    public Response updateProfileInfo(@QueryParam("personId") int personId, Map<String, Object> profile) throws NoSuchAlgorithmException {
        String oldPasswordInput = profile.getOrDefault("old_password", "").toString();
        String newPassword = profile.getOrDefault("user_password", "").toString();
        String birthDateString = profile.getOrDefault("birth_date", "").toString();
        Date birthDate = null;

        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] hash = messageDigest.digest(oldPasswordInput.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        String oldPasswordInput1 = hexString.toString();

        // Validate and parse birth_date
        if (!birthDateString.isEmpty()) {
            try {
                birthDate = Date.valueOf(birthDateString);
            } catch (IllegalArgumentException e) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"message\": \"Invalid birth_date format. Please use YYYY-MM-DD.\"}")
                        .build();
            }
        }

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Retrieve the current password from the database
            String sqlGetOldPassword = "SELECT user_password FROM person WHERE person_id = ?";
            try (PreparedStatement stmtGetOldPassword = connection.prepareStatement(sqlGetOldPassword)) {
                stmtGetOldPassword.setInt(1, personId);
                try (ResultSet rs = stmtGetOldPassword.executeQuery()) {
                    if (rs.next()) {
                        String currentPassword = rs.getString("user_password");
                        if (!currentPassword.equals(oldPasswordInput1)) {
                            return Response.status(Response.Status.NOT_ACCEPTABLE)
                                    .entity("{\"message\": \"Provided old password does not match the old password.\"}")
                                    .build();
                        }
                    } else {
                        return Response.status(Response.Status.NOT_FOUND)
                                .entity("{\"message\": \"Profile not found\"}").build();
                    }
                }
            }

            // Proceed with the update if the old password matches
            String sqlUpdateProfile = "UPDATE person SET person_name = ?, birth_date = ?, person_gender = ?, email_address = ?, username = ?, user_password = ? WHERE person_id = ?";
            try (PreparedStatement stmtUpdateProfile = connection.prepareStatement(sqlUpdateProfile)) {
                stmtUpdateProfile.setString(1, profile.getOrDefault("person_name", "").toString());
                stmtUpdateProfile.setDate(2, birthDate);
                stmtUpdateProfile.setString(3, profile.getOrDefault("person_gender", "").toString());
                stmtUpdateProfile.setString(4, profile.getOrDefault("email_address", "").toString());
                stmtUpdateProfile.setString(5, profile.getOrDefault("username", "").toString());

                MessageDigest messageDigest1 = MessageDigest.getInstance("SHA-256");
                byte[] hash1  = messageDigest1.digest(newPassword.getBytes(StandardCharsets.UTF_8));
                StringBuilder hexString1 = new StringBuilder(2 * hash1.length);
                for (int i = 0; i < hash1.length; i++) {
                    String hex = Integer.toHexString(0xff & hash1[i]);
                    if(hex.length() == 1) {
                        hexString1.append('0');
                    }
                    hexString1.append(hex);
                }
                String newPassword1 = hexString1.toString();

                stmtUpdateProfile.setString(6, newPassword1);
                stmtUpdateProfile.setInt(7, personId);

                int rowsUpdated = stmtUpdateProfile.executeUpdate();
                if (rowsUpdated > 0) {
                    return Response.ok("{\"success\": true}").build();
                } else {
                    return Response.status(Response.Status.NOT_FOUND)
                            .entity("{\"message\": \"Profile not found\"}").build();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\": \"Error updating profile info\"}").build();
        }
    }
}

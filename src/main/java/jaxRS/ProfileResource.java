package jaxRS;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import somtoday.db.Db;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Path("/profile")
public class ProfileResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProfileInfo(@QueryParam("personId") int personId) {
        if (personId <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\":\"Invalid personId\"}")
                    .build();
        }

        Map<String, Object> profile = new HashMap<>();

        String sql =
                "SELECT person_name, birth_date, person_gender, email_address, username " +
                        "FROM person " +
                        "WHERE person_id = ?";

        try (Connection connection = Db.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, personId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return Response.status(Response.Status.NOT_FOUND)
                            .entity("{\"message\":\"Profile not found\"}")
                            .build();
                }

                profile.put("person_name", rs.getString("person_name"));
                profile.put("birth_date", rs.getDate("birth_date") != null ? rs.getDate("birth_date").toString() : null);
                profile.put("person_gender", rs.getString("person_gender"));
                profile.put("email_address", rs.getString("email_address"));
                profile.put("username", rs.getString("username"));

                // IMPORTANT: do NOT return user_password (even if hashed)
            }

            return Response.ok(profile).build();

        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\":\"Error fetching profile info\"}")
                    .build();
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateProfileInfo(@QueryParam("personId") int personId, Map<String, Object> profile) {
        if (personId <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\":\"Invalid personId\"}")
                    .build();
        }

        String oldPasswordInput = profile.getOrDefault("old_password", "").toString();
        String newPassword = profile.getOrDefault("user_password", "").toString(); // frontend sends new password here
        String birthDateString = profile.getOrDefault("birth_date", "").toString();

        if (oldPasswordInput.isBlank() || newPassword.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\":\"old_password and user_password are required\"}")
                    .build();
        }

        Date birthDate = null;
        if (!birthDateString.isBlank()) {
            try {
                birthDate = Date.valueOf(birthDateString);
            } catch (IllegalArgumentException e) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"message\":\"Invalid birth_date format. Please use YYYY-MM-DD.\"}")
                        .build();
            }
        }

        try (Connection connection = Db.getConnection()) {

            // 1) verify old password
            String sqlGetOldPassword = "SELECT user_password FROM person WHERE person_id = ?";
            String currentPasswordHash;

            try (PreparedStatement stmt = connection.prepareStatement(sqlGetOldPassword)) {
                stmt.setInt(1, personId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        return Response.status(Response.Status.NOT_FOUND)
                                .entity("{\"message\":\"Profile not found\"}")
                                .build();
                    }
                    currentPasswordHash = rs.getString("user_password");
                }
            }

            String oldPasswordHash = sha256Hex(oldPasswordInput);
            if (!currentPasswordHash.equals(oldPasswordHash)) {
                return Response.status(Response.Status.NOT_ACCEPTABLE)
                        .entity("{\"message\":\"Provided old password does not match.\"}")
                        .build();
            }

            // 2) update profile
            String sqlUpdate =
                    "UPDATE person SET person_name = ?, birth_date = ?, person_gender = ?, email_address = ?, username = ?, user_password = ? " +
                            "WHERE person_id = ?";

            String newPasswordHash = sha256Hex(newPassword);

            try (PreparedStatement stmt = connection.prepareStatement(sqlUpdate)) {
                stmt.setString(1, profile.getOrDefault("person_name", "").toString());
                stmt.setDate(2, birthDate);
                stmt.setString(3, profile.getOrDefault("person_gender", "").toString());
                stmt.setString(4, profile.getOrDefault("email_address", "").toString());
                stmt.setString(5, profile.getOrDefault("username", "").toString());
                stmt.setString(6, newPasswordHash);
                stmt.setInt(7, personId);

                int rowsUpdated = stmt.executeUpdate();
                if (rowsUpdated > 0) {
                    return Response.ok("{\"success\": true}").build();
                } else {
                    return Response.status(Response.Status.NOT_FOUND)
                            .entity("{\"message\":\"Profile not found\"}")
                            .build();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\":\"Error updating profile info\"}")
                    .build();
        }
    }

    private String sha256Hex(String input) throws Exception {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] hash = messageDigest.digest(input.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}

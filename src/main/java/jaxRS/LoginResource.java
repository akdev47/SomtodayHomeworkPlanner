package jaxRS;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import org.json.JSONObject;
import somtoday.db.Db;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Path("/login")
public class LoginResource {

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(@FormParam("username") String username,
                          @FormParam("password") String password) {

        if (username == null || username.isBlank() || password == null) {
            return Response.status(Status.BAD_REQUEST)
                    .entity("{\"error\":\"Missing username or password\"}")
                    .build();
        }

        System.out.println("Login attempt for user: " + username);

        try (Connection connection = Db.getConnection()) {

            String passwordHash = sha256Hex(password);

            // check to see if user is in person table
            String personSql = "SELECT person_id, person_name FROM person WHERE username = ? AND user_password = ?";
            Integer personId = null;
            String personName = null;

            try (PreparedStatement stmt = connection.prepareStatement(personSql)) {
                stmt.setString(1, username);
                stmt.setString(2, passwordHash);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        personId = rs.getInt("person_id");
                        personName = rs.getString("person_name");
                    } else {
                        return Response.status(Status.UNAUTHORIZED)
                                .entity("{\"error\":\"Invalid credentials\"}")
                                .build();
                    }
                }
            }

            // check role of logged in user, by default it is student
            String role = "student";

            String adminSql = "SELECT 1 FROM admin WHERE person_id = ? LIMIT 1";
            try (PreparedStatement stmt = connection.prepareStatement(adminSql)) {
                stmt.setInt(1, personId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) role = "admin";
                }
            }

            if (!"admin".equals(role)) {
                String teacherSql = "SELECT 1 FROM teacher WHERE person_id = ? LIMIT 1";
                try (PreparedStatement stmt = connection.prepareStatement(teacherSql)) {
                    stmt.setInt(1, personId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) role = "teacher";
                    }
                }
            }

            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("personId", personId);
            jsonResponse.put("role", role);
            jsonResponse.put("personName", personName);

            return Response.ok(jsonResponse.toString()).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"An error occurred: " + e.getMessage() + "\"}")
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

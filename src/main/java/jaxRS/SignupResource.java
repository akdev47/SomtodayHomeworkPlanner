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

@Path("/signup")
public class SignupResource {

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response signup(
            @FormParam("personName") String personName,
            @FormParam("email") String email,
            @FormParam("username") String username,
            @FormParam("password") String password,
            @FormParam("birthDate") String birthDate, // expected yyyy-mm-dd
            @FormParam("gender") String gender,       // F/M/O
            @FormParam("role") String role            // student/teacher (demo)
    ) {
        if (personName == null || personName.isBlank()
                || email == null || email.isBlank()
                || username == null || username.isBlank()
                || password == null || password.isBlank()
                || birthDate == null || birthDate.isBlank()
                || gender == null || gender.isBlank()
                || role == null || role.isBlank()) {

            return Response.status(Status.BAD_REQUEST)
                    .entity("{\"error\":\"Missing required fields\"}")
                    .build();
        }

        role = role.toLowerCase().trim();
        if (!role.equals("student") && !role.equals("teacher")) {
            return Response.status(Status.BAD_REQUEST)
                    .entity("{\"error\":\"Invalid role. Must be student or teacher.\"}")
                    .build();
        }

        try (Connection connection = Db.getConnection()) {

            // 1) username uniqueness check
            String existsSql = "SELECT 1 FROM person WHERE username = ? LIMIT 1";
            try (PreparedStatement stmt = connection.prepareStatement(existsSql)) {
                stmt.setString(1, username);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Response.status(Status.CONFLICT)
                                .entity("{\"error\":\"Username already exists\"}")
                                .build();
                    }
                }
            }

            // 2) insert into person
            String passwordHash = sha256Hex(password);

            String insertPersonSql =
                    "INSERT INTO person (person_name, birth_date, person_gender, email_address, username, user_password) " +
                            "VALUES (?, ?, ?, ?, ?, ?) " +
                            "RETURNING person_id";

            Integer personId;
            try (PreparedStatement stmt = connection.prepareStatement(insertPersonSql)) {
                stmt.setString(1, personName);
                stmt.setDate(2, java.sql.Date.valueOf(birthDate));
                stmt.setString(3, gender);
                stmt.setString(4, email);
                stmt.setString(5, username);
                stmt.setString(6, passwordHash);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        return Response.status(Status.INTERNAL_SERVER_ERROR)
                                .entity("{\"error\":\"Failed to create person\"}")
                                .build();
                    }
                    personId = rs.getInt("person_id");
                }
            }

            // 3) insert role table (demo)
            if (role.equals("teacher")) {
                String insertTeacherSql = "INSERT INTO teacher (person_id) VALUES (?)";
                try (PreparedStatement stmt = connection.prepareStatement(insertTeacherSql)) {
                    stmt.setInt(1, personId);
                    stmt.executeUpdate();
                }
            } else {
                // NOTE: this assumes class_id can be NULL or has a default.
                // If your student.class_id is NOT NULL, you must either:
                // - choose a default class_id here, or
                // - add a class dropdown to signup.
                String insertStudentSql = "INSERT INTO student (person_id) VALUES (?)";
                try (PreparedStatement stmt = connection.prepareStatement(insertStudentSql)) {
                    stmt.setInt(1, personId);
                    stmt.executeUpdate();
                }
            }

            JSONObject json = new JSONObject();
            json.put("personId", personId);
            json.put("role", role);
            json.put("personName", personName);

            return Response.ok(json.toString()).build();

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

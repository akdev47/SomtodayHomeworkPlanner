package jaxRS;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import somtoday.db.Db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Path("/fetchLessonsPage")
public class FetchLessonsPageResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetchLessonsPage(@QueryParam("role") String role, @QueryParam("personId") String personId) {

        if (role == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"role is required\"}")
                    .build();
        }

        Integer personIdInt = null;
        if (!"admin".equals(role)) {
            if (personId == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\":\"personId is required for this role\"}")
                        .build();
            }
            try {
                personIdInt = Integer.parseInt(personId);
            } catch (NumberFormatException e) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\":\"personId must be a number\"}")
                        .build();
            }
        }

        String sql;
        boolean needsPersonParam = false;

        if ("teacher".equals(role)) {
            sql =
                    "SELECT l.lesson_id, l.lesson_name, l.lesson_description, c.class_name, p.person_name AS teacher_name " +
                            "FROM lesson l " +
                            "JOIN class c ON l.class_id = c.class_id " +
                            "JOIN teacher t ON l.teacher_id = t.teacher_id " +
                            "JOIN person p ON t.person_id = p.person_id " +
                            "WHERE t.person_id = ?";
            needsPersonParam = true;

        } else if ("student".equals(role)) {
            sql =
                    "SELECT l.lesson_id, l.lesson_name, l.lesson_description, c.class_name, p.person_name AS teacher_name " +
                            "FROM lesson l " +
                            "JOIN class c ON l.class_id = c.class_id " +
                            "JOIN student s ON c.class_id = s.class_id " +
                            "JOIN teacher t ON l.teacher_id = t.teacher_id " +
                            "JOIN person p ON t.person_id = p.person_id " +
                            "WHERE s.person_id = ?";
            needsPersonParam = true;

        } else if ("admin".equals(role)) {
            sql =
                    "SELECT l.lesson_id, l.lesson_name, l.lesson_description, c.class_name, p.person_name AS teacher_name " +
                            "FROM lesson l " +
                            "JOIN class c ON l.class_id = c.class_id " +
                            "JOIN teacher t ON l.teacher_id = t.teacher_id " +
                            "JOIN person p ON t.person_id = p.person_id";

        } else {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Invalid role\"}")
                    .build();
        }

        JSONArray jsonArray = new JSONArray();

        try (Connection connection = Db.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            if (needsPersonParam) {
                stmt.setInt(1, personIdInt);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("lesson_id", rs.getInt("lesson_id"));
                    jsonObject.put("lesson_name", rs.getString("lesson_name"));
                    jsonObject.put("lesson_description", rs.getString("lesson_description"));
                    jsonObject.put("class_name", rs.getString("class_name"));
                    jsonObject.put("teacher_name", rs.getString("teacher_name"));
                    jsonArray.put(jsonObject);
                }
            }

            return Response.ok(jsonArray.toString()).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"An error occurred\"}")
                    .build();
        }
    }
}

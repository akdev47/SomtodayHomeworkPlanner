package jaxRS;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import somtoday.db.Db;
import somtoday.model.Class;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@Path("/fetchClasses")
public class FetchClassesResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetchClasses(@QueryParam("role") String role, @QueryParam("personId") String personId) {
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

        List<Class> classList = new ArrayList<>();

        try (Connection connection = Db.getConnection()) {

            String sql;
            boolean needsPersonParam = false;

            if ("teacher".equals(role)) {
                sql =
                        "SELECT DISTINCT c.class_id, c.class_name, c.class_capacity, " +
                                "       (c.profile_picture IS NOT NULL) AS has_profile_picture " +
                                "FROM class c " +
                                "JOIN lesson l ON c.class_id = l.class_id " +
                                "JOIN teacher t ON l.teacher_id = t.teacher_id " +
                                "WHERE t.person_id = ?";
                needsPersonParam = true;

            } else if ("student".equals(role)) {
                sql =
                        "SELECT DISTINCT c.class_id, c.class_name, c.class_capacity, " +
                                "       (c.profile_picture IS NOT NULL) AS has_profile_picture " +
                                "FROM class c " +
                                "JOIN student s ON c.class_id = s.class_id " +
                                "WHERE s.person_id = ?";
                needsPersonParam = true;

            } else if ("admin".equals(role)) {
                sql =
                        "SELECT c.class_id, c.class_name, c.class_capacity, " +
                                "       (c.profile_picture IS NOT NULL) AS has_profile_picture " +
                                "FROM class c";

            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\":\"Invalid role\"}")
                        .build();
            }

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                if (needsPersonParam) {
                    stmt.setInt(1, personIdInt);
                }

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Class cls = new Class();
                        cls.setClassID(rs.getInt("class_id"));
                        cls.setClassName(rs.getString("class_name"));
                        cls.setClassCapacity(rs.getInt("class_capacity"));
                        cls.setHasProfilePicture(rs.getBoolean("has_profile_picture"));
                        classList.add(cls);
                    }
                }
            }

            return Response.ok(toJson(classList)).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"An error occurred\"}")
                    .build();
        }
    }

    private String toJson(List<Class> classList) {
        JSONArray jsonArray = new JSONArray();
        for (Class cls : classList) {
            JSONObject json = new JSONObject();
            json.put("class_id", cls.getClassID());
            json.put("class_name", cls.getClassName());
            json.put("class_capacity", cls.getClassCapacity());
            json.put("has_profile_picture", cls.isHasProfilePicture());
            jsonArray.put(json);
        }
        return jsonArray.toString();
    }
}

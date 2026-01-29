package jaxRS;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import somtoday.db.Db;

import java.sql.*;
import java.util.*;

@Path("/fetchAssignments")
public class FetchAssignmentsResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetchAssignments(@QueryParam("role") String role, @QueryParam("personId") String personId) {

        if (role == null || personId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Missing role or personId\"}")
                    .build();
        }

        int personIdInt;
        try {
            personIdInt = Integer.parseInt(personId);
        } catch (NumberFormatException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"personId must be a number\"}")
                    .build();
        }

        // Use homework_id as key so we don’t duplicate
        Map<Integer, JSONObject> homeworkById = new LinkedHashMap<>();
        List<JSONObject> splits = new ArrayList<>();

        try (Connection connection = Db.getConnection()) {

            String sql;
            boolean needsPersonParam = false;

            if ("teacher".equals(role)) {
                sql =
                        "SELECT DISTINCT c.class_name, h.homework_id, h.homework_name, h.publish_date, h.due_date " +
                                "FROM homework h " +
                                "JOIN class c ON h.class_id = c.class_id " +
                                "JOIN teacher t ON h.teacher_id = t.teacher_id " +
                                "WHERE h.student_id IS NULL AND t.person_id = ? " +
                                "ORDER BY h.publish_date DESC";
                needsPersonParam = true;

            } else if ("student".equals(role)) {
                sql =
                        "SELECT DISTINCT c.class_name, h.homework_id, h.homework_name, h.publish_date, h.due_date, " +
                                "       sh.splitted_homework_id, sh.split_name " +
                                "FROM homework h " +
                                "LEFT JOIN splitted_homework sh ON h.homework_id = sh.homework_id AND sh.accepted = true " +
                                "JOIN class c ON h.class_id = c.class_id " +
                                "JOIN student s ON h.student_id = s.student_id " +
                                "WHERE s.person_id = ? " +
                                "ORDER BY h.publish_date DESC";
                needsPersonParam = true;

            } else if ("admin".equals(role)) {
                sql =
                        "SELECT DISTINCT c.class_name, h.homework_id, h.homework_name, h.publish_date, h.due_date " +
                                "FROM homework h " +
                                "JOIN class c ON h.class_id = c.class_id " +
                                "WHERE h.student_id IS NULL " +
                                "ORDER BY h.publish_date DESC";

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
                        int homeworkId = rs.getInt("homework_id");

                        // build homework list without duplicates
                        if (!homeworkById.containsKey(homeworkId)) {
                            JSONObject hw = new JSONObject();
                            hw.put("class_name", rs.getString("class_name"));
                            hw.put("homework_id", homeworkId);
                            hw.put("homework_name", rs.getString("homework_name"));
                            hw.put("publish_date", rs.getDate("publish_date").toString());
                            hw.put("due_date", rs.getDate("due_date").toString());
                            homeworkById.put(homeworkId, hw);
                        }

                        // student role: collect splits (if present)
                        if ("student".equals(role)) {
                            Integer splitId = (Integer) rs.getObject("splitted_homework_id");
                            if (splitId != null) {
                                JSONObject split = new JSONObject();
                                split.put("split_id", splitId);
                                split.put("homework_id", homeworkId);
                                split.put("split_name", rs.getString("split_name"));
                                splits.add(split);
                            }
                        }
                    }
                }
            }

            JSONObject out = new JSONObject();
            out.put("homeworks", new JSONArray(homeworkById.values()));
            out.put("splits", new JSONArray(splits));

            return Response.ok(out.toString()).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"An error occurred\"}")
                    .build();
        }
    }
}

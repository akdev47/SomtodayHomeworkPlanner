package jaxRS;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.json.JSONArray;
import org.json.JSONObject;
import somtoday.db.Db;
import somtoday.model.Teacher;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Path("/fetchAllTeachers")
public class FetchAllTeachersResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetchAllTeachers() {
        List<Teacher> teacherList = new ArrayList<>();

        String sql =
                "SELECT t.teacher_id, p.person_name " +
                        "FROM teacher t " +
                        "JOIN person p ON t.person_id = p.person_id";

        try (Connection connection = Db.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Teacher teacher = new Teacher();
                teacher.setTeacherID(rs.getInt("teacher_id"));
                teacher.setPersonName(rs.getString("person_name"));
                teacherList.add(teacher);
            }

            return Response.ok(toJson(teacherList)).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"An error occurred\"}")
                    .build();
        }
    }

    private String toJson(List<Teacher> teacherList) {
        JSONArray jsonArray = new JSONArray();
        for (Teacher teacher : teacherList) {
            JSONObject json = new JSONObject();
            json.put("teacher_id", teacher.getTeacherID());
            json.put("person_name", teacher.getPersonName());
            jsonArray.put(json);
        }
        return jsonArray.toString();
    }
}

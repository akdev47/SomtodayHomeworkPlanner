package jaxRS;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.sql.*;
import somtoday.model.Homework;
import somtoday.model.SplitHomework;

@Path("/fetchAssignmentSplitDetails")
public class FetchAssignmentSplitDetailsResource {
    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=somtoday6";
    private static final String DB_USER = "dab_di23242b_168";
    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetchAssignmentSplitDetails(@QueryParam("role") String role, @QueryParam("personId") String personId, @QueryParam("splitId") String splitId) {
        if (role == null || personId == null || splitId == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\": \"Missing required parameters\"}").build();
        }

        Homework homeW = new Homework();
        SplitHomework sh = new SplitHomework();

        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            String sql;
            if ("teacher".equals(role) || "student".equals(role) || "admin".equals(role)) {
                sql = "SELECT h.homework_name, sh.split_name, h.description , sh.time_indication " +
                        "FROM somtoday6.homework h " +
                        "JOIN somtoday6.splitted_homework sh ON sh.homework_id = h.homework_id " +
                        "WHERE sh.splitted_homework_id = ?";
            } else {
                return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\": \"Invalid role\"}").build();
            }

            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, Integer.parseInt(splitId));
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                homeW.setHomeworkName(resultSet.getString("homework_name"));
                sh.setSplitName(resultSet.getString("split_name"));
                homeW.setDescription(resultSet.getString("description"));
                sh.setTimeIndication(resultSet.getTime("time_indication"));
            }

            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\": \"An error occurred.\"}").build();
        }

        return Response.ok(toJson(homeW, sh)).build();
    }

    private String toJson(Homework homeW, SplitHomework sh) {
        StringBuilder json = new StringBuilder();
        json.append("[{");
        json.append("\"homework_name\":\"").append(homeW.getHomeworkName()).append("\",");
        json.append("\"split_name\":\"").append(sh.getSplitName()).append("\",");
        json.append("\"hw_description\":\"").append(homeW.getDescription()).append("\",");
        json.append("\"time_indication\":\"").append(sh.getTimeIndication()).append("\"");
        json.append("}]");
        return json.toString();
    }
}

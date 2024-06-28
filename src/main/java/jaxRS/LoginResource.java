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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Path("/login")
public class LoginResource {

    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=somtoday6";
    private static final String DB_USER = "dab_di23242b_168";
    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(@FormParam("username") String username,
                          @FormParam("password") String password) {
        System.out.println("Login attempt for user: " + username);  // Log the username

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            // check to see if user is in person table
            String sql = "SELECT person_id, person_name FROM somtoday6.Person WHERE username = ? AND user_password = ?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                int personId = resultSet.getInt("person_id");
                String personName = resultSet.getString("person_name");

                // check role of logged in user, by default it is student
                String roleSql;
                String role = "student";

                // check for admin
                roleSql = "SELECT COUNT(*) AS count FROM somtoday6.Admin WHERE person_id = ?";
                preparedStatement = connection.prepareStatement(roleSql);
                preparedStatement.setInt(1, personId);
                resultSet = preparedStatement.executeQuery();
                if (resultSet.next() && resultSet.getInt("count") > 0) {
                    role = "admin";
                } else {
                    // check for teacher
                    roleSql = "SELECT COUNT(*) AS count FROM somtoday6.Teacher WHERE person_id = ?";
                    preparedStatement = connection.prepareStatement(roleSql);
                    preparedStatement.setInt(1, personId);
                    resultSet = preparedStatement.executeQuery();
                    if (resultSet.next() && resultSet.getInt("count") > 0) {
                        role = "teacher";
                    }
                }

                // Create the JSON response
                JSONObject jsonResponse = new JSONObject();
                jsonResponse.put("personId", personId);
                jsonResponse.put("role", role);
                jsonResponse.put("personName", personName);

                return Response.ok(jsonResponse.toString()).build();
            } else {
                return Response.status(Status.UNAUTHORIZED).entity("{\"error\":\"Invalid credentials\"}").build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"An error occurred: " + e.getMessage() + "\"}").build();
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

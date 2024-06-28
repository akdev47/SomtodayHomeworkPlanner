package jaxRS;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Path("/getClassPicture")
public class GetClassPictureResource {

    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=somtoday6";
    private static final String DB_USER = "dab_di23242b_168";
    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";

    @GET
    @Produces("image/jpeg")
    public Response getClassPicture(@QueryParam("classId") String classId) {
        if (classId == null || classId.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing classId parameter").build();
        }

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            Class.forName("org.postgresql.Driver");

            String sql = "SELECT profile_picture FROM somtoday6.Class WHERE class_id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, Integer.parseInt(classId));
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        byte[] imgData = resultSet.getBytes("profile_picture");

                        if (imgData != null) {
                            StreamingOutput stream = new StreamingOutput() {
                                @Override
                                public void write(OutputStream output) throws IOException {
                                    output.write(imgData);
                                }
                            };
                            return Response.ok(stream).build();
                        } else {
                            return Response.status(Response.Status.NOT_FOUND).entity("No image found for the given classId").build();
                        }
                    } else {
                        return Response.status(Response.Status.NOT_FOUND).entity("Class not found for the given classId").build();
                    }
                }
            }
        } catch (NumberFormatException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid classId parameter").build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An internal server error occurred!").build();
        }
    }
}


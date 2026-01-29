package jaxRS;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import somtoday.db.Db;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Path("/getClassPicture")
public class GetClassPictureResource {

    @GET
    @Produces("image/jpeg")
    public Response getClassPicture(@QueryParam("classId") String classId) {
        if (classId == null || classId.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Missing classId parameter")
                    .build();
        }

        int classIdInt;
        try {
            classIdInt = Integer.parseInt(classId);
        } catch (NumberFormatException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid classId parameter")
                    .build();
        }

        String sql = "SELECT profile_picture FROM class WHERE class_id = ?";

        try (Connection connection = Db.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, classIdInt);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return Response.status(Response.Status.NOT_FOUND)
                            .entity("Class not found for the given classId")
                            .build();
                }

                byte[] imgData = rs.getBytes("profile_picture");
                if (imgData == null || imgData.length == 0) {
                    return Response.status(Response.Status.NOT_FOUND)
                            .entity("No image found for the given classId")
                            .build();
                }

                StreamingOutput stream = (OutputStream output) -> {
                    try {
                        output.write(imgData);
                    } catch (IOException ex) {
                        throw ex;
                    }
                };

                return Response.ok(stream).build();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An internal server error occurred!")
                    .build();
        }
    }
}

package jaxRS;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import somtoday.db.Db;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;

@Path("/addToClass")
public class AddClassResource {

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response addClass(
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request,

            @FormDataParam("className") String className,
            @FormDataParam("classCapacity") int classCapacity,
            @FormDataParam("students") List<String> students,
            @FormDataParam("classPicture") InputStream fileInputStream,
            @FormDataParam("classPicture") FormDataContentDisposition fileMetaData
    ) {

        System.out.println("Received className: " + className);
        System.out.println("Received classCapacity: " + classCapacity);

        try (Connection connection = Db.getConnection()) {
            connection.setAutoCommit(false);

            String sql =
                    "INSERT INTO class (class_name, class_capacity, profile_picture) " +
                            "VALUES (?, ?, ?) RETURNING class_id";

            int classId;

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, className);
                ps.setInt(2, classCapacity);

                if (fileInputStream != null) {
                    byte[] bytes = readAllBytes(fileInputStream);
                    if (bytes.length > 0) {
                        ps.setBytes(3, bytes);
                    } else {
                        ps.setNull(3, Types.BINARY);
                    }
                } else {
                    ps.setNull(3, Types.BINARY);
                }

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("No class_id returned");
                    }
                    classId = rs.getInt("class_id");
                }
            }

            if (students != null) {
                String studentSql =
                        "UPDATE student SET class_id = ? WHERE student_id = ?";

                try (PreparedStatement ps = connection.prepareStatement(studentSql)) {
                    for (String studentIdStr : students) {
                        int studentId = Integer.parseInt(studentIdStr);

                        ps.setInt(1, classId);
                        ps.setInt(2, studentId);
                        ps.executeUpdate();

                        int personId = fetchPersonId(connection, studentId);
                        insertNotification(
                                connection,
                                personId,
                                "Admin added you to a class: " + className
                        );
                    }
                }
            }

            connection.commit();

            /* ===============================
               🔁 DYNAMIC REDIRECT (NO HARDCODE)
               =============================== */
            URI redirect = uriInfo.getBaseUriBuilder()
                    .replacePath(request.getContextPath())
                    .path("classes.html")
                    .build();

            return Response.seeOther(redirect).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred.")
                    .build();
        }
    }

    /* ----------------- helpers ----------------- */

    private byte[] readAllBytes(InputStream inputStream) throws Exception {
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            byte[] data = new byte[16384];
            int nRead;
            while ((nRead = inputStream.read(data)) != -1) {
                buffer.write(data, 0, nRead);
            }
            return buffer.toByteArray();
        }
    }

    private int fetchPersonId(Connection connection, int studentId) throws Exception {
        String sql = "SELECT person_id FROM student WHERE student_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("person_id");
                }
            }
        }
        throw new Exception("Person ID not found for student ID: " + studentId);
    }

    private void insertNotification(Connection connection, int personId, String info) throws Exception {
        String sql =
                "INSERT INTO notification (date, sender, info, person_id) " +
                        "VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(LocalDate.now()));
            ps.setString(2, "Admin");
            ps.setString(3, info);
            ps.setInt(4, personId);
            ps.executeUpdate();
        }
    }
}

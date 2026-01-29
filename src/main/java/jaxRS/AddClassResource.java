package jaxRS;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
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

            // NOTE:
            // Prefer table name "class" only if you created it lowercase and unquoted.
            // If you end up naming it "classes", update this SQL accordingly.
            String sql = "INSERT INTO class (class_name, class_capacity, profile_picture) " +
                    "VALUES (?, ?, ?) RETURNING class_id";

            int classId;

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, className);
                preparedStatement.setInt(2, classCapacity);

                if (fileInputStream != null) {
                    byte[] fileBytes = readAllBytes(fileInputStream);
                    if (fileBytes.length > 0) {
                        preparedStatement.setBytes(3, fileBytes);
                    } else {
                        preparedStatement.setNull(3, Types.BINARY);
                    }
                } else {
                    preparedStatement.setNull(3, Types.BINARY);
                }

                try (ResultSet rs = preparedStatement.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Insert class failed: no class_id returned.");
                    }
                    classId = rs.getInt("class_id");
                }
            }

            if (students != null) {
                String studentSql = "UPDATE student SET class_id = ? WHERE student_id = ?";

                try (PreparedStatement studentStmt = connection.prepareStatement(studentSql)) {
                    for (String studentIdStr : students) {
                        int studentId = Integer.parseInt(studentIdStr);

                        studentStmt.setInt(1, classId);
                        studentStmt.setInt(2, studentId);
                        studentStmt.executeUpdate();

                        int personId = fetchPersonId(connection, studentId);
                        insertNotification(connection, personId, "Admin added you to a class: " + className);
                    }
                }
            }

            connection.commit();

            // Keep your redirect (same as before)
            return Response.seeOther(new URI("/SomtodayHomeworkPlanner_war/classes.html")).build();

        } catch (Exception e) {
            e.printStackTrace();
            // If commit failed mid-way, try rollback if we still have a connection:
            // (Not possible here since try-with-resources closed it already on exceptions.)
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred.")
                    .build();
        }
    }

    private byte[] readAllBytes(InputStream inputStream) throws Exception {
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            int nRead;
            byte[] data = new byte[16384];
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            return buffer.toByteArray();
        }
    }

    private int fetchPersonId(Connection connection, int studentId) throws Exception {
        String sql = "SELECT person_id FROM student WHERE student_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("person_id");
                }
            }
        }
        throw new Exception("Person ID not found for student ID: " + studentId);
    }

    private void insertNotification(Connection connection, int personId, String info) throws Exception {
        String sql = "INSERT INTO notification (date, sender, info, person_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDate(1, Date.valueOf(LocalDate.now()));
            pstmt.setString(2, "Admin");
            pstmt.setString(3, info);
            pstmt.setInt(4, personId);
            pstmt.executeUpdate();
        }
    }
}

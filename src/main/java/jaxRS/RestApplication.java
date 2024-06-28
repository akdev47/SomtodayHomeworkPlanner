package jaxRS;

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import jakarta.ws.rs.ApplicationPath;

@ApplicationPath("/api")
public class RestApplication extends ResourceConfig {
    public RestApplication() {
        packages("jaxRS");
        register(MultiPartFeature.class);
    }
}
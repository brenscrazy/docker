package dao;

import org.junit.*;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class MongoClientDaoTest {

    @ClassRule
    public static GenericContainer stockServer = new FixedHostPortGenericContainer("stock:1.0-SNAPSHOT")
            .withFixedExposedPort(8080, 8080)
            .withExposedPorts(8080, 27017);

    @Before
    public void before() throws Exception {
        stockServer.start();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/addCompany?id=0&name=TESTCOMPANY&stockPrice=10&amount=100000"))
                .GET()
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        Assert.assertEquals("Inserted", response.body());
    }

    @After
    public void after() {
        stockServer.stop();
    }

    @Test
    public void nothing() {

    }


}

package dao;

import com.mongodb.rx.client.MongoClient;
import com.mongodb.rx.client.MongoClients;
import com.mongodb.rx.client.MongoDatabase;
import dto.PurchaseResult;
import dto.SellResult;
import dto.User;
import dto.UserStocks;
import org.junit.*;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class DaoIntegrationTest {

    @ClassRule
    public static GenericContainer stockServer = new FixedHostPortGenericContainer("stock:1.0-SNAPSHOT")
            .withFixedExposedPort(8080, 8080)
            .withExposedPorts(8080);
    private static ClientDao dao;

    @BeforeClass
    public static void beforeClass() throws Exception {
        stockServer.start();
        MongoClient client = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = client.getDatabase("market");
        dao = new MongoClientDao(
                database.getCollection("users"),
                database.getCollection("usersStocks"));
    }

    @Before
    public void before() throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8080/addCompany?id=0&name=TESTCOMPANY&stockPrice=10&amount=100000"))
                .GET()
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        Assert.assertEquals("Inserted", response.body());
    }

    @After
    public void after() throws URISyntaxException, IOException, InterruptedException {
        Assert.assertEquals("Removed", performHTTPRequest("http://localhost:8080/deleteCompany?id=0"));
        dao.removeUser(0).toBlocking().first();
        dao.deleteUserStocks(0, 0).toBlocking().first();
    }

    @Test
    public void successfulPurchase() {
        PurchaseResult purchaseResult = dao.addUser(new User(0, 10000000))
                .flatMap(res -> dao.buyStocks(0, 0, 10)).toBlocking().single();
        Assert.assertTrue(purchaseResult.isHasBought());
        Assert.assertEquals(100, purchaseResult.getSpent(), 0.00001);
    }

    @Test
    public void notEnoughMoneyPurchase() {
        PurchaseResult purchaseResult = dao.addUser(new User(0, 10))
                .flatMap(res -> dao.buyStocks(0, 0, 10)).toBlocking().single();
        Assert.assertFalse(purchaseResult.isHasBought());
        Assert.assertEquals(0, purchaseResult.getSpent(), 0.00001);
        Assert.assertEquals("Not enough money", purchaseResult.getVerdict());
    }

    @Test
    public void notEnoughStocksPurchase() {
        PurchaseResult purchaseResult = dao.addUser(new User(0, 1000000000))
                .flatMap(res -> dao.buyStocks(0, 0, 10000000)).toBlocking().single();
        Assert.assertFalse(purchaseResult.isHasBought());
        Assert.assertEquals(0, purchaseResult.getSpent(), 0.00001);
        Assert.assertEquals("Not enough stocks on market", purchaseResult.getVerdict());
    }

    @Test
    public void sellTest() {
        SellResult sellResult = dao.addUser(new User(0, 1000))
                .flatMap(res -> dao.buyStocks(0, 0, 10))
                .flatMap(res -> dao.sellStocks(0, 0, 3)).toBlocking().single();
        Assert.assertEquals(30, sellResult.getEarned(), 0.00001);
        UserStocks userStocks = dao.getStocks(0).toBlocking().single();
        Assert.assertEquals(7, userStocks.getStocksBought());
    }

    @Test
    public void sellFailedTest() {
        SellResult sellResult = dao.addUser(new User(0, 1000))
                .flatMap(res -> dao.buyStocks(0, 0, 10))
                .flatMap(res -> dao.sellStocks(0, 0, 13)).toBlocking().first();
        Assert.assertEquals(0, sellResult.getEarned(), 0.00001);
        UserStocks userStocks = dao.getStocks(0).toBlocking().single();
        Assert.assertEquals(10, userStocks.getStocksBought());
    }

    @Test
    public void incomeTest() throws URISyntaxException, IOException, InterruptedException {
        PurchaseResult purchaseResult = dao.addUser(new User(0, 1000))
                .flatMap(res -> dao.buyStocks(0, 0, 10)).toBlocking().single();
        Assert.assertEquals(100, purchaseResult.getSpent(), 0.00001);
        performHTTPRequest("http://localhost:8080/setStockPrice?id=0&stockPrice=100");
        SellResult sellResult = dao.sellStocks(0, 0, 10).toBlocking().single();
        Assert.assertEquals(1000, sellResult.getEarned(), 0.00001);
    }

    @Test
    public void wholeTest() throws URISyntaxException, IOException, InterruptedException {
        double whole = dao.addUser(new User(0, 1000))
                .flatMap(res -> dao.buyStocks(0, 0, 10))
                .flatMap(res -> dao.getWholeMoney(0)).toBlocking().single();
        Assert.assertEquals(100, whole, 0.00001);
        performHTTPRequest("http://localhost:8080/setStockPrice?id=0&stockPrice=100");
        whole = dao.getWholeMoney(0).toBlocking().single();
        Assert.assertEquals(1000, whole, 0.00001);
    }

    @Test
    public void owningsTest() throws URISyntaxException, IOException, InterruptedException {
        List<UserStocks> userStocksList = dao.addUser(new User(0, 1000))
                .flatMap(res -> dao.buyStocks(0, 0, 10))
                .flatMap(res -> dao.getStocks(0)).toList().toBlocking().single();
        Assert.assertEquals(1, userStocksList.size());
        UserStocks userStocks = userStocksList.get(0);
        Assert.assertEquals(10, userStocks.getStocksBought());
        Assert.assertEquals(10, userStocks.getPrice(), 0.00001);
        performHTTPRequest("http://localhost:8080/setStockPrice?id=0&stockPrice=100");
        userStocksList = dao.sellStocks(0, 0, 3)
                .flatMap(res -> dao.getStocks(0)).toList().toBlocking().single();
        Assert.assertEquals(1, userStocksList.size());
        userStocks = userStocksList.get(0);
        Assert.assertEquals(7, userStocks.getStocksBought());
        Assert.assertEquals(100, userStocks.getPrice(), 0.00001);

    }



    private String performHTTPRequest(String url) throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .GET()
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

}

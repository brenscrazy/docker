import io.reactivex.netty.protocol.http.server.HttpServer;
import server.NettyServer;

public class Main {

    public static void main(String[] args) {
        NettyServer server = new NettyServer();
        HttpServer
                .newServer(8081)
                .start((req, resp) -> resp.writeString(server.handleRequest(req).onErrorReturn(
                        e -> ("An error occurred: " + e.getMessage()))))
                .awaitShutdown();
    }

}

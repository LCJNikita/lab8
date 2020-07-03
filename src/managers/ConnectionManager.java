package managers;

import client.CommandParser;
import client.commands.*;
import server.Response;
import server.database.User;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.HashSet;

public class ConnectionManager {

    private static ConnectionManager instance;
    private static SocketChannel socketChannel;
    private static InetSocketAddress address;
    private static User user;
    private static CommandParser parser;

    private ConnectionManager() {}

    public static ConnectionManager getInstance() {
        if (instance == null) {
            throw new NullPointerException("Instance doesn't exist. First call create(address, port) to create object");
        }
        return instance;
    }

    public static void create(String serverAddress, int port) {
        instance = new ConnectionManager();
        address = new InetSocketAddress(serverAddress, port);
        parser = new CommandParser(new HashSet<>(Arrays.asList("help",  "login", "register", "info", "show", "add",
                    "update", "remove_by_id", "clear", "execute_script", "exit", "add_if_max", "add_if_min",
                    "history", "max_by_oscars_count", "filter_less_than_mpaa_rating",
                    "print_field_descending_oscars_count")), null);
    }

    public Response execute(String commandStr) throws IOException, ClassNotFoundException {
        socketChannel = SocketChannel.open(address);
        Command c = parser.parse(commandStr);
        c.setUser(user);
        return c.executeAndReturn(socketChannel);
    }

    public static User getUser() { return user; }

    public static void setUser(User user) { ConnectionManager.user = user; }

    public static CommandParser getParser() { return parser; }
}
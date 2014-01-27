package net.kkolyan.jhole2.utils;

public final class Address {
    private String host;
    private int port;

    public Address(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return host+":"+port;
    }

    public static Address parseAddress(String address) {
        int sep = address.lastIndexOf(":");
        String host = address.substring(0, sep);
        int port = Integer.parseInt(address.substring(sep + 1));
        return new Address(host, port);
    }
}

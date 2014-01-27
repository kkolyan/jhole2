package net.kkolyan.jhole2.utils;


import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
* @author <a href="mailto:nplekhanov86@gmail.com">nplekhanov</a>
*/
public class HttpRequest {
    private final String method;
    private final String address;
    private final String protocol;
    private final Map<String,String> headers;

    HttpRequest(String method, String address, String protocol, Map<String, String> headers) {
        this.method = method;
        this.address = address;
        this.protocol = protocol;
        this.headers = headers;
    }

    public static HttpRequest parseRequest(LineReader reader) throws IOException {
        String greeting = reader.readLine().trim();
        String[] parts = greeting.split("\\s");
        if (parts.length != 3) {
            throw new IllegalStateException("invalid greeting: "+greeting);
        }
        Map<String,String> headers = new LinkedHashMap<String, String>();
        while (true) {
            String header = reader.readLine().trim();
            if (header.isEmpty()) {
                break;
            }
            String[] kv = header.split(":",2);
            headers.put(kv[0].trim(),kv[1].trim());
        }
        return new HttpRequest(parts[0], parts[1], parts[2], headers);
    }

    public String getMethod() {
        return method;
    }

    public String getAddress() {
        return address;
    }

    public String getProtocol() {
        return protocol;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(method).append(" ").append(address).append(" ").append(protocol).append("\n");
        for (Map.Entry<String, String> header: headers.entrySet()) {
            sb.append(header.getKey()).append(": ").append(header.getValue()).append("\n");
        }
        sb.append("\n");
        return sb.toString();
    }
}

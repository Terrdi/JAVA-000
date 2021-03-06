package org.geektime.java.common;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import org.apache.http.ProtocolVersion;
import org.geektime.java.util.AddressUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Terrdi
 * @description 请求包装类
 * @date 2020/10/27
 */
public class Request<T extends Serializable> {
    public Request(InetAddress address, Protocol protocol) {
        this.address = address;
        this.protocol = protocol;
    }

    public Request(String address, String protocol) throws UnknownHostException {
        this(AddressUtils.resolve(address), Protocol.valueOf(protocol));
    }

    public Request(HttpRequest request) throws UnknownHostException {
        this(null, Protocol.resolve(request.protocolVersion()));
        String[] address = request.headers().get("Host").split(":");
        if (address.length > 1) {
            this.port = Integer.valueOf(address[1]);
        }
        this.address = AddressUtils.resolve(address[0]);
        this.setMethod(HttpMethod.resolve(request.method()));
        this.setUri(request.uri());
        request.headers().forEach(header -> this.headers.put(header.getKey(), header.getValue()));
    }

    public Request(String url, HttpMethod method) throws UnknownHostException {
        this.setMethod(method);
        URI uri = URI.create(url);
        this.protocol = Protocol.resolve(uri.getScheme()).orElseThrow(() -> new IllegalArgumentException("不存在的协议名称 " + uri.getScheme()));
        this.setPort(uri.getPort());
        this.address = AddressUtils.resolve(uri.getHost());
        this.setUri(uri.getPath());
    }

    public Request(String url) throws UnknownHostException {
        this(url, HttpMethod.GET);
    }


    /**
     * IP地址
     */
    private InetAddress address;

    /**
     * 数据
     */
    private T data;

    /**
     * 端口
     */
    private int port;

    /**
     * 协议
     */
    private Protocol protocol;

    /**
     * uri
     */
    private String uri;

    /**
     * 只针对于HTTP的请求方法
     */
    private HttpMethod method;

    /**
     * 请求头
     */
    private Map<String, String> headers = new HashMap<>();

    public String getUri() {
        StringBuilder sb = new StringBuilder();
        sb.append(Objects.requireNonNull(this.getProtocol(), "Protocol Cannot be null.").getName()).append("://");
        sb.append(Optional.ofNullable(this.getAddress()).orElseGet(() -> {
            try {
                return InetAddress.getLocalHost();
            } catch (UnknownHostException e) {
                try {
                    return InetAddress.getByName("localhost");
                } catch (UnknownHostException unknownHostException) {
                    throw new RuntimeException(unknownHostException.getMessage(), unknownHostException);
                }
            }
        }).getHostName());
        if (this.port > 0 && this.port <= 65536) {
            sb.append(':').append(this.port);
        }
        if (Objects.nonNull(this.uri) && !this.uri.isEmpty()){
            if (!this.uri.startsWith("/")) {
                sb.append("/");
            }
            sb.append(this.uri);
        }

        return sb.toString();
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public byte[] serializeData() {
        if (Objects.isNull(this.getData())) {
            return new byte[0];
        }
        try (ByteArrayOutputStream bao = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bao)) {
            oos.writeObject(this.getData());
            return bao.toByteArray();
        } catch (IOException e) {
            throw new IllegalArgumentException("无法序列化的对象 " + this.getData());
        }
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public HttpMethod getMethod() {
        if (Objects.nonNull(protocol) && (protocol == Protocol.HTTP || protocol == Protocol.HTTPS ||
                protocol == Protocol.HTTP_2_0 || protocol == Protocol.HTTP_1_0 || protocol == Protocol.HTTP_1_1)) {
            return Objects.requireNonNull(this.method, "不存在的方法");
        }
        throw new IllegalArgumentException("只有 HTTP/HTTPS 协议才有方法");
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public Request<T> addHeader(String name, String value) {
        headers.put(name, value);
        return this;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * 请求的协议
     */
    public enum Protocol {
        /**
         * HTTP 默认为 http1.1
         */
        HTTP("HTTP", 80, 1, 1),
        /**
         * HTTP 1.0
         */
        HTTP_1_0("HTTP", 80, 1, 0),
        /**
         * HTTP 1.1
         */
        HTTP_1_1("HTTP", 80, 1, 1),
        /**
         * HTTP 1.1
         */
        HTTP_2_0("HTTP", 80, 2, 0),
        /**
         * HTTPS
         */
        HTTPS("HTTPS", 443, 1, 0);

        /**
         * 默认端口
         */
        private int defaultPort;

        /**
         * 主版本号
         */
        private int major;

        /**
         * 次版本号
         */
        private int minor;

        /**
         * 协议名称
         */
        private String name;

        Protocol(String name, int defaultPort, int major, int minor) {
            this.name = name;
            this.defaultPort = defaultPort;
            this.major = major;
            this.minor = minor;
        }

        public static Optional<Protocol> resolve(String pro) {
            String[] names = pro.split("[\\W\\_]+");
            Optional<Protocol> result = Optional.empty();
            for (Protocol protocol : values()) {
                boolean flag = true;
                switch (names.length) {
                    case 3:
                        flag = flag && Integer.parseInt(names[2]) == protocol.getMinor();
                    case 2:
                        flag = flag && Integer.parseInt(names[1]) == protocol.getMajor();
                    case 1:
                        flag = flag && protocol.getName().equalsIgnoreCase(names[0]);
                        break;
                    default:
                        throw new EnumConstantNotPresentException(Protocol.class, pro);
                }

                if (flag) {
                    result = Optional.of(protocol);
                    break;
                }
            }
            return result;
        }

        public static Protocol resolve(HttpVersion protocolVersion) {
            for (Protocol protocol : values()) {
                if (protocol.getName().equalsIgnoreCase(protocolVersion.protocolName()) &&
                    protocol.major == protocolVersion.majorVersion() && protocol.minor == protocolVersion.minorVersion()) {
                    return protocol;
                }
            }
            throw new EnumConstantNotPresentException(Protocol.class, protocolVersion.toString());
        }

        public static Protocol resolve(ProtocolVersion protocolVersion) {
            for (Protocol protocol : values()) {
                if (protocol.getName().equalsIgnoreCase(protocolVersion.getProtocol()) &&
                        protocol.major == protocolVersion.getMajor() && protocol.minor == protocolVersion.getMinor()) {
                    return protocol;
                }
            }
            throw new EnumConstantNotPresentException(Protocol.class, protocolVersion.toString());
        }

        public int getDefaultPort() {
            return defaultPort;
        }

        public String getName() {
            return name;
        }

        public int getMajor() {
            return major;
        }

        public int getMinor() {
            return minor;
        }

        @Override
        public String toString() {
            if (this.name.equalsIgnoreCase("HTTP")) {
                return String.format("%s/%s.%s", this.name, this.major, this.minor);
            } else {
                return super.toString();
            }
        }
    }

    /**
     * 请求的方法
     */
    public enum HttpMethod {
        GET("GET"),
        POST("POST"),
        DELETE("DELETE"),
        PUT("PUT"),
        PATCH("PATCH"),
        OPTIONS("OPTIONS"),
        HEAD("HEAD"),
        INFO("INFO");

        private String method;

        HttpMethod(String method) {
            this.method = method;
        }

        public static HttpMethod resolve(io.netty.handler.codec.http.HttpMethod method) {
            for (HttpMethod m : values()) {
                if (m.method.equalsIgnoreCase(method.name())) {
                    return m;
                }
            }
            throw new EnumConstantNotPresentException(HttpMethod.class, method.name());
        }

        public String getMethod() {
            return method;
        }
    }
}

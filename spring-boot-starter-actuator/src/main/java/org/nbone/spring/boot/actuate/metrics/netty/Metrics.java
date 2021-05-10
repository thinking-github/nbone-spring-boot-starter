package org.nbone.spring.boot.actuate.metrics.netty;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * @author thinking
 * @version 1.0
 * @since 2019-09-28
 * <p>
 * see reactor.netty.Metrics
 */
public class Metrics {
    // Names
    /**
     * Name prefix that will be used for the HTTP server's metrics
     * registered in Micrometer's global registry
     */
    public static final String HTTP_SERVER_PREFIX = "netty.http.server";

    /**
     * Name prefix that will be used for the HTTP client's metrics
     * registered in Micrometer's global registry
     */
    public static final String HTTP_CLIENT_PREFIX = "netty.http.client";

    /**
     * Name prefix that will be used for the TCP server's metrics
     * registered in Micrometer's global registry
     */
    public static final String TCP_SERVER_PREFIX = "netty.tcp.server";

    /**
     * Name prefix that will be used for the TCP client's metrics
     * registered in Micrometer's global registry
     */
    public static final String TCP_CLIENT_PREFIX = "netty.tcp.client";

    /**
     * Name prefix that will be used for the UDP server's metrics
     * registered in Micrometer's global registry
     */
    public static final String UDP_SERVER_PREFIX = "netty.udp.server";

    /**
     * Name prefix that will be used for the UDP client's metrics
     * registered in Micrometer's global registry
     */
    public static final String UDP_CLIENT_PREFIX = "netty.udp.client";

    /**
     * Name prefix that will be used for the PooledConnectionProvider's metrics
     * registered in Micrometer's global registry
     */
    public static final String CONNECTION_PROVIDER_PREFIX = "netty.connection.provider";

    /**
     * Name prefix that will be used for the ByteBufAllocator's metrics
     * registered in Micrometer's global registry
     */
    public static final String BYTE_BUF_ALLOCATOR_PREFIX = "netty.bytebuf.allocator";


    // Metrics
    /**
     * Amount of the data received, in bytes
     */
    public static final String DATA_RECEIVED = ".data.received";

    /**
     * Amount of the data sent, in bytes
     */
    public static final String DATA_SENT = ".data.sent";

    /**
     * Number of errors that occurred
     */
    public static final String ERRORS = ".errors";

    /**
     * Time spent for TLS handshake
     */
    public static final String TLS_HANDSHAKE_TIME = ".tls.handshake.time";

    /**
     * Time spent for connecting to the remote address
     */
    public static final String CONNECT_TIME = ".connect.time";

    /**
     * Time spent in consuming incoming data
     */
    public static final String DATA_RECEIVED_TIME = ".data.received.time";

    /**
     * Time spent in sending outgoing data
     */
    public static final String DATA_SENT_TIME = ".data.sent.time";

    /**
     * Total time for the request/response
     */
    public static final String RESPONSE_TIME = ".response.time";


    // AddressResolverGroup Metrics
    /**
     * Time spent for resolving the address
     */
    public static final String ADDRESS_RESOLVER = ".address.resolver";


    // PooledConnectionProvider Metrics
    /**
     * The number of all connections, active or idle
     */
    public static final String TOTAL_CONNECTIONS = ".total.connections";

    /**
     * The number of the connections that have been successfully acquired and are in active use
     */
    public static final String ACTIVE_CONNECTIONS = ".active.connections";

    /**
     * The number of the idle connections
     */
    public static final String IDLE_CONNECTIONS = ".idle.connections";

    /**
     * The number of requests that are waiting for a connection
     */
    public static final String PENDING_CONNECTIONS = ".pending.connections";


    // ByteBufAllocator Metrics
    /**
     * The number of the bytes of the heap memory
     */
    public static final String USED_HEAP_MEMORY = ".used.heap.memory";

    /**
     * The number of the bytes of the direct memory
     */
    public static final String USED_DIRECT_MEMORY = ".used.direct.memory";

    /**
     * The number of heap arenas
     */
    public static final String HEAP_ARENAS = ".heap.arenas";

    /**
     * The number of direct arenas
     */
    public static final String DIRECT_ARENAS = ".direct.arenas";

    /**
     * The number of thread local caches
     */
    public static final String THREAD_LOCAL_CACHES = ".threadlocal.caches";

    /**
     * The size of the small cache
     */
    public static final String SMALL_CACHE_SIZE = ".small.cache.size";

    /**
     * The size of the normal cache
     */
    public static final String NORMAL_CACHE_SIZE = ".normal.cache.size";

    /**
     * The chunk size for an arena
     */
    public static final String CHUNK_SIZE = ".chunk.size";


    // Tags
    public static final String REMOTE_ADDRESS = "remote.address";

    public static final String URI = "uri";

    public static final String STATUS = "status";

    public static final String METHOD = "method";

    public static final String ID = "id";

    public static final String NAME = "name";

    public static final String TYPE = "type";

    public static final String SUCCESS = "SUCCESS";

    public static final String ERROR = "ERROR";


    public static String formatSocketAddress(SocketAddress socketAddress) {
        if (socketAddress != null) {
            if (socketAddress instanceof InetSocketAddress) {
                InetSocketAddress address = (InetSocketAddress) socketAddress;
                return address.getHostString() + ":" + address.getPort();
            } else {
                return socketAddress.toString();
            }
        }
        return null;
    }
}

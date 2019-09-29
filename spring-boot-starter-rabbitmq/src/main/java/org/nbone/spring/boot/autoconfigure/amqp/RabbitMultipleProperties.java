package org.nbone.spring.boot.autoconfigure.amqp;

import lombok.Getter;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 支持多个 rabbitmq 配置
 *
 * @author thinking
 * @version 1.0
 * @since 2019-07-26
 */
@ConfigurationProperties(prefix = "spring.rabbitmq")
public class RabbitMultipleProperties {

    /**
     * username:password@host:port/virtual-host  <br/>
     * <li>spring.rabbitmq.multiple.catcat: cut_news:qwe123@10.10.88.17:6673/test <br/>
     * <li>spring.rabbitmq.multiple.catcatAccount: user_account:user_account@10.10.88.17:6673/account <br/>
     */
    private final Map<String, String> multiple = new HashMap<>();
    private String host = "localhost";

    public Map<String, String> getMultiple() {
        return multiple;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Returns the comma-separated addresses or a single address ({@code host:port})
     * created from the configured host and port if no addresses have been set.
     *
     * @return the addresses
     */
    public String determineAddresses(List<Address> addr) {
        if (CollectionUtils.isEmpty(addr)) {
            //return this.host + ":" + this.port;
            return null;
        }
        List<String> addressStrings = new ArrayList<String>();
        for (Address parsedAddress : addr) {
            addressStrings.add(parsedAddress.host + ":" + parsedAddress.port);
        }
        return StringUtils.collectionToCommaDelimitedString(addressStrings);
    }

    protected List<Address> parseAddresses(String addresses) {
        List<Address> parsedAddresses = new ArrayList<Address>();
        for (String address : StringUtils.commaDelimitedListToStringArray(addresses)) {
            parsedAddresses.add(new Address(address));
        }
        return parsedAddresses;
    }


    /**
     * username:password@host:port/virtual-host
     *
     * @see RabbitProperties
     */

    @Getter
    protected static final class Address {

        private static final String PREFIX_AMQP = "amqp://";

        private static final int DEFAULT_PORT = 5672;

        private String host;

        private int port;

        private String username;

        private String password;

        private String virtualHost;

        private Address(String input) {
            input = input.trim();
            input = trimPrefix(input);
            input = parseUsernameAndPassword(input);
            input = parseVirtualHost(input);
            parseHostAndPort(input);
        }

        private String trimPrefix(String input) {
            if (input.startsWith(PREFIX_AMQP)) {
                input = input.substring(PREFIX_AMQP.length());
            }
            return input;
        }

        private String parseUsernameAndPassword(String input) {
            if (input.contains("@")) {
                String[] split = StringUtils.split(input, "@");
                String creds = split[0];
                input = split[1];
                split = StringUtils.split(creds, ":");
                this.username = split[0];
                if (split.length > 0) {
                    this.password = split[1];
                }
            }
            return input;
        }

        private String parseVirtualHost(String input) {
            int hostIndex = input.indexOf("/");
            if (hostIndex >= 0) {
                this.virtualHost = input.substring(hostIndex + 1);
                if (this.virtualHost.isEmpty()) {
                    this.virtualHost = "/";
                }
                input = input.substring(0, hostIndex);
            }
            return input;
        }

        private void parseHostAndPort(String input) {
            int portIndex = input.indexOf(':');
            if (portIndex == -1) {
                this.host = input;
                this.port = DEFAULT_PORT;
            } else {
                this.host = input.substring(0, portIndex);
                this.port = Integer.valueOf(input.substring(portIndex + 1));
            }
        }

    }
}

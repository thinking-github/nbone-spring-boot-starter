package org.nbone.spring.boot.actuate.endpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author thinking
 * @version 1.0
 * @since 2019-09-28
 */
@RestController
@RequestMapping(value = "/nbone", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class ProxyRequestController {

    private static final Logger logger = LoggerFactory.getLogger(ProxyRequestController.class);

    @Resource
    private RestTemplate restTemplate;

    private ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * proxy executor cluster node status query
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "cluster/status", method = {RequestMethod.POST, RequestMethod.GET})
    public List<Map<String, Object>> getRedisPool(HttpServletRequest request) {
        String nodes = request.getParameter("nodes");
        String path = request.getParameter("path");

        return getRedisPool(nodes, path);
    }

    @RequestMapping(value = "cluster/status", method = {RequestMethod.POST, RequestMethod.GET}, consumes = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, Object>> getRedisPoolRequestBody(@RequestBody Map<String, String> requestMap) {
        String nodes = requestMap.get("nodes");
        String path = requestMap.get("path");

        return getRedisPool(nodes, path);
    }

    private List<Map<String, Object>> getRedisPool(String nodes, String path) {
        if (StringUtils.isEmpty(nodes)) {
            return Collections.EMPTY_LIST;
        }
        if (path == null) {
            path = "";
        }
        String[] nodeArray = StringUtils.commaDelimitedListToStringArray(nodes);

        if (nodeArray.length == 1) {
            Map<String, Object> status = restTemplate.getForObject(nodeArray[0] + path, Map.class);
            return Collections.singletonList(status);
        }

        // 并行获取数据
        List results = new ArrayList(nodeArray.length);
        CountDownLatch countDownLatch = new CountDownLatch(nodeArray.length);
        for (int i = 0; i < nodeArray.length; i++) {
            String node = nodeArray[i];
            String finalPath = path;
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Map<String, Object> status = restTemplate.getForObject(node + finalPath, Map.class);
                        results.add(status);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                        results.add(Collections.singletonMap("error", e.getClass() + " : " + e.getMessage()));
                    } finally {
                        countDownLatch.countDown();
                    }
                }
            });
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
        return results;
    }


}

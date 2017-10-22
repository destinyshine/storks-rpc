package io.destinyshine.storks.registry.consul.client.rest;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import io.destinyshine.storks.http.NettyHttpClient;
import io.destinyshine.storks.registry.consul.ConsulResponse;
import io.destinyshine.storks.registry.consul.ConsulService;
import io.destinyshine.storks.registry.consul.client.ConsulAccessor;
import io.destinyshine.storks.registry.consul.client.ConsulClient;
import io.destinyshine.storks.registry.consul.client.rest.agent.AgentCheck;
import io.destinyshine.storks.registry.consul.client.rest.agent.AgentService;
import io.destinyshine.storks.utils.CollectionUtils;
import io.destinyshine.storks.utils.json.JsonGenerator;
import io.destinyshine.storks.utils.json.JsonParser;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpHeaders;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import static io.destinyshine.storks.utils.functions.Functions.emptyConsumer;

/**
 * Created by liujianyu.ljy on 17/9/4.
 *
 * X-Consul-Index:6
 * X-Consul-Knownleader:true
 * X-Consul-Lastcontact
 *
 * @author liujianyu.ljy
 * @date 2017/09/04
 */
@Slf4j
public class RestConsulClient extends ConsulAccessor implements ConsulClient {

    private NettyHttpClient httpClient = new NettyHttpClient();

    public RestConsulClient(String host, int port) {
        super(host, port);
    }

    /**
     * PUT /agent/check/pass/:check_id
     *
     * @param serviceId
     */
    @Override
    public CompletableFuture<Void> checkPassService(String serviceId) {
        String path = path("/agent/check/pass/service:" + serviceId);
        logger.info("checkPass, path={}, serviceId={}", path, serviceId);
        return httpClient.put(path, Unpooled.EMPTY_BUFFER)
            .thenAccept(emptyConsumer());
    }

    /**
     * PUT /agent/check/fail/:check_id
     *
     * @param serviceId
     */
    @Override
    public CompletableFuture<Void> checkFailService(String serviceId) {
        String path = path("/agent/check/fail/service:" + serviceId);
        logger.info("checkFail, path={}, serviceId={}", path, serviceId);
        return httpClient.put(path, Unpooled.EMPTY_BUFFER)
            .thenAccept(emptyConsumer());
    }

    /**
     * PUT /agent/service/register
     *
     * @param service
     * @return
     */
    @Override
    public CompletableFuture<Void> registerService(ConsulService service) {
        String path = path("/agent/service/register");
        AgentService payload = toAgentService(service);
        ByteBuf byteBuf = Unpooled.wrappedBuffer(JsonGenerator.generate(payload).getBytes());
        return httpClient.put(path, byteBuf)
            .thenAccept(emptyConsumer());
    }

    /**
     * PUT /agent/service/deregister/{serviceId}
     *
     * @param serviceId
     * @return
     */
    @Override
    public CompletableFuture<Void> unregisterService(String serviceId) {
        String path = path("/agent/service/deregister/" + serviceId);
        return httpClient.put(path, Unpooled.EMPTY_BUFFER).thenAccept(emptyConsumer());
    }

    @Override
    public CompletableFuture<ConsulResponse<List<ConsulService>>> lookupHealthService(String serviceName,
                                                                                      long lastConsulIndex) {
        String path = path("/health/service/" + serviceName + "?passing=true");
        return httpClient.get(path)
            .thenApply(resp -> {
                ByteBuf body = resp.content();
                List<Map<String, ?>> healthServices = (List<Map<String, ?>>)JsonParser.parse(body.toString(Charset.defaultCharset()));

                HttpHeaders headers = resp.headers();

                Long consulIndex = getLongHeader(headers, "X-Consul-Index");
                Boolean consulKnownLeader = getBooleanHeader(headers, "X-Consul-Knownleader");
                Long consulLastContact = getLongHeader(headers, "X-Consul-Lastcontact");

                if (CollectionUtils.isEmpty(healthServices)) {
                    return null;
                }

                ConsulResponse<List<ConsulService>> response = null;
                List<ConsulService> consulServices = new ArrayList<>(healthServices.size());

                for (Map<String, ?> originService : healthServices) {
                    try {
                        ConsulService newService = toConsulService(originService);
                        consulServices.add(newService);
                    } catch (Exception e) {
                        String serviceId = "null";
                        if (originService.get("Service") != null) {
                            serviceId = (String)((Map)originService.get("Service")).get("Id");
                        }
                        logger.error(
                            "convert consul service fail. org consulservice:"
                                + serviceId, e);
                    }
                }

                if (!consulServices.isEmpty()) {
                    response = new ConsulResponse<>();
                    response.setValue(consulServices);
                    response.setConsulIndex(consulIndex);
                    response.setConsulKnownLeader(consulKnownLeader);
                    response.setConsulLastContact(consulLastContact);
                }

                return response;
            });

    }

    private String path(String path) {
        return "http://" + host + ":" + port + "/v1" + path;
    }

    private Long getLongHeader(HttpHeaders httpHeaders, String headerName) {
        String headerValue = httpHeaders.get(headerName);
        if (StringUtils.isBlank(headerValue)) {
            return null;
        } else {
            return Long.valueOf(headerValue);
        }
    }

    private Boolean getBooleanHeader(HttpHeaders httpHeaders, String headerName) {
        String headerValue = httpHeaders.get(headerName);
        if (StringUtils.isBlank(headerValue)) {
            return null;
        } else {
            return Boolean.valueOf(headerValue);
        }
    }

    private AgentService toAgentService(ConsulService service) {
        AgentService agentService = new AgentService();
        agentService.setAddress(service.getAddress());
        agentService.setId(service.getId());
        agentService.setName(service.getName());
        agentService.setPort(service.getPort());
        agentService.setTags(service.getTags());
        AgentCheck check = new AgentCheck();
        if (StringUtils.isNotBlank(service.getCheckHttp())) {
            check.setHttp(service.getCheckHttp());
        }
        if (StringUtils.isNotBlank(service.getCheckTcp())) {
            check.setTcp(service.getCheckTcp());
            check.setInterval(service.getCheckInterval());
        }
        if (Objects.nonNull(service.getCheckTtl())) {
            check.setTtl(service.getCheckTtl() + "s");
        }
        agentService.setCheck(check);
        return agentService;
    }

    private ConsulService toConsulService(Map<String, ?> healthService) {
        ConsulService service = new ConsulService();
        Map<String, ?> origin = (Map<String, ?>)healthService.get("Service");
        service.setAddress((String)origin.get("Address"));
        service.setId((String)origin.get("Id"));
        service.setName((String)origin.get("Service"));
        service.setPort(((Number)origin.get("Port")).intValue());
        service.setTags((List<String>)origin.get("Tags"));
        return service;
    }

}

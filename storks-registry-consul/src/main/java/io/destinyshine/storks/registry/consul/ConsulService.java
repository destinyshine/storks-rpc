package io.destinyshine.storks.registry.consul;

import java.util.List;

import io.destinyshine.storks.core.ServiceKey;
import lombok.Data;

@Data
public class ConsulService {

	private String id;

	private String name;

	private List<String> tags;

	private String address;

	private Integer port;
	
	private String checkHttp;
	private String checkTcp;
	private String checkInterval;
	private Integer checkTtl;

	private transient ServiceKey serviceKey;
}

package com.adt.devblog.hazelcast.configuration;

import com.hazelcast.config.*;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class HazelcastConfiguration {

  private static final Logger LOGGER = LoggerFactory.getLogger(HazelcastConfiguration.class);

  private static final String HAZLECAST_INSTANCE = "REST_HZ_INSTANCE";
  public static final String TASK_MAP = "REST_TASK_MAP";
  public static final String TASK_RESULT_MAP = "REST_TASK_RESULT_MAP";
  public static final String REST_EXECUTOR_SERVICE = "REST_EXECUTOR_SERVICE";

  @Value("${local.server.port:8080}")
  private int port;

  @Bean
  @Primary
  public HazelcastInstance hazelcastInstance() {

    // add port to the hc instance name
    String instanceIdentifier = HAZLECAST_INSTANCE + "-" + port;

    Config config = new Config();
    config.setInstanceName(instanceIdentifier)
        .addMapConfig(
            new MapConfig()
                .setName(TASK_MAP)
                .setMaxSizeConfig(new MaxSizeConfig(200, MaxSizeConfig.MaxSizePolicy.FREE_HEAP_SIZE))
                .setEvictionPolicy(EvictionPolicy.LRU)
                .setTimeToLiveSeconds(-1))
        .addMapConfig(
            new MapConfig()
                .setName(TASK_RESULT_MAP)
                .setMaxSizeConfig(new MaxSizeConfig(200, MaxSizeConfig.MaxSizePolicy.FREE_HEAP_SIZE))
                .setEvictionPolicy(EvictionPolicy.LRU)
                .setTimeToLiveSeconds(-1))
        .addExecutorConfig(
            new ExecutorConfig()
                .setName(REST_EXECUTOR_SERVICE)
                .setPoolSize(5)
                .setQueueCapacity(50)
                .setStatisticsEnabled(true)
        );

    // force to use only local network
    config.getNetworkConfig().setPortAutoIncrement(true);
    config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
    config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(true);
    config.getNetworkConfig().getJoin().getTcpIpConfig().addMember("127.0.0.1");
    config.getNetworkConfig().getInterfaces().addInterface("127.0.0.1");
    config.getNetworkConfig().getInterfaces().setEnabled(true);

    LOGGER.info("Hazelcast InstanceIdentifier is: {}", instanceIdentifier);

    return Hazelcast.getOrCreateHazelcastInstance(config);
  }

}

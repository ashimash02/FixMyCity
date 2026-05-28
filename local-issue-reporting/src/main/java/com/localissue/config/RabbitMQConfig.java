package com.localissue.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

@Configuration
public class RabbitMQConfig {

    // ── Names ────────────────────────────────────────────────────────────────

    public static final String ISSUES_EXCHANGE    = "civic.issues";
    // Separate direct exchange for DLQ — avoids routing conflicts with the main topic exchange
    public static final String ISSUES_DLX         = "civic.issues.dlx";

    public static final String AI_SUMMARY_QUEUE   = "civic.ai.summary";
    public static final String AI_SUMMARY_DLQ     = "civic.ai.summary.dlq";

    public static final String ROUTING_KEY_CREATED  = "issue.created";
    public static final String ROUTING_KEY_UPDATED  = "issue.updated";
    public static final String ROUTING_KEY_RESOLVED = "issue.resolved";
    // Fixed routing key used by RepublishMessageRecoverer when sending to DLX
    public static final String ROUTING_KEY_DEAD     = "issue.dead";

    // ── Retry settings ───────────────────────────────────────────────────────

    private static final int    MAX_ATTEMPTS        = 3;
    private static final long   INITIAL_INTERVAL_MS = 1_000L;
    private static final double MULTIPLIER          = 2.0;
    private static final long   MAX_INTERVAL_MS     = 10_000L;

    // ── Exchanges ────────────────────────────────────────────────────────────

    /** Topic exchange: routing key pattern issue.# captures all current and future issue subtypes. */
    @Bean
    public TopicExchange issuesExchange() {
        return ExchangeBuilder.topicExchange(ISSUES_EXCHANGE).durable(true).build();
    }

    /** Direct exchange backing the DLQ — simpler routing than topic for dead-letter delivery. */
    @Bean
    public DirectExchange issuesDlx() {
        return ExchangeBuilder.directExchange(ISSUES_DLX).durable(true).build();
    }

    // ── Queues ───────────────────────────────────────────────────────────────

    /**
     * Main AI processing queue.
     * x-dead-letter-exchange ensures both consumer nacks and retry exhaustion
     * route to the DLQ rather than silently dropping messages.
     */
    @Bean
    public Queue aiSummaryQueue() {
        return QueueBuilder.durable(AI_SUMMARY_QUEUE)
                .withArgument("x-dead-letter-exchange", ISSUES_DLX)
                .withArgument("x-dead-letter-routing-key", ROUTING_KEY_DEAD)
                .build();
    }

    /** Dead letter queue — holds poisoned messages permanently for manual inspection or replay. */
    @Bean
    public Queue aiSummaryDlq() {
        return QueueBuilder.durable(AI_SUMMARY_DLQ).build();
    }

    // ── Bindings ─────────────────────────────────────────────────────────────

    @Bean
    public Binding aiSummaryBinding(Queue aiSummaryQueue, TopicExchange issuesExchange) {
        return BindingBuilder.bind(aiSummaryQueue).to(issuesExchange).with("issue.#");
    }

    @Bean
    public Binding aiSummaryDlqBinding(Queue aiSummaryDlq, DirectExchange issuesDlx) {
        return BindingBuilder.bind(aiSummaryDlq).to(issuesDlx).with(ROUTING_KEY_DEAD);
    }

    // ── Serialisation ────────────────────────────────────────────────────────

    /**
     * Use Spring Boot's auto-configured ObjectMapper so that JavaTimeModule,
     * @JsonTypeInfo, and other registered modules are already wired in.
     */
    @Bean
    public Jackson2JsonMessageConverter messageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }

    // ── Retry & dead-letter recovery ─────────────────────────────────────────

    /**
     * Stateless retry: 3 attempts with exponential backoff (1s → 2s → 4s).
     * After exhausting retries, RepublishMessageRecoverer forwards the message
     * to the DLX so it lands in the DLQ rather than being silently dropped.
     */
    @Bean
    public RetryOperationsInterceptor retryInterceptor(RabbitTemplate rabbitTemplate) {
        return RetryInterceptorBuilder.stateless()
                .maxAttempts(MAX_ATTEMPTS)
                .backOffOptions(INITIAL_INTERVAL_MS, MULTIPLIER, MAX_INTERVAL_MS)
                .recoverer(new RepublishMessageRecoverer(rabbitTemplate, ISSUES_DLX, ROUTING_KEY_DEAD))
                .build();
    }

    /**
     * Override the default listener container factory to attach the retry interceptor
     * and JSON converter. Named "rabbitListenerContainerFactory" to replace Spring Boot's
     * auto-configured default so @RabbitListener picks it up automatically.
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter messageConverter,
            RetryOperationsInterceptor retryInterceptor) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setAdviceChain(retryInterceptor);
        return factory;
    }
}

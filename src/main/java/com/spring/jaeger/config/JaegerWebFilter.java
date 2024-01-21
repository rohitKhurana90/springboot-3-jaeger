package com.spring.jaeger.config;

import io.opentracing.Scope;
import io.opentracing.Span;

import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;

import io.opentracing.tag.Tags;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import io.opentracing.propagation.TextMapAdapter;


@Component
@RequestMapping("/")
public class JaegerWebFilter implements Filter {

    @Value("${application.name}")
    private String applicationName;

    @Autowired
    private Tracer tracer;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization logic, if needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String helloTo = "Surbhi";
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        Map<String, String> headers = Collections.list(httpRequest.getHeaderNames())
                .stream()
                .collect(Collectors.toMap(h -> h, httpRequest::getHeader));
        Tracer.SpanBuilder spanBuilder;
        try {
            SpanContext parentSpan = tracer.extract(Format.Builtin.HTTP_HEADERS, new TextMapAdapter(headers));
            if (parentSpan == null) {
                spanBuilder = tracer.buildSpan("new-span");
            } else {
                spanBuilder = tracer.buildSpan("new-span").asChildOf(parentSpan);
            }
        } catch (IllegalArgumentException e) {
            spanBuilder = tracer.buildSpan("new-span");
        }
        Span span = spanBuilder.withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER).start();
        // Injecting span context into response headers
        tracer.inject(span.context(), Format.Builtin.HTTP_HEADERS, new TextMapAdapter(headers));
        try (Scope scope = tracer.scopeManager().activate(span)) {
            String helloStr = String.format("Hello, %s!", helloTo);
            span.log(Map.of("event", "string-format", "value", helloStr));
            chain.doFilter(request, response);
        } finally {
            span.finish();
        }
    }


    @Override
    public void destroy() {
        // Cleanup logic, if needed
    }

    @Nullable
    private Map<String, Object> spanLogDecorator(ServletRequest request, Span span) {

        final Object handler = request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE);
        if (handler == null) {
            return null;
        }

        final Map<String, Object> logs = new HashMap<>(4);
        logs.put("event", "handle");

        final Object pattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        final String patternAsString = pattern == null ? null : pattern.toString();
        if (pattern != null) {
            logs.put("handler", patternAsString);
        }

        if (handler instanceof HandlerMethod handlerMethod) {

            final String methodName = handlerMethod.getMethod().getName();
            logs.put("handler.method_name", handlerMethod.getMethod().getName());
            span.setOperationName(methodName);
            logs.put("handler.class_simple_name", handlerMethod.getBeanType().getSimpleName());
        } else {
            if (pattern != null) {
                span.setOperationName(patternAsString);
            }
            logs.put("handler.class_simple_name", handler.getClass().getSimpleName());
        }

        Tags.COMPONENT.set(span, "java-spring-boot");
        // Adjust the following lines according to ServletRequest methods
        // Tags.HTTP_METHOD.set(span, request.getMethod().name());
        // Tags.HTTP_URL.set(span, request.getRequestURL().toString());
        // Tags.PEER_HOSTNAME.set(span, request.getRemoteHost());
        // Tags.PEER_PORT.set(span, request.getRemotePort());
        // Add handling for remote address

        return logs;
    }
}

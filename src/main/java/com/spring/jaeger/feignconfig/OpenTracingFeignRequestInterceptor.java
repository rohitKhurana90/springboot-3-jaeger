package com.spring.jaeger.feignconfig;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import io.opentracing.tag.Tags;
import org.springframework.http.HttpHeaders;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class OpenTracingFeignRequestInterceptor implements RequestInterceptor {

    private final Tracer tracer;

    public OpenTracingFeignRequestInterceptor(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public void apply(RequestTemplate requestTemplate) {
        String helloTo = "Rohit";
        Span activeSpan = tracer.activeSpan();
        try (Scope scope = tracer.scopeManager().activate(activeSpan)) {
            activeSpan.setTag("hello-to", helloTo);

            Tags.SPAN_KIND.set(activeSpan, Tags.SPAN_KIND_CLIENT);
            Tags.HTTP_METHOD.set(activeSpan, "GET");
            tracer.inject(tracer.activeSpan().context(), Format.Builtin.HTTP_HEADERS, new HttpHeadersTextMap(requestTemplate));
        } finally {
            activeSpan.finish();
        }
    }

    private static class HttpHeadersTextMap implements TextMap {

        private final RequestTemplate requestTemplate;

        public HttpHeadersTextMap(RequestTemplate requestTemplate) {
            this.requestTemplate = requestTemplate;
        }

        @Override
        public Iterator<Map.Entry<String, String>> iterator() {
            throw new UnsupportedOperationException("iterator is write-only");
        }

        @Override
        public void put(String key, String value) {
            requestTemplate.header(key, value);
        }
    }
}

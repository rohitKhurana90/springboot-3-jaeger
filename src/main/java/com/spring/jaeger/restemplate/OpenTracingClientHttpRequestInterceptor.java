package com.spring.jaeger.restemplate;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import io.opentracing.tag.Tags;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class OpenTracingClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    private final Tracer tracer;

    public OpenTracingClientHttpRequestInterceptor(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        // Inject span context into the request headers
        String helloTo = "Rohit";
        Span activeSpan = tracer.activeSpan();
        try (Scope scope = tracer.scopeManager().activate(activeSpan)) {
            activeSpan.setTag("hello-to", helloTo);

            Tags.SPAN_KIND.set(activeSpan, Tags.SPAN_KIND_CLIENT);
            Tags.HTTP_METHOD.set(activeSpan, "GET");

            HttpHeaders httpHeaders = new HttpHeaders();
            tracer.inject(tracer.activeSpan().context(), Format.Builtin.HTTP_HEADERS, new HttpHeadersTextMap(httpHeaders));
            request.getHeaders().addAll(httpHeaders);
            return execution.execute(request, body);
        } finally {
            activeSpan.finish();
        }
    }

    private static class HttpHeadersTextMap implements TextMap {

        private final HttpHeaders headers;

        public HttpHeadersTextMap(HttpHeaders headers) {
            this.headers = headers;
        }

        @Override
        public Iterator<Map.Entry<String, String>> iterator() {
            throw new UnsupportedOperationException("iterator is write-only");
        }

        @Override
        public void put(String key, String value) {
            headers.set(key, value);
        }
    }
}

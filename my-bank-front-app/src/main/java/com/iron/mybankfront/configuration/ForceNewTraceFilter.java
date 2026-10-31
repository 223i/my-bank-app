package com.iron.mybankfront.configuration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ForceNewTraceFilter extends OncePerRequestFilter {

    private static final Set<String> TRACE_HEADERS_LOWER = Set.of(
            "x-b3-traceid",
            "x-b3-spanid",
            "x-b3-parentspanid",
            "x-b3-sampled",
            "x-b3-flags",
            "b3",
            "traceparent",
            "tracestate"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        HttpServletRequestWrapper wrapped = new HttpServletRequestWrapper(request) {
            @Override
            public String getHeader(String name) {
                if (name != null && TRACE_HEADERS_LOWER.contains(name.toLowerCase(Locale.ROOT))) {
                    return null;
                }
                return super.getHeader(name);
            }

            @Override
            public Enumeration<String> getHeaders(String name) {
                if (name != null && TRACE_HEADERS_LOWER.contains(name.toLowerCase(Locale.ROOT))) {
                    return Collections.emptyEnumeration();
                }
                return super.getHeaders(name);
            }

            @Override
            public Enumeration<String> getHeaderNames() {
                Enumeration<String> original = super.getHeaderNames();
                if (original == null) {
                    return null;
                }
                Set<String> names = new HashSet<>();
                while (original.hasMoreElements()) {
                    String n = original.nextElement();
                    if (n == null) {
                        continue;
                    }
                    if (!TRACE_HEADERS_LOWER.contains(n.toLowerCase(Locale.ROOT))) {
                        names.add(n);
                    }
                }
                return Collections.enumeration(names);
            }
        };

        filterChain.doFilter(wrapped, response);
    }
}


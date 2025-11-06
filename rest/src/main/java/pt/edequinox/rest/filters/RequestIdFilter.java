package pt.edequinox.rest.filters;

import pt.edequinox.api.filters.FiltersContext;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@Component
public class RequestIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String requestId = request.getHeader(FiltersContext.REQUEST_ID_HEADER);
            if (requestId == null || requestId.isEmpty()) {
                requestId = UUID.randomUUID().toString();
            }

            FiltersContext.put(requestId);
            response.setHeader(FiltersContext.REQUEST_ID_HEADER, requestId);

            try {
                filterChain.doFilter(request, response);
            } finally {
                FiltersContext.remove();
            }
        } finally {
            FiltersContext.remove();
        }

    }
}
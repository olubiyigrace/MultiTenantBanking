//package com.bank.config;
//
//import jakarta.servlet.*;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.core.Ordered;
//import org.springframework.core.annotation.Order;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//
//@Component
//@Order(Ordered.HIGHEST_PRECEDENCE)
//public class InstitutionFilter implements Filter {
//    private static final String INSTITUTION_HEADER = "X-Institution-ID";
//
//    @Override
//    public void doFilter(
//            final ServletRequest servletRequest,
//            final ServletResponse servletResponse,
//            final FilterChain filterChain) throws IOException, ServletException {
//
//       final HttpServletRequest request = (HttpServletRequest) servletRequest;
//       final HttpServletResponse response = (HttpServletResponse) servletResponse;
//
//       final String institutionId = resolverHeader(request);
//       if (institutionId == null || institutionId.isBlank()){
//           response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//           response.setContentType("application/json");
//           response.getWriter().write("{\"error\":\"Institution ID is missing, add the header X-Institution-ID\"}");
//           return;
//       }
//       try{
//           InstitutionContext.setCurrentInstitution(institutionId);
//           filterChain.doFilter(servletRequest, servletResponse);
//       }finally{
//           InstitutionContext.clear();
//       }
//    }
//
//    private String resolverHeader(final HttpServletRequest request) {
//        final String institutionId = request.getHeader(INSTITUTION_HEADER);
//        if (institutionId != null && !institutionId.isBlank()) {
//            return institutionId.toLowerCase();
//        }
//        return null;
//    }
//}

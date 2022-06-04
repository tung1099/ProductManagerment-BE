package com.example.productmanagement.security;


import com.example.productmanagement.service.jwt.JwtService;
import com.example.productmanagement.service.user.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private JwtService jwtService;

    @Autowired
    private IUserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request); // Lấy JWT từ trong request người dùng gửi đi (Hàm tạo ở dưới)
            if (jwt != null && jwtService.validateJwtToken(jwt)) { // Kiểm tra JWT null hay bị lỗi không
                String username = jwtService.getUserNameFromJwtToken(jwt); // Lấy username từ chuỗi JWT ở trên

                UserDetails userDetails = userService.loadUserByUsername(username); // Lấy thông tin user qua username
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(// Truyền thông tin của user hiện tại vào
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("Can NOT set user authentication -> Message: {}", e);
        }
        filterChain.doFilter(request, response);
    }

    // Lấy JWT từ trong request người dùng gửi đi
    private String getJwtFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.replace("Bearer ", "");
        }

        return null;
    }
}

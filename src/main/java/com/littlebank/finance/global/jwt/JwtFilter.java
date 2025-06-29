package com.littlebank.finance.global.jwt;

import com.littlebank.finance.global.redis.RedisDao;
import com.littlebank.finance.global.redis.RedisPolicy;
import com.littlebank.finance.global.util.CookieUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    public static final String AUTHORIZATION_HEADER = "Authorization";
    private final CookieUtil cookieUtil;
    private final TokenProvider tokenProvider;
    private final RedisDao redisDao;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        HttpServletRequest servletRequest = request;
        String accessToken = resolveToken(servletRequest);
        log.info("요청 uri : " + servletRequest.getRequestURI());
        log.info("access 토큰 존재 여부 : " + StringUtils.hasText(accessToken));

        if (StringUtils.hasText(accessToken) && tokenProvider.validateToken(accessToken)) {
            Authentication authentication = tokenProvider.getAuthentication(accessToken);
            log.info("user info : " + authentication.getName());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            log.info("유효한 JWT 토큰이 없습니다. uri : {}", servletRequest.getRequestURI());
        }

        String refreshToken = cookieUtil.getCookieValue(servletRequest);
        log.info("refresh 토큰 존재 여부 : " + StringUtils.hasText(refreshToken));

        if (refreshToken != null && tokenProvider.validateToken(refreshToken)) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String name = (authentication != null)
                    ? authentication.getName() : tokenProvider.getAuthentication(refreshToken).getName();
            String loginUserKey = RedisPolicy.LOGIN_USER_KEY_PREFIX + name;

            if (!redisDao.existData(loginUserKey) || !refreshToken.equals(redisDao.getValues(loginUserKey))) {
                log.warn("로그아웃된 상태입니다.");

                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }
}

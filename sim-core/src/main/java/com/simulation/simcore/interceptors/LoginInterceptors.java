package com.simulation.simcore.interceptors;

import com.simulation.simcore.utils.Jwt;
import com.simulation.simcore.utils.ThreadTool;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

@Component
public class LoginInterceptors implements HandlerInterceptor {
    private StringRedisTemplate redisTemplate;
    @Autowired
    public void setRedisTemplate(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest req, @Nonnull HttpServletResponse response, @Nonnull Object handler) throws Exception {
        String token = req.getHeader("Authorization");
        try {
            ValueOperations<String, String> operations = redisTemplate.opsForValue();
            String redisToken = operations.get(token);
            if (redisToken == null) {
                throw new RuntimeException();
            }
            Map<String, Object> claims = Jwt.parseToken(token);
            ThreadTool.set(claims);
            return true;
        } catch (Exception e) {
            response.setStatus(401);
            return false;
        }
    }

    @Override
    public void afterCompletion(@Nonnull HttpServletRequest req, @Nonnull HttpServletResponse res, @Nonnull Object handler, Exception ex) throws Exception {
        ThreadTool.remove();
    }
}

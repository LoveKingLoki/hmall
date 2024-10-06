package com.hmall.gateway.filters;

import com.hmall.gateway.config.AuthProperties;
import com.hmall.gateway.util.JwtTool;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final AuthProperties authProperties;

    private final JwtTool jwtTool;

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // 获取是否需要登录校验
        if(isExclude(request.getPath().toString())){
            return chain.filter(exchange);
        }
        //获取token
        String token = null;
        Long userId = null;
        List<String> headers = request.getHeaders().get("authorization");
        if(headers != null && !headers.isEmpty()){
            token = headers.get(0);
        }
        try {
            userId = jwtTool.parseToken(token);
        }catch (Exception e){
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        String userinfo = userId.toString();
        // 传递用户信息
        ServerWebExchange swe = exchange.mutate().
                request(builder -> builder.header("user-info", userinfo))
                .build();
        return chain.filter(swe);
    }

    private boolean isExclude(String path) {
        for(String pathPattern: authProperties.getExcludePaths()){
            if(antPathMatcher.match(pathPattern, path)){
                return true;
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}

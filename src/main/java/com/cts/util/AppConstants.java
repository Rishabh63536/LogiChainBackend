package com.cts.util;

public class AppConstants {

   //urls reachable without jwt
    public static final String[] PUBLIC_URLS = {
            // for Swagger
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",

            //endpoints for authnetication
            "/api/v1/users/register",
            "/api/v1/users/login",
           "/pod-images/**"
    };

    private AppConstants() {
    }
}
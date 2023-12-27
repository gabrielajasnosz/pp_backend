package com.pp.ppbacked

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig() {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        val corsConfig = prepareCorsConfiguration()

        UrlBasedCorsConfigurationSource()
            .registerCorsConfiguration("/**", corsConfig)

        http
            .cors { it.configurationSource { corsConfig } }
            .csrf { it.disable() }
        return http.build()
    }

    private fun prepareCorsConfiguration(): CorsConfiguration {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = listOf("*")
        configuration.allowedMethods = listOf("*")
        configuration.allowedHeaders = listOf("*")
        return configuration
    }
}
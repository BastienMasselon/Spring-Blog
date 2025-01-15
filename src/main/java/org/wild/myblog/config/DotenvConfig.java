package org.wild.myblog.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import java.util.HashMap;
import java.util.Map;

public class DotenvConfig implements EnvironmentPostProcessor {
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        // Charger le fichier .env
        Dotenv dotenv = Dotenv.load();
        // Stocker les variables du .env dans un map
        Map<String, Object> dotenvProperties = new HashMap<>();
        dotenv.entries().forEach(entry -> dotenvProperties.put(entry.getKey(), entry.getValue()));
        // Ajouter les propriétés au contexte Spring
        environment.getPropertySources().addFirst(new MapPropertySource("dotenvProperties", dotenvProperties));
    }
}

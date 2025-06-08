package br.edu.utfpr.alunos.webpet.infra.config;

import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class JpaOptimizationConfig implements HibernatePropertiesCustomizer {
    
    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        // Batch processing for better performance
        hibernateProperties.put("hibernate.jdbc.batch_size", 25);
        hibernateProperties.put("hibernate.order_inserts", true);
        hibernateProperties.put("hibernate.order_updates", true);
        hibernateProperties.put("hibernate.jdbc.batch_versioned_data", true);
        
        // Query optimization
        hibernateProperties.put("hibernate.query.in_clause_parameter_padding", true);
        hibernateProperties.put("hibernate.query.fail_on_pagination_over_collection_fetch", true);
        hibernateProperties.put("hibernate.query.plan_cache_max_size", 2048);
        hibernateProperties.put("hibernate.query.plan_parameter_metadata_max_size", 128);
        
        // Connection and statement optimization
        hibernateProperties.put("hibernate.connection.provider_disables_autocommit", true);
        hibernateProperties.put("hibernate.jdbc.lob.non_contextual_creation", true);
        
        // Statistics for monitoring (disable in production)
        hibernateProperties.put("hibernate.generate_statistics", false);
        
        // Second level cache (disabled for compatibility)
        hibernateProperties.put("hibernate.cache.use_second_level_cache", false);
        hibernateProperties.put("hibernate.cache.use_query_cache", false);
    }
}
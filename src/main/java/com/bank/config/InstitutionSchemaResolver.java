package com.bank.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class InstitutionSchemaResolver {
    private final JdbcTemplate jdbcTemplate;

    private static final String PUBLIC_SCHEMA = "public";

    @Cacheable(value = "institutionSchemas", key = "#institutionId")
    public String resolveInstitutionSchema(final String institutionId) {
        if (institutionId == null) {
            return PUBLIC_SCHEMA;
        }
        try {
            final String institutionRcNumber = this.jdbcTemplate.queryForObject(
                    "SELECT institution_rc_number FROM institutions WHERE id = ?",
                    String.class,
                    institutionId);
            if (institutionRcNumber != null) {
                final String schemaName = "institution_" + institutionRcNumber.toLowerCase();
                log.debug("Resolved Institution schema: {} for institution: {}", schemaName, institutionId);
                return schemaName;
            }
            log.warn("Institution schema not found for institution: {}, using public schema", institutionId);
            return PUBLIC_SCHEMA;
        } catch (final Exception e) {
            log.error("Error resolving institution schema for institution: {}", institutionId, e);
            return PUBLIC_SCHEMA;
        }
    }
}

package com.bank.services.impl;

import com.bank.entities.Institution;
import com.bank.exceptions.InstitutionProvisioningException;
import com.bank.services.ProvisioningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProvisioningServiceImpl implements ProvisioningService {
    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    @Override
    public void provisionInstitution(Institution institution) {
        final String schemaName = "institution" + institution.getInstitutionType();
        try {
            log.info("Provisioning institution: {} (schema: {})", institution.getInstitutionName(), schemaName);
            createSchema(schemaName);

            log.info("Schema {} created successfully", schemaName);
            runInstitutionMigration(schemaName);
        } catch (final Exception e){
            log.error("Error provisioning institution: {}", institution.getInstitutionType(), e);
            try {
                dropSchema(schemaName);
            } catch (final Exception ex){
                log.error("Error dropping schema: {}", schemaName, ex);
            }
            throw new InstitutionProvisioningException("Failed to provision institution");
        }
    }

    private void dropSchema(String schemaName) {
        final String sql = String.format("DROP SCHEMA IF EXIST %s", schemaName);
        jdbcTemplate.execute(sql);
    }

    private void runInstitutionMigration(String schemaName) {
        log.info("Running institution migration for schema: {}", schemaName);
        final Flyway institutionFlyway = Flyway.configure()
                .dataSource(dataSource)
                .schemas(schemaName)
                .locations("classpath:db/migration/institution")
                .baselineOnMigrate(true)
                .table("flyway_schema_history")
                .validateOnMigrate(true)
                .cleanDisabled(true)
                .load();
        log.info("Institution flyway migration started");
        institutionFlyway.migrate();
        log.info("Institution flyway migration completed");

    }

    private void createSchema(final String schemaName) {
        final String sql = String.format("CREATE SCHEMA IF NOT EXIST %s", schemaName);
        jdbcTemplate.execute(sql);
    }
}

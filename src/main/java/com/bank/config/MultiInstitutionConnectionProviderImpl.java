package com.bank.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.cfg.MultiTenancySettings;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class MultiInstitutionConnectionProviderImpl implements MultiTenantConnectionProvider<String>, HibernatePropertiesCustomizer {
    private final DataSource dataSource;

    @Override
    public Connection getAnyConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public Connection getConnection(String institutionIdentifier) throws SQLException {
        log.debug("Getting connection for institution: {}", institutionIdentifier);
        final Connection connection = getAnyConnection();
        try{
            if (institutionIdentifier != null && !institutionIdentifier.equals("public")){
                connection.createStatement().execute("SET search_path TO " + institutionIdentifier + ", public ");
                log.trace("Set search path to: {}", institutionIdentifier);
            }
        }catch (SQLException e){
            log.error("Error setting search_path ", e);
            throw e;
        }
        return connection;
    }

    @Override
    public void releaseConnection(String s, Connection connection) throws SQLException {
        try{
            connection.createStatement().execute("Set search_path TO public");
        } catch (SQLException e){
            log.error("Error setting search_path", e);
            throw e;
        }
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    @Override
    public boolean isUnwrappableAs(Class<?> aClass) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> aClass) {
        return null;
    }

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put(MultiTenancySettings.MULTI_TENANT_CONNECTION_PROVIDER, this);

    }
}

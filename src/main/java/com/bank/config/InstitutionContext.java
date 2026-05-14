package com.bank.config;

public class InstitutionContext {
    private static final ThreadLocal<String> CURRENT_INSTITUTION = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_SCHEMA = new ThreadLocal<>();

    public static void setCurrentInstitution(final String institution){
        CURRENT_INSTITUTION.set(institution);
    }
    public static String getCurrentInstitution(){
        return CURRENT_INSTITUTION.get();
    }
    public static void setCurrentSchema(final String institution){
        CURRENT_SCHEMA.set(institution);
    }
    public static String getCurrentSchema(){
        return CURRENT_SCHEMA.get();
    }
    public static void clear(){
        CURRENT_INSTITUTION.remove();
        CURRENT_SCHEMA.remove();
    }
}

package com.bank.others.config;

import org.springframework.stereotype.Component;

public class InstitutionContext {
    private static final ThreadLocal<String> CURRENT_INSTITUTION = new ThreadLocal<>();

    public static void setCurrentInstitution(final String institution){
        CURRENT_INSTITUTION.set(institution);
    }
    public static String getCurrentInstitution(){
        return CURRENT_INSTITUTION.get();
    }
    public static void clear(){CURRENT_INSTITUTION.remove();}
}


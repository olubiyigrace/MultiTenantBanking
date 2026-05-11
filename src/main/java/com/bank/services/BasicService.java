package com.bank.services;

import java.util.List;
import java.util.UUID;

public interface BasicService<I, O> {
    void create(final I request);
}

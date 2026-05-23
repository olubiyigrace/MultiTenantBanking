create table loan_products
(
    id                     varchar(255) not null primary key,
    created_at             timestamp(6) not null,
    max_tenure_months      integer      not null,
    min_amount             numeric(38, 2),
    max_amount             numeric(38, 2),
    interest_rate_percent  numeric(5, 2),
    processing_fee_percent numeric(5, 2),
    description            text,
    name                   varchar(255) not null,
    requires_guarantor     boolean,
    requires_collateral    boolean,
    interest_type          varchar(255),
    is_active              boolean,
    institution_id       varchar(255),
    constraint fk_loan_product_institution_id
        foreign key (institution_id)
            references institutions(id),
        constraint loan_products_interest_type_check
            check (
                (interest_type)::text = ANY (
        ARRAY[
        ('FLAT':: character varying)::text,
        ('REDUCING_BALANCE':: character varying)::text
        ]
        ))
    );



create table audit_logs
(
    id             varchar(255) not null primary key,
    action         varchar(255) not null,
    created_at     timestamp(6) not null,
    entity_id      UUID   not null,
    entity_type    varchar(255) not null,
    ip_address     varchar(255) not null,
    new_value      varchar(255) not null,
    old_value      varchar(255) not null,
    user_id        varchar(255) not null,
    institution_id       varchar(255),
    constraint fk_audit_log_institution_id
        foreign key (institution_id)
            references institutions(id),
    constraint fk_audit_log_user_id
        foreign key (user_id) references users(id)
);




create table loan_applications
(
    id                 varchar(255) not null primary key,
    created_at         timestamp(6) not null,
    disbursed_at       timestamp(6) not null,
    fully_repaid_at    timestamp(6) not null,
    reviewed_at        timestamp(6) not null,
    requested_amount   numeric(38,2) not null,
    approved_amount    numeric(38,2),
    tenure_months      integer not null,
    purpose            varchar(255),
    interest_rate_percent numeric(5,2),
    interest_type      varchar(255),
    total_interest     numeric(38,2),
    total_repayable    numeric(38,2),
    monthly_installment numeric(38,2),
    processing_fee     numeric(38,2),
    net_disbursement   numeric(38,2),
    rejection_reason   text,
    reviewed_by        varchar(255),
    loan_application_status varchar(255)
        constraint loan_applications_loan_application_status_check
            check (
                (loan_application_status)::text = ANY (
        ARRAY[
        ('PENDING'::character varying)::text,
        ('UNDER_REVIEW'::character varying)::text,
        ('APPROVED'::character varying)::text,
        ('REJECTED'::character varying)::text,
        ('DISBURSED'::character varying)::text,
        ('FULLY REPAID'::character varying)::text,
        ('DEFAULTED'::character varying)::text,
        ('WRITTEN_OFF'::character varying)::text
        ]
        )),
    member_profile_id      varchar(255) not null,
    loan_product_id        varchar(255) not null,
    loan_officer_id        varchar(255) not null,
    institution_id            varchar(255),
    constraint fk_loan_application_institution_id
        foreign key (institution_id) references institutions(id),
    constraint fk_loan_application_member_profile_id
        foreign key (member_profile_id) references member_profiles(id),
    constraint fk_loan_application_loan_product_id
        foreign key (loan_product_id) references loan_products(id),
    constraint fk_loan_application_loan_officer_id
        foreign key (loan_officer_id) references users(id)
);





create table loan_repayment_schedule
(
    id                 varchar(255) not null primary key,
    paid_at            timestamp(6) not null,
    installment_number integer not null,
    principal_due      numeric(38,2) not null,
    interest_due       numeric(38,2) not null,
    total_due          numeric(38,2) not null,
    amount_paid        numeric(38,2) not null,
    balance_remaining  numeric(38,2) not null,
    due_date           date not null,
    loan_repayment_status varchar(255)
        constraint loan_repayment_schedule_loan_repayment_status_check
            check (
                (loan_repayment_status)::text = ANY (
        ARRAY[
        ('PENDING'::character varying)::text,
        ('PARTIAL'::character varying)::text,
        ('PAID'::character varying)::text,
        ('OVERDUE'::character varying)::text
        ]
        )),
    loan_application_id      varchar(255) not null,
         constraint fk_loan_repayment_schedule_loan_application_id
        foreign key (loan_application_id) references loan_applications(id)
);




create table loan_guarantors
(
    id                 varchar(255) not null primary key,
    created_at         timestamp(6) not null,
    responded_at       timestamp(6) not null,
    guarantor_member_id UUID not null,
    guarantor_status varchar(255)
        constraint loan_guarantors_guarantor_status_check
            check (
                (guarantor_status)::text = ANY (
        ARRAY[
        ('PENDING'::character varying)::text,
        ('ACCEPTED'::character varying)::text,
        ('REJECTED'::character varying)::text
        ]
        )),
    loan_application_id      varchar(255) not null,
         constraint fk_loan_guarantor_loan_application_id
        foreign key (loan_application_id) references loan_applications(id)
);




create table loan_collaterals
(
    id                 varchar(255) not null primary key,
    created_at         timestamp(6) not null,
    estimated_value    numeric(38,2),
    description        text,
    document_url       varchar(255),
    loan_application_id      varchar(255) not null,
    constraint fk_loan_collateral_loan_application_id
        foreign key (loan_application_id) references loan_applications(id)
);




create table savings_interest_accruals
(
    id                            varchar(255) not null primary key,
    credited_at                   timestamp(6) not null,
    period_start                  date not null,
    period_end                    date not null,
    opening_balance               numeric(38,2) not null,
    interest_amount               numeric(38,2) not null,
    savings_account_id           varchar(255) not null,
    constraint fk_savings_interest_accrual_savings_account_id
        foreign key (savings_account_id) references savings_accounts(id)

);



create table transactions
(
    id                            varchar(255) not null primary key,
    created_at                    timestamp(6) not null,
    reference                     varchar(255) not null,
    description                   text not null,
    amount                        numeric(38,2) not null,
    balance_before                numeric(38,2) not null,
    balance_after                 numeric(38,2) not null,
    reversed_by_transaction_id    UUID not null,

    transaction_status              varchar(255)
        constraint transactions_transaction_status_check
            check (
                (transaction_status)::text = ANY (
        ARRAY[
        ('PENDING'::character varying)::text,
        ('COMPLETED'::character varying)::text,
        ('FAILED'::character varying)::text,
        ('REVERSED'::character varying)::text
        ]
        )),
    transaction_type              varchar(255)
        constraint transactions_transaction_type_check
            check (
                (transaction_type)::text = ANY (
        ARRAY[
        ('DEPOSIT'::character varying)::text,
        ('WITHDRAWAL'::character varying)::text,
        ('LOAN_DISBURSEMENT'::character varying)::text,
        ('LOAN_REPAYMENT'::character varying)::text,
        ('INTEREST_CREDIT'::character varying)::text,
        ('TRANSFER_IN'::character varying)::text,
        ('TRANSFER_OUT'::character varying)::text,
        ('FEE_DEBIT'::character varying)::text
        ]
        )),
    performed_by_user_id         varchar(255) not null,
              institution_id       varchar(255),
    constraint fk_transaction_institution_id
        foreign key (institution_id)
        references institutions(id),
    savings_account_id           varchar(255) not null,
    constraint fk_transaction_performed_by_user_id
        foreign key (performed_by_user_id) references users(id),
    constraint fk_transaction_savings_account_id
        foreign key (savings_account_id) references savings_accounts(id)
);
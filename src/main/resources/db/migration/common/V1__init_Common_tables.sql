create table institutions
(
    id                              varchar(255) not null primary key,
    created_at                      timestamp(6) not null,
    admin_email                     varchar(255) not null,
    admin_name                      varchar(255) not null,
    admin_nin                       varchar(255) not null,
    admin_password                  varchar(255) not null,
    admin_phone                     varchar(255) not null,
    admin_username                  varchar(255) not null,
    is_verified                     boolean      not null,
    base_currency                   varchar(255) not null,
    email_verification_token        varchar(255),
    email_verification_token_expiry timestamp(6),
    email_verified_at               timestamp(6),
    institution_email               varchar(255) not null unique,
    institution_name                varchar(255) not null,
    institution_phone               varchar(255) not null,
    institution_rc_number           varchar(255) not null unique,
    institution_status              varchar(255) not null
        constraint institutions_institution_status_check
            check ((institution_status)::text = ANY
        (ARRAY [('PENDING':: character varying)::text,
        ('ACTIVE':: character varying)::text,
        ('INACTIVE':: character varying)::text,
        ('SUSPENDED':: character varying)::text])
) ,
    institution_type                varchar(255) not null
        constraint institutions_institution_type_check
            check ((institution_type)::text = ANY
                   (ARRAY [('COOPERATIVE'::character varying)::text,
                   ('MICROFINANCE'::character varying)::text]))
);


create table users
(
    id                              varchar(255) not null primary key,
    created_at                      timestamp(6) not null,
    deleted_at                      timestamp(6),
    email                           varchar(255) not null unique,
    username                        varchar(255) not null,
    is_verified                     boolean      not null,
    name                            varchar(255) not null,
    nin                             varchar(255) not null,
    reset_password_token            varchar(255),
    password                        varchar(255) not null,
    email_verification_token        varchar(255),
    email_verification_token_expiry timestamp(6),
    email_verified_at               timestamp(6),
    reset_password_token_expiry     timestamp(6),
    phone                           varchar(255) not null,
    institution_id                  varchar(255),
    user_account_type               varchar(255) not null
        constraint users_user_account_type_check
            check (
                (user_account_type)::text = ANY (
        ARRAY[
        ('SUPER_ADMIN':: character varying)::text,
        ('INSTITUTION_ADMIN':: character varying)::text,
        ('LOAN_OFFICER':: character varying)::text,
        ('ACCOUNTANT':: character varying)::text,
        ('MEMBER':: character varying)::text
        ]
        )
) ,
    constraint fk_user_institution_id
        foreign key (institution_id)
        references institutions(id)
);



create table user_sessions
(
    id           varchar(255) not null primary key,
    access_token text         not null,
    revoked      boolean      not null,
    expiry_date  timestamp    not null,
    user_id      varchar(255) not null,
    constraint fk_user_session_user_id
        foreign key (user_id)
            references users (id)
);



create table logout_tokens
(
    token           varchar(1000) not null primary key,
    expiry_date     timestamp     not null,
    user_session_id varchar(255)  not null,
    constraint fk_logout_token_user_session_id
        foreign key (user_session_id)
            references user_sessions (id)
);



create table member_profiles
(
    id                varchar(255)   not null primary key,
    created_at        timestamp(6)   not null,
    member_number     varchar(255)   not null unique,
    bvn               varchar(255)   not null unique,
    employment_status varchar(255)   not null,
    next_of_kin_name  varchar(255)   not null,
    next_of_kin_phone varchar(50)    not null,
    monthly_income    numeric(38, 2) not null,
    address           text           not null,
    date_of_birth     date           not null,
    institution_id    varchar(255)   not null,
    user_id           varchar(255)   not null,
    profile_status    varchar(255)
        constraint member_profiles_profile_status_check
            check (
                (profile_status)::text = ANY (
        ARRAY[
        ('ACTIVE':: character varying)::text,
        ('SUSPENDED':: character varying)::text,
        ('EXITED':: character varying)::text
        ]
        )
) ,
    constraint fk_member_profile_institution_id
        foreign key (institution_id)
        references institutions(id),
    constraint fk_member_profile_user_id
        foreign key (user_id)
         references users(id)
);



create table savings_accounts
(
    id                    varchar(255)   not null primary key,
    created_at            timestamp(6)   not null,
    maturity_date         date,
    account_number        varchar(255)   not null,
    member_id             varchar(255)   not null,
    version               integer        not null,
    balance               numeric(38, 2) not null,
    minimum_balance       numeric(38, 2) not null,
    interest_rate_percent numeric(5, 2)  not null,
    institution_id        varchar(255)   not null,
    target_amount         numeric(38, 2),
    savings_status        varchar(255)
        constraint savings_accounts_savings_status_check
            check (
                (savings_status)::text = ANY (
        ARRAY[
        ('ACTIVE':: character varying)::text,
        ('FROZEN':: character varying)::text,
        ('CLOSED':: character varying)::text
        ]
        )
) ,
    savings_account_type  varchar(255)   not null
        constraint savings_accounts_savings_account_type_check
            check (
                (savings_account_type)::text = ANY (
        ARRAY[
        ('REGULAR':: character varying)::text,
        ('TARGET':: character varying)::text,
        ('FIXED':: character varying)::text
        ]
        )
) ,
    constraint fk_savings_account_member_id
        foreign key (member_id)
        references member_profiles(id),
    constraint fk_savings_account_institution_id
        foreign key (institution_id)
        references institutions(id)
);


create table loan_applications
(
    id                      varchar(255)   not null primary key,
    created_at              timestamp(6)   not null,
    disbursed_at            timestamp(6),
    fully_repaid_at         timestamp(6),
    reviewed_at             timestamp(6),
    requested_amount        numeric(38, 2) not null,
    approved_amount         numeric(38, 2),
    tenure_months           numeric(38, 2),
    purpose                 text           not null,
    interest_rate_percent   numeric(5, 2),
    total_interest          numeric(38, 2),
    total_repayable         numeric(38, 2),
    monthly_installment     numeric(38, 2),
    processing_fee          numeric(38, 2),
    net_disbursement        numeric(38, 2),
    rejection_reason        text,
    reviewed_by             varchar(255),
    member_id               varchar(255)   not null,
    institution_id          varchar(255)   not null,
    loan_product_id         varchar(255)   not null,
    loan_officer_id         varchar(255),
    loan_application_status varchar(255)   not null
        constraint loan_applications_loan_application_status_check
            check (
                (loan_application_status)::text = ANY (
        ARRAY[
        ('PENDING':: character varying)::text,
        ('UNDER_REVIEW':: character varying)::text,
        ('APPROVED':: character varying)::text,
        ('REJECTED':: character varying)::text,
        ('DISBURSED':: character varying)::text,
        ('FULLY REPAID':: character varying)::text,
        ('DEFAULTED':: character varying)::text,
        ('WRITTEN_OFF':: character varying)::text
        ])
) ,
    recommendation_status varchar(255)
      constraint loan_applications_recommendation_status_check
            check (
                (recommendation_status)::text = ANY (
        ARRAY[
        ('RECOMMENDED_APPROVAL':: character varying)::text,
        ('RECOMMENDED_REJECTION':: character varying)::text
        ])),
    constraint fk_loan_application_institution_id
        foreign key (institution_id) references institutions(id),
    constraint fk_loan_application_member_id
        foreign key (member_id) references member_profiles(id),
    constraint fk_loan_application_loan_officer_id
        foreign key (loan_officer_id) references users(id),
        interest_type          varchar(255)
        constraint loan_applications_interest_type_check
            check (
                (interest_type)::text = ANY (
        ARRAY[
        ('FLAT':: character varying)::text,
        ('REDUCING_BALANCE':: character varying)::text
        ])
));



create table loan_collaterals
(
    id                  varchar(255)   not null primary key,
    created_at          timestamp(6)   not null,
    estimated_value     numeric(38, 2) not null,
    description         text           not null,
    document_url        varchar(255)   not null,
    loan_application_id varchar(255)   not null,
    constraint fk_loan_collateral_loan_application_id
        foreign key (loan_application_id) references loan_applications (id)
);



create table loan_guarantors
(
    id                  varchar(255) not null primary key,
    created_at          timestamp(6) not null,
    responded_at        timestamp(6),
    loan_application_id varchar(255) not null,
    guarantor_member_id varchar(255) not null,
    guarantor_status    varchar(255) not null
        constraint loan_guarantors_guarantor_status_check
            check (
                (guarantor_status)::text = ANY (
        ARRAY[
        ('PENDING':: character varying)::text,
        ('ACCEPTED':: character varying)::text,
        ('REJECTED':: character varying)::text
        ]
        )
) ,
         constraint fk_loan_guarantor_loan_application_id
        foreign key (loan_application_id) references loan_applications(id)
);



create table loan_products
(
    id                     varchar(255)   not null primary key,
    created_at             timestamp(6)   not null,
    max_tenure_months      numeric(38, 2) not null,
    min_amount             numeric(38, 2) not null,
    max_amount             numeric(38, 2) not null,
    interest_rate_percent  numeric(5, 2)  not null,
    processing_fee_percent numeric(5, 2)  not null,
    description            text           not null,
    name                   varchar(255)   not null,
    requires_guarantor     boolean        not null,
    requires_collateral    boolean        not null,
    is_active              boolean        not null,
    institution_id         varchar(255)   not null,
    constraint fk_loan_product_institution_id
        foreign key (institution_id)
            references institutions (id),
    interest_type          varchar(255)   not null
        constraint loan_products_interest_type_check
            check (
                (interest_type)::text = ANY (
        ARRAY[
        ('FLAT':: character varying)::text,
        ('REDUCING_BALANCE':: character varying)::text
        ])
)
    );



create table loan_repayment_schedule
(
    id                    varchar(255)   not null primary key,
    paid_at               timestamp(6)   not null,
    installment_number    varchar(255)   not null,
    principal_due         numeric(38, 2) not null,
    interest_due          numeric(38, 2) not null,
    total_due             numeric(38, 2) not null,
    amount_paid           numeric(38, 2) not null,
    loan_application_id   varchar(255)   not null,
    balance_remaining     numeric(38, 2) not null,
    due_date              date           not null,
    loan_repayment_status varchar(255)   not null
        constraint loan_repayment_schedule_loan_repayment_status_check
            check (
                (loan_repayment_status)::text = ANY (
        ARRAY[
        ('PENDING':: character varying)::text,
        ('PARTIAL':: character varying)::text,
        ('PAID':: character varying)::text,
        ('OVERDUE':: character varying)::text
        ]
        )
) ,
         constraint fk_loan_repayment_schedule_loan_application_id
        foreign key (loan_application_id) references loan_applications(id)
);



create table transactions
(
    id                         varchar(255)   not null primary key,
    created_at                 timestamp(6)   not null,
    reference                  varchar(255)   not null,
    description                text           not null,
    amount                     numeric(38, 2) not null,
    balance_before             numeric(38, 2) not null,
    balance_after              numeric(38, 2) not null,
    reversed_by_transaction_id varchar(255)   not null,
    performed_by_user_id       varchar(255)   not null,
    savings_account_id         varchar(255)   not null,
    institution_id             varchar(255)   not null,
    transaction_status         varchar(255)   not null
        constraint transactions_transaction_status_check
            check (
                (transaction_status)::text = ANY (
        ARRAY[
        ('PENDING':: character varying)::text,
        ('COMPLETED':: character varying)::text,
        ('FAILED':: character varying)::text,
        ('REVERSED':: character varying)::text
        ]
        )
) ,
    transaction_type              varchar(255) not null
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
    constraint fk_transaction_institution_id
        foreign key (institution_id) references institutions(id),
    constraint fk_transaction_performed_by_user_id
        foreign key (performed_by_user_id) references users(id),
    constraint fk_transaction_savings_account_id
        foreign key (savings_account_id) references savings_accounts(id)
);



create table savings_interest_accruals
(
    id                 varchar(255)   not null primary key,
    credited_at        timestamp(6)   not null,
    period_start       date           not null,
    period_end         date           not null,
    opening_balance    numeric(38, 2) not null,
    interest_amount    numeric(38, 2) not null,
    savings_account_id varchar(255)   not null,
    constraint fk_savings_interest_accrual_savings_account_id
        foreign key (savings_account_id) references savings_accounts (id)

);



create table audit_logs
(
    id             varchar(255) not null primary key,
    action         text         not null,
    created_at     timestamp(6) not null,
    entity_id      varchar(255) not null,
    entity_type    varchar(255) not null,
    ip_address     varchar(255) not null,
    new_value      varchar(255) not null,
    old_value      varchar(255) not null,
    user_id        varchar(255) not null,
    institution_id varchar(255) not null,
    constraint fk_audit_log_institution_id
        foreign key (institution_id)
            references institutions (id),
    constraint fk_audit_log_user_id
        foreign key (user_id) references users (id)
);
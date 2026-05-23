create table institutions
(
    id                              varchar(255) not null
        primary key,
    created_at                      timestamp(6) not null,
    admin_email                     varchar(255) not null,
    admin_name                      varchar(255) not null,
    admin_nin                       varchar(255) not null,
    admin_password                  varchar(255) not null,
    admin_phone                     varchar(255) not null,
    admin_username                  varchar(255) not null,
    base_currency                   varchar(255),
    email_verification_token        varchar(255),
    email_verification_token_expiry timestamp(6),
    email_verified_at               timestamp(6),
    institution_email               varchar(255) not null
        unique,
    institution_name                varchar(255) not null,
    institution_phone               varchar(255) not null,
    institution_rc_number           varchar(255) not null
        unique,
    institution_status              varchar(255)
        constraint institutions_institution_status_check
            check ((institution_status)::text = ANY
        (ARRAY [('PENDING'::character varying)::text,
                ('ACTIVE'::character varying)::text,
                ('INACTIVE'::character varying)::text,
                ('SUSPENDED'::character varying)::text])),
    institution_type                varchar(255)
        constraint institutions_institution_type_check
            check ((institution_type)::text = ANY
                   (ARRAY [('COOPERATIVE'::character varying)::text,
                   ('MICROFINANCE'::character varying)::text])),
    is_verified                     boolean
);


create table users
(
    id                varchar(255) not null primary key,
    created_at        timestamp(6) not null,
    deleted_at        timestamp(6),
    email             varchar(255) not null unique,
    name              varchar(255) not null,
    nin               varchar(255) not null,
    reset_password_token               varchar(255),
    password          varchar(255) not null,
    email_verification_token        varchar(255),
    email_verification_token_expiry timestamp(6),
    email_verified_at               timestamp(6),
    reset_password_token_expiry               timestamp(6),
    phone             varchar(255) not null,
    user_account_type varchar(255)
        constraint users_user_account_type_check
            check (
                (user_account_type)::text = ANY (
        ARRAY[
        ('SUPER_ADMIN'::character varying)::text,
        ('INSTITUTION_ADMIN'::character varying)::text,
        ('LOAN_OFFICER'::character varying)::text,
        ('ACCOUNTANT'::character varying)::text,
        ('MEMBER'::character varying)::text
        ]
        )),
    username          varchar(255) not null,
    institution_id       varchar(255),
    is_verified       boolean,
    constraint fk_user_institution_id
        foreign key (institution_id)
        references institutions(id)
);


create table logout_tokens
(
    token             VARCHAR(1000) primary key,
    expiry_date       timestamp not null,
    user_id             varchar(255),
    constraint fk_logout_token_user_id
        foreign key (user_id)
        references users(id)
);



create table member_profiles
(
    id                          varchar(255) not null primary key,
    created_at                  timestamp(6) not null,
    member_number               varchar(255) not null unique,
    bvn                         varchar(255) not null unique,
    employment_status           varchar(255) not null,
    next_of_kin_name            varchar(255) not null,
    next_of_kin_phone           varchar(50) not null,
    monthly_income              numeric(38,2) not null,
    address                     text not null,
    date_of_birth               date not null,
    savings_account_type        varchar(255)
        constraint member_profiles_savings_account_type_check
            check (
                (savings_account_type)::text = ANY (
        ARRAY[
        ('REGULAR'::character varying)::text,
        ('TARGET'::character varying)::text,
        ('FIXED'::character varying)::text
        ]
        )),
    profile_status              varchar(255)
        constraint member_profiles_profile_status_check
            check (
                (profile_status)::text = ANY (
        ARRAY[
        ('ACTIVE'::character varying)::text,
        ('SUSPENDED'::character varying)::text,
        ('EXITED'::character varying)::text
        ]
        )),
         institution_id       varchar(255),
    constraint fk_member_profile_institution_id
        foreign key (institution_id)
        references institutions(id),
    user_id                      varchar(255) unique not null,
    constraint fk_member_profile_user_id
        foreign key (user_id)
         references users(id)
);





create table savings_accounts
(
    id                            varchar(255) not null primary key,
    created_at                    timestamp(6) not null,
    maturity_date                 date,
    account_number                varchar(255) not null,
    member_id                     varchar(255) not null,
    version                       integer not null,
    balance                       numeric(38,2) not null,
    minimum_balance               numeric(38,2) not null,
    interest_rate_percent         numeric(5,2) not null,
    target_amount                 numeric(38,2),
    savings_status                varchar(255)
        constraint savings_accounts_savings_status_check
            check (
                (savings_status)::text = ANY (
        ARRAY[
        ('ACTIVE'::character varying)::text,
        ('FROZEN'::character varying)::text,
        ('CLOSED'::character varying)::text
        ]
        )),
    savings_account_type            varchar(255)
        constraint savings_accounts_savings_account_type_check
            check (
                (savings_account_type)::text = ANY (
        ARRAY[
        ('REGULAR'::character varying)::text,
        ('TARGET'::character varying)::text,
        ('FIXED'::character varying)::text
        ]
        )),
             institution_id       varchar(255),
    constraint fk_savings_account_institution_id
        foreign key (institution_id)
        references institutions(id)
);
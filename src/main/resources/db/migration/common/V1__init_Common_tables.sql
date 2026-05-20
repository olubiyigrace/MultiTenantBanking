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
    enabled           boolean,
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
    institution_id    varchar(255),
    is_verified       boolean,

    constraint fk_user_institution_id
        foreign key (institution_id)
        references institutions(id)
);
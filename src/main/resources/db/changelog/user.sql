--liquibase formatted sql
--changeset Alexander:27

-- create table users
-- (
--     account_id   uuid not null
--         primary key,
--     account_name varchar(255),
--     secret       varchar(255),
--     role         varchar(255)
-- );
--
-- alter table users
--     owner to postgres;
--
-- create table order_dish_ids
-- (
--     order_id integer not null
--         references orders,
--     dish_ids integer not null,
--     primary key (order_id, dish_ids)
-- );
--
-- alter table order_dish_ids
--     owner to postgres;
--
-- create table menu
-- (
--     id               serial
--         primary key,
--     title            varchar(255),
--     summary          varchar(255),
--     cost             numeric(38, 2),
--     preparation_time integer not null,
--     created_by       varchar(255)
-- );
--
-- alter table menu
--     owner to postgres;
--
-- create table orders
-- (
--     id               serial
--         primary key,
--     order_start_time timestamp,
--     order_end_time   timestamp,
--     total_cost       numeric(38, 2),
--     customer_name    varchar(255)
-- );
--
-- alter table orders
--     owner to postgres;
-- Wallet — initial Supabase schema (Phase 16/17 schema half, plan.md §38-§43, §84-§85, §88).
--
-- Run this once in the Supabase SQL Editor (Dashboard → SQL Editor → New query → paste → Run).
--
-- Design notes:
--   * All app-facing timestamps (created_at/updated_at/date/due_date/...) are `bigint` epoch
--     milliseconds, matching the local Room entities exactly (every local *Entity.kt field is a
--     `Long` epoch-millis value, never a Kotlin Instant/Postgres timestamptz) — this keeps the
--     eventual sync engine a straight value copy, no timezone/epoch conversion either direction.
--   * `updated_at`/`version`/`device_id` are plain client-managed columns (plan.md §43's
--     conflict-resolution trio), NOT server-side triggers — the client sets them on every write
--     so "last write wins" compares the client's own edit clock, and a future sync engine's
--     `WHERE version = :expected` optimistic-concurrency check (§43) works as specified.
--   * Enums are `text` + `check (...)`, not native Postgres `enum` types — matches Room's own
--     `Converters.kt` (stores `.name`), and avoids native enums' painful `ALTER TYPE` story.
--   * `institutions`, `category_groups`, `categories` have a NULLABLE `user_id`: a null row is a
--     system-seeded shared row (mirrors the local `isSystem` flag); a non-null row is a user's own
--     custom addition. Every other table requires `user_id`.
--   * `automation_candidates` has no mirror here — device-local pending SMS parses, never synced.
--   * No local Debt feature exists yet (Room entity/UI) — `debts`/`debt_payments` are included
--     per plan.md §88 so the table is ready the moment that feature is built.
--   * This migration only creates the schema. It does not touch `auth.users` (Supabase-managed)
--     and no sync engine exists yet to populate these tables beyond `profiles` (written once at
--     sign-in by the Android app's `UserRepositoryImpl`).

-- ============================================================================
-- profiles — plan.md §84. PK *is* auth.users.id, not a separate surrogate id.
-- ============================================================================
create table public.profiles (
    id           uuid primary key references auth.users(id) on delete cascade,
    display_name text not null,
    avatar_url   text,
    created_at   bigint not null,
    updated_at   bigint not null
);

alter table public.profiles enable row level security;

create policy "profiles_select_own" on public.profiles
    for select using (auth.uid() = id);
create policy "profiles_insert_own" on public.profiles
    for insert with check (auth.uid() = id);
create policy "profiles_update_own" on public.profiles
    for update using (auth.uid() = id);
create policy "profiles_delete_own" on public.profiles
    for delete using (auth.uid() = id);

-- ============================================================================
-- institutions — shared reference list (nullable user_id = system-seeded row).
-- ============================================================================
create table public.institutions (
    id         uuid primary key default gen_random_uuid(),
    user_id    uuid references auth.users(id) on delete cascade,
    name       text not null,
    type       text not null,
    logo       text,
    country    text,
    created_at bigint not null,
    updated_at bigint not null,
    version    integer not null default 1,
    device_id  text
);

alter table public.institutions enable row level security;

create policy "institutions_select" on public.institutions
    for select using (user_id is null or auth.uid() = user_id);
create policy "institutions_insert_own" on public.institutions
    for insert with check (auth.uid() = user_id);
create policy "institutions_update_own" on public.institutions
    for update using (auth.uid() = user_id);
create policy "institutions_delete_own" on public.institutions
    for delete using (auth.uid() = user_id);

-- ============================================================================
-- category_groups — nullable user_id = system-seeded group.
-- ============================================================================
create table public.category_groups (
    id         uuid primary key default gen_random_uuid(),
    user_id    uuid references auth.users(id) on delete cascade,
    name       text not null,
    color      text not null,
    icon       text,
    type       text not null check (type in ('EXPENSE', 'INCOME')),
    sort_order integer not null,
    is_system  boolean not null default false,
    created_at bigint not null,
    updated_at bigint not null,
    version    integer not null default 1,
    device_id  text
);

alter table public.category_groups enable row level security;

create policy "category_groups_select" on public.category_groups
    for select using (user_id is null or auth.uid() = user_id);
create policy "category_groups_insert_own" on public.category_groups
    for insert with check (auth.uid() = user_id);
create policy "category_groups_update_own" on public.category_groups
    for update using (auth.uid() = user_id);
create policy "category_groups_delete_own" on public.category_groups
    for delete using (auth.uid() = user_id);

-- ============================================================================
-- categories — nullable user_id = system-seeded category.
-- ============================================================================
create table public.categories (
    id         uuid primary key default gen_random_uuid(),
    user_id    uuid references auth.users(id) on delete cascade,
    group_id   uuid not null references public.category_groups(id) on delete cascade,
    name       text not null,
    icon       text,
    sort_order integer not null,
    is_system  boolean not null default false,
    created_at bigint not null,
    updated_at bigint not null,
    version    integer not null default 1,
    device_id  text
);

alter table public.categories enable row level security;

create policy "categories_select" on public.categories
    for select using (user_id is null or auth.uid() = user_id);
create policy "categories_insert_own" on public.categories
    for insert with check (auth.uid() = user_id);
create policy "categories_update_own" on public.categories
    for update using (auth.uid() = user_id);
create policy "categories_delete_own" on public.categories
    for delete using (auth.uid() = user_id);

-- ============================================================================
-- Generic helper: every table from here down is fully user-owned (required
-- user_id) — the same four-policy shape is repeated per table rather than
-- hidden behind a function, so each table's RLS is visible by reading its own
-- `create policy` lines instead of jumping to a shared definition.
-- ============================================================================

-- ============================================================================
-- accounts
-- ============================================================================
create table public.accounts (
    id                    uuid primary key default gen_random_uuid(),
    user_id               uuid not null references auth.users(id) on delete cascade,
    name                  text not null,
    type                  text not null check (type in (
                              'BANK', 'CASH', 'CREDIT_CARD', 'MOBILE_WALLET', 'SAVINGS',
                              'INVESTMENT', 'LOAN', 'OTHER'
                          )),
    institution_id        uuid references public.institutions(id) on delete set null,
    currency              text not null,
    opening_balance_minor bigint not null,
    is_archived           boolean not null default false,
    created_at            bigint not null,
    updated_at            bigint not null,
    version               integer not null default 1,
    device_id             text
);

alter table public.accounts enable row level security;

create policy "accounts_select_own" on public.accounts
    for select using (auth.uid() = user_id);
create policy "accounts_insert_own" on public.accounts
    for insert with check (auth.uid() = user_id);
create policy "accounts_update_own" on public.accounts
    for update using (auth.uid() = user_id);
create policy "accounts_delete_own" on public.accounts
    for delete using (auth.uid() = user_id);

-- ============================================================================
-- transactions
-- ============================================================================
create table public.transactions (
    id                        uuid primary key default gen_random_uuid(),
    user_id                   uuid not null references auth.users(id) on delete cascade,
    account_id                uuid not null references public.accounts(id) on delete cascade,
    type                      text not null check (type in (
                                  'EXPENSE', 'INCOME', 'TRANSFER', 'REFUND', 'ADJUSTMENT'
                              )),
    amount_minor              bigint not null,
    currency                  text not null,
    category_id               uuid references public.categories(id) on delete set null,
    payee                     text,
    note                      text,
    date                      bigint not null,
    created_at                bigint not null,
    updated_at                bigint not null,
    is_recurring              boolean not null default false,
    deleted_at                bigint,
    transfer_id               uuid references public.transactions(id) on delete set null,
    recurring_transaction_id  uuid,
    source                    text not null default 'MANUAL' check (source in (
                                  'MANUAL', 'SMS', 'NOTIFICATION'
                              )),
    source_reference          text,
    place                     text,
    version                   integer not null default 1,
    device_id                 text
);

alter table public.transactions enable row level security;

create policy "transactions_select_own" on public.transactions
    for select using (auth.uid() = user_id);
create policy "transactions_insert_own" on public.transactions
    for insert with check (auth.uid() = user_id);
create policy "transactions_update_own" on public.transactions
    for update using (auth.uid() = user_id);
create policy "transactions_delete_own" on public.transactions
    for delete using (auth.uid() = user_id);

-- ============================================================================
-- transaction_splits
-- ============================================================================
create table public.transaction_splits (
    id             uuid primary key default gen_random_uuid(),
    user_id        uuid not null references auth.users(id) on delete cascade,
    transaction_id uuid not null references public.transactions(id) on delete cascade,
    category_id    uuid not null references public.categories(id) on delete cascade,
    amount_minor   bigint not null,
    note           text,
    created_at     bigint not null,
    updated_at     bigint not null,
    version        integer not null default 1,
    device_id      text
);

alter table public.transaction_splits enable row level security;

create policy "transaction_splits_select_own" on public.transaction_splits
    for select using (auth.uid() = user_id);
create policy "transaction_splits_insert_own" on public.transaction_splits
    for insert with check (auth.uid() = user_id);
create policy "transaction_splits_update_own" on public.transaction_splits
    for update using (auth.uid() = user_id);
create policy "transaction_splits_delete_own" on public.transaction_splits
    for delete using (auth.uid() = user_id);

-- ============================================================================
-- labels
-- ============================================================================
create table public.labels (
    id         uuid primary key default gen_random_uuid(),
    user_id    uuid not null references auth.users(id) on delete cascade,
    name       text not null,
    color      text not null,
    created_at bigint not null,
    updated_at bigint not null,
    version    integer not null default 1,
    device_id  text
);

alter table public.labels enable row level security;

create policy "labels_select_own" on public.labels
    for select using (auth.uid() = user_id);
create policy "labels_insert_own" on public.labels
    for insert with check (auth.uid() = user_id);
create policy "labels_update_own" on public.labels
    for update using (auth.uid() = user_id);
create policy "labels_delete_own" on public.labels
    for delete using (auth.uid() = user_id);

-- ============================================================================
-- transaction_labels — join table; local PK is composite (transactionId,
-- labelId), mirrored here as a unique constraint over a surrogate id.
-- ============================================================================
create table public.transaction_labels (
    id             uuid primary key default gen_random_uuid(),
    user_id        uuid not null references auth.users(id) on delete cascade,
    transaction_id uuid not null references public.transactions(id) on delete cascade,
    label_id       uuid not null references public.labels(id) on delete cascade,
    created_at     bigint not null,
    updated_at     bigint not null,
    version        integer not null default 1,
    device_id      text,
    unique (transaction_id, label_id)
);

alter table public.transaction_labels enable row level security;

create policy "transaction_labels_select_own" on public.transaction_labels
    for select using (auth.uid() = user_id);
create policy "transaction_labels_insert_own" on public.transaction_labels
    for insert with check (auth.uid() = user_id);
create policy "transaction_labels_update_own" on public.transaction_labels
    for update using (auth.uid() = user_id);
create policy "transaction_labels_delete_own" on public.transaction_labels
    for delete using (auth.uid() = user_id);

-- ============================================================================
-- budgets
-- ============================================================================
create table public.budgets (
    id           uuid primary key default gen_random_uuid(),
    user_id      uuid not null references auth.users(id) on delete cascade,
    name         text not null,
    period       text not null check (period in ('WEEKLY', 'MONTHLY', 'CUSTOM')),
    start_date   bigint not null,
    end_date     bigint not null,
    amount_minor bigint not null,
    currency     text not null,
    created_at   bigint not null,
    updated_at   bigint not null,
    version      integer not null default 1,
    device_id    text
);

alter table public.budgets enable row level security;

create policy "budgets_select_own" on public.budgets
    for select using (auth.uid() = user_id);
create policy "budgets_insert_own" on public.budgets
    for insert with check (auth.uid() = user_id);
create policy "budgets_update_own" on public.budgets
    for update using (auth.uid() = user_id);
create policy "budgets_delete_own" on public.budgets
    for delete using (auth.uid() = user_id);

-- ============================================================================
-- budget_categories — join table; local PK is composite (budgetId, categoryId).
-- ============================================================================
create table public.budget_categories (
    id          uuid primary key default gen_random_uuid(),
    user_id     uuid not null references auth.users(id) on delete cascade,
    budget_id   uuid not null references public.budgets(id) on delete cascade,
    category_id uuid not null references public.categories(id) on delete cascade,
    limit_minor bigint not null,
    created_at  bigint not null,
    updated_at  bigint not null,
    version     integer not null default 1,
    device_id   text,
    unique (budget_id, category_id)
);

alter table public.budget_categories enable row level security;

create policy "budget_categories_select_own" on public.budget_categories
    for select using (auth.uid() = user_id);
create policy "budget_categories_insert_own" on public.budget_categories
    for insert with check (auth.uid() = user_id);
create policy "budget_categories_update_own" on public.budget_categories
    for update using (auth.uid() = user_id);
create policy "budget_categories_delete_own" on public.budget_categories
    for delete using (auth.uid() = user_id);

-- ============================================================================
-- recurring_transactions
-- ============================================================================
create table public.recurring_transactions (
    id          uuid primary key default gen_random_uuid(),
    user_id     uuid not null references auth.users(id) on delete cascade,
    account_id  uuid not null references public.accounts(id) on delete cascade,
    category_id uuid references public.categories(id) on delete set null,
    amount_minor bigint not null,
    currency    text not null,
    frequency   text not null check (frequency in ('DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY')),
    next_date   bigint not null,
    end_date    bigint,
    type        text not null check (type in (
                    'EXPENSE', 'INCOME', 'TRANSFER', 'REFUND', 'ADJUSTMENT'
                )),
    payee       text,
    note        text,
    is_active   boolean not null default true,
    auto_post   boolean not null default true,
    created_at  bigint not null,
    updated_at  bigint not null,
    version     integer not null default 1,
    device_id   text
);

alter table public.recurring_transactions enable row level security;

create policy "recurring_transactions_select_own" on public.recurring_transactions
    for select using (auth.uid() = user_id);
create policy "recurring_transactions_insert_own" on public.recurring_transactions
    for insert with check (auth.uid() = user_id);
create policy "recurring_transactions_update_own" on public.recurring_transactions
    for update using (auth.uid() = user_id);
create policy "recurring_transactions_delete_own" on public.recurring_transactions
    for delete using (auth.uid() = user_id);

-- ============================================================================
-- goals
-- ============================================================================
create table public.goals (
    id                   uuid primary key default gen_random_uuid(),
    user_id              uuid not null references auth.users(id) on delete cascade,
    name                 text not null,
    target_amount_minor  bigint not null,
    current_amount_minor bigint not null,
    currency             text not null,
    target_date          bigint,
    account_id           uuid references public.accounts(id) on delete set null,
    created_at           bigint not null,
    updated_at           bigint not null,
    version              integer not null default 1,
    device_id            text
);

alter table public.goals enable row level security;

create policy "goals_select_own" on public.goals
    for select using (auth.uid() = user_id);
create policy "goals_insert_own" on public.goals
    for insert with check (auth.uid() = user_id);
create policy "goals_update_own" on public.goals
    for update using (auth.uid() = user_id);
create policy "goals_delete_own" on public.goals
    for delete using (auth.uid() = user_id);

-- ============================================================================
-- goal_contributions
-- ============================================================================
create table public.goal_contributions (
    id           uuid primary key default gen_random_uuid(),
    user_id      uuid not null references auth.users(id) on delete cascade,
    goal_id      uuid not null references public.goals(id) on delete cascade,
    amount_minor bigint not null,
    date         bigint not null,
    note         text,
    created_at   bigint not null,
    updated_at   bigint not null,
    version      integer not null default 1,
    device_id    text
);

alter table public.goal_contributions enable row level security;

create policy "goal_contributions_select_own" on public.goal_contributions
    for select using (auth.uid() = user_id);
create policy "goal_contributions_insert_own" on public.goal_contributions
    for insert with check (auth.uid() = user_id);
create policy "goal_contributions_update_own" on public.goal_contributions
    for update using (auth.uid() = user_id);
create policy "goal_contributions_delete_own" on public.goal_contributions
    for delete using (auth.uid() = user_id);

-- ============================================================================
-- merchants
-- ============================================================================
create table public.merchants (
    id             uuid primary key default gen_random_uuid(),
    user_id        uuid not null references auth.users(id) on delete cascade,
    canonical_name text not null,
    created_at     bigint not null,
    updated_at     bigint not null,
    version        integer not null default 1,
    device_id      text
);

alter table public.merchants enable row level security;

create policy "merchants_select_own" on public.merchants
    for select using (auth.uid() = user_id);
create policy "merchants_insert_own" on public.merchants
    for insert with check (auth.uid() = user_id);
create policy "merchants_update_own" on public.merchants
    for update using (auth.uid() = user_id);
create policy "merchants_delete_own" on public.merchants
    for delete using (auth.uid() = user_id);

-- ============================================================================
-- merchant_aliases — local PK is composite (merchantId, alias).
-- ============================================================================
create table public.merchant_aliases (
    id          uuid primary key default gen_random_uuid(),
    user_id     uuid not null references auth.users(id) on delete cascade,
    merchant_id uuid not null references public.merchants(id) on delete cascade,
    alias       text not null,
    created_at  bigint not null,
    updated_at  bigint not null,
    version     integer not null default 1,
    device_id   text,
    unique (merchant_id, alias)
);

alter table public.merchant_aliases enable row level security;

create policy "merchant_aliases_select_own" on public.merchant_aliases
    for select using (auth.uid() = user_id);
create policy "merchant_aliases_insert_own" on public.merchant_aliases
    for insert with check (auth.uid() = user_id);
create policy "merchant_aliases_update_own" on public.merchant_aliases
    for update using (auth.uid() = user_id);
create policy "merchant_aliases_delete_own" on public.merchant_aliases
    for delete using (auth.uid() = user_id);

-- ============================================================================
-- templates
-- ============================================================================
create table public.templates (
    id          uuid primary key default gen_random_uuid(),
    user_id     uuid not null references auth.users(id) on delete cascade,
    name        text not null,
    account_id  uuid not null references public.accounts(id) on delete cascade,
    category_id uuid not null references public.categories(id) on delete cascade,
    label_id    uuid not null references public.labels(id) on delete cascade,
    payee       text,
    place       text,
    created_at  bigint not null,
    updated_at  bigint not null,
    version     integer not null default 1,
    device_id   text
);

alter table public.templates enable row level security;

create policy "templates_select_own" on public.templates
    for select using (auth.uid() = user_id);
create policy "templates_insert_own" on public.templates
    for insert with check (auth.uid() = user_id);
create policy "templates_update_own" on public.templates
    for update using (auth.uid() = user_id);
create policy "templates_delete_own" on public.templates
    for delete using (auth.uid() = user_id);

-- ============================================================================
-- notifications — includes DEBT_DUE_SOON/DEBT_SETTLED ahead of the local Debt
-- feature existing, so no further migration is needed once it's built.
-- ============================================================================
create table public.notifications (
    id                  uuid primary key default gen_random_uuid(),
    user_id             uuid not null references auth.users(id) on delete cascade,
    type                text not null check (type in (
                            'TRANSACTION_CAPTURED', 'TRANSACTION_NEEDS_REVIEW', 'BUDGET_WARNING',
                            'BUDGET_EXCEEDED', 'RECURRING_DUE', 'GOAL_MILESTONE',
                            'POSSIBLE_DUPLICATE', 'SYNC_CONFLICT', 'SYSTEM',
                            'DEBT_DUE_SOON', 'DEBT_SETTLED'
                        )),
    title               text not null,
    body                text not null,
    deep_link           text,
    created_at          bigint not null,
    read_at             bigint,
    related_entity_type text,
    related_entity_id   text,
    updated_at          bigint not null,
    version             integer not null default 1,
    device_id           text
);

alter table public.notifications enable row level security;

create policy "notifications_select_own" on public.notifications
    for select using (auth.uid() = user_id);
create policy "notifications_insert_own" on public.notifications
    for insert with check (auth.uid() = user_id);
create policy "notifications_update_own" on public.notifications
    for update using (auth.uid() = user_id);
create policy "notifications_delete_own" on public.notifications
    for delete using (auth.uid() = user_id);

-- ============================================================================
-- debts — plan.md §88. No local Room entity/UI yet; table is ready ahead of
-- that feature landing.
-- ============================================================================
create table public.debts (
    id                 uuid primary key default gen_random_uuid(),
    user_id            uuid not null references auth.users(id) on delete cascade,
    direction          text not null check (direction in ('I_OWE', 'OWED_TO_ME')),
    counterparty_name  text not null,
    counterparty_type  text not null check (counterparty_type in (
                           'PERSON', 'BANK', 'MOBILE_WALLET', 'OTHER'
                       )),
    principal_amount_minor bigint not null,
    currency           text not null,
    purpose            text,
    note               text,
    linked_account_id  uuid references public.accounts(id) on delete set null,
    incurred_date      bigint not null,
    due_date           bigint,
    status             text not null default 'OPEN' check (status in ('OPEN', 'SETTLED')),
    created_at         bigint not null,
    updated_at         bigint not null,
    version            integer not null default 1,
    device_id          text
);

alter table public.debts enable row level security;

create policy "debts_select_own" on public.debts
    for select using (auth.uid() = user_id);
create policy "debts_insert_own" on public.debts
    for insert with check (auth.uid() = user_id);
create policy "debts_update_own" on public.debts
    for update using (auth.uid() = user_id);
create policy "debts_delete_own" on public.debts
    for delete using (auth.uid() = user_id);

-- ============================================================================
-- debt_payments — plan.md §88.
-- ============================================================================
create table public.debt_payments (
    id                     uuid primary key default gen_random_uuid(),
    user_id                uuid not null references auth.users(id) on delete cascade,
    debt_id                uuid not null references public.debts(id) on delete cascade,
    amount_minor           bigint not null,
    date                   bigint not null,
    note                   text,
    linked_transaction_id  uuid references public.transactions(id) on delete set null,
    created_at             bigint not null,
    updated_at             bigint not null,
    version                integer not null default 1,
    device_id              text
);

alter table public.debt_payments enable row level security;

create policy "debt_payments_select_own" on public.debt_payments
    for select using (auth.uid() = user_id);
create policy "debt_payments_insert_own" on public.debt_payments
    for insert with check (auth.uid() = user_id);
create policy "debt_payments_update_own" on public.debt_payments
    for update using (auth.uid() = user_id);
create policy "debt_payments_delete_own" on public.debt_payments
    for delete using (auth.uid() = user_id);

-- ============================================================================
-- Indexes — every foreign key used in a WHERE/JOIN gets one; Postgres does not
-- auto-index FK columns the way it does primary keys.
-- ============================================================================
create index idx_accounts_user_id on public.accounts(user_id);
create index idx_accounts_institution_id on public.accounts(institution_id);
create index idx_transactions_user_id on public.transactions(user_id);
create index idx_transactions_account_id on public.transactions(account_id);
create index idx_transactions_category_id on public.transactions(category_id);
create index idx_transaction_splits_transaction_id on public.transaction_splits(transaction_id);
create index idx_transaction_splits_category_id on public.transaction_splits(category_id);
create index idx_categories_group_id on public.categories(group_id);
create index idx_transaction_labels_transaction_id on public.transaction_labels(transaction_id);
create index idx_transaction_labels_label_id on public.transaction_labels(label_id);
create index idx_budget_categories_budget_id on public.budget_categories(budget_id);
create index idx_budget_categories_category_id on public.budget_categories(category_id);
create index idx_recurring_transactions_account_id on public.recurring_transactions(account_id);
create index idx_goal_contributions_goal_id on public.goal_contributions(goal_id);
create index idx_merchant_aliases_merchant_id on public.merchant_aliases(merchant_id);
create index idx_templates_account_id on public.templates(account_id);
create index idx_notifications_user_id on public.notifications(user_id);
create index idx_debts_user_id on public.debts(user_id);
create index idx_debt_payments_debt_id on public.debt_payments(debt_id);

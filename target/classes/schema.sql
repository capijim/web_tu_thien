create table if not exists donations (
  id bigserial primary key,
  donor_name varchar(255) not null,
  amount numeric(12,2) not null,
  message text,
  created_at timestamptz not null default now()
);



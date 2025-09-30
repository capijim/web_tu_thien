create table if not exists donations (
  id bigint primary key auto_increment,
  donor_name varchar(255) not null,
  amount decimal(12,2) not null,
  message text,
  created_at timestamp not null default current_timestamp
);



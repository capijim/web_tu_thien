create table if not exists users (
  id bigint primary key auto_increment,
  name varchar(255) not null,
  email varchar(255) not null unique,
  password varchar(255) not null,
  created_at timestamp not null default current_timestamp
);

create table if not exists campaigns (
  id bigint primary key auto_increment,
  user_id bigint not null,
  title varchar(255) not null,
  description text not null,
  target_amount decimal(12,2) not null,
  current_amount decimal(12,2) default 0,
  category varchar(100) not null,
  image_url varchar(500),
  status varchar(20) default 'active',
  end_date timestamp,
  created_at timestamp not null default current_timestamp,
  foreign key (user_id) references users(id)
);

create table if not exists donations (
  id bigint primary key auto_increment,
  campaign_id bigint not null,
  donor_name varchar(255) not null,
  amount decimal(12,2) not null,
  message text,
  created_at timestamp not null default current_timestamp,
  foreign key (campaign_id) references campaigns(id)
);



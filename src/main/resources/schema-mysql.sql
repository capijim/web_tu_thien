create table if not exists users (
  id bigint primary key auto_increment,
  name varchar(255) not null,
  email varchar(255) not null unique,
  password varchar(255) not null,
  created_at timestamp not null default current_timestamp
);

create table if not exists partners (
  id bigint primary key auto_increment,
  name varchar(255) not null,
  email varchar(255) unique,
  phone varchar(50),
  address varchar(500),
  created_at timestamp not null default current_timestamp
);

create table if not exists campaigns (
  id bigint primary key auto_increment,
  partner_id bigint not null,
  title varchar(255) not null,
  description text not null,
  target_amount decimal(12,2) not null,
  current_amount decimal(12,2) default 0,
  category varchar(100) not null,
  image_url varchar(500),
  status varchar(20) default 'active',
  end_date timestamp,
  created_at timestamp not null default current_timestamp,
  foreign key (partner_id) references partners(id)
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

create table if not exists admins (
  id bigint primary key auto_increment,
  username varchar(100) not null unique,
  email varchar(255) not null unique,
  password varchar(255) not null,
  full_name varchar(255) not null,
  is_active boolean default true,
  created_at timestamp not null default current_timestamp,
  updated_at timestamp not null default current_timestamp on update current_timestamp
);

create table if not exists payments (
  id bigint primary key auto_increment,
  donation_id bigint,
  vnpay_transaction_id varchar(100),
  vnpay_response_code varchar(10),
  vnpay_txn_ref varchar(100) not null unique,
  amount decimal(12,2) not null,
  bank_code varchar(50),
  payment_status varchar(20) not null default 'PENDING',
  created_at timestamp not null default current_timestamp,
  updated_at timestamp not null default current_timestamp on update current_timestamp,
  foreign key (donation_id) references donations(id)
);


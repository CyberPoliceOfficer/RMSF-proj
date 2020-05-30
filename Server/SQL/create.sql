create table Nodes
    (serial_number  varchar(16),
     localization   varchar(100),
     primary key(serial_number)
    );


create table Measurements
   (serial_number  varchar(16),
    timestamp_ timestamp,
    hotspots numeric(8,2) ARRAY[9],
    temperature numeric(8,2),
    rpm numeric(8,2),
    primary key(serial_number, timestamp_),
    foreign key(serial_number) references Nodes(serial_number) on update cascade on delete cascade
   );

create table Thresholds
   (serial_number  varchar(16),
    relay_on boolean,
    fan_threshold numeric(3,0),
    relay_threshold numeric(3,0),
    primary key(serial_number),
    foreign key(serial_number) references Nodes(serial_number) on update cascade on delete cascade
	);
    
create table Alarms
   (serial_number  varchar(16),
    start_time timestamp,
    primary key(serial_number, start_time),
    foreign key(serial_number) references Nodes(serial_number) on update cascade on delete cascade
	);
    
create table Controller
   (serial_number  varchar(16),
    kp numeric(3,0),
    ki numeric(3,0),
    primary key(serial_number),
    foreign key(serial_number) references Nodes(serial_number) on update cascade on delete cascade
	);

create table Potencial_Users
   (email  varchar(500), 
    password_  varchar(100),
    primary key(email)
   );

create table Users
   (email  varchar(500), 
    password_  varchar(100),
    name varchar(500),
    is_admin boolean,
    primary key(email),
    foreign key(email) references Potencial_Users(email) on update cascade on delete cascade
   );


insert into Potencial_Users values ('vale@gmail.com', 'Corona');
insert into Potencial_Users values ('correia@gmail.com', 'FunTimes');
insert into Potencial_Users values ('noone@hotmail.com', 'password');
insert into Potencial_Users values ('extra@hotmail.com', 'password1');

insert into Users values ('vale@gmail.com', 'Corona', 'JoaoVale', true);
insert into Users values ('correia@gmail.com', 'FunTimes', 'JoaoCorreia', true);
insert into Users values ('noone@hotmail.com', 'password', 'NoOne', false);

insert into Nodes values ('00033593D90D285E', 'Bucelas,Loures');

insert into Thresholds values ('00033593D90D285E', true, 35, 80);

insert into Controller values ('00033593D90D285E', -100, -1);








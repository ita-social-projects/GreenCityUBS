INSERT INTO languages(id,code)
VALUES (1,'ua'),
       (2,'en'),
       (3,'ru');

INSERT INTO users(id,current_points, uuid, violations)
VALUES (1,1000,'uuid1',0),
       (2,1000,'uuid2',0),
       (3,1000,'uuid3',0),
       (4,1000,'uuid4',0),
       (5,1000,'uuid5',0),
       (6,1000,'uuid6',0),
       (7,1000,'uuid7',0),
       (8,1000,'uuid8',0),
       (9,1000,'uuid9',0),
       (10,1000,'uuid10',0);

INSERT INTO ubs_user(id,email,first_name,last_name,phone_number,users_id,address_id)
VALUES (1,'first.mike@gmail.com','Mike','Miopes','095634873',1,null),
       (2,'second.ozil@gmail.com','Ozil','Golden','062124873',2,null),
       (3,'third.max@gmail.com','Max','Basket','063634433',3,null),
       (4,'fourth.lio@gmail.com','Lio','Melor','044634543',4,null),
       (5,'fifth.jack@gmail.com','Jack','Grindon','095634433',5,null),
       (6,'sixth.katy@gmail.com','Katy','Perry','076623873',6,null),
       (7,'seventh.richard@gmail.com','Richard','Tolkon','085874873',7,null),
       (8,'eighth.oliver@gmail.com','Oliver','Gastambide','065474873',8,null),
       (9,'nineth.gregor@gmail.com','Gregor','Tutson','081704873',9,null),
       (10,'tenth.siona@gmail.com','Siona','Milletta','085874873',10,null);


INSERT INTO orders
VALUES  (1,'order comment',null,null,null,'2021-03-13 10:03:38.289346','FORMED',0,null,1,1,null),
        (2,'order comment',null,null,null,'2021-03-14 11:00:00.000000','FORMED',0,null,2,2,null),
        (3,'order comment',null,null,null,'2021-03-15 12:00:00.000000','FORMED',0,null,3,3,null),
        (4,'order comment',null,null,null,'2021-03-16 13:00:00.000000','FORMED',0,null,4,4,null),
        (5,'order comment',null,null,null,'2021-03-17 14:00:00.000000','FORMED',0,null,5,5,null),
        (6,'order comment',null,null,null,'2021-03-18 15:00:00.000000','FORMED',0,null,6,6,null),
        (7,'order comment',null,null,null,'2021-03-19 16:00:00.000000','FORMED',0,null,7,7,null),
        (8,'order comment',null,null,null,'2021-03-20 17:00:00.000000','FORMED',0,null,8,8,null),
        (9,'order comment',null,null,null,'2021-03-21 18:00:00.000000','FORMED',0,null,7,7,null),
        (10,'order comment',null,null,null,'2021-03-22 19:00:00.000000','FORMED',0,null,8,8,null);

INSERT INTO certificate (code,status,expiration_date,points,order_id,creation_date)
VALUES ('1234-1234','ACTIVE','2021-05-01',100,1,'2021-04-01'),
       ('1235-1235','ACTIVE','2021-05-02',200,1,'2021-04-02'),
       ('1236-1236','ACTIVE','2021-05-03',300,3,'2021-04-03'),
       ('1237-1237','ACTIVE','2021-05-04',400,4,'2021-04-04'),

       ('1238-1238','USED','2021-05-01',500,2,'2021-04-01'),
       ('1239-1239','USED','2021-05-02',600,6,'2021-04-02'),
       ('1240-1240','USED','2021-05-03',700,6,'2021-04-03'),

       ('1241-1241','EXPIRED','2021-04-05',800,5,'2021-03-05'),
       ('1242-1242','EXPIRED','2021-05-03',900,5,'2021-04-03'),
       ('1243-1243','EXPIRED','2021-05-04',100,7,'2021-04-04');

INSERT INTO bag (id,capacity,price)
 VALUES (1,100,400),
        (2,100,600),
        (3,100,400);

INSERT INTO bag_translations(id,name, bag_id, language_id)
VALUES (1,'Бавовняна сумка',1,1),
       (2,'Березентова сумка',2,1),
       (3,'Сумка з бамбукової тканини',3,1),
       (4,'Cotton bag',1,2),
       (5,'Tarpaulin bag',2,2),
       (6,'Bamboo fabric bag',3,2),
       (7,'Хлопчатобумажная сумка',1,3),
       (8,'Березентова сумка',2,3),
       (9,'Сумка из бамбуковой ткани',3,3);


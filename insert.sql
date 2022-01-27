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

INSERT INTO courier values (1,2,30,250,500000,1,'LIMIT_BY_AMOUNT_OF_BAG');

INSERT INTO courier_translations
values (1,'УБС-КУР''ЄР',1,1,'Опис лімітів'),
       (2,'UBS-COURIER',1,1,'Description limit');

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

INSERT INTO certificate (code,status,expiration_date,points,order_id,creation_date,date_of_use)
VALUES ('1244-1244','ACTIVE','2021-05-01',100,1,'2021-04-01','2021-04-01'),
       ('1245-1245','ACTIVE','2021-05-03',200,1,'2021-04-02','2021-05-25'),

       ('1246-1246','USED','2021-05-11',500,2,'2021-04-20','2021-05-30'),
       ('1247-1247','USED','2021-05-19',600,6,'2021-04-30','2021-04-25'),

       ('1248-1248','EXPIRED','2021-08-05',800,5,'2021-03-05','2021-05-10'),
       ('1249-1249','EXPIRED','2021-05-03',900,5,'2021-04-03','2021-01-25');


INSERT INTO locations(id,location_status)
VALUES (1,'ACTIVE');

INSERT INTO bag (id,capacity,price,location_id)
 VALUES (1,100,400,1),
        (2,100,600,1),
        (3,100,400,1);

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

INSERT INTO receiving_stations (id, name)
VALUES (1, 'Саперно-Слобідська'),
       (2, 'Петрівка');

INSERT INTO positions (id, name)
VALUES (1, 'Менеджер обдзвону'),
       (2, 'Менеджер послуги'),
       (3, 'Логіст'),
       (4, 'Штурман'),
       (5, 'Водій');


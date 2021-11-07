-- This SQL script delete all UBS User's;

-- Drop all constraint for change constraint type to cascade;
alter table orders drop constraint fk1evtdroba5rlynltqtprpcaay;

alter table orders
    add constraint fk1evtdroba5rlynltqtprpcaay
        foreign key (ubs_user_id) references ubs_user
            on delete cascade ;

alter table events drop constraint fk_events_order_id;

alter table events add constraint fk_events_order_id
    foreign key (order_id) references orders
        on delete cascade;

alter table order_additional drop constraint fkpry0lrfjcpcpjo0wl6w6spscy;

alter table order_additional
    add constraint fkpry0lrfjcpcpjo0wl6w6spscy
        foreign key (orders_id) references orders
            on delete cascade;

alter table order_bag_mapping drop constraint fkpkamv5em3c345mcyo3b6gr50a;

alter table order_bag_mapping
    add constraint fkpkamv5em3c345mcyo3b6gr50a
        foreign key (order_id) references orders
            on delete cascade;

alter table payment drop constraint fk_payment_to_order;

alter table payment
    add constraint fk_payment_to_order
        foreign key (order_id) references orders
            on delete cascade ;

alter table user_notifications drop constraint fk_user_notifications_order_id;

alter table user_notifications
    add constraint fk_user_notifications_order_id
        foreign key (order_id) references orders
            on DELETE cascade;

alter table violations_description_mapping drop constraint fk5d5vb0bsddsipw5kdyd5bhfx;

alter table violations_description_mapping
    add constraint fk5d5vb0bsddsipw5kdyd5bhfx
        foreign key (order_id) references orders
            on update restrict on delete cascade;

alter table certificate drop constraint fkpk7mwwg8e6a5owtr4bmxpum7b;

alter table certificate
    add constraint fkpk7mwwg8e6a5owtr4bmxpum7b
        foreign key(order_id) references orders
            on delete cascade ;

alter table change_of_points drop constraint fk5s2hfa0bsddsipw5kdyd5bhfx;

alter table change_of_points
    add constraint fk5s2hfa0bsddsipw5kdyd5bhfx
        foreign key (order_id) references orders
            on update restrict on delete cascade ;

alter table notification_parameters drop constraint fk_notification_id;

alter table notification_parameters
    add constraint fk_notification_id
        foreign key (notification_id) references user_notifications
            on delete cascade ;

alter table violation_images drop constraint fk_violation_image_mapping;

alter table violation_images
    add constraint fk_violation_image_mapping
        foreign key (violation_id) references violations_description_mapping
            on DELETE cascade ;

-- Now we can delete all data and reference from table ubs_users and
delete  from ubs_user;


-- Restore all references to previous state
alter table orders drop constraint fk1evtdroba5rlynltqtprpcaay;

alter table orders
    add constraint fk1evtdroba5rlynltqtprpcaay
        foreign key (ubs_user_id) references ubs_user;

alter table events drop constraint fk_events_order_id;
alter table events add constraint fk_events_order_id
    foreign key (order_id) references orders;

alter table order_additional drop constraint fkpry0lrfjcpcpjo0wl6w6spscy;

alter table order_additional
    add constraint fkpry0lrfjcpcpjo0wl6w6spscy
        foreign key (orders_id) references orders;

alter table order_bag_mapping drop constraint fkpkamv5em3c345mcyo3b6gr50a;

alter table order_bag_mapping
    add constraint fkpkamv5em3c345mcyo3b6gr50a
        foreign key (order_id) references orders;

alter table payment drop constraint fk_payment_to_order;

alter table payment
    add constraint fk_payment_to_order
        foreign key (order_id) references orders;

alter table user_notifications drop constraint fk_user_notifications_order_id;

alter table user_notifications
    add constraint fk_user_notifications_order_id
        foreign key (order_id) references orders;

alter table violations_description_mapping drop constraint fk5d5vb0bsddsipw5kdyd5bhfx;

alter table violations_description_mapping
    add constraint fk5d5vb0bsddsipw5kdyd5bhfx
        foreign key (order_id) references orders
            on update restrict on delete restrict;

alter table certificate drop constraint fkpk7mwwg8e6a5owtr4bmxpum7b;

alter table certificate
    add constraint fkpk7mwwg8e6a5owtr4bmxpum7b
        foreign key(order_id) references orders;

alter table change_of_points drop constraint fk5s2hfa0bsddsipw5kdyd5bhfx;

alter table change_of_points
    add constraint fk5s2hfa0bsddsipw5kdyd5bhfx
        foreign key (order_id) references orders
            on update restrict on delete restrict;

alter table notification_parameters drop constraint fk_notification_id;

alter table notification_parameters
    add constraint fk_notification_id
        foreign key (notification_id) references user_notifications;

alter table violation_images drop constraint fk_violation_image_mapping;

alter table violation_images
    add constraint fk_violation_image_mapping
        foreign key (violation_id) references violations_description_mapping;
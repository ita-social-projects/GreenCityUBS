INSERT INTO public.users (id, current_points, uuid, violations, recipient_name, recipient_surname, recipient_email, recipient_phone,
                          last_order_location, date_of_registration)
VALUES (1, 0, 'a3669bb0-842d-11ec-a8a3-0242ac120002', 0, 'Ivan', 'Ivanov', 'ivan@gmail.com', '+380981099667', 1, null);

INSERT INTO public.address (id, city, address_comment, latitude, longitude, district, entrance_number, house_corpus,
                            house_number, street, actual, user_id, status, region, city_en, region_en, street_en, district_en)
VALUES (1, 'Київ', '', 50.446509500000005, 30.510173, 'Шевченківський', '2', '44', '3', 'Богдана Хмельницького вулиця', true, 1, 'IN_ORDER',
        'Київська область', 'Kyiv', 'Kyiv region', 'Bohdana Khmelnytskoho Street', 'Shevchenkivskyi');

INSERT INTO public.ubs_user (id, email, first_name, last_name, phone_number, users_id, address_id)
VALUES (1, 'ivan@gmail.com', 'Ivan', 'Ivanov', '++380981099667', 1, 1);

INSERT INTO public.orders (id, comment, deliver_from, deliver_to, note, order_date, order_status, points_to_use,
                           receiving_station, ubs_user_id, users_id, order_payment_status, cancellation_reason,
                           cancellation_comment, reason_not_taking_bag_description, image_reason_not_taking_bags,
                           date_of_export, employee_id, blocked, admin_comment, counter_order_payment_id, courier_locations_id,
                           sum_total_amount_without_discounts)
VALUES (1, '', null, null, null, '2022-01-22 18:25:05.536212', 'FORMED', 0, null, 1, 1, 'PAID', null, null, null, '', null, null, false, '', null, 1, null);
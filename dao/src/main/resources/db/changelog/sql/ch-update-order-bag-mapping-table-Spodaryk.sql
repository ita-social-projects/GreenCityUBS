UPDATE order_bag_mapping
SET capacity = bag.capacity,
    price = bag.full_price,
    name = bag.name,
    name_eng = bag.name_eng
    FROM bag
WHERE order_bag_mapping.bag_id = bag.id;

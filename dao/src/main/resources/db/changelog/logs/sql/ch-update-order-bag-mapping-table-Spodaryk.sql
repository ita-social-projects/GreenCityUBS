update order_bag_mapping
set capacity=bag.capacity,
    price=bag.full_price,
    name=bag.name,
    name_eng=bag.name_eng from bag
where bag_id=bag.id
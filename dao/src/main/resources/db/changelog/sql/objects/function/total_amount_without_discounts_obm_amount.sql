CREATE
OR REPLACE FUNCTION public.total_amount_without_discounts_obm_amount()
RETURNS trigger
LANGUAGE plpgsql
AS $function$
BEGIN
    IF
((TG_OP = 'UPDATE' OR TG_OP = 'INSERT') AND (old.amount IS NOT NULL OR new.amount IS NOT NULL)) THEN
UPDATE orders o
SET sum_total_amount_without_discounts = (SELECT SUM(obm.amount * b.full_price)
                                          FROM order_bag_mapping obm
                                                   INNER JOIN bag b ON obm.bag_id = b.id
                                          WHERE order_id = o.id)
WHERE o.id = old.order_id
   OR o.id = new.order_id;
RETURN NEW;
END IF;
END;
$function$;

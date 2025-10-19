-- RANGE BETWEEN for value-based windows instead of row-based
SELECT
    order_date,
    product_id,
    quantity_sold,
    unit_price,
    quantity_sold * unit_price AS revenue,
    SUM(quantity_sold * unit_price) OVER (
        PARTITION BY product_id
        ORDER BY order_date
        RANGE BETWEEN INTERVAL '7 days' PRECEDING AND CURRENT ROW
    ) AS revenue_last_7days,
    AVG(quantity_sold) OVER (
        PARTITION BY product_id
        ORDER BY order_date
        RANGE BETWEEN INTERVAL '30 days' PRECEDING AND CURRENT ROW
    ) AS avg_quantity_30days,
    COUNT(*) OVER (
        PARTITION BY product_id
        ORDER BY order_date
        RANGE BETWEEN INTERVAL '30 days' PRECEDING AND CURRENT ROW
    ) AS order_count_30days,
    MAX(unit_price) OVER (
        PARTITION BY product_id
        ORDER BY order_date
        RANGE BETWEEN INTERVAL '90 days' PRECEDING AND CURRENT ROW
    ) AS max_price_90days
FROM product_orders
WHERE order_date >= '2024-01-01'
    AND order_status = 'completed'
ORDER BY product_id, order_date

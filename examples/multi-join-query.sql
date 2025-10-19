SELECT
    c.customer_name,
    c.email,
    p.product_name,
    p.category,
    oi.quantity,
    oi.unit_price,
    oi.quantity * oi.unit_price as line_total,
    o.order_date,
    o.shipping_address
FROM customers c
INNER JOIN orders o ON c.customer_id = o.customer_id
INNER JOIN order_items oi ON o.order_id = oi.order_id
INNER JOIN products p ON oi.product_id = p.product_id
WHERE o.order_date >= '2024-01-01'
    AND p.category IN ('Electronics', 'Computers')
    AND oi.quantity > 1
ORDER BY o.order_date DESC, line_total DESC

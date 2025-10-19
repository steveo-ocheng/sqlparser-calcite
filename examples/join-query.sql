SELECT
    u.username,
    u.email,
    o.order_id,
    o.order_date,
    o.total_amount,
    o.status
FROM users u
INNER JOIN orders o ON u.user_id = o.user_id
WHERE o.order_date >= '2024-01-01'
    AND o.total_amount > 100
    AND u.country = 'USA'
ORDER BY o.order_date DESC, o.total_amount DESC

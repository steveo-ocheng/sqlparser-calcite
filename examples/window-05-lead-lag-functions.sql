-- LEAD and LAG for comparing adjacent rows
SELECT
    customer_id,
    transaction_date,
    transaction_amount,
    transaction_type,
    LAG(transaction_amount, 1) OVER (PARTITION BY customer_id ORDER BY transaction_date) AS previous_amount,
    LEAD(transaction_amount, 1) OVER (PARTITION BY customer_id ORDER BY transaction_date) AS next_amount,
    LAG(transaction_date, 1) OVER (PARTITION BY customer_id ORDER BY transaction_date) AS previous_date,
    LEAD(transaction_date, 1) OVER (PARTITION BY customer_id ORDER BY transaction_date) AS next_date,
    transaction_amount - LAG(transaction_amount, 1, 0) OVER (PARTITION BY customer_id ORDER BY transaction_date) AS amount_change,
    EXTRACT(DAY FROM transaction_date - LAG(transaction_date, 1) OVER (PARTITION BY customer_id ORDER BY transaction_date)) AS days_since_last
FROM transactions
WHERE transaction_status = 'completed'
    AND transaction_date >= '2024-01-01'
ORDER BY customer_id, transaction_date

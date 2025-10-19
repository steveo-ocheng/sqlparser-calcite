-- Conditional window aggregates with FILTER and CASE
SELECT
    transaction_date,
    merchant_id,
    merchant_name,
    transaction_amount,
    transaction_type,
    -- Filtered aggregates
    SUM(transaction_amount) FILTER (WHERE transaction_type = 'sale') OVER (
        PARTITION BY merchant_id ORDER BY transaction_date
    ) AS running_sales,
    SUM(transaction_amount) FILTER (WHERE transaction_type = 'refund') OVER (
        PARTITION BY merchant_id ORDER BY transaction_date
    ) AS running_refunds,
    COUNT(*) FILTER (WHERE transaction_type = 'sale') OVER (
        PARTITION BY merchant_id ORDER BY transaction_date
    ) AS sale_count,
    -- Conditional aggregates using CASE
    SUM(CASE WHEN transaction_type = 'sale' THEN transaction_amount ELSE 0 END) OVER (
        PARTITION BY merchant_id ORDER BY transaction_date
    ) AS total_sales_case,
    AVG(CASE WHEN transaction_type = 'sale' THEN transaction_amount END) OVER (
        PARTITION BY merchant_id ORDER BY transaction_date ROWS BETWEEN 29 PRECEDING AND CURRENT ROW
    ) AS avg_sale_30days,
    MAX(CASE WHEN transaction_type = 'sale' THEN transaction_amount ELSE 0 END) OVER (
        PARTITION BY merchant_id ORDER BY transaction_date ROWS BETWEEN 6 PRECEDING AND CURRENT ROW
    ) AS max_sale_7days,
    -- Net calculations
    SUM(CASE WHEN transaction_type = 'sale' THEN transaction_amount WHEN transaction_type = 'refund' THEN -transaction_amount ELSE 0 END) OVER (
        PARTITION BY merchant_id ORDER BY transaction_date
    ) AS net_revenue
FROM merchant_transactions
WHERE transaction_date >= '2024-01-01'
    AND transaction_status = 'completed'
ORDER BY merchant_id, transaction_date

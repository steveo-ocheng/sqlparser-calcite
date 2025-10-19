-- RANK vs DENSE_RANK comparison with ties handling
SELECT
    product_id,
    product_name,
    category,
    sales_amount,
    RANK() OVER (PARTITION BY category ORDER BY sales_amount DESC) AS rank_with_gaps,
    DENSE_RANK() OVER (PARTITION BY category ORDER BY sales_amount DESC) AS rank_no_gaps,
    PERCENT_RANK() OVER (PARTITION BY category ORDER BY sales_amount DESC) AS percentile,
    CUME_DIST() OVER (PARTITION BY category ORDER BY sales_amount DESC) AS cumulative_distribution
FROM product_sales
WHERE sales_date >= '2024-01-01'
    AND sales_amount > 0
ORDER BY category, sales_amount DESC

-- Advanced percentile and distribution calculations
SELECT
    product_id,
    product_name,
    category,
    price,
    sales_volume,
    revenue,
    PERCENT_RANK() OVER (PARTITION BY category ORDER BY price) AS price_percentile,
    CUME_DIST() OVER (PARTITION BY category ORDER BY revenue DESC) AS revenue_cumulative_dist,
    PERCENTILE_CONT(0.25) WITHIN GROUP (ORDER BY price) OVER (PARTITION BY category) AS price_p25,
    PERCENTILE_CONT(0.50) WITHIN GROUP (ORDER BY price) OVER (PARTITION BY category) AS price_median,
    PERCENTILE_CONT(0.75) WITHIN GROUP (ORDER BY price) OVER (PARTITION BY category) AS price_p75,
    PERCENTILE_CONT(0.90) WITHIN GROUP (ORDER BY price) OVER (PARTITION BY category) AS price_p90,
    PERCENTILE_DISC(0.50) WITHIN GROUP (ORDER BY sales_volume) OVER (PARTITION BY category) AS volume_median_discrete,
    price - PERCENTILE_CONT(0.50) WITHIN GROUP (ORDER BY price) OVER (PARTITION BY category) AS price_vs_median,
    CASE
        WHEN price > PERCENTILE_CONT(0.75) WITHIN GROUP (ORDER BY price) OVER (PARTITION BY category) THEN 'Premium'
        WHEN price > PERCENTILE_CONT(0.50) WITHIN GROUP (ORDER BY price) OVER (PARTITION BY category) THEN 'Mid-Range'
        WHEN price > PERCENTILE_CONT(0.25) WITHIN GROUP (ORDER BY price) OVER (PARTITION BY category) THEN 'Budget'
        ELSE 'Economy'
    END AS price_tier
FROM products
WHERE active_status = 'active'
    AND price > 0
ORDER BY category, price DESC

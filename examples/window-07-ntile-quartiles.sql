-- NTILE for dividing data into quartiles, deciles, percentiles
SELECT
    customer_id,
    customer_name,
    total_purchases,
    total_revenue,
    NTILE(4) OVER (ORDER BY total_revenue DESC) AS revenue_quartile,
    NTILE(10) OVER (ORDER BY total_revenue DESC) AS revenue_decile,
    NTILE(100) OVER (ORDER BY total_revenue DESC) AS revenue_percentile,
    NTILE(5) OVER (PARTITION BY customer_segment ORDER BY total_revenue DESC) AS segment_quintile,
    CASE
        WHEN NTILE(4) OVER (ORDER BY total_revenue DESC) = 1 THEN 'Top 25%'
        WHEN NTILE(4) OVER (ORDER BY total_revenue DESC) = 2 THEN 'Second 25%'
        WHEN NTILE(4) OVER (ORDER BY total_revenue DESC) = 3 THEN 'Third 25%'
        ELSE 'Bottom 25%'
    END AS quartile_label
FROM customer_summary
WHERE account_status = 'active'
    AND total_revenue > 0
ORDER BY total_revenue DESC
LIMIT 500

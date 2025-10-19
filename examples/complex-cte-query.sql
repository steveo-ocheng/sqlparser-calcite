WITH
-- Monthly sales aggregation
monthly_sales AS (
    SELECT
        DATE_TRUNC('month', order_date) AS sales_month,
        customer_id,
        product_id,
        SUM(quantity * unit_price) AS total_revenue,
        COUNT(DISTINCT order_id) AS order_count,
        AVG(quantity) AS avg_quantity
    FROM orders o
    INNER JOIN order_items oi ON o.order_id = oi.order_id
    WHERE o.status = 'completed'
        AND o.order_date >= '2024-01-01'
    GROUP BY DATE_TRUNC('month', order_date), customer_id, product_id
    HAVING SUM(quantity * unit_price) > 1000
),
-- Customer lifetime value calculation
customer_ltv AS (
    SELECT
        ms.customer_id,
        c.customer_name,
        c.customer_segment,
        SUM(ms.total_revenue) AS lifetime_value,
        COUNT(DISTINCT ms.sales_month) AS active_months,
        AVG(ms.total_revenue) AS avg_monthly_revenue,
        MAX(ms.total_revenue) AS best_month_revenue
    FROM monthly_sales ms
    INNER JOIN customers c ON ms.customer_id = c.customer_id
    WHERE c.status = 'active'
    GROUP BY ms.customer_id, c.customer_name, c.customer_segment
),
-- Product performance metrics
product_metrics AS (
    SELECT
        p.product_id,
        p.product_name,
        p.category,
        SUM(ms.total_revenue) AS total_product_revenue,
        COUNT(DISTINCT ms.customer_id) AS unique_customers,
        SUM(ms.order_count) AS total_orders,
        AVG(ms.avg_quantity) AS overall_avg_quantity
    FROM monthly_sales ms
    INNER JOIN products p ON ms.product_id = p.product_id
    GROUP BY p.product_id, p.product_name, p.category
),
-- Top customers per segment
ranked_customers AS (
    SELECT
        customer_id,
        customer_name,
        customer_segment,
        lifetime_value,
        active_months,
        ROW_NUMBER() OVER (PARTITION BY customer_segment ORDER BY lifetime_value DESC) AS segment_rank,
        PERCENT_RANK() OVER (ORDER BY lifetime_value DESC) AS percentile_rank
    FROM customer_ltv
)
-- Final query combining all CTEs
SELECT
    rc.customer_name,
    rc.customer_segment,
    rc.lifetime_value,
    rc.active_months,
    rc.segment_rank,
    ROUND(rc.percentile_rank * 100, 2) AS top_percentile,
    pm.product_name AS favorite_product,
    pm.category AS favorite_category,
    pm.total_product_revenue,
    pm.unique_customers AS product_customer_base,
    CASE
        WHEN rc.lifetime_value > 50000 THEN 'VIP'
        WHEN rc.lifetime_value > 20000 THEN 'Premium'
        WHEN rc.lifetime_value > 5000 THEN 'Standard'
        ELSE 'Basic'
    END AS customer_tier
FROM ranked_customers rc
INNER JOIN monthly_sales ms ON rc.customer_id = ms.customer_id
INNER JOIN product_metrics pm ON ms.product_id = pm.product_id
WHERE rc.segment_rank <= 5
    AND rc.percentile_rank <= 0.1
    AND pm.total_product_revenue > (
        SELECT AVG(total_product_revenue) * 1.5
        FROM product_metrics
    )
ORDER BY rc.customer_segment ASC, rc.lifetime_value DESC, pm.total_product_revenue DESC
LIMIT 50

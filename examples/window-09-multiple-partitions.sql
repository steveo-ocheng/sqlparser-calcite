-- Multiple window functions with different partitions in same query
SELECT
    sale_date,
    region,
    store_id,
    product_category,
    sales_amount,
    -- Regional analytics
    SUM(sales_amount) OVER (PARTITION BY region ORDER BY sale_date) AS region_running_total,
    AVG(sales_amount) OVER (PARTITION BY region) AS region_avg_sale,
    RANK() OVER (PARTITION BY region ORDER BY sales_amount DESC) AS region_sale_rank,
    -- Store analytics
    SUM(sales_amount) OVER (PARTITION BY store_id ORDER BY sale_date) AS store_running_total,
    AVG(sales_amount) OVER (PARTITION BY store_id) AS store_avg_sale,
    -- Category analytics
    SUM(sales_amount) OVER (PARTITION BY product_category ORDER BY sale_date) AS category_running_total,
    PERCENT_RANK() OVER (PARTITION BY product_category ORDER BY sales_amount DESC) AS category_percentile,
    -- Combined partitions
    RANK() OVER (PARTITION BY region, product_category ORDER BY sales_amount DESC) AS region_category_rank,
    -- Overall analytics
    SUM(sales_amount) OVER (ORDER BY sale_date) AS overall_running_total,
    PERCENT_RANK() OVER (ORDER BY sales_amount DESC) AS overall_percentile
FROM daily_sales
WHERE sale_date >= '2024-01-01'
    AND sales_amount > 0
ORDER BY region, store_id, sale_date

-- Top N per group using window functions and filtering
WITH ranked_products AS (
    SELECT
        category_id,
        category_name,
        product_id,
        product_name,
        total_sales,
        units_sold,
        average_rating,
        ROW_NUMBER() OVER (PARTITION BY category_id ORDER BY total_sales DESC) AS sales_rank,
        RANK() OVER (PARTITION BY category_id ORDER BY average_rating DESC) AS rating_rank,
        DENSE_RANK() OVER (PARTITION BY category_id ORDER BY units_sold DESC) AS volume_rank,
        PERCENT_RANK() OVER (PARTITION BY category_id ORDER BY total_sales DESC) AS sales_percentile,
        COUNT(*) OVER (PARTITION BY category_id) AS products_in_category,
        SUM(total_sales) OVER (PARTITION BY category_id) AS category_total_sales,
        ROUND(total_sales / SUM(total_sales) OVER (PARTITION BY category_id) * 100, 2) AS pct_of_category_sales
    FROM product_performance
    WHERE active_flag = 1
        AND total_sales > 0
)
SELECT
    category_name,
    product_name,
    total_sales,
    units_sold,
    average_rating,
    sales_rank,
    rating_rank,
    volume_rank,
    sales_percentile,
    products_in_category,
    category_total_sales,
    pct_of_category_sales,
    CASE
        WHEN sales_rank <= 3 THEN 'Top 3'
        WHEN sales_rank <= 10 THEN 'Top 10'
        WHEN sales_percentile <= 0.2 THEN 'Top 20%'
        ELSE 'Other'
    END AS performance_tier
FROM ranked_products
WHERE sales_rank <= 5
    OR rating_rank <= 3
ORDER BY category_name, sales_rank
LIMIT 100

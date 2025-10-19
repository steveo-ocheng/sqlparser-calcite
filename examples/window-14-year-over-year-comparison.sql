-- Year-over-year and period-over-period comparisons
SELECT
    sale_month,
    product_category,
    region,
    monthly_revenue,
    -- Year-over-year comparisons
    LAG(monthly_revenue, 12) OVER (PARTITION BY product_category, region ORDER BY sale_month) AS revenue_same_month_last_year,
    monthly_revenue - LAG(monthly_revenue, 12) OVER (PARTITION BY product_category, region ORDER BY sale_month) AS yoy_revenue_change,
    ROUND(
        (monthly_revenue - LAG(monthly_revenue, 12) OVER (PARTITION BY product_category, region ORDER BY sale_month)) /
        NULLIF(LAG(monthly_revenue, 12) OVER (PARTITION BY product_category, region ORDER BY sale_month), 0) * 100,
        2
    ) AS yoy_growth_pct,
    -- Quarter-over-quarter
    LAG(monthly_revenue, 3) OVER (PARTITION BY product_category, region ORDER BY sale_month) AS revenue_last_quarter,
    ROUND(
        (monthly_revenue - LAG(monthly_revenue, 3) OVER (PARTITION BY product_category, region ORDER BY sale_month)) /
        NULLIF(LAG(monthly_revenue, 3) OVER (PARTITION BY product_category, region ORDER BY sale_month), 0) * 100,
        2
    ) AS qoq_growth_pct,
    -- Month-over-month
    LAG(monthly_revenue, 1) OVER (PARTITION BY product_category, region ORDER BY sale_month) AS revenue_last_month,
    ROUND(
        (monthly_revenue - LAG(monthly_revenue, 1) OVER (PARTITION BY product_category, region ORDER BY sale_month)) /
        NULLIF(LAG(monthly_revenue, 1) OVER (PARTITION BY product_category, region ORDER BY sale_month), 0) * 100,
        2
    ) AS mom_growth_pct,
    -- Moving annual total
    SUM(monthly_revenue) OVER (
        PARTITION BY product_category, region
        ORDER BY sale_month
        ROWS BETWEEN 11 PRECEDING AND CURRENT ROW
    ) AS trailing_12month_revenue
FROM monthly_sales_summary
WHERE sale_month >= '2022-01-01'
ORDER BY product_category, region, sale_month

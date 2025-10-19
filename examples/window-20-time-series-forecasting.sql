-- Time series analysis with trend and seasonality detection
WITH daily_metrics AS (
    SELECT
        metric_date,
        region,
        SUM(sales_amount) AS daily_sales,
        COUNT(DISTINCT customer_id) AS daily_customers,
        AVG(order_value) AS avg_order_value
    FROM sales_data
    WHERE metric_date >= '2023-01-01'
    GROUP BY metric_date, region
)
SELECT
    metric_date,
    region,
    daily_sales,
    daily_customers,
    avg_order_value,
    -- Moving averages for trend
    AVG(daily_sales) OVER (PARTITION BY region ORDER BY metric_date ROWS BETWEEN 6 PRECEDING AND CURRENT ROW) AS ma_7day,
    AVG(daily_sales) OVER (PARTITION BY region ORDER BY metric_date ROWS BETWEEN 29 PRECEDING AND CURRENT ROW) AS ma_30day,
    AVG(daily_sales) OVER (PARTITION BY region ORDER BY metric_date ROWS BETWEEN 89 PRECEDING AND CURRENT ROW) AS ma_90day,
    -- Exponential smoothing approximation
    AVG(daily_sales) OVER (PARTITION BY region ORDER BY metric_date ROWS BETWEEN 6 PRECEDING AND CURRENT ROW) * 0.7 +
    AVG(daily_sales) OVER (PARTITION BY region ORDER BY metric_date ROWS BETWEEN 29 PRECEDING AND CURRENT ROW) * 0.3 AS weighted_ma,
    -- Volatility and standard deviation
    STDDEV(daily_sales) OVER (PARTITION BY region ORDER BY metric_date ROWS BETWEEN 29 PRECEDING AND CURRENT ROW) AS volatility_30day,
    -- Percentage change
    ROUND(
        (daily_sales - LAG(daily_sales, 1) OVER (PARTITION BY region ORDER BY metric_date)) /
        NULLIF(LAG(daily_sales, 1) OVER (PARTITION BY region ORDER BY metric_date), 0) * 100,
        2
    ) AS pct_change_day,
    ROUND(
        (daily_sales - LAG(daily_sales, 7) OVER (PARTITION BY region ORDER BY metric_date)) /
        NULLIF(LAG(daily_sales, 7) OVER (PARTITION BY region ORDER BY metric_date), 0) * 100,
        2
    ) AS pct_change_week,
    -- Seasonality indicators (compare to same day last year)
    LAG(daily_sales, 365) OVER (PARTITION BY region ORDER BY metric_date) AS same_day_last_year,
    daily_sales - LAG(daily_sales, 365) OVER (PARTITION BY region ORDER BY metric_date) AS yoy_change,
    -- Trend detection
    CASE
        WHEN AVG(daily_sales) OVER (PARTITION BY region ORDER BY metric_date ROWS BETWEEN 6 PRECEDING AND CURRENT ROW) >
             AVG(daily_sales) OVER (PARTITION BY region ORDER BY metric_date ROWS BETWEEN 29 PRECEDING AND CURRENT ROW)
        THEN 'Uptrend'
        WHEN AVG(daily_sales) OVER (PARTITION BY region ORDER BY metric_date ROWS BETWEEN 6 PRECEDING AND CURRENT ROW) <
             AVG(daily_sales) OVER (PARTITION BY region ORDER BY metric_date ROWS BETWEEN 29 PRECEDING AND CURRENT ROW)
        THEN 'Downtrend'
        ELSE 'Stable'
    END AS trend_direction,
    -- Anomaly detection (values beyond 2 standard deviations)
    CASE
        WHEN ABS(daily_sales - AVG(daily_sales) OVER (PARTITION BY region ORDER BY metric_date ROWS BETWEEN 29 PRECEDING AND CURRENT ROW)) >
             2 * STDDEV(daily_sales) OVER (PARTITION BY region ORDER BY metric_date ROWS BETWEEN 29 PRECEDING AND CURRENT ROW)
        THEN 1
        ELSE 0
    END AS is_anomaly,
    -- Z-score
    ROUND(
        (daily_sales - AVG(daily_sales) OVER (PARTITION BY region ORDER BY metric_date ROWS BETWEEN 29 PRECEDING AND CURRENT ROW)) /
        NULLIF(STDDEV(daily_sales) OVER (PARTITION BY region ORDER BY metric_date ROWS BETWEEN 29 PRECEDING AND CURRENT ROW), 0),
        2
    ) AS z_score,
    -- Cumulative metrics
    SUM(daily_sales) OVER (PARTITION BY region ORDER BY metric_date) AS cumulative_sales_ytd,
    SUM(daily_sales) OVER (PARTITION BY region, EXTRACT(YEAR FROM metric_date) ORDER BY metric_date) AS cumulative_sales_year
FROM daily_metrics
WHERE metric_date >= '2024-01-01'
ORDER BY region, metric_date

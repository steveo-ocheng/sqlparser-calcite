-- Cohort analysis using window functions
WITH user_cohorts AS (
    SELECT
        user_id,
        DATE_TRUNC('month', registration_date) AS cohort_month,
        registration_date,
        first_purchase_date,
        EXTRACT(MONTH FROM AGE(first_purchase_date, registration_date)) AS months_to_first_purchase
    FROM users
    WHERE registration_date >= '2023-01-01'
),
cohort_activity AS (
    SELECT
        uc.cohort_month,
        uc.user_id,
        DATE_TRUNC('month', a.activity_date) AS activity_month,
        EXTRACT(MONTH FROM AGE(DATE_TRUNC('month', a.activity_date), uc.cohort_month)) AS cohort_age_months,
        SUM(a.revenue) AS monthly_revenue,
        COUNT(DISTINCT a.activity_date) AS active_days
    FROM user_cohorts uc
    INNER JOIN user_activity a ON uc.user_id = a.user_id
    WHERE a.activity_date >= uc.registration_date
    GROUP BY uc.cohort_month, uc.user_id, DATE_TRUNC('month', a.activity_date)
)
SELECT
    cohort_month,
    activity_month,
    cohort_age_months,
    COUNT(DISTINCT user_id) AS active_users,
    SUM(monthly_revenue) AS total_revenue,
    AVG(monthly_revenue) AS avg_revenue_per_user,
    SUM(active_days) AS total_active_days,
    -- Cohort size and retention
    FIRST_VALUE(COUNT(DISTINCT user_id)) OVER (PARTITION BY cohort_month ORDER BY cohort_age_months) AS cohort_size,
    ROUND(
        COUNT(DISTINCT user_id)::NUMERIC /
        FIRST_VALUE(COUNT(DISTINCT user_id)) OVER (PARTITION BY cohort_month ORDER BY cohort_age_months) * 100,
        2
    ) AS retention_rate,
    -- Cumulative metrics
    SUM(SUM(monthly_revenue)) OVER (PARTITION BY cohort_month ORDER BY cohort_age_months) AS cumulative_revenue,
    AVG(SUM(monthly_revenue)) OVER (PARTITION BY cohort_month ORDER BY cohort_age_months ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW) AS avg_monthly_revenue_to_date,
    -- Month-over-month changes
    LAG(COUNT(DISTINCT user_id)) OVER (PARTITION BY cohort_month ORDER BY cohort_age_months) AS previous_month_active,
    COUNT(DISTINCT user_id) - LAG(COUNT(DISTINCT user_id)) OVER (PARTITION BY cohort_month ORDER BY cohort_age_months) AS user_change_mom
FROM cohort_activity
GROUP BY cohort_month, activity_month, cohort_age_months
ORDER BY cohort_month, cohort_age_months

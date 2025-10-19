WITH
-- Daily user activity metrics
daily_activity AS (
    SELECT
        user_id,
        DATE(activity_timestamp) AS activity_date,
        COUNT(DISTINCT session_id) AS session_count,
        SUM(page_views) AS total_page_views,
        SUM(time_spent_seconds) AS total_time_spent,
        MAX(time_spent_seconds) AS longest_session,
        COUNT(DISTINCT CASE WHEN conversion_flag = 1 THEN session_id END) AS conversion_sessions
    FROM user_activity
    WHERE activity_timestamp >= CURRENT_DATE - INTERVAL '90 days'
        AND user_status = 'active'
    GROUP BY user_id, DATE(activity_timestamp)
),
-- User engagement scoring
engagement_scores AS (
    SELECT
        da.user_id,
        COUNT(DISTINCT da.activity_date) AS active_days,
        AVG(da.session_count) AS avg_daily_sessions,
        SUM(da.total_page_views) AS total_views,
        AVG(da.total_time_spent) AS avg_daily_time,
        SUM(da.conversion_sessions) AS total_conversions,
        CASE
            WHEN COUNT(DISTINCT da.activity_date) >= 60 THEN 5
            WHEN COUNT(DISTINCT da.activity_date) >= 30 THEN 4
            WHEN COUNT(DISTINCT da.activity_date) >= 14 THEN 3
            WHEN COUNT(DISTINCT da.activity_date) >= 7 THEN 2
            ELSE 1
        END AS frequency_score,
        CASE
            WHEN AVG(da.total_time_spent) >= 3600 THEN 5
            WHEN AVG(da.total_time_spent) >= 1800 THEN 4
            WHEN AVG(da.total_time_spent) >= 900 THEN 3
            WHEN AVG(da.total_time_spent) >= 300 THEN 2
            ELSE 1
        END AS engagement_score
    FROM daily_activity da
    GROUP BY da.user_id
),
-- Revenue attribution
user_revenue AS (
    SELECT
        t.user_id,
        SUM(t.transaction_amount) AS total_revenue,
        COUNT(DISTINCT t.transaction_id) AS transaction_count,
        AVG(t.transaction_amount) AS avg_transaction_value,
        MAX(t.transaction_amount) AS max_transaction,
        MIN(t.transaction_date) AS first_purchase_date,
        MAX(t.transaction_date) AS last_purchase_date,
        EXTRACT(DAY FROM MAX(t.transaction_date) - MIN(t.transaction_date)) AS customer_lifespan_days
    FROM transactions t
    WHERE t.status = 'completed'
        AND t.transaction_date >= CURRENT_DATE - INTERVAL '365 days'
    GROUP BY t.user_id
),
-- Combined user segments
user_segments AS (
    SELECT
        u.user_id,
        u.user_name,
        u.email,
        u.registration_date,
        u.country,
        u.subscription_tier,
        es.active_days,
        es.avg_daily_sessions,
        es.total_views,
        es.total_conversions,
        es.frequency_score,
        es.engagement_score,
        COALESCE(ur.total_revenue, 0) AS total_revenue,
        COALESCE(ur.transaction_count, 0) AS purchase_count,
        COALESCE(ur.avg_transaction_value, 0) AS avg_order_value,
        CASE
            WHEN COALESCE(ur.total_revenue, 0) > 10000 AND es.engagement_score >= 4 THEN 'Champion'
            WHEN COALESCE(ur.total_revenue, 0) > 5000 AND es.engagement_score >= 3 THEN 'Loyal'
            WHEN COALESCE(ur.total_revenue, 0) > 1000 AND es.frequency_score >= 3 THEN 'Potential Loyalist'
            WHEN COALESCE(ur.total_revenue, 0) > 500 THEN 'New Customer'
            WHEN es.engagement_score >= 4 AND COALESCE(ur.total_revenue, 0) = 0 THEN 'Promising'
            WHEN es.active_days < 7 THEN 'At Risk'
            ELSE 'Need Attention'
        END AS customer_segment,
        (es.frequency_score + es.engagement_score) AS combined_engagement_score
    FROM users u
    INNER JOIN engagement_scores es ON u.user_id = es.user_id
    LEFT JOIN user_revenue ur ON u.user_id = ur.user_id
    WHERE u.account_status = 'active'
),
-- Segment performance aggregates
segment_performance AS (
    SELECT
        customer_segment,
        subscription_tier,
        COUNT(DISTINCT user_id) AS user_count,
        AVG(total_revenue) AS avg_segment_revenue,
        SUM(total_revenue) AS total_segment_revenue,
        AVG(combined_engagement_score) AS avg_engagement,
        PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY total_revenue) AS median_revenue,
        PERCENTILE_CONT(0.75) WITHIN GROUP (ORDER BY total_revenue) AS p75_revenue
    FROM user_segments
    GROUP BY customer_segment, subscription_tier
)
-- Final detailed user report with segment benchmarks
SELECT
    us.user_name,
    us.email,
    us.country,
    us.subscription_tier,
    us.customer_segment,
    us.active_days,
    ROUND(us.avg_daily_sessions, 2) AS avg_sessions_per_day,
    us.total_views,
    us.total_conversions,
    us.total_revenue,
    us.purchase_count,
    ROUND(us.avg_order_value, 2) AS avg_order_value,
    us.combined_engagement_score,
    sp.user_count AS segment_user_count,
    ROUND(sp.avg_segment_revenue, 2) AS segment_avg_revenue,
    ROUND((us.total_revenue / NULLIF(sp.avg_segment_revenue, 0) - 1) * 100, 2) AS revenue_vs_segment_pct,
    CASE
        WHEN us.total_revenue > sp.p75_revenue THEN 'Top 25%'
        WHEN us.total_revenue > sp.median_revenue THEN 'Top 50%'
        ELSE 'Below Median'
    END AS revenue_quartile,
    ROW_NUMBER() OVER (PARTITION BY us.customer_segment ORDER BY us.total_revenue DESC) AS segment_rank
FROM user_segments us
INNER JOIN segment_performance sp
    ON us.customer_segment = sp.customer_segment
    AND us.subscription_tier = sp.subscription_tier
WHERE us.combined_engagement_score >= 5
    OR us.total_revenue >= 1000
    OR us.customer_segment IN ('Champion', 'Loyal', 'Potential Loyalist')
ORDER BY
    CASE us.customer_segment
        WHEN 'Champion' THEN 1
        WHEN 'Loyal' THEN 2
        WHEN 'Potential Loyalist' THEN 3
        WHEN 'New Customer' THEN 4
        WHEN 'Promising' THEN 5
        ELSE 6
    END,
    us.total_revenue DESC,
    us.combined_engagement_score DESC
LIMIT 200

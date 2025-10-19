-- Gap and island detection using window functions
SELECT
    user_id,
    login_date,
    ROW_NUMBER() OVER (PARTITION BY user_id ORDER BY login_date) AS row_num,
    login_date - ROW_NUMBER() OVER (PARTITION BY user_id ORDER BY login_date) * INTERVAL '1 day' AS island_group,
    LAG(login_date) OVER (PARTITION BY user_id ORDER BY login_date) AS previous_login,
    LEAD(login_date) OVER (PARTITION BY user_id ORDER BY login_date) AS next_login,
    EXTRACT(DAY FROM login_date - LAG(login_date) OVER (PARTITION BY user_id ORDER BY login_date)) AS gap_days,
    CASE
        WHEN LAG(login_date) OVER (PARTITION BY user_id ORDER BY login_date) IS NULL THEN 'First Login'
        WHEN EXTRACT(DAY FROM login_date - LAG(login_date) OVER (PARTITION BY user_id ORDER BY login_date)) = 1 THEN 'Consecutive'
        WHEN EXTRACT(DAY FROM login_date - LAG(login_date) OVER (PARTITION BY user_id ORDER BY login_date)) <= 7 THEN 'Recent'
        ELSE 'Gap Detected'
    END AS login_pattern,
    COUNT(*) OVER (
        PARTITION BY user_id, login_date - ROW_NUMBER() OVER (PARTITION BY user_id ORDER BY login_date) * INTERVAL '1 day'
    ) AS streak_length
FROM user_logins
WHERE login_date >= '2024-01-01'
ORDER BY user_id, login_date

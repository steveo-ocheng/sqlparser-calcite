-- FIRST_VALUE and LAST_VALUE with frame specifications
SELECT
    session_id,
    user_id,
    page_view_timestamp,
    page_url,
    time_on_page,
    FIRST_VALUE(page_url) OVER (PARTITION BY session_id ORDER BY page_view_timestamp ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) AS entry_page,
    LAST_VALUE(page_url) OVER (PARTITION BY session_id ORDER BY page_view_timestamp ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) AS exit_page,
    FIRST_VALUE(page_view_timestamp) OVER (PARTITION BY session_id ORDER BY page_view_timestamp) AS session_start,
    LAST_VALUE(page_view_timestamp) OVER (PARTITION BY session_id ORDER BY page_view_timestamp ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) AS session_end,
    SUM(time_on_page) OVER (PARTITION BY session_id) AS total_session_time,
    COUNT(*) OVER (PARTITION BY session_id) AS pages_in_session
FROM page_views
WHERE page_view_timestamp >= CURRENT_DATE - INTERVAL '7 days'
ORDER BY session_id, page_view_timestamp

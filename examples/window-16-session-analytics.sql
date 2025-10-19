-- Session analytics with complex window calculations
SELECT
    user_id,
    session_id,
    event_timestamp,
    event_type,
    page_url,
    -- Session boundaries
    FIRST_VALUE(event_timestamp) OVER session_window AS session_start,
    LAST_VALUE(event_timestamp) OVER (PARTITION BY session_id ORDER BY event_timestamp ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) AS session_end,
    EXTRACT(EPOCH FROM event_timestamp - FIRST_VALUE(event_timestamp) OVER session_window) AS seconds_into_session,
    -- Event sequencing
    ROW_NUMBER() OVER session_window AS event_sequence,
    COUNT(*) OVER session_window AS total_events_in_session,
    LAG(event_type) OVER session_window AS previous_event_type,
    LEAD(event_type) OVER session_window AS next_event_type,
    LAG(page_url) OVER session_window AS previous_page,
    LEAD(page_url) OVER session_window AS next_page,
    -- Time between events
    EXTRACT(EPOCH FROM event_timestamp - LAG(event_timestamp) OVER session_window) AS seconds_since_last_event,
    EXTRACT(EPOCH FROM LEAD(event_timestamp) OVER session_window - event_timestamp) AS seconds_to_next_event,
    -- Session statistics
    SUM(CASE WHEN event_type = 'page_view' THEN 1 ELSE 0 END) OVER session_window AS page_views_in_session,
    SUM(CASE WHEN event_type = 'click' THEN 1 ELSE 0 END) OVER session_window AS clicks_in_session,
    SUM(CASE WHEN event_type = 'conversion' THEN 1 ELSE 0 END) OVER session_window AS conversions_in_session,
    MAX(CASE WHEN event_type = 'conversion' THEN 1 ELSE 0 END) OVER session_window AS session_converted
FROM user_events
WHERE event_timestamp >= CURRENT_DATE - INTERVAL '30 days'
WINDOW session_window AS (PARTITION BY session_id ORDER BY event_timestamp)
ORDER BY user_id, session_id, event_timestamp

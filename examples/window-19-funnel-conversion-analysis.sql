-- Funnel conversion analysis with window functions
WITH funnel_events AS (
    SELECT
        user_id,
        session_id,
        event_timestamp,
        event_name,
        CASE
            WHEN event_name = 'landing_page' THEN 1
            WHEN event_name = 'product_view' THEN 2
            WHEN event_name = 'add_to_cart' THEN 3
            WHEN event_name = 'checkout_start' THEN 4
            WHEN event_name = 'payment_info' THEN 5
            WHEN event_name = 'purchase_complete' THEN 6
        END AS funnel_step,
        ROW_NUMBER() OVER (PARTITION BY session_id ORDER BY event_timestamp) AS event_sequence,
        LEAD(event_name) OVER (PARTITION BY session_id ORDER BY event_timestamp) AS next_event,
        LEAD(event_timestamp) OVER (PARTITION BY session_id ORDER BY event_timestamp) AS next_event_time,
        EXTRACT(EPOCH FROM LEAD(event_timestamp) OVER (PARTITION BY session_id ORDER BY event_timestamp) - event_timestamp) AS seconds_to_next_event
    FROM user_funnel_events
    WHERE event_timestamp >= CURRENT_DATE - INTERVAL '30 days'
        AND event_name IN ('landing_page', 'product_view', 'add_to_cart', 'checkout_start', 'payment_info', 'purchase_complete')
)
SELECT
    user_id,
    session_id,
    event_name,
    funnel_step,
    event_timestamp,
    event_sequence,
    next_event,
    seconds_to_next_event,
    -- Max funnel step reached
    MAX(funnel_step) OVER (PARTITION BY session_id) AS max_funnel_step,
    -- Conversion flags
    MAX(CASE WHEN event_name = 'purchase_complete' THEN 1 ELSE 0 END) OVER (PARTITION BY session_id) AS session_converted,
    MAX(CASE WHEN event_name = 'add_to_cart' THEN 1 ELSE 0 END) OVER (PARTITION BY session_id) AS added_to_cart,
    MAX(CASE WHEN event_name = 'checkout_start' THEN 1 ELSE 0 END) OVER (PARTITION BY session_id) AS started_checkout,
    -- Time in funnel
    FIRST_VALUE(event_timestamp) OVER (PARTITION BY session_id ORDER BY event_timestamp) AS funnel_entry_time,
    EXTRACT(EPOCH FROM event_timestamp - FIRST_VALUE(event_timestamp) OVER (PARTITION BY session_id ORDER BY event_timestamp)) AS seconds_in_funnel,
    -- Completion times
    MAX(CASE WHEN event_name = 'purchase_complete' THEN event_timestamp END) OVER (PARTITION BY session_id) AS conversion_time,
    EXTRACT(EPOCH FROM
        MAX(CASE WHEN event_name = 'purchase_complete' THEN event_timestamp END) OVER (PARTITION BY session_id) -
        FIRST_VALUE(event_timestamp) OVER (PARTITION BY session_id ORDER BY event_timestamp)
    ) AS time_to_conversion_seconds,
    -- Drop-off detection
    CASE
        WHEN funnel_step < MAX(funnel_step) OVER (PARTITION BY session_id) THEN 0
        WHEN next_event IS NULL AND event_name != 'purchase_complete' THEN 1
        ELSE 0
    END AS dropped_at_step,
    -- User journey summary
    STRING_AGG(event_name, ' -> ') OVER (PARTITION BY session_id ORDER BY event_timestamp ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) AS full_journey
FROM funnel_events
ORDER BY user_id, session_id, event_timestamp

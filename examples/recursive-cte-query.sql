WITH RECURSIVE employee_hierarchy AS (
    -- Anchor member: Top-level managers
    SELECT
        e.employee_id,
        e.employee_name,
        e.manager_id,
        e.department_id,
        e.salary,
        e.hire_date,
        1 AS hierarchy_level,
        CAST(e.employee_name AS VARCHAR(1000)) AS reporting_chain
    FROM employees e
    WHERE e.manager_id IS NULL

    UNION ALL

    -- Recursive member: All subordinates
    SELECT
        e.employee_id,
        e.employee_name,
        e.manager_id,
        e.department_id,
        e.salary,
        e.hire_date,
        eh.hierarchy_level + 1,
        CAST(eh.reporting_chain || ' -> ' || e.employee_name AS VARCHAR(1000))
    FROM employees e
    INNER JOIN employee_hierarchy eh ON e.manager_id = eh.employee_id
    WHERE eh.hierarchy_level < 10
),
-- Department statistics
department_stats AS (
    SELECT
        d.department_id,
        d.department_name,
        d.location,
        COUNT(eh.employee_id) AS total_employees,
        AVG(eh.salary) AS avg_salary,
        MAX(eh.salary) AS max_salary,
        MIN(eh.salary) AS min_salary,
        MAX(eh.hierarchy_level) AS max_depth
    FROM employee_hierarchy eh
    INNER JOIN departments d ON eh.department_id = d.department_id
    GROUP BY d.department_id, d.department_name, d.location
    HAVING COUNT(eh.employee_id) >= 5
),
-- High performers with tenure
high_performers AS (
    SELECT
        eh.employee_id,
        eh.employee_name,
        eh.department_id,
        eh.salary,
        eh.hierarchy_level,
        eh.reporting_chain,
        EXTRACT(YEAR FROM AGE(CURRENT_DATE, eh.hire_date)) AS years_of_service,
        CASE
            WHEN EXTRACT(YEAR FROM AGE(CURRENT_DATE, eh.hire_date)) >= 10 THEN 'Veteran'
            WHEN EXTRACT(YEAR FROM AGE(CURRENT_DATE, eh.hire_date)) >= 5 THEN 'Experienced'
            WHEN EXTRACT(YEAR FROM AGE(CURRENT_DATE, eh.hire_date)) >= 2 THEN 'Intermediate'
            ELSE 'Junior'
        END AS tenure_category
    FROM employee_hierarchy eh
    WHERE eh.salary > (
        SELECT AVG(salary) * 1.2
        FROM employee_hierarchy
        WHERE department_id = eh.department_id
    )
)
-- Final comprehensive report
SELECT
    hp.employee_name,
    hp.reporting_chain,
    hp.hierarchy_level AS org_level,
    hp.years_of_service,
    hp.tenure_category,
    hp.salary,
    ds.department_name,
    ds.location,
    ds.total_employees AS dept_size,
    ds.avg_salary AS dept_avg_salary,
    ROUND((hp.salary / ds.avg_salary - 1) * 100, 2) AS salary_premium_pct,
    ROUND(hp.salary / NULLIF(ds.max_salary, 0) * 100, 2) AS pct_of_dept_max,
    RANK() OVER (PARTITION BY hp.department_id ORDER BY hp.salary DESC) AS dept_salary_rank,
    DENSE_RANK() OVER (ORDER BY hp.years_of_service DESC) AS company_tenure_rank
FROM high_performers hp
INNER JOIN department_stats ds ON hp.department_id = ds.department_id
WHERE ds.total_employees >= 10
    AND hp.years_of_service >= 2
    AND ds.avg_salary > 50000
ORDER BY hp.tenure_category DESC, hp.salary DESC, hp.years_of_service DESC
LIMIT 100

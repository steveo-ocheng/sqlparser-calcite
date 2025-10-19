-- Named window definitions with WINDOW clause
SELECT
    employee_id,
    employee_name,
    department,
    job_title,
    salary,
    performance_score,
    -- Using named windows
    ROW_NUMBER() OVER dept_window AS dept_rank,
    PERCENT_RANK() OVER dept_window AS dept_percentile,
    AVG(salary) OVER dept_window AS dept_avg_salary,
    MAX(salary) OVER dept_window AS dept_max_salary,
    AVG(performance_score) OVER job_window AS job_avg_performance,
    RANK() OVER perf_window AS performance_rank,
    NTILE(4) OVER perf_window AS performance_quartile,
    salary - AVG(salary) OVER dept_window AS salary_vs_dept_avg,
    performance_score - AVG(performance_score) OVER job_window AS score_vs_job_avg
FROM employees
WHERE employment_status = 'active'
    AND hire_date < CURRENT_DATE - INTERVAL '1 year'
WINDOW
    dept_window AS (PARTITION BY department ORDER BY salary DESC),
    job_window AS (PARTITION BY job_title ORDER BY salary DESC),
    perf_window AS (ORDER BY performance_score DESC, salary DESC)
ORDER BY department, salary DESC

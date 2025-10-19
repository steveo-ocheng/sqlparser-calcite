-- ROW_NUMBER with PARTITION BY for ranking within groups
SELECT
    employee_id,
    employee_name,
    department,
    salary,
    hire_date,
    ROW_NUMBER() OVER (PARTITION BY department ORDER BY salary DESC) AS dept_salary_rank,
    ROW_NUMBER() OVER (ORDER BY salary DESC) AS overall_salary_rank,
    ROW_NUMBER() OVER (PARTITION BY department ORDER BY hire_date ASC) AS dept_seniority_rank
FROM employees
WHERE employment_status = 'active'
    AND salary > 50000
ORDER BY department, salary DESC
LIMIT 100

SELECT
    department,
    job_title,
    COUNT(*) as employee_count,
    AVG(salary) as average_salary,
    MIN(salary) as min_salary,
    MAX(salary) as max_salary,
    SUM(salary) as total_salary
FROM employees
WHERE hire_date > '2020-01-01'
    AND employment_status = 'active'
GROUP BY department, job_title
HAVING COUNT(*) >= 3
ORDER BY department, average_salary DESC
LIMIT 50

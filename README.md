# Assignment
Write application which accepts incoming requests to:  
POST /upload/{number}  
Content-Type=text/csv

{number} = number is the sum of two numbers to be found in the input array


Application should:
* bind input to List<List<Integer>>
    * Example of input data: [[11, -4, 3, 4, 3, 2], [2, 5, 5, 3, 0, 1]]
* fail-fast on invalid input(identify cases when input should be considered incorrect)
* solve 2sum problem for every row in provided csv
* respond with List[List[List[Integer]]] where elements of output list are solutions per each line of provided csv.
    * Example of response:
      [[[11, -4], [2, 5]], [[2,7], [11,-2]]]

# 2sum problem
Find all the pairs of two integers in an unsorted array that sum up to a given S.  
Examples:  
S=7 => [3, 5, 2, -4, 8, 11] ->  [[11, -4], [2, 5]]  
S=9 => [2, 7, 11, 15, 3, -2], -> [[2,7], [11,-2]]

# Error Handling
In case of invalid input application should respond with a json containing details which are enough to identify what's     
wrong with input:  
<code>
{ "code": "wrong.input.type", "message": "..." }
</code>

# Tests

Write some tests for your rest api controllers and for validation logics of 2sum problem

# Solution
BTW, `/upload/{number}` means only one sum-to-find for the whole set of input numbers (every 'row')
So, with demo purposes, I've modified the task: 
- `upload/{number}` uses POST JSON `[[11, -4, 3, 4, 3, 2], [2, 5, 5, 3, 0, 1]]` finding `number`;
- `upload/file/{number}` is closer to initial task 
  * single `number` in URL; 
  * file contents is similar to described `[[11, -4, 3, 4, 3, 2], [2, 5, 5, 3, 0, 1]]`)
- `upload/csv/` is slightly closer to the real task because it
  * expects `csv` file;
  * csv has the sum being searched, as the first number in every row;
- Solver class (with 2 methods for file/csv uploads) is in `SumSolver` class and being tested separately
- Many things are hard-coded to not overthink 8-D
- But returning error codes are (just in case) extensible in `ErrorTypes`
- Tests show the usage and behaviour
- Play Sird router used for uploads receiving
- I rather used Tapir or Lagom for building microservices before

# 364Final
final project description: 
we need to solve this NP complete problem by making threads that can be run by simultaneously (Parellel) by
comunicating through a MQTT broker.

Different components to make
1. Solvers: request work in for of NP complete problem from broker and return finished result
2. Project manager/producer: receives and assigns work to different threads as part of the NP complete problem
3. (maybe) Blackboard Singleton for problem data: don't think it will be necessary since each branch will have its own set of data
4. TspFrame, onSolve() method: contains the "main" code where manager and solver threads will be initialized and started

progress
solvers: complete
Manager: incomplete
Blackboard: complete
TspFrame:
    1. onLoad() : complete
    2. onSolve() : incomplete
    3. onClear() : complete

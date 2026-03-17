# 364Final
final project description: 
we need to solve this NP complete problem by making threads that can be run by simultaneously (Parellel) by
comunicating through a MQTT broker.

Different components to make
1. MQTT broker
2. Solvers: request work in for of NP complete problem from broker and return finished result
3. Project manager: receives and assigns work to different threads as part of the NP complete problem
4. (maybe) Blackboard Singleton for problem data: don't think it will be necessary since each branch will have its own set of data
